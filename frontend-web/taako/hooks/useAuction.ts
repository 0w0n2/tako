import { useState } from "react";
import { getAuctions } from "@/lib/auction";
import { GetHotCards } from "@/types/auction";

export const useAuction = () => {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    // 경매 조회
    const handlerGetAuctions = async (params: Partial<GetHotCards>) => {
        setLoading(true);

        try {
            const res = await getAuctions(params as GetHotCards);
            return res;
        } catch (err: any) {
            console.error(err);
            throw err;
        } finally {
            setLoading(false);
        }
    };

    return {
        handlerGetAuctions,
        loading,
        error,
    };
};