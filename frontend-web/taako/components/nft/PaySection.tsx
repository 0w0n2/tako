// components/mypage/PaySection.tsx
'use client';

import { useMemo, useState } from 'react';
import { Button } from '@/components/ui/button';
import { useAddress } from '@/hooks/useAddress';
import AddressBookModal from '@/components/modals/AddressBookModal';
import { AlertTriangle } from 'lucide-react';

type Props = {
  trackingNumber?: string | null;
  onPay?: (defaultAddressId: number) => Promise<void> | void; // ← 여기로 결제 로직 주입
};

export default function PaySection({ trackingNumber, onPay }: Props) {
  const { defaultAddress, ensureDefaultAddress } = useAddress();
  const [open, setOpen] = useState(false);
  const [paying, setPaying] = useState(false); // ★ 추가

  const trackingMissing = useMemo(
    () => !((trackingNumber ?? '').trim().length > 0),
    [trackingNumber]
  );

  const onClickPay = async () => {
    try {
      setPaying(true);
      const def = await ensureDefaultAddress(); // 기본 주소 보장
      await onPay?.(def.id);                    // 실제 결제 로직 실행(부모에서 제공)
    } finally {
      setPaying(false);
    }
  };

  return (
    <div className="space-y-4 rounded-xl border p-4">
      {trackingMissing && (
        <div className="flex items-start gap-2 rounded-md border border-amber-300 bg-amber-50 p-3 text-amber-800">
          <AlertTriangle size={18} className="mt-0.5" />
          <div className="text-sm">
            <div className="font-medium">운송장이 미발급 되었습니다.</div>
            <div>운송장 번호가 발급되기 전에는 결제를 진행할 수 없습니다.</div>
          </div>
        </div>
      )}

      <div className="rounded-md border p-3">
        <div className="flex items-center justify-between">
          <div className="text-sm">
            <div className="font-medium">기본 배송지</div>
            {defaultAddress ? (
              <>
                <div>{defaultAddress.placeName}</div>
                <div className="text-gray-600">
                  {defaultAddress.baseAddress} ({defaultAddress.zipcode})
                </div>
              </>
            ) : (
              <div className="text-gray-500">기본 배송지가 없습니다. 배송지를 추가해 주세요.</div>
            )}
          </div>
          <Button variant="outline" onClick={() => setOpen(true)}>
            배송지 관리
          </Button>
        </div>
      </div>

      <div className="flex justify-end">
        <Button onClick={onClickPay} disabled={trackingMissing || paying}>
          {paying ? '결제 중...' : '결제하기'}
        </Button>
      </div>

      <AddressBookModal open={open} onClose={() => setOpen(false)} />
    </div>
  );
}
