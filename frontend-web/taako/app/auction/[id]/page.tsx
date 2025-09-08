// 경매 상세 페이지
interface AuctionDetailPageProps {
  params: {
    id: string;
  };
}

export default function AuctionDetailPage({ params }: AuctionDetailPageProps) {
  const { id } = params;

  return (
    <div>
      <h1>경매 상세</h1>
      {/* 경매 상세 정보 컴포넌트 */}
      <p>경매 ID: {id}</p>
    </div>
  );
}
