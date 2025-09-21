import MainBanner from "@/components/sections/MainBanner";
import MainHotCardSection from "@/components/sections/MainHotCardSection";
import MainItemEndCloseSection from "@/components/sections/MainItemEndCloseSection";
import MainItemListSection from "@/components/sections/MainItemListSection";

export default function Home() {
  return (
    <div>
      <MainBanner />

      {/* 인기 카테고리 */}
      <MainHotCardSection />

      {/* 마감 임박 경매 */}
      <MainItemEndCloseSection />

      {/* 전체 카테고리 */}

      {/* 포켓몬, 유희왕 경매 */}
      <div className="default-container pt-30">
        <h2>진행중인 포켓몬 경매 {`>`}</h2>
        <MainItemListSection id={3} />
      </div>
      <div className="default-container pt-30">
        <h2>진행중인 유희왕 경매 {`>`}</h2>
        <MainItemListSection id={1} />
      </div>
    </div>
  );
}