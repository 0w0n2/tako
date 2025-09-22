import { useMemo } from "react";
import { useQuery, UseQueryOptions } from "@tanstack/react-query";
import { getAuctions } from "@/lib/auction";
import { GetAuction, GetHotCards } from "@/types/auction";

type AuctionsResponse = { result: { content: GetAuction[] } } | any;

type Options = Omit<UseQueryOptions<AuctionsResponse>, 'queryKey' | 'queryFn'>;

export function useAuctionsQuery(
  params: Partial<GetHotCards>,
  options?: Options
) {
  const merged = useMemo<Partial<GetHotCards>>(
    () => ({ page: 0, ...params }),
    [params]
  );

  const queryKey = useMemo(() => ['auctions', merged], [merged]);

  return useQuery<AuctionsResponse>({
    queryKey,
    queryFn: () => getAuctions(merged as GetHotCards),
    retry: 1,
    refetchOnWindowFocus: false,
    staleTime: 1000 * 30,
    gcTime: 1000 * 60 * 5,
    placeholderData: (prev: AuctionsResponse | undefined) => prev,
    ...options,
  });
}


