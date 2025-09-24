import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { getDelivery, postRecipientAddress } from "@/lib/delivery";
import type { DeliveryInfo } from "@/types/delivery";

export function useDelivery(auctionId: number) {
  const qc = useQueryClient();

  const deliveryQuery = useQuery<{
    result: DeliveryInfo;
    isSuccess: boolean;
  }>({
    queryKey: ["delivery", auctionId],
    queryFn: async () => {
      const data = await getDelivery(auctionId);
      return { result: data.result, isSuccess: data.isSuccess };
    },
    enabled: !!auctionId,
    refetchInterval: 10_000, // 10초마다 배송상태 폴링
  });

  const setRecipientMutation = useMutation({
    mutationFn: async (addressId: number) => {
      const data = await postRecipientAddress(auctionId, addressId);
      return data.result;
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["delivery", auctionId] });
    },
  });

  const info = deliveryQuery.data?.result;
  const hasRecipient = !!info?.recipientAddress?.id;
  const hasTracking = !!info?.trackingNumber;
  const needsRecipient = !hasRecipient;
  const status = info?.status ?? "WAITING";

  return {
    info,
    status,
    hasRecipient,
    hasTracking,
    needsRecipient,
    loading: deliveryQuery.isLoading,
    error: deliveryQuery.isError,
    setRecipient: setRecipientMutation.mutateAsync,
    settingRecipient: setRecipientMutation.isPending,
  };
}
