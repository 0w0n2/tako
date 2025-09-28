from fastapi import (
    FastAPI,
    UploadFile,
    File,
    HTTPException,
    WebSocket,
    APIRouter,
    Security,
    status,
    Request,
    Body,
    Form,  # (기존 기능 유지: Form 사용)
)
from fastapi.responses import JSONResponse
from fastapi.security import HTTPBearer
from fastapi.middleware.cors import CORSMiddleware
from typing import Dict, Tuple, List
from PIL import Image, ImageStat
from io import BytesIO
import numpy as np
import cv2, math, uvicorn, os, uuid, datetime
from ws import ws_manager
from sqlalchemy.ext.asyncio import create_async_engine, AsyncEngine
from sqlalchemy import text
from dotenv import load_dotenv

load_dotenv()

# ===== YOLO =====
# Ultralytics는 lazy import 권장 (모델 로딩 비용이 큼)
from ultralytics import YOLO

app = FastAPI(title="Card Condition Checker", root_path="/ai")

api_router = APIRouter(prefix="/ai")

origins = [
    "http://localhost:3000",
    "https://dev-api.tako.today",
    "https://dev.tako.today",
    # ==== 추가: 운영 도메인 ====
    "https://tako.today",
    "https://api.tako.today",
]

app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,
    allow_credentials=True,
    allow_methods=["*"],  # Allows all HTTP methods
    allow_headers=["*"],  # Allows all headers
)


@api_router.get("/docs")
async def docs():
    return {"message": "docs"}


app.include_router(api_router)


security = HTTPBearer(auto_error=False)


async def optional_auth(request: Request, token=Security(security)):
    if request.url.path in ["/docs", "/redoc", "/openapi.json"]:
        return None  # 인증 건너뜀
    if token is None:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED, detail="Not authenticated"
        )
    return token.credentials


# ===== 모델 로딩 (서버 기동 시 1회) =====
VERIFY_MODEL_PATH = os.getenv("VERIFY_MODEL_PATH", "models/card_verification.pt")
SEG_MODEL_PATH = os.getenv("SEG_MODEL_PATH", "models/card_segmentation.pt")
DEFECT_MODEL_PATH = os.getenv("DEFECT_MODEL_PATH", "models/card_defect_detection.pt")

# ==== DB 엔진 설정 (도메인별 분기) ====
# 기본 권장: DATABASE_URL_TAKO, DATABASE_URL_API 사용
# 호환: DATABASE_URL_DEV(=TAKO), DATABASE_URL_PROD(=API) 대체로도 동작
DATABASE_URL_TAKO = os.getenv("DATABASE_URL")
DATABASE_URL_API = os.getenv("DATABASE_URL_PROD")

if not DATABASE_URL_TAKO:
    raise RuntimeError(
        "DATABASE_URL 환경변수가 필요합니다."
    )
if not DATABASE_URL_API:
    raise RuntimeError(
        "DATABASE_URL_PROD 환경변수가 필요합니다."
    )

engine_tako: AsyncEngine = create_async_engine(DATABASE_URL_TAKO, future=True)
engine_api: AsyncEngine = create_async_engine(DATABASE_URL_API, future=True)

# 도메인 매핑 (필요 시 자유롭게 추가/수정 가능)
TAKO_HOSTS = {
    "dev.tako.today",
    "dev-api.tako.today",
}
API_HOSTS = {
    "api.tako.today",
    "tako.today",
}


def _extract_host(request: Request) -> str:
    """
    리버스 프록시(Nginx) 환경 고려: x-forwarded-host 우선, 없으면 Host.
    포트 제거 및 소문자 정규화.
    """
    host = request.headers.get("x-forwarded-host") or request.headers.get("host") or ""
    host = host.split(",")[0].strip().split(":")[0].lower()
    return host


def pick_engine(request: Request) -> AsyncEngine:
    host = _extract_host(request)
    if host in API_HOSTS:
        return engine_api
    # 기본: tako 계열
    return engine_tako


async def insert_grade(engine: AsyncEngine, hash, grade):
    now = datetime.datetime.utcnow()

    sql = text(
        """
        INSERT INTO card_ai_grade (grade_code, created_at, updated_at, hash, physical_card_hash)
        VALUES (:grade_code, :created_at, :updated_at, :hash, :physical_card_hash)
    """
    )
    async with engine.begin() as conn:
        await conn.execute(
            sql,
            {
                "grade_code": grade,
                "created_at": now,
                "updated_at": now,
                "hash": hash,
                "physical_card_hash": None,
            },
        )


