<div align="center">
    <h3>블록체인 기반 TCG 카드 P2P 경매 플랫폼</h3>
    <h1>🐙 TAKO 🚀</h1> 
</div>

**블록체인**과 **AI 기술**을 결합하여 **TCG(Trading Card Game) 중고 카드 거래**를 혁신하는 **경매 플랫폼**<br/>
**AI**가 카드의 품질을 객관적으로 평가하고, **블록체인** 기술로 안전하고 투명한 경매 **거래**와, 각 상품에 대한 경매 기록을 위변조 불가능한 **디지털 자산**으로 보관하는 서비스를 제공하여 기존 중고 카드 시장의 가장 큰 문제인 불신과 위변조 위험을 해결합니다.

- **개발 기간** : 2025.08.07 ~ 2025.09.29 **(7주)**
- **플랫폼** : Web
- **개발 인원** : 5명
- **기관** : 삼성 청년 SW·AI 아카데미 13기 <br><br>

## 🔎 목차

<div align="center">

### <a href="#developers">🌟 팀원 구성</a>

- [팀 구성](#팀-구성)
- [설치 파일 및 리소스](#설치-파일-및-리소스)
- [기술 스택](#기술-스택)
- [시스템 아키텍처](#시스템-아키텍처)
- [주요 기능](#주요-기능)
- [디렉터리 구조](#루트-디렉터리-구조)
- [도메인별 상세](#도메인별-상세)
- [로컬 실행(Dev)](#로컬-실행dev)
- [프로덕션 배포](#프로덕션-배포)
- [스마트 컨트랙트(백엔드-솔리디티)](#스마트-컨트랙트백엔드-솔리디티)
- [환경 변수](#환경-변수)
- [API 문서](#api-문서)
- [트러블슈팅](#트러블슈팅)

</div>

### <a href="#developers">🌟 팀원 구성</a>

<a name="developers"></a>

<div align="center">

<div align="center">
    <table>
        <tr>
            <td colspan="2" align="center"> 
                <a href="https://github.com/0w0n2">
                <img src="./readme-assets/hyewon_lee.png" width="160px" /> <br>
                🍙 이혜원 <br>
                (Backend & Blockchain & Leader) </a> <br>
            </td>
            <td colspan="2" align="center"> 
                <a href="https://github.com/0w0n2">
                <img src="./readme-assets/hyewon_lee.png" width="160px" /> <br>
                🍙 이혜원 <br>
                (Backend & Blockchain & Leader) </a> <br>
            </td>
            <td colspan="2" align="center"> 
                <a href="https://github.com/0w0n2">
                <img src="./readme-assets/hyewon_lee.png" width="160px" /> <br>
                🍙 이혜원 <br>
                (Backend & Blockchain & Leader) </a> <br>
            </td>
        </tr>
        <tr>
          <td colspan="2" valign="top">
            <sub>
              - 인프라 및 CI/CD 파이프라인 구축·운영 <br>
              - Spring: OpenVidu 세션 / Release API 구현 <br>
              - FastAPI 기반 Webhook Handler 서버 구현 <br>
              - 협업 및 이벤트 알림 자동화 파이프라인 구축
            </sub>
          </td>
          <td colspan="2" valign="top">
            <sub>
              - 대기방 / 초대코드 API 구현 <br>
              - WebSocket / STOMP 기반 채팅 API 구현 <br>
              - Spring: AWS S3, MongoDB 연동
            </sub>
          </td>
          <td colspan="2" valign="top">
            <sub>
              - Security, OAuth2, SMTP 기반 인증 API 구현 <br>
              - 마이페이지 / 세션 락 API 구현 <br>
              - OpenAI 기반 STT, TTS API 구현 <br>
              - Lip Model → Runpod 마이그레이션
            </sub>
          </td>
        </tr>
        <tr align="center">
            <td></td> <!-- 왼쪽 빈 공간 -->
            <td colspan="2" align="center"> 
                <a href="https://github.com/0w0n2">
                <img src="./readme-assets/hyewon_lee.png" width="160px" /> <br>
                🍙 이혜원 <br>
                (Backend & Blockchain & Leader) </a> <br>
            </td>
            <td colspan="2" align="center"> 
                <a href="https://github.com/0w0n2">
                <img src="./readme-assets/hyewon_lee.png" width="160px" /> <br>
                🍙 이혜원 <br>
                (Backend & Blockchain & Leader) </a> <br>
            </td>
            <td></td> <!-- 오른쪽 빈 공간 -->
        </tr>
        <tr>
            <td></td> <!-- 왼쪽 빈 공간 -->
            <td colspan="2" valign="top">
              <sub>
                - Security, OAuth2, SMTP 기반 인증 API 구현 <br>
                - 마이페이지 / 세션 락 API 구현 <br>
                - OpenAI 기반 STT, TTS API 구현 <br>
                - Lip Model → Runpod 마이그레이션
              </sub>
            </td>
            <td colspan="2" valign="top">
              <sub>
                - Security, OAuth2, SMTP 기반 인증 API 구현 <br>
                - 마이페이지 / 세션 락 API 구현 <br>
                - OpenAI 기반 STT, TTS API 구현 <br>
                - Lip Model → Runpod 마이그레이션
              </sub>
            </td>
            <td></td> <!-- 오른쪽 빈 공간 -->
        </tr>
    </table>
</div>

## 👥 팀 구성

- 팀원1: Backend & DevOps (CI/CD, 배포, 로그/모니터링)
- 팀원2: Backend & Leader (도메인 설계, 핵심 API, 데이터 모델)
- 팀원3: Backend (인증/보안, 알림, 배치/스케줄러)
- 팀원4: Backend & AI (AI 모델 서버, 카드 검증 파이프라인)
- 팀원5: Frontend & Design (Next.js UI/UX, 컴포넌트 시스템)
- 팀원6: Frontend & Docs (기능 페이지, 문서화, 정적 리소스)

> 실제 참가자 정보는 리포지토리/위키에 등록된 멤버를 참고하세요.

---

<!-- markdownlint-disable-next-line MD033 -->

<a id="설치-파일-및-리소스"></a>

## 📦 설치 파일 및 리소스

- AI 모델 가중치 파일(이미 포함):
  - `backend-fastapi/models/card_defect_detection.pt`
  - `backend-fastapi/models/card_segmentation.pt`
  - `backend-fastapi/models/card_verification.pt`
- 별도의 모델 다운로드가 필요한 경우 FastAPI `.env`에서 경로를 오버라이드할 수 있습니다.
  - `VERIFY_MODEL_PATH`, `SEG_MODEL_PATH`, `DEFECT_MODEL_PATH`

---

<!-- markdownlint-disable-next-line MD033 -->

<a id="기술-스택"></a>

## 🛠️ 기술 스택

| 영역                          | 언어/런타임         | 프레임워크                          | 주요 라이브러리/도구                                                                                                                      | 아이콘                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              | 비고                                                 |
| ----------------------------- | ------------------- | ----------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------- |
| Frontend (frontend-web/taako) | TypeScript, Node.js | Next.js 14, React 18, TailwindCSS 4 | Zustand, @tanstack/react-query, @heroui/react, framer-motion, recharts, swiper, ethers, @web3-react, Firebase(FCM)                        | ![TypeScript](https://img.shields.io/badge/TypeScript-3178C6?style=for-the-badge&logo=typescript&logoColor=white) ![Next.js](https://img.shields.io/badge/Next.js-000000?style=for-the-badge&logo=nextdotjs&logoColor=white) ![React](https://img.shields.io/badge/React-61DAFB?style=for-the-badge&logo=react&logoColor=black) ![TailwindCSS](https://img.shields.io/badge/TailwindCSS-06B6D4?style=for-the-badge&logo=tailwindcss&logoColor=white) ![Node.js](https://img.shields.io/badge/Node.js-339933?style=for-the-badge&logo=node.js&logoColor=white) ![Firebase](https://img.shields.io/badge/Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black) ![Ethereum](https://img.shields.io/badge/Ethereum-3C3C3D?style=for-the-badge&logo=ethereum&logoColor=white)                                                                                                               | SSR/SPA, SW 생성 스크립트                            |
| Backend (backend-spring)      | Java 17             | Spring Boot 3.5.5                   | Spring Data JPA, Redis(Lettuce), Spring Security, OAuth2 Client, jjwt, springdoc-openapi, QueryDSL, Firebase Admin, scrimage(WebP), web3j | ![Java](https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=openjdk&logoColor=white) ![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white) ![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white) ![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white) ![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white) ![Amazon S3](https://img.shields.io/badge/Amazon_S3-569A31?style=for-the-badge&logo=amazons3&logoColor=white) ![OpenAPI](https://img.shields.io/badge/OpenAPI-6BA539?style=for-the-badge&logo=openapiinitiative&logoColor=white) ![Gradle](https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white) | MySQL 8, Redis 8, AWS S3, SSE                        |
| AI (backend-fastapi)          | Python 3.10         | FastAPI, Uvicorn                    | torch 2.8, torchvision, ultralytics(YOLO), OpenCV, numpy, scipy, SQLAlchemy(Async)                                                        | ![Python](https://img.shields.io/badge/Python-3776AB?style=for-the-badge&logo=python&logoColor=white) ![FastAPI](https://img.shields.io/badge/FastAPI-009688?style=for-the-badge&logo=fastapi&logoColor=white) ![PyTorch](https://img.shields.io/badge/PyTorch-EE4C2C?style=for-the-badge&logo=pytorch&logoColor=white) ![OpenCV](https://img.shields.io/badge/OpenCV-5C3EE8?style=for-the-badge&logo=opencv&logoColor=white) ![NumPy](https://img.shields.io/badge/NumPy-013243?style=for-the-badge&logo=numpy&logoColor=white) ![SciPy](https://img.shields.io/badge/SciPy-8CAAE6?style=for-the-badge&logo=scipy&logoColor=white)                                                                                                                                                                                                                                                                 | root_path=/ai, 결과 DB 기록                          |
| Blockchain (backend-solidity) | Solidity            | Hardhat                             | OpenZeppelin, Upgrades, dotenv                                                                                                            | ![Solidity](https://img.shields.io/badge/Solidity-363636?style=for-the-badge&logo=solidity&logoColor=white) ![OpenZeppelin](https://img.shields.io/badge/OpenZeppelin-4E5EE4?style=for-the-badge&logo=openzeppelin&logoColor=white) ![Ethereum](https://img.shields.io/badge/Ethereum-3C3C3D?style=for-the-badge&logo=ethereum&logoColor=white)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     | Sepolia, 배포/테스트 스크립트                        |
| DevOps / Infra (deploy)       | -                   | Docker, Docker Compose              | Nginx, Jenkins, Certbot(Cloudflare DNS)                                                                                                   | ![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white) ![Nginx](https://img.shields.io/badge/Nginx-009639?style=for-the-badge&logo=nginx&logoColor=white) ![Jenkins](https://img.shields.io/badge/Jenkins-D24939?style=for-the-badge&logo=jenkins&logoColor=white) ![Amazon EC2](https://img.shields.io/badge/Amazon_EC2-FF9900?style=for-the-badge&logo=amazonec2&logoColor=white) ![Ubuntu](https://img.shields.io/badge/Ubuntu-E95420?style=for-the-badge&logo=ubuntu&logoColor=white)                                                                                                                                                                                                                                                                                                                                                            | nginx 라우팅(/, /ai, api.\*), 외부 네트워크 e104_net |

---

<!-- markdownlint-disable-next-line MD033 -->

<a id="시스템-아키텍처"></a>

## 🏗️ 시스템 아키텍처

```text
[Client (Next.js)] ──HTTPS──> [Nginx]
   │                        ├─ / (프론트)  → tako_front(_dev):3000
   │                        ├─ /ai/       → tako_ai(:8000) FastAPI
   │                        └─ api.*      → tako_back(_dev):8080 Spring
   │
   └── Web3(ethers) ──> Sepolia (AuctionFactory, TakoCardNFT)

[Spring] ⇄ MySQL 8.0
[Spring] ⇄ Redis 8.0
[Spring] → AWS S3 (미디어)
[Spring] → FCM (푸시)
[FastAPI] ⇄ MySQL (AI 결과 기록)
```

---

<!-- markdownlint-disable-next-line MD033 -->

<a id="주요-기능"></a>

## ✨ 주요 기능

- 경매/입찰/정산 도메인 API 및 SSE 기반 실시간 스트림(/v1/auctions/{id}/live)
- 회원/인증(OAuth2, JWT), 알림(FCM), 문의/리뷰/카테고리 등 커머스 기능
- 카드 이미지 업로드/검증: 전면·후면 검출, 측면 곡률 산출, 결함(tear/crease) 탐지 → 점수/등급 산출
- NFT(ERC-721) 발행 및 경매 연동, Escrow(경매) 컨트랙트
- 어드민/통계/이미지 처리(WebP)

---

<!-- markdownlint-disable-next-line MD033 -->

<a id="루트-디렉터리-구조"></a>

## 📂 루트 디렉터리 구조

```text
S13P21E104/
├─ backend-fastapi/        # AI 모델 API (FastAPI, YOLO)
├─ backend-solidity/       # Hardhat, AuctionEscrow, AuctionFactory, TakoCardNFT
├─ backend-spring/         # Spring Boot 메인 백엔드 (JPA/Redis/S3/SSE)
├─ deploy/                 # docker-compose(dev/prod/ai), nginx, env 예시
├─ frontend-web/
│  └─ taako/               # Next.js 14 웹 프론트엔드
├─ Jenkinsfile             # CI/CD 파이프라인
└─ dev.Jenkinsfile         # Dev 파이프라인
```

---

<!-- markdownlint-disable-next-line MD033 -->

<a id="도메인별-상세"></a>

## 📚 도메인별 상세

<!-- markdownlint-disable MD033 -->

<details>
<summary><strong>Frontend — Next.js (frontend-web/taako)</strong></summary>

- 개요: Next.js 14 기반 경매/카드/NFT UI. Tailwind v4, Zustand, React Query, ethers(Web3), Firebase FCM 연동.
- 주요 스크립트:

  ```json
  {
    "predev": "node scripts/generate-firebase-sw.cjs",
    "dev": "next dev",
    "prebuild": "node scripts/generate-firebase-sw.cjs",
    "build": "next build",
    "start": "next start"
  }
  ```

- 실행(로컬):

  ```cmd
  cd frontend-web/taako
  npm ci
  npm run dev
  ```

- 환경 변수(빌드 ARG, NEXT*PUBLIC*\*):

  - NEXT_PUBLIC_TAKO_NFT, NEXT_PUBLIC_SPENDER_ADDRESS
  - NEXT_PUBLIC_FIREBASE_API_KEY … NEXT_PUBLIC_FIREBASE_VAPID_KEY
  - NEXT_PUBLIC_DEBUG_FCM

- 포트/라우팅: 3000 (Nginx 프록시: tako.today 혹은 dev.tako.today)

- 디렉터리 구조

  ```text
  frontend-web/taako/
  ├─ app/                    # App Router (페이지/레이아웃/에러/route 그룹)
  │  ├─ admin/               # 관리자 페이지
  │  ├─ auction/             # 경매 목록/상세/참여
  │  ├─ category/            # 카테고리 탐색
  │  ├─ login/, signup/      # 인증 플로우
  │  ├─ mypage/              # 마이페이지/설정
  │  ├─ notice/, notification/# 공지/알림
  │  └─ ...
  ├─ components/             # UI 컴포넌트/섹션/모달 등
  │  ├─ atoms/, ui/          # 기본 UI 단위
  │  ├─ auction/, cards/     # 도메인 컴포넌트
  │  ├─ providers/           # 전역 Provider
  │  └─ ...
  ├─ hooks/                  # 커스텀 훅(데이터/상태/비즈니스)
  ├─ lib/                    # API 클라이언트/유틸/웹3/포맷터
  ├─ public/                 # 정적 리소스(이미지/폰트)
  ├─ scripts/                # 빌드 전 처리(Firebase SW 등)
  ├─ stores/                 # Zustand 스토어
  ├─ styles/, types/         # 전역 스타일/타입
  ├─ Dockerfile              # 컨테이너 빌드(런타임 next start)
  └─ next.config.js          # Next.js 설정
  ```

- 주요 폴더 설명
  - `app/`: 페이지 라우팅과 레이아웃 구성, route 그룹핑
  - `components/`: 재사용 가능한 UI 및 도메인별 컴포넌트 모음
  - `hooks/`: React Query, 비즈니스 로직 훅, 폼/지갑 등 상태 로직
  - `lib/`: HTTP 클라이언트, 이더리움/지갑 헬퍼, FCM, 로거 등
  - `public/`: 정적 자산(이미지/아이콘/폰트)

</details>

<details>
<summary><strong>Backend — Spring Boot (backend-spring)</strong></summary>

- 개요: 경매/회원/리뷰/알림 등 도메인 API. JPA + Redis, S3 업로드, JWT/OAuth2 보안, SSE 스트림.
- 주요 의존성: Spring Web, Validation, Data JPA, Data Redis, Batch, Security, OAuth2 Client, Mail, springdoc-openapi, QueryDSL, jjwt, web3j, Firebase Admin, scrimage.
- 포트: 8080 (Nginx: api.tako.today 또는 dev-api.tako.today)
- 실행(JAR):

  ```cmd
  cd backend-spring
  gradlew.bat clean bootJar -x test
  java -jar build\libs\app.jar
  ```

- 핵심 설정(application.yml 발췌):

  ```yaml
  spring:
    datasource: { url: ${DB_URL}, username: ${DB_USERNAME} }
    data: { redis: { host: ${REDIS_HOST}, port: ${REDIS_PORT} } }
    jpa: { hibernate: { ddl-auto: ${JPA_HIBERNATE_DDL} } }
  security:
    jwt: { secret-key: ${SECURITY_JWT_SECRET_KEY} }
  block-chain:
    sepolia: { rpc-url: ${SEPOLIA_RPC_URL} }
  ```

- 주요 엔드포인트:

  - Swagger: `/swagger-ui/index.html`
  - SSE: `/v1/auctions/{id}/live`, `/v1/notifications/stream`
  - 업로드 제한: 멤버/카테고리/경매/공지/문의 별도 정책(이미지/동영상, 크기/개수)

- 디렉터리 구조(요약)

  ```text
  backend-spring/
  ├─ src/main/java/com/bukadong/tcg/
  │  ├─ api/                         # 도메인 API 레이어
  │  │  ├─ auction/                  # 경매(엔드포인트/DTO/서비스/리포지토리)
  │  │  ├─ auth/, member/            # 인증/회원
  │  │  ├─ bid/, trade/, trust/      # 입찰/거래/신뢰
  │  │  ├─ card/, category/          # 카드/카테고리
  │  │  ├─ notice/, inquiry/         # 공지/문의
  │  │  ├─ notification/, push/, fcm/# 알림/푸시/FCM
  │  │  └─ ...
  │  ├─ global/
  │  │  ├─ blockchain/               # web3j 연동/설정
  │  │  ├─ config/, security/        # 스프링 설정/보안
  │  │  ├─ properties/, constant/    # 설정 바인딩/상수
  │  │  ├─ batch/                    # 배치 작업
  │  │  └─ util/, common/            # 공통 유틸/응답 등
  │  └─ TcgApplication.java          # 부트스트랩
  ├─ src/main/resources/
  │  ├─ application.yml, logback-spring.xml
  │  └─ firebase/                    # FCM 서비스 계정
  ├─ codegen/                        # 컨트랙트 ABI
  ├─ build.gradle, Dockerfile        # 빌드/컨테이너 설정
  └─ gradlew(.bat), gradle/          # Gradle Wrapper
  ```

- 주요 폴더 설명
  - `api/*`: 도메인 단위로 controller/service/repository/DTO 구성
  - `global/security`: JWT/OAuth2, CORS, 인가/필터 구성
  - `global/blockchain`: 이더리움 RPC, 컨트랙트 연동 유틸
  - `codegen/`: 컨트랙트 ABI 파일(백엔드 호출에 사용)

</details>

<details>
<summary><strong>Backend — FastAPI AI (backend-fastapi)</strong></summary>

- 개요: 카드 전/후면 검출, 측면 곡률 계산, 결함(tear/crease) 탐지 → 점수/등급 산출.
- 포트/경로: 8000, root_path `/ai` (Nginx: `/ai/*`)
- 실행(Docker 기본 CMD): `uvicorn main:app --host 0.0.0.0 --port 8000`
- 모델/ENV:
  - VERIFY_MODEL_PATH, SEG_MODEL_PATH, DEFECT_MODEL_PATH (기본: `models/*.pt`)
  - DATABASE_URL (결과 기록용)
- 입력 제약:
  - 확장자: png, jpg, jpeg, webp / 최소 해상도: 800x800 / 밝기: 30~225(L 채널 평균)
- 엔드포인트:

  - POST `/condition-check` (Form-Data)
    - fields: job_id, image_front, image_back, image_side_1~4
    - 응답: step별 요약, score, grade, hash
  - WS `/condition-check/ws?job_id=...` (진행 상황 알림)
  - `/docs`(Swagger), `/ai/docs`(헬스용 JSON)

- 디렉터리 구조

  ```text
  backend-fastapi/
  ├─ main.py                 # FastAPI 앱, /ai prefix, 검증 파이프라인
  ├─ ws.py                   # WebSocket 매니저
  ├─ models/                 # YOLO 가중치(.pt)
  │  ├─ card_defect_detection.pt
  │  ├─ card_segmentation.pt
  │  └─ card_verification.pt
  ├─ requirements.txt        # 파이썬 의존성
  └─ Dockerfile              # uvicorn 실행 이미지
  ```

- 주요 파일 설명
  - `main.py`: 파일 유효성/밝기/사이즈 체크 → YOLO 검증/세그멘테이션 → 결함탐지 → 점수/등급/DB 기록
  - `ws.py`: job_id 기반 WebSocket broadcast

</details>

<details>
<summary><strong>Blockchain — Hardhat/Solidity (backend-solidity)</strong></summary>

- 컨트랙트: `AuctionEscrow.sol`, `AuctionFactory.sol`, `TakoCardNFT.sol`
- 도구: Hardhat, OpenZeppelin(업그레이더블), dotenv
- 테스트/업그레이드: `test/TakoCardNFT.upgrade.test.ts` 등 포함
- 실행:

  ```cmd
  cd backend-solidity
  npm ci
  npx hardhat compile
  npx hardhat run scripts/deploy-TakoNFT.ts --network sepolia
  npx hardhat run scripts/deploy-auctionFactory.ts --network sepolia
  ```

- 백엔드 연동 ENV:

  - `AUCTION_FACTORY_CONTRACT_ADDRESS`, `TAKO_CARD_NFT_CONTRACT_ADDRESS`

- 디렉터리 구조

  ```text
  backend-solidity/
  ├─ contracts/
  │  ├─ AuctionEscrow.sol
  │  ├─ AuctionFactory.sol
  │  ├─ TakoCardNFT.sol
  │  └─ Lock.sol
  ├─ scripts/                    # 배포/유틸 스크립트
  ├─ test/                       # 하드햇 테스트
  ├─ ignition/modules/           # ignition 모듈 샘플
  ├─ .openzeppelin/              # 업그레이드 메타데이터
  ├─ hardhat.config.ts           # 네트워크/플러그인 구성
  └─ package.json, tsconfig.json # 의존성/타입스크립트 설정
  ```

- 주요 폴더 설명
  - `contracts/`: 경매/에스크로/NFT 컨트랙트 소스
  - `scripts/`: sepolia 배포 스크립트 모음
  - `.openzeppelin/`: Proxy/업그레이드 상태 관리 파일

</details>

<details>
<summary><strong>Deploy — Docker Compose / Nginx / Jenkins</strong></summary>

- Compose 파일:
  - Dev: `deploy/docker-compose.dev.yml` (mysql_dev, redis_dev, tako_back_dev, tako_front_dev)
  - AI: `deploy/docker-compose.ai.yml` (tako_ai, tako_ai_dev)
  - Prod: `deploy/docker-compose.prod.yml` (mysql_prod, redis_prod, tako_back, tako_front)
  - 공통 네트워크: `e104_net`(external)
- Nginx(SSL/프록시): `deploy/nginx/nginx.conf`
  - tako.today → 프론트, api.tako.today → 백엔드, `/ai/` → FastAPI
  - SSE 최적화(버퍼링 off, gzip off, 긴 타임아웃)
  - Certbot(dns-cloudflare)로 인증서 관리
- Jenkins: 루트 `Jenkinsfile`, `dev.Jenkinsfile` 기반 파이프라인

- 디렉터리 구조

  ```text
  deploy/
  ├─ docker-compose.dev.yml   # dev: mysql_dev, redis_dev, tako_back_dev, tako_front_dev
  ├─ docker-compose.prod.yml  # prod: mysql_prod, redis_prod, tako_back, tako_front
  ├─ docker-compose.ai.yml    # ai: tako_ai, tako_ai_dev
  ├─ nginx/
  │  ├─ docker-compose.nginx.yml
  │  └─ nginx.conf            # 도메인 라우팅/SSL/SSE
  └─ .env.example             # 환경변수 예시
  ```

- 주요 파일 설명
  - `nginx.conf`: tako.today/api.tako.today/dev.\* 라우팅, `/ai/` 프록시, SSE 최적화, Certbot 경로
  - `docker-compose.*.yml`: 서비스/네트워크/자원 제한 정의, external 네트워크 `e104_net` 사용

</details>

<!-- markdownlint-enable MD033 -->

---

<!-- markdownlint-disable-next-line MD033 -->

<a id="로컬-실행dev"></a>

## 🧑‍💻 로컬 실행(Dev)

사전 요구사항: Docker, Docker Compose, Node.js(선택) 설치.

1. 네트워크/볼륨 생성(처음 1회)

```cmd
docker network create e104_net
docker volume create mysql_dev_data
docker volume create redis_dev_data
```

1. 환경변수 파일 준비

- `deploy/.env.example`를 참고해 `deploy/.env.dev`, `deploy/.env.dev.ai`를 작성합니다.
- 필수 값: DB*URL/USERNAME/PASSWORD, JWT, AWS S3, OAuth2, FIREBASE, NEXT_PUBLIC*\* 등

1. Dev 백엔드/프론트 띄우기

```cmd
cd deploy
docker compose -f docker-compose.dev.yml up -d --build
```

1. AI 서버(FastAPI) 띄우기

```cmd
cd deploy
docker compose -f docker-compose.ai.yml up -d --build
```

1. (선택) Nginx Reverse Proxy

```cmd
cd deploy/nginx
docker compose -f docker-compose.nginx.yml up -d
```

접속:

- 프론트(dev): <https://dev.tako.today> (Nginx 사용 시) 또는 <http://localhost:3000>
- 백엔드(dev): <https://dev-api.tako.today> (Nginx 사용 시) 또는 <http://localhost:8080>
- AI(dev): 프록시 경유 시 <https://dev.tako.today/ai/> 또는 <http://localhost:8000>

> 주의: Nginx 구성은 외부 인증서(letsEncrypt) 볼륨을 공유합니다. 로컬 단독 실행 시에는 직접 3000/8080/8000 포트로 접근하세요.

---

<!-- markdownlint-disable-next-line MD033 -->

<a id="프로덕션-배포"></a>

## 🚀 프로덕션 배포

1. 프로덕션 환경변수 파일 작성

- `deploy/.env.example`를 복사해 `deploy/.env.prod`를 작성합니다.
- 프론트 이미지 빌드 ARG(NEXT*PUBLIC*\*) 값과 백엔드/AI/DB/Redis/FCM 값을 모두 채워야 합니다.

1. 서비스 기동

```cmd
cd deploy
docker compose -f docker-compose.prod.yml up -d --build
docker compose -f docker-compose.ai.yml up -d --build
cd nginx
docker compose -f docker-compose.nginx.yml up -d
```

1. 도메인/프록시

- tako.today → 프론트(tako_front)
- api.tako.today → 백엔드(tako_back)
- /ai/ → FastAPI(tako_ai)
- dev.\* 하위 도메인은 dev 스택(tako_front_dev, tako_back_dev)을 대상으로 라우팅

---

<!-- markdownlint-disable-next-line MD033 -->

<a id="스마트-컨트랙트백엔드-솔리디티"></a>

## ⛓️ 스마트 컨트랙트(백엔드-솔리디티)

폴더: `backend-solidity`

- 주요 컨트랙트: `AuctionEscrow.sol`, `AuctionFactory.sol`, `TakoCardNFT.sol`
- 도구: Hardhat, OpenZeppelin, 업그레이어블(contracts-upgradeable)
- 스크립트: `scripts/deploy-auctionFactory.ts`, `scripts/deploy-TakoNFT.ts`, `scripts/deploy-auctionEscrow.ts`

예시 (Sepolia):

```cmd
cd backend-solidity
npm ci
npx hardhat compile
npx hardhat run scripts/deploy-TakoNFT.ts --network sepolia
npx hardhat run scripts/deploy-auctionFactory.ts --network sepolia
```

배포 주소는 `deploy/.env.*`의 다음 키로 백엔드에 주입합니다.

- `AUCTION_FACTORY_CONTRACT_ADDRESS`
- `TAKO_CARD_NFT_CONTRACT_ADDRESS`

---

<!-- markdownlint-disable-next-line MD033 -->

<a id="환경-변수"></a>

## 🔧 환경 변수

모든 키는 `deploy/.env.*`에서 관리합니다. 주요 항목:

- 서버: `SERVER_PORT`, `SWAGGER_URI`
- DB: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `DB_DRIVER`
- Redis: `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD`
- JPA: `JPA_HIBERNATE_DDL`, `JPA_SHOW_SQL`, `JPA_FORMAT_SQL`
- 보안/JWT: `SECURITY_JWT_SECRET_KEY`, `..._EXPIRE_TIME_*`
- CORS: `SECURITY_CORS_*`
- OAuth2: `OAUTH2_GOOGLE_CLIENT_ID`, `OAUTH2_GOOGLE_CLIENT_SECRET`, `OAUTH2_REDIRECT_URI`
- AWS S3: `AWS_ACCESS_KEY`, `AWS_SECRET_KEY`, `AWS_REGION_STATIC`, `AWS_S3_BUCKET`
- FCM: `FIREBASE_SERVICE_ACCOUNT_B64`
- 블록체인: `SEPOLIA_RPC_URL`, `SEPOLIA_PRIVATE_KEY`, `SEPOLIA_WALLET_ADDRESS`, `AUCTION_FACTORY_CONTRACT_ADDRESS`, `TAKO_CARD_NFT_CONTRACT_ADDRESS`
- 프론트 빌드 ARG: `NEXT_PUBLIC_*`
- AI: `DATABASE_URL`, `VERIFY_MODEL_PATH`, `SEG_MODEL_PATH`, `DEFECT_MODEL_PATH`

> 참고: FastAPI는 `root_path=/ai`로 동작합니다. Nginx 프록시 경유 시 `/ai/*` 경로로 접근합니다.

---

<!-- markdownlint-disable-next-line MD033 -->

<a id="api-문서"></a>

## 📘 API 문서

- Spring Backend: `/swagger-ui/index.html` (Nginx 사용 시 <https://api.tako.today/swagger-ui/index.html>)
- AI(FastAPI): `/docs` (예: <http://localhost:8000/docs>, Nginx: <https://tako.today/ai/docs>)

---

<!-- markdownlint-disable-next-line MD033 -->

<a id="트러블슈팅"></a>

## 🐛 트러블슈팅

- Docker 네트워크가 없다는 오류
  - `docker network create e104_net`
- 외부 볼륨이 없다는 오류
  - `docker volume create mysql_prod_data` / `redis_prod_data` (dev의 경우 `*_dev_data`)
- Nginx 502/504
  - 대상 컨테이너가 같은 `e104_net` 네트워크에 붙어 있는지 확인
  - 서비스 포트/이름이 nginx.conf upstream과 일치하는지 확인
- FastAPI 모델 로드 실패
  - `backend-fastapi/models/*.pt` 경로 확인 또는 `.env`의 `*_MODEL_PATH` 확인
- AI DB 연결 오류
  - `deploy/.env.*`의 `DATABASE_URL`이 FastAPI 컨테이너에서 접근 가능한 호스트명/포트를 가리키는지 확인

---

## 📄 라이선스

사내/과제 리포지토리 기준. 외부 배포용 라이선스가 필요하면 별도 `LICENSE` 추가.

---

문서 상태: Markdown lint 친화적으로 구성(첫 줄 H1, inline HTML 미사용). 실제 도메인/키/주소는 환경에 맞게 조정하세요.
