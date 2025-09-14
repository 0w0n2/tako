"use client"

import * as React from "react"
import { useForm, SubmitHandler, Controller } from "react-hook-form"
import {Button} from "@heroui/button";

import RegisterImage from "@/components/atoms/RegisterImage"
import CreateAuctionCategories from "@/components/categories/CreateAuctionCategories"
import AuctionNewCalendar from "@/components/auction/new/AuctionNewCalendar"
import { AuctionFormProps } from "@/types/auction";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";

export default function NewAuctionPage() {
  const { register, handleSubmit, control, watch, formState: { errors } } = useForm<AuctionFormProps>({
    defaultValues: {
      images: [],
      category: { majorCategoryName: "", minorCategoryName: "" },
      title: "",
      description: "",
      calendar: null,
      startPrice: 0,
      buyItNow: false,
      buyItNowPrice: 0,
    }
  });

  const onSubmit: SubmitHandler<AuctionFormProps> = data => {
    console.log(data);
  };

  const isBuyItNow = watch("buyItNow");

  return (
    <div className="default-container pb-[40px]">
      <h2 className="mb-10">경매 등록하기</h2>

      <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-10">
        {/* 사진 등록 */}
        <div className="flex items-start gap-5">
          <div className="flex-1 flex items-start gap-2 mt-2">
            <h3>사진 등록</h3>
            <span className="text-[#FF0000]">*</span>
          </div>
          <div className="flex-5 flex flex-col gap-2 overflow-hidden">
            <Controller
              name="images"
              control={control}
              rules={{ required: "이미지를 1개 이상 등록해주세요." }}
              render={({ field, fieldState }) => (
                <div>
                  <RegisterImage onChange={field.onChange} />
                  <p className="text-[#a5a5a5] mt-1 text-sm">상품 이미지를 등록해주세요.<br/>
                  첫 번째 사진이 대표 이미지로 사용됩니다.</p>
                  {fieldState.error && <p className="text-red-500 text-sm mt-1">{fieldState.error.message}</p>}
                </div>
              )}
            />
          </div>
        </div>

        {/* 카테고리 */}
        <Controller
          name="category"
          control={control}
          rules={{ validate: (value) => (value.majorCategoryName !== "" && value.minorCategoryName !== "") || "카테고리를 선택해주세요." }}
          render={({ field, fieldState }) => (
            <div className="flex items-start gap-5">
              <div className="flex-1 flex flex-col gap-2">
                <div className="flex items-start gap-2 mt-2">
                  <Label>카테고리</Label>
                  <span className="text-[#FF0000]">*</span>
                </div>
                <div className="flex gap-2 text-sm text-[#a5a5a5]">
                  <p>전체</p>
                  {field.value.majorCategoryName && (
                    <p>{`>`} {field.value.majorCategoryName}</p>
                  )}
                  {field.value.minorCategoryName && (
                    <p>{`>`} {field.value.minorCategoryName}</p>
                  )}
                </div>
              </div>
              <div className="flex-5">
                <CreateAuctionCategories onChange={field.onChange} />
                {fieldState.error && <p className="text-red-500 text-sm mt-1">{fieldState.error.message}</p>}
              </div>
            </div>
          )}
        />

        {/* 제목 */}
        <div className="flex items-start gap-5">
          <div className="flex-1 flex items-start gap-2 mt-2">
            <Label htmlFor="title">제목</Label>
            <span className="text-[#FF0000]">*</span>
          </div>
          <div className="flex-5">
            <Input
              id="title"
              {...register("title", { required: "제목을 입력해주세요." })}
              placeholder="제목"
              className="h-[50px] bg-[#191924] border-[#353535]"
            />
            {errors.title && <p className="text-red-500 text-sm mt-1">{errors.title.message}</p>}
          </div>
        </div>

        {/* 카드 감정하기(AI) */}
        <div className="flex items-start gap-5">
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

          <div className="flex-5 flex flex-col gap-4">
            <div className="flex justify-between gap-2 mt-3">
              <p className="text-sm text-[#a5a5a5]">카드 이미지를 등록해 주세요<br/>
              (촬영 가이드 참고)</p>
              <Button className="rounded-lg px-6 h-[50px] bg-[#7DB7CD] border-1 border-[#7DB7CD] text-[#111] shadow-lg">
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

            <div className="grid grid-cols-4 gap-4">
              {["(앞면)","(뒷면)","(긴모서리)","(짧은모서리)"].map((label) => (
                <div key={label} className="flex flex-col items-center gap-2">
                  <div className="w-full aspect-[4/5] border border-[#353535] bg-[#191924] rounded" />
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
            <span className="text-sm text-[#a5a5a5]">AI 카드 감정을 통해 등급을 알 수 있어요!</span>
          </div>
        </div>

        {/* 상세설명 */}
        <div className="flex gap-5">
          <div className="flex-1 flex items-start gap-2 mt-2">
            <Label htmlFor="description">상세설명</Label>
            <span className="text-[#ff0000]">*</span>
          </div>
          <div className="flex-5">
            <Textarea
              id="description"
              {...register("description", { required: "상세설명을 입력해주세요." })}
              placeholder="상세설명을 입력해주세요."
              className="bg-[#191924] border-[#353535] h-40"
            />
            {errors.description && <p className="text-red-500 text-sm mt-1">{errors.description.message}</p>}
          </div>
        </div>

        {/* 경매기간 */}
        <Controller
          name="calendar"
          control={control}
          rules={{ required: "경매 기간을 설정해주세요." }}
          render={({ field, fieldState }) => (
            <div className="flex items-start gap-5">
                <div className="flex-1 flex items-center gap-2 mt-2">
                    <Label>경매기간</Label>
                    <span className="text-[#ff0000]">*</span>
                </div>
                <div className="flex-5 flex flex-col md:flex-row md:items-center gap-4">
                    <AuctionNewCalendar onChange={field.onChange} />
                </div>
                {fieldState.error && <p className="text-red-500 text-sm mt-1">{fieldState.error.message}</p>}
            </div>
          )}
        />

        {/* 입찰가 */}
        <div className="flex items-start gap-5">
          <div className="flex-1 flex items-center gap-2 mt-2">
            <h3>입찰가</h3>
            <span className="text-[#ff0000]">*</span>
          </div>
          <div className="flex-5 flex flex-col gap-2">
            <div className="flex items-center gap-4">
                <div className="flex flex-col gap-2">
                    <p className="text-sm">시작 입찰가</p>
                    <div className="relative">
                        <Input
                        type="number"
                        {...register("startPrice", { required: "시작가를 입력해주세요.", valueAsNumber: true, min: { value: 0, message: "0 이상의 값을 입력해주세요." } })}
                        placeholder="0"
                        min={0}
                        className="h-[50px] bg-[#191924] border-[#353535] pr-12"
                        />
                        <span className="absolute right-4 top-1/2 pb-0.5 -translate-y-1/2 text-sm">TKC</span>
                    </div>
                    {errors.startPrice && <p className="text-red-500 text-sm mt-1">{errors.startPrice.message}</p>}
                </div>
                <div className="flex flex-col gap-2">
                    <p className="text-sm">입찰 단위</p>
                    <Controller
                        name="bidUnit"
                        control={control}
                        rules={{ required: "입찰 단위를 선택해주세요." }}
                        render={({ field }) => (
                            <Select onValueChange={(value) => field.onChange(parseInt(value))} defaultValue={field.value ? String(field.value) : ""}>
                                <SelectTrigger className="h-[50px] bg-[#191924] border-[#353535]">
                                    <SelectValue placeholder="선택" />
                                </SelectTrigger>
                                <SelectContent className="bg-[#191924] border-[#353535] text-white">
                                    <SelectItem value="100">100</SelectItem>
                                    <SelectItem value="500">500</SelectItem>
                                    <SelectItem value="1000">1000</SelectItem>
                                </SelectContent>
                            </Select>
                        )}
                    />
                    {errors.bidUnit && <p className="text-red-500 text-sm mt-1">{errors.bidUnit.message}</p>}
                </div>
            </div>
          </div>
        </div>

        {/* 속성 */}
        <div className="flex items-start gap-5">
            <h3 className="flex-1 mt-2">속성</h3>
            <div className="flex-5 flex items-center space-x-2">
                <input
                    type="checkbox"
                    id="buyItNow"
                    {...register("buyItNow")}
                    className="h-4 w-4"
                />
                <Label htmlFor="buyItNow" className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70">즉시구매</Label>
            </div>
        </div>


        {/* 즉시구매가 */}
        {isBuyItNow && (
          <div className="flex gap-5">
            <h3 className="flex-1 mt-2">즉시구매가</h3>
            <div className="flex-5 relative">
                <Input
                  type="number"
                  {...register("buyItNowPrice", { valueAsNumber: true, validate: value => !isBuyItNow || (value && value > 0) || "즉시구매가를 입력해주세요." })}
                  placeholder="0"
                  min={0}
                  disabled={!isBuyItNow}
                  className="h-[50px] bg-[#191924] border-[#353535] pr-20 w-full disabled:bg-gray-800 disabled:cursor-not-allowed"
                />
                <span className="absolute right-4 top-1/2 -translate-y-1/2 text-sm">TKC</span>
                {errors.buyItNowPrice && <p className="text-red-500 text-sm mt-1">{errors.buyItNowPrice.message}</p>}
            </div>
          </div>
        )}

        {/* 제출 */}
        <Button type="submit" size="lg" className="m-auto px-8 h-[50px] bg-[#364153] text-[#7DB7CD] border-1 border-[#7DB7CD] cursor-pointer rounded-lg
                    hover:bg-[#3E4C63] transition-all duration-300">등록하기</Button>
      </form>
    </div>
  )
}