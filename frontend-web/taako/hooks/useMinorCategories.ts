import { useState } from "react";
import { getMinorCategories } from "@/lib/category";
import { MinorCategories } from "@/types/category";

export function useMinorCategories() {
  const [minorCategories, setMinorCategories] = useState<MinorCategories[]>([]);
  const [minorCategoryId, setMinorCategoryId] = useState<number|null>(null);
  const [minorCategoryName, setMinorCategoryName] = useState<string|null>(null);
  const [loading, setLoading] = useState(true);

  const handleGetMinorCategories = async (majorId:number) => {
    try{
      const res = await getMinorCategories(majorId);
      // console.log(res)
      setMinorCategories(res.result);
    }catch(err){
      console.error(err);
    }
  }

  return {
    handleGetMinorCategories, setMinorCategoryId, setMinorCategoryName,
    minorCategories, minorCategoryId, minorCategoryName,
    loading,
  };
}