try:
    verify_model = YOLO(VERIFY_MODEL_PATH)
except Exception as e:
    verify_model = None
    print(f"[WARN] verify_model load failed: {e}")

try:
    seg_model = YOLO(SEG_MODEL_PATH)
except Exception as e:
    seg_model = None
    print(f"[WARN] seg_model load failed: {e}")

try:
    defect_model = YOLO(DEFECT_MODEL_PATH)
except Exception as e:
    defect_model = None
    print(f"[WARN] defect_model load failed: {e}")

# ===== 파라미터/임계값 =====
ALLOWED_EXT = {".png", ".jpg", ".jpeg", ".webp"}
MIN_SIZE = (800, 800)  # (w, h)
# 밝기 기준: L 채널 평균 30~225, 너무 어둡거나 밝으면 탈락
BRIGHT_MIN, BRIGHT_MAX = 30, 225
# YOLO 검증 기준
CONF_THRESH = 0.50  # 너무 낮으면 400
AREA_FRAC_MIN = 0.20  # 바운딩박스 영역이 전체의 50% 미만이면 400
# 결함 검출 기준
DEFECT_CONF_THRESH = 0.40


# 세그멘테이션 곡률 점수
# - 곡률% <= 0.5% -> 0점
# - 이후 0.5% 증가마다 +2점
# - 이미지당 최대 6점, 전체 최대 12점
def curvature_penalty_points(curve_percent: float) -> int:
    if curve_percent <= 0.5:
        return 0
    steps = math.ceil((curve_percent - 0.5) / 0.5)
    return int(min(6, steps * 2))


# ===== 유틸 =====
def ext_ok(filename: str) -> bool:
    _, ext = os.path.splitext(filename.lower())
    return ext in ALLOWED_EXT


def read_image_bytes(upload: UploadFile) -> Image.Image:
    data = upload.file.read()
    if not data:
        raise HTTPException(
            status_code=400, detail=f"{upload.filename} 파일이 비어 있습니다."
        )
    try:
        img = Image.open(BytesIO(data)).convert("RGB")
        return img
    except Exception:
        raise HTTPException(
            status_code=400, detail=f"{upload.filename}은(는) 올바른 이미지가 아닙니다."
        )


def check_size(img: Image.Image) -> bool:
    w, h = img.size
    return (w >= MIN_SIZE[0]) and (h >= MIN_SIZE[1])


def check_brightness(img: Image.Image) -> bool:
    # L 채널 평균값으로 판정
    L = img.convert("L")
    mean = ImageStat.Stat(L).mean[0]
    return BRIGHT_MIN <= mean <= BRIGHT_MAX


def pil_to_numpy(img: Image.Image) -> np.ndarray:
    return cv2.cvtColor(np.array(img), cv2.COLOR_RGB2BGR)


# ===== Step 2: 카드 이미지 검증 (YOLO detect) =====
# 기대 클래스명 예시:
Cardfront_CLASS = "Cardfront"  # TODO: 실제 모델 클래스명 확인 후 수정
Cardback_CLASS = "Cardback"  # TODO: 실제 모델 클래스명 확인 후 수정


