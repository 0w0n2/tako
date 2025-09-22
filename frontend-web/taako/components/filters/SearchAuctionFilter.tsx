"use client";

import * as React from "react"
import {
  Select,
  SelectContent,
  SelectGroup,
  SelectItem,
  SelectLabel,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"
import { Checkbox } from "@/components/ui/checkbox"
import { Label } from "@/components/ui/label"

import { useEffect } from "react";
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

  const updateQuery = (majorId?: number | null, minorId?: number | null) => {
    const newParams = new URLSearchParams(searchParams.toString());
    if (majorId !== undefined) {
      majorId === null ? newParams.delete("categoryMajorId") : newParams.set("categoryMajorId", majorId.toString());
    }
    if (minorId !== undefined) {
      minorId === null ? newParams.delete("categoryMediumId") : newParams.set("categoryMediumId", minorId.toString());
    }
    newParams.set("page", "0");

    router.push(`${pathname}?${newParams.toString()}`);
  };

  const handleMajorClick = (majorId: number) => {
    setMinorCategoryId(null);
    handleGetMinorCategories(majorId);
    updateQuery(majorId, null);
  };

  const handleMinorClick = (minorId: number) => {
    setMinorCategoryId(minorId);
    updateQuery(selectedMajorId, minorId);
  };

  useEffect(() => {
    if (selectedMajorId) handleGetMinorCategories(selectedMajorId);
  }, [selectedMajorId]);

  return (
    <>
        <div className="flex gap-4">
            <Select
                value={selectedMajorId ? majorCategories.find(mc => mc.id === selectedMajorId)?.name : ""}
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
                    {majorCategories.map((item) => (
                        <SelectItem key={item.id} value={item.name}>
                        {item.name}
                        </SelectItem>
                    ))}
                    </SelectGroup>
                </SelectContent>
            </Select>

            <Select
                value={selectedMinorId ? minorCategories.find(mc => mc.id === selectedMinorId)?.name : ""}
                onValueChange={(value) => {
                    const minor = minorCategories.find(mc => mc.name === value);
                    if (minor) handleMinorClick(minor.id);
                }}
                disabled={!selectedMajorId} // 대분류 선택 전에는 비활성화
                >
                <SelectTrigger className="w-[250px]">
                    <SelectValue placeholder="중분류" />
                </SelectTrigger>
                <SelectContent className="max-h-100 overflow-y-auto">
                    <SelectGroup>
                    {minorCategories.map((item) => (
                        <SelectItem key={item.id} value={item.name}>
                        {item.name}
                        </SelectItem>
                    ))}
                    </SelectGroup>
                </SelectContent>
            </Select>
            <div className="flex items-center space-x-1.5">
                <Checkbox id="terms" className="border-[#353535] rounded-[4px]" />
                <Label htmlFor="terms" className="text-md text-[#a5a5a5]">마감임박순</Label>
            </div>
            <div className="flex items-center space-x-1.5">
                <Checkbox id="price" className="border-[#353535] rounded-[4px]" />
                <Label htmlFor="price" className="text-md text-[#a5a5a5]">입찰많은순</Label>
            </div>
        </div>
    </>
  );
}

