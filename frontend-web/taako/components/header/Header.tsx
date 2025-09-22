'use client';

// import { useState, useEffect } from 'react'
import Link from 'next/link'
import Image from 'next/image';
import SearchInput from "../atoms/Input/SearchInput"
// import LoginModal from "../modals/LoginModal"
import HeaderNavigationMenu from './HeaderNavigationMenu';
import { useAuthStore } from "@/stores/useAuthStore";

export default function Header() {
  const token = useAuthStore((state) => state.token);
  // const isLoggedIn = !!token;
  
  // const [isLoginModalOpen, setIsLoginModalOpen] = useState(false);

  // // 로그인 시 모달 닫기
  // useEffect(() => {
  //   if (isLoggedIn) {
  //     setIsLoginModalOpen(false);
  //   }
  // }, [isLoggedIn]);

  return (
    <>
      {/* 헤더 */}
      <div className={`fixed top-0 right-0 w-full z-40 ease-in-out border border-[#353535] bg-background`} >
        <div className={`py-4 header relative rounded-xl default-container flex justify-between items-center`} >
          <div className="flex gap-5 items-center">
            <Link href="/" style={{ fontFamily: 'Pinkfong-B' }}>
              <h1> <Image src="/logo.png" alt="tako-logo" width={100} height={60} /> </h1>
            </Link>
            <SearchInput />
          </div>
          <HeaderNavigationMenu />
        </div>
      </div>
      {/* {isLoginModalOpen && <LoginModal onClose={() => setIsLoginModalOpen(false)} />} */}
    </>
  );
}