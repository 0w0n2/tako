export interface AuctionCategoryData {
  majorCategoryId: number | null
  majorCategoryName: string
  minorCategoryId: number | null
  minorCategoryName: string
}

export interface CreateAuctionCategoriesProps {
  onChange?: (data: AuctionCategoryData) => void
}

export interface CategoryPageProps {
  params: {
    categoryId: number,
    categoryName: string
  }
}

export interface MajorCategories {
  id: number;
  name: string;
  description: string;
}

export interface MinorCategories {
  id: number;
  name: string;
  categoryMajorId: number;
  description: string;
}