def yolo_detect_verify(img: Image.Image, expect_front: bool) -> Tuple[bool, Dict]:
    """
    expect_front=True이면 Cardfront를, False이면 Cardback을 기대.
    조건:
      - 해당 클래스의 최고 conf >= CONF_THRESH
      - 그 바운딩박스 면적 비율 >= AREA_FRAC_MIN
    """
    if verify_model is None:
        raise HTTPException(status_code=500, detail="검증 모델이 로드되지 않았습니다.")

    np_img = pil_to_numpy(img)
    res = verify_model.predict(
        source=np_img, verbose=False, imgsz=640, conf=0.001, iou=0.7
    )[0]

    det_cnt = 0
    best_any_conf = 0.0
    best_any_name = None

    if res.boxes is not None and len(res.boxes) > 0:
        for b in res.boxes:
            det_cnt += 1
            cls_idx = int(b.cls.item())
            name = res.names.get(cls_idx, f"cls_{cls_idx}")
            conf = float(b.conf.item())
            if conf > best_any_conf:
                best_any_conf, best_any_name = conf, name

        print(
            f"[VERIFY] boxes={det_cnt}, best_any=({best_any_name}, {best_any_conf:.3f})"
        )

    # 클래스명 매핑
    names = res.names  # dict: class_idx -> name
    target = Cardfront_CLASS if expect_front else Cardback_CLASS

    ok = False
    best_conf = 0.0
    best_area_frac = 0.0

    H, W = np_img.shape[:2]
    img_area = W * H

    if res.boxes is not None and len(res.boxes) > 0:
        for b in res.boxes:
            cls_idx = int(b.cls.item())
            name = names.get(cls_idx, f"cls_{cls_idx}")
            conf = float(b.conf.item())
            x1, y1, x2, y2 = map(float, b.xyxy[0].tolist())
            box_area = max(0.0, (x2 - x1)) * max(0.0, (y2 - y1))
            area_frac = box_area / img_area if img_area > 0 else 0.0

            if name == target and conf > best_conf:
                best_conf = conf
                best_area_frac = area_frac

        ok = (best_conf >= CONF_THRESH) and (best_area_frac >= AREA_FRAC_MIN)

    return ok, {
        "target": target,
        "best_conf": best_conf,
        "best_area_frac": best_area_frac,
    }


# ===== Step 3: 곡률 계산 (세그멘테이션) =====
def max_bowing_percent_from_mask(mask: np.ndarray) -> float:
    """
    카드 옆면 사진의 마스크에서 '길이 방향' 직선을 기준으로 최대 만곡률(%).
    방법:
      1) 외곽 컨투어 추출 -> 가장 큰 컨투어 선택
      2) PCA로 주축(길이방향) 계산
      3) 컨투어 점들을 주축에 직교한 방향으로 투영, 직선 대비 최대 편차(d_max)
      4) 주축 방향 최소/최대 좌표로 길이 L 추정
      5) 곡률% = (d_max / L) * 100
    """
    # 이진화 보정
    mask_bin = (mask > 0).astype(np.uint8) * 255
    # 컨투어
    counts, _ = cv2.findContours(mask_bin, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_NONE)
    if not counts:
        return 0.0
    cnt = max(counts, key=cv2.contourArea)

    pts = cnt.reshape(-1, 2).astype(np.float32)
    if pts.shape[0] < 10:
        return 0.0

    # PCA
    assert cv2 is not None
    mean_init = np.zeros((1, 2), dtype=np.float32)
    mean, eigenvectors = cv2.PCACompute(pts, mean_init, maxComponents=2)
    mean = mean[0]  # (2,)
    v_long = eigenvectors[0]  # 주축
    v_orth = eigenvectors[1]  # 직교축

    # 좌표계로 투영
    centered = pts - mean
    long_coords = centered @ v_long  # 길이방향 좌표
    orth_coords = centered @ v_orth  # 직교방향 좌표

    L = float(long_coords.max() - long_coords.min())
    if L <= 1e-6:
        return 0.0

    d_max = float(np.abs(orth_coords).max())
    return (d_max / L) * 100.0


def segmentation_curvature_percent(img: Image.Image) -> Tuple[float, Dict]:
    """
    YOLO 세그멘테이션 결과에서 가장 큰 마스크를 사용해 곡률% 계산.
    """
    if seg_model is None:
        raise HTTPException(
            status_code=500, detail="세그멘테이션 모델이 로드되지 않았습니다."
        )

    np_img = pil_to_numpy(img)
    res = seg_model.predict(source=np_img, verbose=False, task="segment")[0]

    # 마스크 스택 만들기
    if res.masks is None or res.masks.data is None or len(res.masks.data) == 0:
        return 0.0, {"has_mask": False}

    # masks.data: (N, H, W) in {0,1}
    masks = res.masks.data.cpu().numpy().astype(np.uint8)
    # 가장 큰 마스크 선택
    areas = masks.reshape(masks.shape[0], -1).sum(axis=1)
    idx = int(np.argmax(areas))
    mask = masks[idx]

    curve_parent = max_bowing_percent_from_mask(mask)
    return float(curve_parent), {
        "has_mask": True,
        "selected_idx": idx,
        "mask_area": int(areas[idx]),
    }


