import { useState, useEffect } from "react";
import { getMinorCategories } from "@/lib/category";
import { MinorCategories } from "@/types/category";

export function useMinorCategories() {
  const [minorCategories, setMinorCategories] = useState<MinorCategories[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchCategories = async () => {
      try {
        const res = await getMinorCategories();
        // console.log(res);
        setMinorCategories(res.result);
      } catch (err) {
        console.error(err);
      }
    };
    fetchCategories();
  }, []);

  return { minorCategories, loading };
}