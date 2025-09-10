'use client';

import { useState } from 'react';
import Link from 'next/link';
import SearchInput from "./atoms/Input/SearchInput";
import LoginModal from "./modals/LoginModal";
import Image from "next/image";

export default function Header() {
  const [isLoginModalOpen, setIsLoginModalOpen] = useState(false);

  const handleLoginClick = () => {
    setIsLoginModalOpen(true);
  };
  
  return (
    <div className="bg-[#141420] border-b border-[#353535] fixed top-0 right-0 w-full z-90">
      <div className="default-container flex justify-between items-center py-5">
        <div className="flex gap-10 items-center">
          <div className="flex items-center"><Link href="/" style={{ fontFamily: 'Pinkfong-B' }}><h1>TAKO</h1></Link></div>
          <SearchInput />
        </div>
        <ul className="category-wrap flex gap-10 items-center">
          <li>
            <div>
              <Link href="#" className="flex items-center gap-2 hover:text-[#F2B90C] hover:font-semibold transition-all duration-300">
                카테고리
                <div><Image src="/icon/arrow-down.svg" width="8" height="4" alt="arrow-down" /></div>
              </Link>
            </div>
            </li>
          <li><Link href="#" className="hover:text-[#F2B90C] hover:font-semibold transition-all duration-300">미니게임</Link></li>
          <li><button onClick={handleLoginClick} className="cursor-pointer hover:text-[#F2B90C] hover:font-semibold transition-all duration-300">로그인</button></li>
          <li><Link href="/mypage" className="hover:text-[#F2B90C] hover:font-semibold transition-all duration-300">마이페이지</Link></li>
          <li><Link href="/notification" className="hover:text-[#F2B90C] hover:font-semibold transition-all duration-300">알림</Link></li>
          <li><Link
            href="/auction/new"
            className="px-8 py-3 bg-[#364153] text-[#7DB7CD] border-1 border-[#7DB7CD] cursor-pointer rounded-lg
            hover:bg-[#3E4C63] transition-all duration-300"
          >경매등록</Link></li>
        </ul>
      </div>
      {isLoginModalOpen && <LoginModal onClose={() => setIsLoginModalOpen(false)} />}
    </div>
  );
}