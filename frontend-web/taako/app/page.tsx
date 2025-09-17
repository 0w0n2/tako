import MainInfoSection from "@/components/sections/MainInfoSection";
import MainCategorySection from "@/components/sections/MainCategorySection";
import MainItemListSection from "@/components/sections/MainItemListSection";

export default function Home() {
  return (
    <div>
      <MainInfoSection />
      {/* <MainCategorySection /> */}
      <MainItemListSection />
    </div>
  );
}