# ===== Step 4: 결함 검출 (YOLO detect) =====
# 클래스명: tear, cease (가정)
# 패널티: tear=15, cease=5
def yolo_detect_defects(img: Image.Image) -> Tuple[int, Dict]:
    """
    카드 앞/뒷면의 결함(찢어짐, 구김) 검출.
    - tear: 개당 15점 감점
    - cease: 개당 5점 감점
    """
    if defect_model is None:
        raise HTTPException(
            status_code=500, detail="결함 검출 모델이 로드되지 않았습니다."
        )

    np_img = pil_to_numpy(img)
    res = defect_model.predict(
        source=np_img, verbose=False, conf=DEFECT_CONF_THRESH, imgsz=640
    )[0]

    penalty = 0
    detections = []

    if res.boxes is not None and len(res.boxes) > 0:
        names = res.names
        for b in res.boxes:
            cls_idx = int(b.cls.item())
            name = names.get(cls_idx, f"cls_{cls_idx}")
            conf = float(b.conf.item())
            x1, y1, x2, y2 = map(float, b.xyxy[0].tolist())

            # 클래스명은 모델에 따라 확인 필요. tear, cease로 가정
            if name == "tear":
                penalty += 15
            elif name == "cease":
                penalty += 5

            detections.append({"type": name, "conf": conf, "box": [x1, y1, x2, y2]})

    return penalty, {"detections": detections}


# ===== 라우트 =====
@app.websocket("/condition-check/ws")
async def condition_check_ws(websocket: WebSocket, job_id: str):
    await ws_manager.connect(job_id, websocket)
    try:
        while True:
            await websocket.receive_text()
    except Exception:
        pass
    finally:
        ws_manager.disconnect(job_id, websocket)


# async def notify(
#     job_id: str, step: str, status: str = "done", extra: dict | None = None
# ):
#     await ws_manager.notify(
#         job_id,
#         {"type": "progress", "step": step, "status": status, "extra": extra or {}},
#     )


from fastapi import Form


