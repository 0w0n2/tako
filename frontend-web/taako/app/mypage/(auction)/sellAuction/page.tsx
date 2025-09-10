import SellAuction from "@/components/sections/auction/SellAuction";

export default function SellAuctionPage() {
  return (
    <div>
      <h2>판매 경매 조회</h2>
      {/* 판매한 경매 목록 컴포넌트 */}

      <div className="mt-6">
        <SellAuction />
      </div>
    </div>
  );
}
