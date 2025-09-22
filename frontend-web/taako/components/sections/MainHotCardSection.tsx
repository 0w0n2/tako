'use client'

import { useState } from "react";
import Marquee from "react-fast-marquee";
import { useMajorCategories } from "@/hooks/useMajorCategories"
import { getHotCard } from "@/lib/card";

export default function MainHotCardSection(){
    const {
        majorCategories,
    } = useMajorCategories();

    // 카테고리 인기카드조회 handler
    const [selectedCategoryId, setSelectedCategoryId] = useState<number | null>(3);
    const handleHotCards = async(categoryId:number) => {
        try{
            setSelectedCategoryId(categoryId);
            const res = await getHotCard(categoryId);
            console.log(res);
        }catch(err:any){
            console.log(err.message);
        }
    }

    return(
        <div className="py-30">
            <div className="default-container flex flex-col items-center gap-8">
                <h2>인기카드</h2>
                <ul className="flex gap-5">
                    {majorCategories && majorCategories.map((item, index) => (
                        <li
                        key={index}
                        className={`px-4 py-1 rounded-full cursor-pointer transition-colors duration-200 ${
                            selectedCategoryId === item.id 
                                ? "bg-[#FBE134] text-[#0D0D0D]" 
                                : "bg-black/20 hover:bg-[#FBE134] hover:text-[#0D0D0D]"
                        }`}
                        onClick={() => {handleHotCards(item.id)}}
                        >{item.name}</li>
                    ))}
                </ul>
            </div>
            <Marquee>
                <div>1</div>
                <div>2</div>
                <div>3</div>
                <div>4</div>
                <div>5</div>
            </Marquee>
        </div>
    )
}