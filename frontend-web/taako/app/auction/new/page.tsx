"use client"

import * as React from "react"
import { zodResolver } from "@hookform/resolvers/zod"
import { useForm } from "react-hook-form"

import { Button } from "@/components/ui/button"
import {
  Form,
  FormControl,
  FormDescription,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form"
import { Input } from "@/components/ui/input"
import { Textarea } from "@/components/ui/textarea"
import { Checkbox } from "@/components/ui/checkbox"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"
import RegisterImage from "@/components/atoms/RegisterImage"
import CreateAuctionCategories from "@/components/categories/CreateAuctionCategories"
import AuctionNewCalendar from "@/components/auction/new/AuctionNewCalendar"
import { formSchema, AuctionFormValues } from "@/types/auctionFormSchema"

export default function NewAuctionPage() {
  const form = useForm<AuctionFormValues>({
    resolver: zodResolver(formSchema),
    defaultValues: {
      title: "",
      description: "",
      bidUnit: "100",
      startPrice: 0,
      buyItNow: false,
      buyItNowPrice: 0,
      category: {
        majorCategoryId: null,
        majorCategoryName: '',
        minorCategoryId: null,
        minorCategoryName: '',
      },
      calendar: {},
    },
  })

  const isBuyItNow = form.watch("buyItNow");

  function onSubmit(values: AuctionFormValues) {
    console.log(values)
  }

  return (
    <div className="default-container pb-[40px]">
      <h2 className="mb-10">경매 등록하기</h2>

      <Form {...form}>
        <form onSubmit={form.handleSubmit(onSubmit)} className="flex flex-col gap-10">
          {/* 사진 등록 */}
          <div className="flex items-start gap-5">
            <div className="flex-1 flex items-start gap-2 mt-2">
              <h3>사진 등록</h3>
              <span className="text-[#FF0000]">*</span>
            </div>
            <div className="flex-5 flex flex-col gap-2 overflow-hidden">
              <RegisterImage />
              <p className="text-[#a5a5a5] mt-1 text-sm">상품 이미지를 등록해주세요.<br/>
              첫 번째 사진이 대표 이미지로 사용됩니다.</p>
            </div>
          </div>

          {/* 카테고리 */}
          <FormField
            control={form.control}
            name="category"
            render={({ field }) => (
              <FormItem className="flex items-start gap-5">
                <div className="flex-1 flex flex-col gap-2">
                  <div className="flex items-start gap-2 mt-2">
                    <FormLabel>카테고리</FormLabel>
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
                  <FormMessage />
                </div>
              </FormItem>
            )}
          />

          {/* 제목 */}
          <FormField
            control={form.control}
            name="title"
            render={({ field }) => (
              <FormItem className="flex items-start gap-5">
                <div className="flex-1 flex items-start gap-2 mt-2">
                  <FormLabel>제목</FormLabel>
                  <span className="text-[#FF0000]">*</span>
                </div>
                <div className="flex-5">
                  <FormControl>
                    <Input placeholder="제목" {...field} className="bg-[#191924] border-[#353535]" />
                  </FormControl>
                  <FormMessage />
                </div>
              </FormItem>
            )}
          />

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
                <Button
                  type="button"
                  className="rounded-lg px-8 py-3 bg-[#7DB7CD] border-1 border-[#7DB7CD] text-[#111] shadow-lg"
                >
                  AI 감정하기
                </Button>
              </div>

              {/* 카드 */}
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
          <FormField
            control={form.control}
            name="description"
            render={({ field }) => (
              <FormItem className="flex gap-5">
                <div className="flex-1 flex items-start gap-2 mt-2">
                  <FormLabel>상세설명</FormLabel>
                  <span className="text-[#ff0000]">*</span>
                </div>
                <div className="flex-5">
                  <FormControl>
                    <Textarea placeholder="상세설명을 입력해주세요." {...field} className="bg-[#191924] border-[#353535] h-40" />
                  </FormControl>
                  <FormMessage />
                </div>
              </FormItem>
            )}
          />

          {/* 경매기간 */}
          <FormField
            control={form.control}
            name="calendar"
            render={({ field }) => (
                <FormItem className="flex items-start gap-5">
                    <div className="flex-1 flex items-center gap-2 mt-2">
                        <FormLabel>경매기간</FormLabel>
                        <span className="text-[#ff0000]">*</span>
                    </div>
                    <div className="flex-5 flex flex-col md:flex-row md:items-center gap-4">
                        <AuctionNewCalendar onChange={field.onChange} />
                        <FormMessage />
                    </div>
                </FormItem>
            )}
          />

          {/* 입찰가 */}
          <div className="flex items-start gap-5">
            <div className="flex-1 flex items-center gap-2 mt-2">
              <h3>입찰가</h3>
              <span className="text-[#ff0000]">*</span>
            </div>
            <div className="flex-5 flex flex-col gap-2">
              <p className="text-sm">입찰 단위</p>
              <div className="flex gap-3">
                <FormField
                  control={form.control}
                  name="bidUnit"
                  render={({ field }) => (
                    <FormItem>
                      <Select onValueChange={field.onChange} defaultValue={field.value}>
                        <FormControl>
                          <SelectTrigger className="border-[#a5a5a5]">
                            <SelectValue />
                          </SelectTrigger>
                        </FormControl>
                        <SelectContent>
                          <SelectItem value="100">100</SelectItem>
                          <SelectItem value="500">500</SelectItem>
                          <SelectItem value="1000">1000</SelectItem>
                        </SelectContent>
                      </Select>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                <FormField
                  control={form.control}
                  name="startPrice"
                  render={({ field }) => (
                    <FormItem className="relative">
                      <FormControl>
                        <Input type="number" placeholder="0" min={0} {...field} onChange={event => field.onChange(+event.target.value)} className="bg-transparent border-[#a5a5a5] pr-12" />
                      </FormControl>
                      <span className="absolute right-4 top-1/2 -translate-y-1/2 text-sm">TKC</span>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>
            </div>
          </div>

          {/* 속성 */}
            <FormField
                control={form.control}
                name="buyItNow"
                render={({ field }) => (
                    <FormItem className="flex items-start gap-5">
                        <h3 className="flex-1">속성</h3>
                        <div className="flex-5 flex items-center space-x-2">
                            <FormControl>
                                <Checkbox
                                    checked={field.value}
                                    onCheckedChange={field.onChange}
                                />
                            </FormControl>
                            <FormLabel>즉시구매</FormLabel>
                        </div>
                    </FormItem>
                )}
            />

          {/* 즉시구매가 */}
          <FormField
            control={form.control}
            name="buyItNowPrice"
            render={({ field }) => (
              <FormItem className="flex gap-5">
                <h3 className="flex-1 mt-2">즉시구매가</h3>
                <div className="flex-5 relative">
                    <FormControl>
                        <Input type="number" placeholder="0" min={0} {...field} disabled={!isBuyItNow} onChange={event => field.onChange(+event.target.value)} className="bg-transparent border-[#353535] pr-20" />
                    </FormControl>
                    <span className="absolute right-4 top-1/2 -translate-y-1/2 text-sm">BTC</span>
                    <FormMessage />
                </div>
              </FormItem>
            )}
          />

          {/* 제출 */}
          <div className="flex justify-end gap-3">
            <Button type="button" variant="outline" size="lg">취소</Button>
            <Button type="submit" size="lg" className="bg-gradient-to-r from-[#863ba9] to-[#487bd9]">경매 등록</Button>
          </div>
        </form>
      </Form>
    </div>
  )
}
