import { useState, useRef } from "react";
import { getAuctions } from "@/lib/auction";
import { GetHotCards } from "@/types/auction";

export const useAuction = () => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // 중복 호출 방지용 ref
  const isFetching = useRef(false);

  const handlerGetAuctions = async (params: Partial<GetHotCards>) => {
    if (isFetching.current) return; // 중복 호출 방지

    setLoading(true);
    isFetching.current = true;

    try {
      const res = await getAuctions(params as GetHotCards);
      return res;
    } catch (err: any) {
      console.error(err);
      setError(err.message || "경매 조회 중 오류가 발생했습니다.");
      throw err;
    } finally {
      setLoading(false);
      isFetching.current = false;
    }
  };

  return {
    handlerGetAuctions,
    loading,
    error,
  };
};