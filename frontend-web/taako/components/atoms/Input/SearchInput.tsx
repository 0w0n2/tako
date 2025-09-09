'use client';

import Image from 'next/image';
import { useState } from 'react';
import { useRouter } from 'next/navigation';

export default function SearchInput() {
    const [searchTerm, setSearchTerm] = useState('');
    const router = useRouter();

    const handleSearch = () => {
        if (searchTerm.trim()) {
            router.push(`/search?cardName=${encodeURIComponent(searchTerm.trim())}`);
        }
    };

    const handleKeyPress = (e: React.KeyboardEvent) => {
        if (e.key === 'Enter') {
            handleSearch();
        }
    };

    return (
        <div className="relative">
            <input
                className="w-[350px] rounded-full border-1 border-[#353535] bg-[#191924] px-6 py-3 focus:outline-none"
                type="text"
                placeholder="검색어를 입력해주세요."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                onKeyPress={handleKeyPress}
            />
            <button 
                onClick={handleSearch}
                className="absolute right-6 top-4 cursor-pointer"
            >
                <Image src="/icon/search.svg" width={16} height={16} alt="검색" />
            </button>
        </div>
    );
}