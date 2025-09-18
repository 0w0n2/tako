import * as React from "react";
import { CreateAuctionCategoriesProps } from '@/types/category';
import { useMajorCategories } from '@/hooks/useMajorCategories';
import { useMinorCategories } from '@/hooks/useMinorCategories';

export default function CreateAuctionCategories({ onChange }: CreateAuctionCategoriesProps) {
  const {
    setMajorCategoryId,
    majorCategories,
    majorLoading
  } = useMajorCategories();

  const {
    handleGetMinorCategories,
    setMinorCategoryId,
    minorCategories
  } = useMinorCategories();

  const [selectedMajor, setSelectedMajor] = React.useState<number | null>(null);
  const [selectedMinor, setSelectedMinor] = React.useState<number | null>(null);

  const handleMajorClick = (cat: { id: number; name: string }) => {
    setSelectedMajor(cat.id);
    setMajorCategoryId(cat.id);
    handleGetMinorCategories(cat.id);
    setSelectedMinor(null); // 이전 소분류 초기화
    onChange(cat.name, ""); // 소분류 선택 전에는 빈 문자열
  };

  const handleMinorClick = (minor: { id: number; name: string }) => {
    setSelectedMinor(minor.id);
    setMinorCategoryId(minor.id);
    const majorName = majorCategories.find((c) => c.id === selectedMajor)?.name || "";
    onChange(majorName, minor.name);
  };

  return (
    <div className="flex gap-2 w-[800px] h-[224px]">
      {/* 대분류 */}
      <div className="flex-2 h-full overflow-y-auto scrollbar-hide border border-[#353535] rounded-md">
        <ul className="bg-[#191924]">
          {majorLoading ? (
            <li className="px-4 py-4 text-gray-500">카테고리를 불러오는 중입니다...</li>
          ) : (
            majorCategories.map((cat) => (
              <li
                key={cat.id}
                onClick={() => handleMajorClick(cat)}
                className={`px-4 py-4 cursor-pointer ${
                  selectedMajor === cat.id
                    ? 'bg-[#f2b90c] text-black'
                    : 'hover:bg-[#f2b90c] hover:text-black'
                }`}
              >
                {cat.name}
              </li>
            ))
          )}
        </ul>
      </div>

      {/* 소분류 */}
      <div className="flex-4 h-full overflow-y-auto scrollbar-hide border border-[#353535] -translate-x-[1px] rounded-md">
        <ul>
          {minorCategories.length > 0 ? (
            minorCategories.map((minor) => (
              <li
                key={minor.id}
                onClick={() => handleMinorClick(minor)}
                className={`px-4 py-4 cursor-pointer bg-[#191924] ${
                  selectedMinor === minor.id
                    ? 'bg-[#f2b90c] text-black'
                    : 'hover:bg-[#f2b90c] hover:text-black'
                }`}
              >
                {minor.name}
              </li>
            ))
          ) : (
            <li className="px-4 py-4 text-gray-500">대분류를 선택하세요</li>
          )}
        </ul>
      </div>
    </div>
  );
}
