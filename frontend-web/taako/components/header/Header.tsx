'use client';

import { useState, useEffect } from 'react'
import Link from 'next/link'
import SearchInput from "../atoms/Input/SearchInput"
import LoginModal from "../modals/LoginModal"
import NavigationMenu from './HeaderNavigationMenu';
import Image from 'next/image';
import { useAuthStore } from "@/stores/useAuthStore";

export default function Header() {
  const [isLoginModalOpen, setIsLoginModalOpen] = useState(false);
  const { isLoggedIn } = useAuthStore();

  // 로그인 시 모달 닫기
  useEffect(() => {
    if (isLoggedIn) {
      setIsLoginModalOpen(false);
    }
  }, [isLoggedIn]);

  return (
    <div className="bg-[#141420] border-b border-[#353535] fixed top-0 right-0 w-full z-90">
      <div className="default-container flex justify-between items-center py-3">
        <div className="flex gap-10 items-center">
          <div className="flex items-center"><Link href="/" style={{ fontFamily: 'Pinkfong-B' }}><h1><Image src="/logo.png" alt="logo" width={120} height={50} /></h1></Link></div>
          <SearchInput />
        </div>
        <NavigationMenu onLoginClick={() => setIsLoginModalOpen(true)} />
      </div>
      {isLoginModalOpen && <LoginModal onClose={() => setIsLoginModalOpen(false)} />}
    </div>
  );
}