@app.post("/condition-check")
async def condition_check(
    job_id: str = Form(...),
    image_front: UploadFile = File(...),
    image_back: UploadFile = File(...),
    image_side_1: UploadFile = File(...),
    image_side_2: UploadFile = File(...),
    image_side_3: UploadFile = File(...),
    image_side_4: UploadFile = File(...),
    request: Request = None,  # (기능 동일: 요청 호스트 판별용 파라미터 추가, 나머지 입력 스키마/동작 불변)
):
    # ---- Step 0: 파일 형식 검증 ----
    uploads = {
        "image_front": image_front,
        "image_back": image_back,
        "image_side_1": image_side_1,
        "image_side_2": image_side_2,
        "image_side_3": image_side_3,
        "image_side_4": image_side_4,
    }
    for key, up in uploads.items():
        assert up.filename is not None
        if not ext_ok(up.filename):
            detail = (
                f"{up.filename}: 올바른 파일 형식이 아닙니다. (png, jpeg, webp, jpg)"
            )
            return JSONResponse(status_code=400, content={"error": detail})

    # ---- 로드 & 공통 전처리 ----
    imgs: Dict[str, Image.Image] = {}
    try:
        for key, up in uploads.items():
            imgs[key] = read_image_bytes(up)
    except HTTPException as e:
        return JSONResponse(status_code=e.status_code, content={"error": e.detail})
    # await notify(job_id, "file_ext_check")

    # ---- Step 1: 사이즈 및 밝기 검증 ----
    for key, img in imgs.items():
        if not check_size(img):
            w, h = img.size
            detail = (
                f"{key}: 이미지 사이즈가 800x800 이상이어야 합니다. (현재: {w}x{h})"
            )
            return JSONResponse(status_code=400, content={"error": detail})
        if not check_brightness(img):
            L = img.convert("L")
            mean = ImageStat.Stat(L).mean[0]
            detail = f"{key}: 이미지가 너무 어둡거나 밝습니다. (밝기: {mean:.2f}, 정상 범위: {BRIGHT_MIN}~{BRIGHT_MAX})"
            return JSONResponse(status_code=400, content={"error": detail})
    # await notify(job_id, "size_brightness_check")

    # ---- Step 2: 카드 맞는지 검증 (YOLO detect) ----
    try:
        ok_front, info_front = yolo_detect_verify(
            imgs["image_front"], expect_front=True
        )
        if not ok_front:
            detail = f"정면 이미지(Cardfront) 인식 실패. conf={info_front['best_conf']:.2f} (기준: {CONF_THRESH:.2f}), area={info_front['best_area_frac']:.2f} (기준: {AREA_FRAC_MIN:.2f})"
            return JSONResponse(status_code=400, content={"error": detail})

        ok_back, info_back = yolo_detect_verify(imgs["image_back"], expect_front=False)
        if not ok_back:
            detail = f"후면 이미지(Cardback) 인식 실패. conf={info_back['best_conf']:.2f} (기준: {CONF_THRESH:.2f}), area={info_back['best_area_frac']:.2f} (기준: {AREA_FRAC_MIN:.2f})"
            return JSONResponse(status_code=400, content={"error": detail})
    except HTTPException as e:
        return JSONResponse(status_code=e.status_code, content={"error": e.detail})
    # await notify(job_id, "card_verify", extra={"front_conf": 0.91, "back_conf": 0.93})

    # ---- Step 3: 카드 휨 검증 (세그멘테이션 -> 곡률%) ----
    side_keys = ["image_side_1", "image_side_2", "image_side_3", "image_side_4"]
    curvature_list: List[float] = []
    curvature_infos: Dict[str, Dict] = {}

    try:
        for k in side_keys:
            curve_percent, extra = segmentation_curvature_percent(imgs[k])
            curvature_list.append(curve_percent)
            curvature_infos[k] = {"curvature_percent": curve_percent, **extra}
    except HTTPException as e:
        return JSONResponse(status_code=e.status_code, content={"error": e.detail})
    # await notify(job_id, "bending", extra={"max_curvature_percent": 1.2})

    # 점수 계산 규칙:
    # - 휨(곡률) 점수는 "감산 점수"로 사용 (곡률 ↑ -> 감산 ↑)
    #   각 이미지별 penalty = curvature_penalty_points(curve%)
    per_image_penalties = [curvature_penalty_points(c) for c in curvature_list]
    top2_penalty = sum(sorted(per_image_penalties, reverse=True)[:2])
    bend_penalty_total = min(4, top2_penalty)

    # ---- Step 4: 기타 결함 검증 (찢어짐, 구김 등) ----
    try:
        defect_penalty_front, defect_info_front = yolo_detect_defects(
            imgs["image_front"]
        )
        defect_penalty_back, defect_info_back = yolo_detect_defects(imgs["image_back"])
        other_penalties = defect_penalty_front + defect_penalty_back
    except HTTPException as e:
        return JSONResponse(status_code=e.status_code, content={"error": e.detail})
    # await notify(job_id, "other_defects", extra={"total_penalty": other_penalties})

    # ---- 최종 점수/등급 ----
    base_score = 100
    final_score = max(0, base_score - (bend_penalty_total + other_penalties))

    def grade(score: int) -> str:
        if score >= 98:
            return "S+"
        if score >= 95:
            return "S"
        if score >= 90:
            return "A"
        if score >= 85:
            return "B"
        if score >= 80:
            return "C"
        return "D"

    hash = str(uuid.uuid4())
    result = {
        "steps": {
            "file_ext_check": "ok",
            "size_brightness_check": "ok",
            "card_verify": {"front": info_front, "back": info_back},
            "bending": {
                "curvatures_percent": {
                    key: curvature_infos[key]["curvature_percent"] for key in side_keys
                },
                "per_image_penalties": {
                    key: curvature_penalty_points(
                        curvature_infos[key]["curvature_percent"]
                    )
                    for key in side_keys
                },
                "bend_penalty_total": bend_penalty_total,
            },
            "other_defects": {
                "front": defect_info_front,
                "back": defect_info_back,
                "other_penalties_total": other_penalties,
            },
        },
        "score": final_score,
        "grade": grade(final_score),
        "hash": hash,
    }

    # ==== 변경된 부분: 요청 호스트 기준으로 대상 엔진 선택하여 기록 ====
    target_engine = pick_engine(request)
    await insert_grade(target_engine, hash, grade(final_score))

    return JSONResponse(result)


if __name__ == "__main__":
    uvicorn.run("main:app", host="0.0.0.0", port=8000)
