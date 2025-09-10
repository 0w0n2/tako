'use client'

import { useState } from 'react';
import Image from 'next/image';
import { AuctionDetailProps } from '@/types/auction';

interface AuctionDetailImages{
    props: AuctionDetailProps
}

export default function AuctionDetailImages({ props }: AuctionDetailImages){
    // --- 이미지 선택 기능 ---
    const [selectedImageIndex, setSelectedImageIndex] = useState(0);

    // --- 돋보기 기능 ---
    const [isZoomed, setIsZoomed] = useState(false);
    const [mousePosition, setMousePosition] = useState({ x: 0, y: 0 });

    return(
        <div>
            <div className="flex-2 w-full max-w-md">
                <div className="sticky top-60 z-8">
                <div className="flex flex-col gap-4">
                    <div 
                    className="rounded-2xl overflow-hidden border-2 border-black relative aspect-square cursor-zoom-in"
                    onMouseEnter={() => setIsZoomed(true)}
                    onMouseLeave={() => setIsZoomed(false)}
                    onMouseMove={(e) => {
                        const rect = e.currentTarget.getBoundingClientRect();
                        const x = ((e.clientX - rect.left) / rect.width) * 100;
                        const y = ((e.clientY - rect.top) / rect.height) * 100;
                        setMousePosition({ x, y });
                    }}
                    >
                    <Image
                    src={props.imageUrl[selectedImageIndex] || '/no-image.jpg'}
                    alt="상품 대표 이미지" 
                    width={100}
                    height={100}
                    className={`object-cover w-full h-full transition-transform duration-200 ${
                        isZoomed ? 'scale-150' : 'scale-100'
                    }`}
                    style={{
                        transformOrigin: `${mousePosition.x}% ${mousePosition.y}%`
                    }}
                    />
                    </div>
                    <ul className="grid grid-cols-4 gap-4">
                        {props.imageUrl.map((url, index) => (
                        <li 
                            key={index} 
                            className={`relative w-25 h-25 aspect-square rounded-xl overflow-hidden border cursor-pointer transition-all ${
                            selectedImageIndex === index 
                                ? 'border-black border-2' 
                                : 'border-[#eeeeee]'
                            }`}
                            onClick={() => setSelectedImageIndex(index)}
                        >
                            <Image 
                            src={url}
                            alt={`상품 썸네일${index + 1}`}
                            width={50}
                            height={50}
                            className="object-cover w-full h-full"
                            />
                        </li>
                        ))}
                    </ul>
                </div>
                </div>
            </div>
        </div>
    )
}