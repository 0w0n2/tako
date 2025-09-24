"use client"

import * as React from "react"
import { useForm, SubmitHandler, Controller } from "react-hook-form"
import { useState } from "react"

import Image from "next/image"
import CreateAuctionCategories from "@/components/categories/CreateAuctionCategories"
import AuctionNewCalendar from "@/components/auction/new/AuctionNewCalendar"
import RegisterImage from "@/components/atoms/RegisterImage"
import { AuctionFormProps } from "@/types/auction"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Button } from "@/components/ui/button"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { createAuction } from "@/lib/auction"
import RankElement from "@/components/atoms/RankElement"

export default function NewAuctionPage() {
  const [selectedCardName, setSelectedCardName] = React.useState<string>("");
  const [selectedCardImageUrl, setSelectedCardImageUrl] = React.useState<string>("");
  const [uploadedImages, setUploadedImages] = React.useState<{[key: string]: File | null}>({
    front: null,
    back: null,
    edge1: null,
    edge2: null,
    edge3: null,
    edge4: null
  });
  const [grade, setGrade] = useState<string>("");
  const [gradeHash, setGradeHash] = useState<string>("")

  const { register, handleSubmit, control, watch, setValue, formState: { errors } } = useForm<AuctionFormProps>({
    defaultValues: {
      files : [],
      requestDto: {
        gradeHash: null,
        categoryMajorId: null,
        categoryMediumId: null,
        cardId: null,
        title: "",
        detail: "",
        startDatetime:'',
        endDatetime: '',
        buyNowFlag: false,
        buyNowPrice: 0,
        bidUnit: 0,
        startPrice: 0,
      }
    }
  });

  const isBuyItNow = watch("requestDto.buyNowFlag");

  // AI 감정하기 핸들러
  const handleAIGrading = async () => {
    try {
      // 업로드된 이미지가 있는지 확인
      const hasImages = Object.values(uploadedImages).some(image => image !== null);
      
      if (!hasImages) {
        alert("카드 이미지를 먼저 업로드해주세요.");
        return;
      }

      // FormData 생성
      const formData = new FormData();
      
      // 무작위 5자리 숫자 job_id 생성
      const jobId = Math.floor(10000 + Math.random() * 90000).toString();
      formData.append('job_id', jobId);
      
      // 각 이미지를 적절한 키로 추가
      if (uploadedImages.front) {
        formData.append('image_front', uploadedImages.front);
      }
      if (uploadedImages.back) {
        formData.append('image_back', uploadedImages.back);
      }
      if (uploadedImages.edge1) {
        formData.append('image_side_1', uploadedImages.edge1);
      }
      if (uploadedImages.edge2) {
        formData.append('image_side_2', uploadedImages.edge2);
      }
      if (uploadedImages.edge3) {
        formData.append('image_side_3', uploadedImages.edge3);
      }
      if (uploadedImages.edge4) {
        formData.append('image_side_4', uploadedImages.edge4);
      }

      const response = await fetch(`${process.env.NEXT_PUBLIC_AI_API_BASE_URL}/condition-check`, {
        method: 'POST',
        body: formData,
      });
      
      if (!response.ok) {
        console.log(response)
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      
      const responseData = await response.json();

      // 응답에서 grade 값을 추출
      const grade = responseData.grade;
      setGrade(grade)

      const hash = responseData.hash
      setGradeHash(hash)

    } catch (error) {
      console.error("AI 감정 중 오류 발생:", error);
      alert("AI 감정 중 오류가 발생했습니다. 다시 시도해주세요.");
    }
  };

  // 이미지 업로드 핸들러
  const handleImageUpload = (type: string, file: File | null) => {
    setUploadedImages(prev => ({
      ...prev,
      [type]: file
    }));
  };

  // 이미지 미리보기 URL 생성
  const getImagePreview = (file: File | null) => {
    if (!file) return null;
    return URL.createObjectURL(file);
  };

  // 컴포넌트 언마운트 시 URL 객체 정리
  React.useEffect(() => {
    return () => {
      Object.values(uploadedImages).forEach(file => {
        if (file) {
          const url = URL.createObjectURL(file);
          URL.revokeObjectURL(url);
        }
      });
    };
  }, [uploadedImages]);

  const onSubmit: SubmitHandler<AuctionFormProps> = data => {
    const { requestDto } = data;

    // 디버깅: 폼 데이터 출력
    console.log("폼 제출 데이터:", data);
    console.log("requestDto:", requestDto);

    requestDto.gradeHash = gradeHash;

    // 시작,종료 시간 비교
      if (requestDto.startDatetime && requestDto.endDatetime) {
        const start = new Date(requestDto.startDatetime);
        const end = new Date(requestDto.endDatetime);
        if (end <= start) {
          alert("종료시간이 시작시간보다 빠를 수 없습니다.");
          return;
        }
      }
      
    const requiredFields = [
      "categoryMajorId",
      "categoryMediumId",
      "cardId",
      "title",
      "detail",
      "startDatetime",
      "endDatetime",
      "bidUnit",
      "startPrice"
    ] as const;

    let emptyFields: string[] = requiredFields.filter((key) => {
      const value = requestDto[key as keyof typeof requestDto];
      if (value === null || value === "") return true;
      if (typeof value === "number") {
        // startPrice와 bidUnit은 0보다 큰 값이어야 함
        if (key === "startPrice" || key === "bidUnit") {
          return value <= 0;
        }
        // 다른 숫자 필드는 0도 허용
        return false;
      }
      return false;
    });

    // buyNowFlag true일 때 buyNowPrice 필수 체크
    if (requestDto.buyNowFlag && (!requestDto.buyNowPrice || requestDto.buyNowPrice <= 0)) {
      emptyFields.push("buyNowPrice");
    }

    // 업로드된 이미지들을 files 배열로 변환
    const uploadedFiles = Object.values(uploadedImages).filter(file => file !== null) as File[];

    // 디버깅: 검증 결과 출력
    console.log("빈 필드들:", emptyFields);
    console.log("업로드된 파일 수:", uploadedFiles.length);

    if (uploadedFiles.length === 0 || emptyFields.length > 0) {
      alert("입력하지 않은 필수값이 있습니다.");
      return;
    }

    try {
      createAuction(requestDto, uploadedFiles);
    } catch (err) {
      console.error(err);
    }
  };

  return (
    <div className="small-container pb-10">
      <h2 className="mb-10">경매 등록하기</h2>

      <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-15" encType="multipart/form-data">
      <Controller
          name="files"
          control={control}
          rules={{
            validate: value =>
              (value && value.length > 0) || "이미지를 1개 이상 등록해주세요."
          }}
          render={({ field: { onChange }, fieldState }) => (
            <div className="flex flex-col gap-5">
              <div className="flex-1 flex items-center gap-2">
                <Label>사진 등록</Label>
                <span className="text-red-500">*</span>
              </div>
              <div className="flex-5 flex flex-col gap-2">
                <RegisterImage
                  onChange={(files) => {
                    // Controller가 파일 배열을 받을 수 있게 래핑
                    onChange(files);
                  }}
                />
                {fieldState.error && <p className="text-red-500 text-sm mt-1">{fieldState.error.message}</p>}
              </div>
            </div>
          )}
        />
        {/* 카테고리 */}
        <div className="flex flex-col gap-5 relative">
          <div className="flex-1">
            <div className="flex gap-2">
              <Label>카테고리</Label>
              <span className="text-red-500">*</span>
            </div>
          </div>
          <div className="flex-5">
            <CreateAuctionCategories
              onChange={(majorId, majorName, minorId, minorName, cardId, cardName, cardImageUrl) => {
                setValue("requestDto.categoryMajorId", majorId);
                setValue("requestDto.categoryMediumId", minorId);
                setValue("requestDto.cardId", cardId);
                setSelectedCardName(cardName ?? "");
                setSelectedCardImageUrl(cardImageUrl ?? "")
              }}
              onReset={() => {
                setSelectedCardName("");
                setSelectedCardImageUrl("");
              }}
            />
            {(errors.requestDto?.categoryMajorId || errors.requestDto?.categoryMediumId) && (
              <p className="text-red-500 text-sm mt-1">카테고리를 선택해주세요.</p>
            )}
            {selectedCardImageUrl ? (
              <div className="w-[250px] flex flex-col gap-2 items-center">
                <div className="w-full">
                  <Image
                    className="w-full h-full object-fit"
                    src={selectedCardImageUrl[0]}
                    alt="선택된 카드 이미지"
                    width={100}
                    height={100}
                    unoptimized
                  />
                </div>
                <p className="text-[#a5a5a5] text-center">{selectedCardName}</p>
              </div>
            ) : null}
          </div>
        </div>

        {/* 제목 */}
        <div className="flex flex-col items-start gap-5">
          <div className="flex items-center gap-2 mt-2">
            <Label htmlFor="title">제목</Label>
            <span className="text-red-500">*</span>
          </div>
          <div className="w-full">
            <Input
              id="title"
              className="h-14 placeholder:text-md"
              {...register("requestDto.title", { required: "제목을 입력해주세요." })}
              placeholder="제목"
            />
            {errors.requestDto?.title && <p className="text-red-500 text-sm mt-1">{errors.requestDto?.title.message}</p>}
          </div>
        </div>

        {/* 카드 감정하기(AI) */}
        <div className="flex flex-col items-start gap-5">
          <div className="flex-1 flex flex-col gap-1">
            <div className="flex items-center gap-2 mt-2">
              <h3>카드 감정하기(AI)</h3>
              
              <span className="text-[#FF0000]">*</span>
            </div>
            <div className="flex gap-2 items-center">
              <div className="w-[15px] h-[15px] rounded-full border border-[#c3c3c3] flex items-center justify-center text-[10px]">i</div>
              <span className="text-sm text-[#a3a3a3]">촬영가이드</span>
            </div>
          </div>

          <div className="w-full flex flex-col gap-4">
            <div className="flex items-center justify-between gap-2 mt-3">
              <p className="text-sm text-[#a5a5a5] flex-1">카드 이미지를 등록해 주세요<br/>
              (촬영 가이드 참고)</p>
              <Button
                type="button"
                onClick={handleAIGrading}
                className="rounded-lg px-6 h-[50px] bg-[#7DB7CD] border-1 border-[#7DB7CD] text-[#111] shadow-lg">
                AI 감정하기
                <svg
                fill="black"
                height={24}
                viewBox="0 0 24 24"
                width={24}
                xmlns="http://www.w3.org/2000/svg"
              >
                <path
                  clipRule="evenodd"
                  d="M17.44 6.236c.04.07.11.12.2.12 2.4 0 4.36 1.958 4.36 4.355v5.934A4.368 4.368 0 0117.64 21H6.36A4.361 4.361 0 012 16.645V10.71a4.361 4.361 0 014.36-4.355c.08 0 .16-.04.19-.12l.06-.12.106-.222a97.79 97.79 0 01.714-1.486C7.89 3.51 8.67 3.01 9.64 3h4.71c.97.01 1.76.51 2.22 1.408.157.315.397.822.629 1.31l.141.299.1.22zm-.73 3.836c0 .5.4.9.9.9s.91-.4.91-.9-.41-.909-.91-.909-.9.41-.9.91zm-6.44 1.548c.47-.47 1.08-.719 1.73-.719.65 0 1.26.25 1.72.71.46.459.71 1.068.71 1.717A2.438 2.438 0 0112 15.756c-.65 0-1.26-.25-1.72-.71a2.408 2.408 0 01-.71-1.717v-.01c-.01-.63.24-1.24.7-1.699zm4.5 4.485a3.91 3.91 0 01-2.77 1.15 3.921 3.921 0 01-3.93-3.926 3.865 3.865 0 011.14-2.767A3.921 3.921 0 0112 9.402c1.05 0 2.04.41 2.78 1.15.74.749 1.15 1.738 1.15 2.777a3.958 3.958 0 01-1.16 2.776z"
                  fill="black"
                  fillRule="evenodd"
                />
              </svg>
              </Button>
            </div>

            <div className="grid grid-cols-3 gap-4">
              {[
                { key: "front", label: "(앞면)" },
                { key: "back", label: "(뒷면)" },
                { key: "edge1", label: "(모서리 1)" },
                { key: "edge2", label: "(모서리 2)" },
                { key: "edge3", label: "(모서리 3)" },
                { key: "edge4", label: "(모서리 4)" }
              ].map(({ key, label }) => (
                <div key={key} className="flex flex-col items-center gap-2">
                  <div className="relative w-full aspect-[4/5] border border-[#353535] bg-[#191924] rounded overflow-hidden">
                    {uploadedImages[key] ? (
                      <>
                        <Image
                          src={getImagePreview(uploadedImages[key])!}
                          alt={label}
                          width={200}
                          height={250}
                          className="object-cover w-full h-full"
                          unoptimized
                        />
                        <button
                          type="button"
                          onClick={() => handleImageUpload(key, null)}
                          className="absolute top-1 right-1 w-6 h-6 bg-red-500 text-white rounded-full flex items-center justify-center text-xs hover:bg-red-600"
                        >
                          ×
                        </button>
                      </>
                    ) : (
                      <label className="w-full h-full flex items-center justify-center cursor-pointer hover:bg-[#2a2a2a] transition-colors">
                        <input
                          type="file"
                          accept="image/*"
                          className="hidden"
                          onChange={(e) => {
                            const file = e.target.files?.[0] || null;
                            handleImageUpload(key, file);
                          }}
                        />
                        <div className="text-center">
                          <svg
                            className="w-8 h-8 text-[#a5a5a5] mx-auto mb-2"
                            fill="none"
                            stroke="currentColor"
                            viewBox="0 0 24 24"
                          >
                            <path
                              strokeLinecap="round"
                              strokeLinejoin="round"
                              strokeWidth={2}
                              d="M12 6v6m0 0v6m0-6h6m-6 0H6"
                            />
                          </svg>
                          <span className="text-xs text-[#a5a5a5]">이미지 업로드</span>
                        </div>
                      </label>
                    )}
                  </div>
                  <span className="text-sm text-[#a5a5a5]">{label}</span>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* 카드등급 */}
        <div className="flex items-center gap-5">
          <div className="flex-1 flex flex-col gap-1">
            <h3 className="mt-2">카드등급</h3>
            <div className="flex items-center gap-2">
              <div className="w-[15px] h-[15px] rounded-full border border-[#c3c3c3] flex items-center justify-center text-[10px]">i</div>
              <span className="text-sm text-[#a5a5a5]">등급가이드</span>
            </div>
          </div>
          <div className="flex-5">
            <span className="text-sm text-[#a5a5a5]">{grade ? <RankElement rank={grade} /> : "AI 카드 감정을 통해 등급을 알 수 있어요!"}</span>
          </div>
        </div>

        {/* 상세설명 */}
        <div className="flex flex-col items-start gap-5">
          <div className="flex items-start gap-2 mt-2">
            <Label htmlFor="detail">상세설명</Label>
            <span className="text-red-500">*</span>
          </div>
          <div className="w-full">
            <Textarea
              id="detail"
              className="h-[200px] p-4 placeholder:text-md"
              {...register("requestDto.detail", { required: "상세설명을 입력해주세요." })}
              placeholder="상세설명을 입력해주세요."
            />
            {errors.requestDto?.detail && <p className="text-red-500 text-sm mt-1">{errors.requestDto?.detail.message}</p>}
          </div>
        </div>

        {/* 경매 기간 */}
        <div className="">
          <Controller
            name="requestDto.startDatetime"
            control={control}
            rules={{ required: "경매 시작일을 설정해주세요." }}
            render={({ field: _field, fieldState }) => (
              <div className="flex items-start gap-5">
                <div className="flex-1 flex items-center gap-2 mt-2">
                  <Label>경매기간</Label>
                  <span className="text-red-500">*</span>
                </div>
                <div className="flex-5">
                  <AuctionNewCalendar
                    onChange={({ startDate, startTime, endDate, endTime }) => {
                      // 시작일시 저장
                      if (startDate) {
                        const startDateStr = startDate.toISOString().split("T")[0];
                        const startIso = `${startDateStr}T${startTime}`;
                        setValue("requestDto.startDatetime", startIso, { shouldValidate: true });
                      }

                      // 종료일시 저장
                      if (endDate) {
                        const endDateStr = endDate.toISOString().split("T")[0];
                        const endIso = `${endDateStr}T${endTime}`;
                        setValue("requestDto.endDatetime", endIso, { shouldValidate: true });
                      }
                    }}
                  />
                  {fieldState.error && (
                    <p className="text-red-500 text-sm mt-1">{fieldState.error.message}</p>
                  )}
                  {errors.requestDto?.endDatetime && (
                    <p className="text-red-500 text-sm mt-1">{errors.requestDto?.endDatetime.message}</p>
                  )}
                </div>
              </div>
            )}
          />
        </div>


        {/* 시작 입찰가 */}
        <div className="flex items-start gap-5">
          <div className="flex-1 flex items-center gap-2 mt-2">
            <Label>시작 입찰가</Label>
            <span className="text-red-500">*</span>
          </div>
          <div className="flex-5">
            <div className="flex gap-2">
              <Input
                type="number"
                className="w-[200px]"
                {...register("requestDto.startPrice", {
                  required: "시작 입찰가를 입력해주세요.",
                  setValueAs: (value) => value === "" ? 0 : parseFloat(value),
                  min: { value: 0.01, message: "0 이상의 값을 입력해주세요." },
                })}
                placeholder="0"
              />

              {/* 입찰단위 */}
              <div className="w-[150px]">
                <Controller
                  name="requestDto.bidUnit"
                  control={control}
                  rules={{ required: "입찰 단위를 선택해주세요." }}
                  render={({ field, fieldState }) => (
                    <>
                      <Select
                        onValueChange={(value) => field.onChange(parseFloat(value))}
                        value={field.value !== null && field.value !== undefined ? String(field.value) : ""}
                      >
                        <SelectTrigger className="h-[50px] bg-[#191924] border-[#353535]">
                          <SelectValue placeholder="입찰 단위 선택" />
                        </SelectTrigger>
                        <SelectContent className="bg-[#191924] border-[#353535] text-white">
                          {["0.01","0.05","0.1","0.5","1","5","10","50","100","500","1000","5000"].map(v => (
                            <SelectItem key={v} value={v}>{v}</SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                      {fieldState.error && (
                        <p className="text-red-500 text-sm mt-1">{fieldState.error.message}</p>
                      )}
                    </>
                  )}
                />
              </div>
            </div>
            {errors.requestDto?.startPrice && (
              <p className="text-red-500 text-sm mt-1">{errors.requestDto.startPrice.message}</p>
            )}
            {errors.requestDto?.startPrice && errors.requestDto?.bidUnit && (
              <p className="text-red-500 text-sm mt-1">{errors.requestDto.startPrice.message}</p>
            )}
            </div>
        </div>


        {/* 즉시구매 */}
        <div className="flex items-center gap-5">
          <div className="flex-1">
            <Label>즉시구매</Label>
          </div>
          <div className="flex-5 flex items-center gap-2">
            <input type="checkbox" {...register("requestDto.buyNowFlag")} id="buyNowFlag" />
            <Label htmlFor="buyNowFlag" className="text-sm text-[#a5a5a5]">즉시구매가능</Label>
          </div>
        </div>

        {/* 즉시구매가 */}
        {isBuyItNow && (
          <div className="flex items-start gap-5">
            <div className="flex-1 flex items-center gap-2 mt-2">
              <Label htmlFor="buyNowPrice">즉시구매가</Label>
              <span className="text-red-500">*</span>
            </div>
            <div className="flex-5">
              <Input
                id="buyNowPrice"
                type="number"
                className="h-12 w-[200px]"
                {...register("requestDto.buyNowPrice", {
                  setValueAs: (value) => value === "" ? 0 : parseFloat(value),
                  validate: value => value > 0 || "즉시구매가를 입력해주세요."
                })}
                placeholder="0"
              />
              {errors.requestDto?.buyNowPrice && <p className="text-red-500 text-sm mt-1">{errors.requestDto?.buyNowPrice.message}</p>}
            </div>
          </div>
        )}

        <Button
          className="h-12 bg-[#7DB7CD] text-[#2B3235] cursor-pointer"
          type="submit">등록하기</Button>
      </form>
    </div>
  )
}
