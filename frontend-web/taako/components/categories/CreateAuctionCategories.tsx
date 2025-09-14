'use client'

import { useState, useEffect } from 'react'
import { CreateAuctionCategoriesProps } from '@/types/category'

// 더미 카테고리 데이터
const categories = [
  {
    id: 1,
    name: 'Pokemon',
    minors: [
      { id: 1, name: '피카츄' },
      { id: 2, name: '알로라 피카츄' },
      { id: 3, name: '라이츄' },
      { id: 4, name: '파이리' },
      { id: 5, name: '꼬부기' },
      { id: 6, name: '버터풀' },
    ],
  },
  {
    id: 2,
    name: 'Yu-Gi-OH!',
    minors: [
      { id: 1, name: '청눈의 백룡' },
      { id: 2, name: '흑마도사' },
      { id: 3, name: '카오스 솔저' },
    ],
  },
  {
    id: 3,
    name: 'SSAFY',
    minors: [
      { id: 1, name: '싸피 굿즈1' },
      { id: 2, name: '싸피 굿즈2' },
    ],
  },
  {
    id: 4,
    name: '쿠키런',
    minors: [
      { id: 1, name: '용감한 쿠키' },
      { id: 2, name: '달빛술사 쿠키' },
      { id: 3, name: '천사 쿠키' },
    ],
  },
  {
    id: 5,
    name: '소닉',
    minors: [
      { id: 1, name: '소닉' },
      { id: 2, name: '테일즈' },
      { id: 3, name: '너클즈' },
    ],
  },
]

export default function CreateAuctionCategories({ onChange }: CreateAuctionCategoriesProps) {
  const [majorCategoryId, setMajorCategory] = useState<number | null>(null)
  const [minorCategoryId, setMinorCategory] = useState<number | null>(null)

  // 현재 선택된 majorCategory의 minors 가져오기
  const selectedMajor = categories.find((c) => c.id === majorCategoryId)

  // 부모에 카테고리 정보 넘겨주기기
  useEffect(() => {
    const major = categories.find((c) => c.id === majorCategoryId)
    const minor = major?.minors.find((m) => m.id === minorCategoryId)
  
    onChange?.({
      majorCategoryId,
      majorCategoryName: major?.name || '',
      minorCategoryId,
      minorCategoryName: minor?.name || '',
    })
  }, [majorCategoryId, minorCategoryId, onChange])

  return (
    <div className="flex gap-2 w-[500px] h-[270px]">
      {/* 대분류 */}
      <div className="flex-1 h-full overflow-y-auto scrollbar-hide border border-[#353535] rounded-md">
        <ul className="bg-[#191924]">
          {categories.map((cat) => (
            <li
              key={cat.id}
              onClick={() => {
                setMajorCategory(cat.id)
                setMinorCategory(null) // 대분류 변경 시 소분류 초기화
              }}
              className={`px-4 py-4 cursor-pointer ${
                majorCategoryId === cat.id
                  ? 'bg-[#f2b90c] text-black'
                  : 'hover:bg-[#f2b90c] hover:text-black'
              }`}
            >
              {cat.name}
            </li>
          ))}
        </ul>
      </div>

      {/* 소분류 */}
      <div className="flex-1 h-full overflow-y-auto scrollbar-hide border border-[#353535] -translate-x-[1px] rounded-md">
        <ul className="">
          {selectedMajor ? (
            selectedMajor.minors.map((minor) => (
              <li
                key={minor.id}
                onClick={() => setMinorCategory(minor.id)}
                className={`px-4 py-4 cursor-pointer bg-[#191924] ${
                  minorCategoryId === minor.id
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
  )
}