import { useState, useEffect } from "react";
import { getMajorCategories } from "@/lib/category";
import { MajorCategories } from "@/types/category";

export function useMajorCategories() {
  const [majorCategories, setMajorCategories] = useState<MajorCategories[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchCategories = async () => {
      try {
        const res = await getMajorCategories();
        // console.log(res);
        setMajorCategories(res.result);
      } catch (err) {
        console.error(err);
      }
    };
    fetchCategories();
  }, []);

  return { majorCategories, loading };
}