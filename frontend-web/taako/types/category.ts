export interface AuctionCategoryData {
    majorCategoryId: number | null
    majorCategoryName: string
    minorCategoryId: number | null
    minorCategoryName: string
  }
  
export interface CreateAuctionCategoriesProps {
  onChange?: (data: AuctionCategoryData) => void
}  