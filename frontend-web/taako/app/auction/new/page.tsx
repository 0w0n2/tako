"use client"

import * as React from "react"
import { useForm, SubmitHandler, Controller } from "react-hook-form"

import RegisterImage from "@/components/atoms/RegisterImage"
import CreateAuctionCategories from "@/components/categories/CreateAuctionCategories"
import AuctionNewCalendar from "@/components/auction/new/AuctionNewCalendar"
import { AuctionFormProps } from "@/types/auction"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Button } from "@/components/ui/button"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"

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
      bidUnit: undefined,
    }
  });

  const isBuyItNow = watch("buyItNow");

  const onSubmit: SubmitHandler<AuctionFormProps> = data => {
    console.log(data);
  };

  return (
    <div className="default-container pb-10">
      <h2 className="mb-10">경매 등록하기</h2>

      <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-10">

        {/* 사진 등록 */}
        <Controller
          name="images"
          control={control}
          rules={{ required: "이미지를 1개 이상 등록해주세요." }}
          render={({ field, fieldState }) => (
            <div className="flex items-start gap-5">
              <div className="flex-1 flex items-center gap-2 mt-2">
                <Label>사진 등록</Label>
                <span className="text-red-500">*</span>
              </div>
              <div className="flex-5 flex flex-col gap-2">
                <RegisterImage onChange={field.onChange} />
                {fieldState.error && <p className="text-red-500 text-sm mt-1">{fieldState.error.message}</p>}
              </div>
            </div>
          )}
        />

        {/* 카테고리 */}
        <Controller
          name="category"
          control={control}
          rules={{validate: value => 
            value.majorCategoryName?.length && value.minorCategoryName?.length
              ? true
              : "카테고리를 선택해주세요."
          }}
          render={({ field, fieldState }) => (
            <div className="flex items-start gap-5">
              <div className="flex-1 flex flex-col gap-2">
                <Label>카테고리</Label>
                <span className="text-red-500">*</span>
                {/* 선택된 카테고리 표시 */}
                <div className="text-sm text-gray-400">
                  전체
                  {field.value.majorCategoryName && ` > ${field.value.majorCategoryName}`}
                  {field.value.minorCategoryName && ` > ${field.value.minorCategoryName}`}
                </div>
              </div>
              <div className="flex-5">
                <CreateAuctionCategories
                  onChange={(major, minor) => field.onChange({ majorCategoryName: major, minorCategoryName: minor })}
                />
                {fieldState.error && <p className="text-red-500 text-sm mt-1">{fieldState.error.message}</p>}
              </div>
            </div>
          )}
        />

        {/* 제목 */}
        <div className="flex items-start gap-5">
          <div className="flex-1 flex items-center gap-2 mt-2">
            <Label htmlFor="title">제목</Label>
            <span className="text-red-500">*</span>
          </div>
          <div className="flex-5">
            <Input
              id="title"
              {...register("title", { required: "제목을 입력해주세요." })}
              placeholder="제목"
            />
            {errors.title && <p className="text-red-500 text-sm mt-1">{errors.title.message}</p>}
          </div>
        </div>

        {/* 상세설명 */}
        <div className="flex items-start gap-5">
          <div className="flex-1 flex items-start gap-2 mt-2">
            <Label htmlFor="description">상세설명</Label>
            <span className="text-red-500">*</span>
          </div>
          <div className="flex-5">
            <Textarea
              id="description"
              {...register("description", { required: "상세설명을 입력해주세요." })}
              placeholder="상세설명을 입력해주세요."
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
                <span className="text-red-500">*</span>
              </div>
              <div className="flex-5">
                <AuctionNewCalendar onChange={field.onChange} />
                {fieldState.error && <p className="text-red-500 text-sm mt-1">{fieldState.error.message}</p>}
              </div>
            </div>
          )}
        />

        {/* 입찰가 */}
        <div className="flex items-start gap-5">
          <div className="flex-1 flex items-center gap-2 mt-2">
            <Label>시작 입찰가</Label>
            <span className="text-red-500">*</span>
          </div>
          <div className="flex-5">
            <Input
              type="number"
              {...register("startPrice", {
                required: "시작가를 입력해주세요.",
                valueAsNumber: true,
                min: { value: 0, message: "0 이상의 값을 입력해주세요." }
              })}
              placeholder="0"
            />
            {errors.startPrice && <p className="text-red-500 text-sm mt-1">{errors.startPrice.message}</p>}
          </div>
        </div>

        {/* 입찰단위 */}
        <div className="flex-1">
          <Controller
            name="bidUnit"
            control={control}
            rules={{ required: "입찰 단위를 선택해주세요." }}
            render={({ field }) => (
              <Select onValueChange={(value) => field.onChange(parseInt(value))} defaultValue={field.value ? String(field.value) : ""}>
                <SelectTrigger className="h-[50px] bg-[#191924] border-[#353535]">
                  <SelectValue placeholder="입찰 단위 선택" />
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

        {/* 즉시구매 */}
        <div className="flex items-start gap-5">
          <div className="flex-1 mt-2">
            <Label htmlFor="buyItNow">즉시구매</Label>
          </div>
          <div className="flex-5">
            <input type="checkbox" {...register("buyItNow")} id="buyItNow" />
          </div>
        </div>

        {/* 즉시구매가 */}
        {isBuyItNow && (
          <div className="flex items-start gap-5">
            <div className="flex-1 flex items-center gap-2 mt-2">
              <Label htmlFor="buyItNowPrice">즉시구매가</Label>
              <span className="text-red-500">*</span>
            </div>
            <div className="flex-5">
              <Input
                id="buyItNowPrice"
                type="number"
                {...register("buyItNowPrice", {
                  valueAsNumber: true,
                  validate: value => value > 0 || "즉시구매가를 입력해주세요."
                })}
                placeholder="0"
              />
              {errors.buyItNowPrice && <p className="text-red-500 text-sm mt-1">{errors.buyItNowPrice.message}</p>}
            </div>
          </div>
        )}

        <Button type="submit">등록하기</Button>
      </form>
    </div>
  )
}
