import AuctionCard from "@/components/cards/AuctionCard";

export default function WishAuctionPage() {
  return (
    <div>
      <h2>관심 경매</h2>
      
      <div className="mt-6">
        <p className="text-sm mb-3">총 2건</p>
        <ul className="grid grid-cols-4 gap-12">
          <li>
            <AuctionCard />
          </li>
          <li>
            <AuctionCard />
          </li>
        </ul>
      </div>
    </div>
  );
}
