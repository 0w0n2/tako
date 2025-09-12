import * as z from "zod"

export const formSchema = z.object({
  title: z.string().min(1, { message: "제목을 입력해주세요." }),
  description: z.string().min(1, { message: "상세설명을 입력해주세요." }),
  bidUnit: z.string(),
  startPrice: z.any(),
  buyItNow: z.boolean(),
  buyItNowPrice: z.any().optional(),
  category: z.object({
    majorCategoryId: z.number().nullable(),
    majorCategoryName: z.string(),
    minorCategoryId: z.number().nullable(),
    minorCategoryName: z.string(),
  }),
  calendar: z.object({
    startDate: z.date().optional(),
    endDate: z.date().optional(),
    endTime: z.string().optional(),
  }),
})

export type AuctionFormValues = z.infer<typeof formSchema>;
