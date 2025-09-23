'use client';

import { useEffect, useState } from 'react';
import { AuctionDetailProps } from '@/types/auction';
import { useInquiries } from '@/hooks/useInquiries';
import CreateInquiryModal from '../../modals/CreateInquiryModal';
import InquiryRow from '@/components/sections/auction/InquiryRow';
import InquiryDetailModal from '@/components/modals/InquiryDetailModal';

type Props = {
  props: AuctionDetailProps;
  onTotalChange?: (n: number) => void;   // ← 추가
};

export default function AuctionInquiry({ props, onTotalChange }: Props) {
  const [showForm, setShowForm] = useState(false);
  const [detailId, setDetailId] = useState<number | null>(null);

  const { list, loading, error, addInquiry, fetchList } = useInquiries(props.id);

  // 목록이 바뀔 때마다 총 개수 부모로 전달
  useEffect(() => {
    if (list) onTotalChange?.(list.totalElements ?? 0);
  }, [list, onTotalChange]);

  const openDetail = (id: number) => setDetailId(id);
  const closeDetail = () => setDetailId(null);

  return (
    <div className="space-y-4">
      {loading && <div className="py-10 text-center text-[#999]">불러오는 중...</div>}
      {error && <div className="py-10 text-center text-red-400">{error}</div>}

      {!loading && !error && (list?.content?.length ?? 0) === 0 && (
        <div className="py-10 text-center text-[#999]">등록된 문의글이 없습니다.</div>
      )}

      {!loading && !error && (list?.content?.length ?? 0) > 0 && (
        <div className="space-y-3">
          {list!.content.map((it) => (
            <InquiryRow
              key={it.id}
              item={it}
              canEdit={false}             // 행에서 바로 판단하지 않고 상세 모달에서 본인 여부 판별
              onOpenDetail={openDetail}
              onEditClick={openDetail}    // 상세 모달 열고 그 안에서 수정
              onDeleteClick={openDetail}  // 상세 모달 열고 그 안에서 삭제
            />
          ))}
        </div>
      )}

      <button
        className="w-full py-3 text-sm text-[#cfd8e3] border border-[#353535] rounded-lg cursor-pointer hover:bg-[#1d2430]"
        onClick={() => setShowForm(true)}
      >
        판매자에게 문의하기
      </button>

      {showForm && (
        <CreateInquiryModal
          props={props}
          onClose={() => setShowForm(false)}
          onSaved={() => fetchList()}
          onCreate={addInquiry}
        />
      )}

      {detailId !== null && (
        <InquiryDetailModal
          key={detailId}            // ✅ 추가
          inquiryId={detailId}
          onClose={closeDetail}
          onChanged={fetchList}
        />
      )}
    </div>
  );
}
