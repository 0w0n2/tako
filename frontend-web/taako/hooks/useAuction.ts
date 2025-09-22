import { useState, useCallback } from "react";
import { getAuctions } from "@/lib/auction"
import { GetHotCards } from "@/types/auction";

export const useAuction = () => {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    // 경매 조회
    const handlerGetAuctions = useCallback(async(params: Partial<GetHotCards>) => {
        try{
            setLoading(true);
            setError(null);
            const res = await getAuctions(params as GetHotCards);
            return res;
        }catch(err: any) {
            const errorMessage = err.response?.data?.message || err.message || "경매 조회 중 오류가 발생했습니다.";
            setError(errorMessage);
            console.error("경매 조회 오류:", errorMessage);
            throw err;
        }finally{
            setLoading(false);
        }
    }, []);

    return {
        handlerGetAuctions,
        loading, error,
    }
}