"use client";

import * as React from "react";
import { useForm, SubmitHandler, Controller } from "react-hook-form";
import { useState } from "react";
import { useRouter } from "next/navigation";

import Image from "next/image";
import CreateAuctionCategories from "@/components/categories/CreateAuctionCategories";
import AuctionNewCalendar from "@/components/auction/new/AuctionNewCalendar";
import RegisterImage from "@/components/atoms/RegisterImage";
import CardGradingAI from "@/components/auction/CardGradingAI";
import { AuctionFormProps } from "@/types/auction";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Button } from "@/components/ui/button";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { createAuction } from "@/lib/auction";
import RankElement from "@/components/atoms/RankElement";
import { useLoginRedirect, useWalletRedirect } from "@/hooks/useAuthRedirect";


export default function NewAuctionPage() {
  const router = useRouter();
  const [selectedCardName, setSelectedCardName] = React.useState<string>("");
  const [selectedCardImageUrl, setSelectedCardImageUrl] =
    React.useState<string>("");
  const [grade, setGrade] = useState<string>("");
  const [gradeHash, setGradeHash] = useState<string>("");
  
  // 로그인 상태 확인
  useLoginRedirect();
  // 지갑 주소 연동 확인
  useWalletRedirect();

  const {
    register,
    handleSubmit,
    control,
    watch,
    setValue,
    formState: { errors },
  } = useForm<AuctionFormProps>({
    defaultValues: {
      files: [],
      registerNft: false,
      requestDto: {
        gradeHash: null,
        categoryMajorId: null,
        categoryMediumId: null,
        cardId: null,
        tokenId: null,
        title: "",
        detail: "",
        startDatetime: "",
        endDatetime: "",
        buyNowFlag: false,
        buyNowPrice: 0,
        bidUnit: 0,
        startPrice: 0,
      },
    },
  });

  const isBuyItNow = watch("requestDto.buyNowFlag");
  const isRegisteringNft = watch("registerNft");
  const categoryMajorId = watch("requestDto.categoryMajorId");

  // ID가 4(SSAFY) 이면 NFT 등록을 강제
  const isNftRegistrationForced = categoryMajorId === 4;

  React.useEffect(() => {
    if (isNftRegistrationForced) {
      setValue("registerNft", true);
    }
  }, [isNftRegistrationForced, setValue]);

  // AI 감정 결과 핸들러
  const handleGradeChange = (grade: string, gradeHash: string) => {
    setGrade(grade);
    setGradeHash(gradeHash);
  };

  const onSubmit: SubmitHandler<AuctionFormProps> = async (data) => {
    const { requestDto, registerNft } = data;

    if (!registerNft) {
      requestDto.tokenId = null;
    }

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
      "startPrice",
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
    if (
      requestDto.buyNowFlag &&
      (!requestDto.buyNowPrice || requestDto.buyNowPrice <= 0)
    ) {
      emptyFields.push("buyNowPrice");
    }


    if (emptyFields.length > 0) {
      alert("입력하지 않은 필수값이 있습니다.");
      return;
    }

    try {
      const res = await createAuction(requestDto, data.files || []);

      if (res.code === 200) {
        alert("신규 경매 등록에 성공했습니다.");
        router.push("/");
      } else if (res.code === 1250) {
        alert("NFT 카드 정보를 확인할 수 없습니다.");
      } else if (res.code === 1124) {
        alert(
          "지갑 주소가 연동되어 있지 않습니다. 마이페이지에서 등록 후 다시 시도해주세요."
        );
      } else {
        alert(res.message);
      }
    } catch (err) {
      console.error(err);
      alert("신규 경매 등록 중 오류가 발생했습니다.");
    }
  };

  return (
    <div className="small-container pb-10">
      <h2 className="mb-10">경매 등록하기</h2>

      <form
        onSubmit={handleSubmit(onSubmit)}
        className="flex flex-col gap-15"
        encType="multipart/form-data"
      >
        <Controller
          name="files"
          control={control}
          rules={{
            validate: (value) =>
              (value && value.length > 0) || "이미지를 1개 이상 등록해주세요.",
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
                {fieldState.error && (
                  <p className="text-red-500 text-sm mt-1">
                    {fieldState.error.message}
                  </p>
                )}
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
              onChange={(
                majorId,
                majorName,
                minorId,
                minorName,
                cardId,
                cardName,
                cardImageUrl
              ) => {
                setValue("requestDto.categoryMajorId", majorId);
                setValue("requestDto.categoryMediumId", minorId);
                setValue("requestDto.cardId", cardId);
                setSelectedCardName(cardName ?? "");
                setSelectedCardImageUrl(cardImageUrl ?? "");
              }}
              onReset={() => {
                setSelectedCardName("");
                setSelectedCardImageUrl("");
              }}
            />
            {(errors.requestDto?.categoryMajorId ||
              errors.requestDto?.categoryMediumId) && (
              <p className="text-red-500 text-sm mt-1">
                카테고리를 선택해주세요.
              </p>
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
              {...register("requestDto.title", {
                required: "제목을 입력해주세요.",
              })}
              placeholder="제목"
            />
            {errors.requestDto?.title && (
              <p className="text-red-500 text-sm mt-1">
                {errors.requestDto?.title.message}
              </p>
            )}
          </div>
        </div>

        {/* 카드 감정하기(AI) */}
        <CardGradingAI onGradeChange={handleGradeChange} />

        {/* 카드등급 */}
        <div className="flex items-center gap-5">
          <div className="flex-1 flex flex-col gap-1">
            <h3 className="mt-2">카드 등급</h3>
            <div className="flex items-center gap-2">
              <div className="w-[15px] h-[15px] rounded-full border border-[#c3c3c3] flex items-center justify-center text-[10px]">
                i
              </div>
              <span className="text-sm text-[#a5a5a5]">등급가이드</span>
            </div>
          </div>
          <div className="flex-5">
            <span className="text-sm text-[#a5a5a5]">
              {grade ? (
                <RankElement rank={grade} />
              ) : (
                "AI 카드 감정을 통해 등급을 알 수 있어요."
              )}
            </span>
          </div>
        </div>

        {/* 상세설명 */}
        <div className="flex flex-col items-start gap-5">
          <div className="flex items-start gap-2 mt-2">
            <Label htmlFor="detail">상세 설명</Label>
            <span className="text-red-500">*</span>
          </div>
          <div className="w-full">
            <Textarea
              id="detail"
              className="h-[200px] p-4 placeholder:text-md"
              {...register("requestDto.detail", {
                required: "상세설명을 입력해주세요.",
              })}
              placeholder="상세설명을 입력해주세요."
            />
            {errors.requestDto?.detail && (
              <p className="text-red-500 text-sm mt-1">
                {errors.requestDto?.detail.message}
              </p>
            )}
          </div>
        </div>

        {/* NFT 등록 */}
        <div className="flex flex-col gap-5 border-t border-b border-[#353535] py-6">
          <div className="flex items-center gap-5">
            <div className="flex-1">
              <Label>NFT 카드</Label>
            </div>
            <div className="flex-5 flex items-center gap-2">
              <input
                type="checkbox"
                {...register("registerNft")}
                id="registerNft"
                disabled={isNftRegistrationForced}
                className="w-4 h-4 accent-[#7DB7CD]"
              />
              <Label
                htmlFor="registerNft"
                className={`text-sm text-[#a5a5a5] ${
                  !isNftRegistrationForced && "cursor-pointer"
                }`}
              >
                NFT 토큰 정보 등록
              </Label>
            </div>
          </div>
          {isRegisteringNft && (
            <div className="flex items-start gap-5">
              <div className="flex-1 flex items-center gap-2 mt-2">
                <Label htmlFor="registerNft">토큰 ID</Label>
                <span className="text-red-500">*</span>
              </div>
              <div className="flex-5">
                <Input
                  id="tokenId"
                  type="number"
                  className="h-12 w-full"
                  {...register("requestDto.tokenId", {
                    required: "NFT 등록 시 Token ID는 필수입니다.",
                    setValueAs: (value) => (value ? parseInt(value, 10) : null),
                    pattern: {
                      // (선택) 숫자만 입력받도록 강제
                      value: /^[0-9]+$/,
                      message: "숫자만 입력해주세요.",
                    },
                  })}
                  placeholder="카드의 ID를 입력하세요. (Ex. 156702684)"
                />
                {errors.requestDto?.tokenId && (
                  <p className="text-red-500 text-sm mt-1">
                    {errors.requestDto?.tokenId.message}
                  </p>
                )}
              </div>
            </div>
          )}
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
                  <Label>경매 기간</Label>
                  <span className="text-red-500">*</span>
                </div>
                <div className="flex-5">
                  <AuctionNewCalendar
                    onChange={({ startDate, startTime, endDate, endTime }) => {
                      // 시작일시 저장
                      if (startDate) {
                        const startDateStr = startDate
                          .toISOString()
                          .split("T")[0];
                        const startIso = `${startDateStr}T${startTime}`;
                        setValue("requestDto.startDatetime", startIso, {
                          shouldValidate: true,
                        });
                      }

                      // 종료일시 저장
                      if (endDate) {
                        const endDateStr = endDate.toISOString().split("T")[0];
                        const endIso = `${endDateStr}T${endTime}`;
                        setValue("requestDto.endDatetime", endIso, {
                          shouldValidate: true,
                        });
                      }
                    }}
                  />
                  {fieldState.error && (
                    <p className="text-red-500 text-sm mt-1">
                      {fieldState.error.message}
                    </p>
                  )}
                  {errors.requestDto?.endDatetime && (
                    <p className="text-red-500 text-sm mt-1">
                      {errors.requestDto?.endDatetime.message}
                    </p>
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
            <div className="relative w-[200px]">
              <Input
                type="text"
                inputMode="decimal"
                className="w-full"
                {...register("requestDto.startPrice", {
                  required: "시작 입찰가를 입력해주세요.",
                  setValueAs: (value) => (value === "" ? 0 : parseFloat(value)),
                  min: {
                    value: 0.0001,
                    message: "0.0001 이상의 값을 입력해주세요.",
                  },
                })}
                placeholder="0.00000000"
              />
              <span className="absolute inset-y-0 right-0 flex items-center pr-4 text-gray-400 pointer-events-none">
                ETH
              </span>
            </div>
            {errors.requestDto?.startPrice && (
              <p className="text-red-500 text-sm mt-1">
                {errors.requestDto.startPrice.message}
              </p>
            )}
          </div>
        </div>
        {/* 입찰단위 */}
        <div className="flex items-start gap-5">
          <div className="flex-1 flex items-center gap-2 mt-2">
            <Label>입찰 단위</Label>
            <span className="text-red-500">*</span>
          </div>
          <div className="flex-5">
            <div className="relative w-[200px]">
              <Controller
                name="requestDto.bidUnit"
                control={control}
                rules={{
                  required: "입찰 단위를 선택해주세요.",
                  validate: (v) => v > 0 || "입찰 단위를 선택해주세요.",
                }}
                render={({ field }) => (
                  <Select
                    onValueChange={(value) => field.onChange(parseFloat(value))}
                    value={
                      field.value !== null && field.value !== undefined
                        ? String(field.value)
                        : ""
                    }
                  >
                    <SelectTrigger className="h-[50px] bg-[#191924] border-[#353535]">
                      <SelectValue placeholder="입찰 단위 선택" />
                    </SelectTrigger>
                    <SelectContent className="bg-[#191924] border-[#353535] text-white">
                      {[
                        "0.01",
                        "0.05",
                        "0.1",
                        "0.5",
                        "1",
                        "5",
                        "10",
                        "50",
                        "100",
                        "500",
                        "1000",
                        "5000",
                      ].map((v) => (
                        <SelectItem key={v} value={v}>
                          <div className="flex w-full justify-between">
                            <span>{v}</span>
                          </div>
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                )}
              />
            </div>
            {errors.requestDto?.startPrice && errors.requestDto?.bidUnit && (
              <p className="text-red-500 text-sm mt-1">
                {errors.requestDto.startPrice.message}
              </p>
            )}
          </div>
        </div>

        {/* 즉시구매 */}
        <div className="flex items-center gap-5">
          <div className="flex-1">
            <Label>즉시 구매</Label>
          </div>
          <div className="flex-5 flex items-center gap-2">
            <input
              type="checkbox"
              {...register("requestDto.buyNowFlag")}
              id="buyNowFlag"
            />
            <Label htmlFor="buyNowFlag" className="text-sm text-[#a5a5a5]">
              즉시 구매 가능
            </Label>
          </div>
        </div>

        {/* 즉시구매가 */}
        {isBuyItNow && (
          <div className="flex items-start gap-5">
            <div className="flex-1 flex items-center gap-2 mt-2">
              <Label htmlFor="buyNowPrice">즉시 구매가</Label>
              <span className="text-red-500">*</span>
            </div>
            <div className="flex-5">
              <div className="relative w-[200px]">
                <Input
                  id="buyNowPrice"
                  type="text"
                  inputMode="decimal"
                  className="h-12 w-full"
                  {...register("requestDto.buyNowPrice", {
                    setValueAs: (value) =>
                      value === "" ? 0 : parseFloat(value),
                    validate: (value) =>
                      value > 0 || "즉시 구매가를 입력해주세요.",
                  })}
                  placeholder="0.00000000"
                />
                <span className="absolute inset-y-0 right-0 flex items-center pr-4 text-gray-400 pointer-events-none">
                  ETH
                </span>
              </div>
              {errors.requestDto?.buyNowPrice && (
                <p className="text-red-500 text-sm mt-1">
                  {errors.requestDto?.buyNowPrice.message}
                </p>
              )}
            </div>
          </div>
        )}

        <Button
          className="h-12 bg-[#7DB7CD] text-[#2B3235] cursor-pointer"
          type="submit"
        >
          등록하기
        </Button>
      </form>
    </div>
  );
}
