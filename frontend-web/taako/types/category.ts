export interface CreateAuctionCategoriesProps {
  onChange: (majorName: string, minorName: string) => void;
}

export interface CategoryPageProps {
  params: {
    category_name: string
  }
}

export interface MajorCategories{
  id: number;
  name: string;
  description:string;
}

export interface MinorCategories{
  id: number;
  name: string;
  categoryMajorId:number;
  description:string;
}
