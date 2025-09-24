"use client";

import * as React from "react"
import {
  Select,
  SelectContent,
  SelectGroup,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"
import { Checkbox } from "@/components/ui/checkbox"
import { Label } from "@/components/ui/label"

import { useEffect, useState } from "react";
import { useSearchParams, usePathname, useRouter } from "next/navigation";
import { useMajorCategories } from "@/hooks/useMajorCategories";
import { useMinorCategories } from "@/hooks/useMinorCategories";

export default function SearchAuctionFilter() {
  const { majorCategories } = useMajorCategories();
  const {
    handleGetMinorCategories,
    minorCategories,
    setMinorCategoryId,
    minorLoading,
  } = useMinorCategories();

  const searchParams = useSearchParams();
  const pathname = usePathname();
  const router = useRouter();

  const selectedMajorId = Number(searchParams.get("categoryMajorId")) || null;
  const selectedMinorId = Number(searchParams.get("categoryMediumId")) || null;
  const currentSort = searchParams.get("sort") || "";

  // 🔹 SelectValue 상태 관리
  const [majorValue, setMajorValue] = useState<string>("");
  const [minorValue, setMinorValue] = useState<string>("");

  // 대분류 SelectValue 업데이트
  useEffect(() => {
    if (majorCategories.length && selectedMajorId) {
      const major = majorCategories.find(mc => mc.id === selectedMajorId);
      setMajorValue(major ? major.name : "");
    } else {
      setMajorValue("");
    }
  }, [majorCategories, selectedMajorId]);

  // 중분류 SelectValue 업데이트
  useEffect(() => {
    if (minorCategories.length && selectedMinorId) {
      const minor = minorCategories.find(mc => mc.id === selectedMinorId);
      setMinorValue(minor ? minor.name : "");
    } else {
      setMinorValue("");
    }
  }, [minorCategories, selectedMinorId]);

  // URL 업데이트 함수
  const updateQuery = (
    majorId?: number | null,
    minorId?: number | null,
    sort?: string | null
  ) => {
    const newParams = new URLSearchParams(searchParams.toString());
    if (majorId !== undefined) {
      majorId === null
        ? newParams.delete("categoryMajorId")
        : newParams.set("categoryMajorId", majorId.toString());
    }
    if (minorId !== undefined) {
      minorId === null
        ? newParams.delete("categoryMediumId")
        : newParams.set("categoryMediumId", minorId.toString());
    }
    if (sort !== undefined) {
      sort === null
        ? newParams.delete("sort")
        : newParams.set("sort", sort);
    }
    newParams.set("page", "0");

    router.push(`${pathname}?${newParams.toString()}`);
  };

  // 대분류 선택
  const handleMajorClick = (majorId: number) => {
    setMinorCategoryId(null);
    handleGetMinorCategories(majorId);
    updateQuery(majorId, null);
  };

  // 중분류 선택
  const handleMinorClick = (minorId: number) => {
    setMinorCategoryId(minorId);
    updateQuery(selectedMajorId, minorId);
  };

  // 페이지 로딩 시 대분류가 있으면 중분류 불러오기
  useEffect(() => {
    if (selectedMajorId) handleGetMinorCategories(selectedMajorId);
  }, [selectedMajorId]);

  return (
    <div className="flex gap-4">
      {/* 대분류 */}
      <Select
        value={majorValue}
        onValueChange={(value) => {
          const major = majorCategories.find(mc => mc.name === value);
          if (major) handleMajorClick(major.id);
        }}
      >
        <SelectTrigger className="w-[150px]">
          <SelectValue placeholder="대분류" />
        </SelectTrigger>
        <SelectContent>
          <SelectGroup>
            {majorCategories.map(item => (
              <SelectItem key={item.id} value={item.name}>
                {item.name}
              </SelectItem>
            ))}
          </SelectGroup>
        </SelectContent>
      </Select>

      {/* 중분류 */}
      <Select
        value={minorValue}
        onValueChange={(value) => {
          const minor = minorCategories.find(mc => mc.name === value);
          if (minor) handleMinorClick(minor.id);
        }}
        disabled={!selectedMajorId}
      >
        <SelectTrigger className="w-[250px]">
          <SelectValue placeholder="중분류" />
        </SelectTrigger>
        <SelectContent className="max-h-100 overflow-y-auto">
          <SelectGroup>
            {minorCategories.map(item => (
              <SelectItem key={item.id} value={item.name}>
                {item.name}
              </SelectItem>
            ))}
          </SelectGroup>
        </SelectContent>
      </Select>

      {/* 체크박스 정렬 */}
      <div className="flex items-center space-x-1.5">
        <Checkbox
          id="endtime"
          className="border-[#353535] rounded-[4px]"
          checked={currentSort === "ENDTIME_ASC"}
          onCheckedChange={(checked) => {
            updateQuery(
              selectedMajorId,
              selectedMinorId,
              checked ? "ENDTIME_ASC" : null
            );
          }}
        />
        <Label htmlFor="endtime" className="text-md text-[#a5a5a5]">
          마감임박순
        </Label>
      </div>

      <div className="flex items-center space-x-1.5">
        <Checkbox
          id="bidcount"
          className="border-[#353535] rounded-[4px]"
          checked={currentSort === "BIDCOUNT_DESC"}
          onCheckedChange={(checked) => {
            updateQuery(
              selectedMajorId,
              selectedMinorId,
              checked ? "BIDCOUNT_DESC" : null
            );
          }}
        />
        <Label htmlFor="bidcount" className="text-md text-[#a5a5a5]">
          입찰많은순
        </Label>
      </div>
    </div>
  );
}
