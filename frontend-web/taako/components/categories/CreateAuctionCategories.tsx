"use client";

import * as React from "react";
import { CreateAuctionCategoriesProps } from "@/types/category";
import { useAuctionCategory } from "@/hooks/useAuctionCategory";
import { Button } from "../ui/button";

export default function CreateAuctionCategories({ onChange, onReset }: CreateAuctionCategoriesProps) {
  const {
    majorCategories, majorLoading, minorCategories, minorLoading, cards, loadingCards,
    selectedMajor, selectedMinor, selectedCard,
    handleMajorClick, handleMinorClick, handleCardClick, resetSelection,
  } = useAuctionCategory();

  return (
    <div className="flex flex-col">
      {/* Breadcrumb */}
      <div className="flex justify-between items-start mb-4">
        <div className="text-[#a5a5a5]">
          전체 {"> "}
          {selectedMajor && <span>{majorCategories.find(m => m.id === selectedMajor)?.name} </span>}
          {selectedMinor && <span>{">"} {minorCategories.find(m => m.id === selectedMinor)?.name} </span>}
          {selectedCard && <span>{">"} {cards.find(c => c.id === selectedCard)?.name}</span>}
        </div>
        {selectedCard &&
          <Button
          className="absolute top-0 right-0 px-4 h-10 text-sm text-[#eee] bg-gray-600 cursor-pointer hover:text-[#333]"
          type="button"
          onClick={() => {
            resetSelection();
            onReset?.();
          }}>카테고리 재선택</Button>
        }
      </div>

      {/* 대분류 */}
      {!selectedMajor && (
        <div className="flex flex-col items-start">
          <div className="py-2 px-5 text-sm rounded-full bg-[#353535]">카테고리</div>
          <ul className="w-full flex-col h-15 border-1 border-[#353535] rounded-lg overflow-hidden mt-2">
            {majorLoading ? (
              <li className="p-4 text-gray-500">카테고리를 불러오는 중입니다...</li>
            ) : (
              majorCategories.map((major) => (
                <li
                  key={major.id}
                  onClick={() => handleMajorClick(major, onChange)}
                  className="py-4 px-7 cursor-pointer hover:text-[#f2b90c]"
                >
                  {major.name}
                </li>
              ))
            )}
          </ul>
        </div>
      )}

      {/* 소분류 */}
      {selectedMajor && !selectedMinor && (
        <div className="flex items-start flex-col">
          <div className="py-2 px-5 text-sm rounded-full bg-[#353535]">카드팩</div>
          <ul className="w-full flex-col h-15 border-1 border-[#353535] rounded-lg overflow-hidden mt-2">
            {minorLoading ? (
              <li className="p-4 text-gray-500">카드팩 불러오는 중입니다...</li>
            ) : minorCategories.length > 0 ? (
              minorCategories.map((minor) => (
                <li
                  key={minor.id}
                  onClick={() => handleMinorClick(minor, onChange)}
                  className="py-4 px-7 cursor-pointer hover:text-[#f2b90c]"
                >
                  {minor.name}
                </li>
              ))
            ) : (
              <li className="p-4 text-gray-500">추가된 카드팩이 없습니다</li>
            )}
          </ul>
        </div>
      )}

      {/* 카드 */}
      {selectedMinor && !selectedCard && (
        <div className="flex flex-col items-start">
          <div className="py-2 px-5 text-sm rounded-full bg-[#353535]">카드</div>
          <ul className="w-full flex-col h-15 border-1 border-[#353535] rounded-lg overflow-hidden mt-2">
            {loadingCards ? (
              <li className="p-4 text-gray-500">카드를 불러오는 중입니다...</li>
            ) : cards.length > 0 ? (
              cards.map((card) => (
                <li
                  key={card.id}
                  onClick={() => handleCardClick(card, onChange)}
                  className="py-4 px-7 cursor-pointer hover:text-[#f2b90c]"
                >
                  {card.name}
                </li>
              ))
            ) : (
              <li className="p-4 text-gray-500">카드가 없습니다</li>
            )}
          </ul>
        </div>
      )}
    </div>
  );
}
