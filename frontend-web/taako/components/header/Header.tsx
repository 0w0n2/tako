'use client';

import { useState, useEffect } from 'react'
import Link from 'next/link'
import Image from 'next/image';
import SearchInput from "../atoms/Input/SearchInput"
import LoginModal from "../modals/LoginModal"
import HeaderNavigationMenu from './HeaderNavigationMenu';
import { usePathname } from 'next/navigation'

import {
    NavigationMenu,
    NavigationMenuContent,
    NavigationMenuItem,
    NavigationMenuLink,
    NavigationMenuTrigger,
  } from "@/components/ui/navigation-menu"

import { useAuthStore } from "@/stores/useAuthStore";
import { useMajorCategories } from '@/hooks/useMajorCategories';
import { MajorCategories } from '@/types/category';

export default function Header() {
  const { majorCategories } = useMajorCategories();

  const getCategoryId = (categoryName: string): number => {
    const found = majorCategories.find((item: MajorCategories) => item.name === categoryName);
    return found ? found.id : 0;
  };

  const token = useAuthStore((state) => state.token);
  const isLoggedIn = !!token;
  
  const [isLoginModalOpen, setIsLoginModalOpen] = useState(false);
  const [currentScrollY, setCurrentScrollY] = useState(0);
  const [isHeaderOpen, setIsHeaderOpen] = useState(true);
  const pathname = usePathname();
  const isHome = pathname === '/';

  // 로그인 시 모달 닫기
  useEffect(() => {
    if (isLoggedIn) {
      setIsLoginModalOpen(false);
    }
  }, [isLoggedIn]);

  // 스크롤 이벤트 핸들러 (홈에서만 동작)
  useEffect(() => {
    if (!isHome) {
      // 홈이 아닐 때는 스크롤된 스타일로 고정
      setCurrentScrollY(1);
      setIsHeaderOpen(true);
      return;
    }

    const handleScroll = () => {
      const scrollY = window.scrollY;
      setCurrentScrollY(scrollY);
      
      // 스크롤이 0일 때는 헤더 보이게, 0보다 크면 헤더 숨기게
      if (scrollY === 0) {
        setIsHeaderOpen(true);
      } else if (scrollY > 0 && isHeaderOpen) {
        setIsHeaderOpen(false);
      }
    };

    window.addEventListener('scroll', handleScroll, { passive: true });
    
    return () => {
      window.removeEventListener('scroll', handleScroll);
    };
  }, [isHeaderOpen, isHome]);

  // 헤더 토글 함수
  const toggleHeader = () => {
    if (!isHome) return;
    setIsHeaderOpen(!isHeaderOpen);
  };

  const isScrolledStyle = currentScrollY > 0 || !isHome;
  const effectiveIsHeaderOpen = isHome ? isHeaderOpen : true;

  return (
    <>
      {/* 토글 버튼 - 홈에서만, 스크롤 시 표시 */}
      {isHome && currentScrollY > 0 && (
        <button
          onClick={toggleHeader}
          className={`cursor-pointer fixed left-1/2 -translate-x-1/2 z-50 bg-[#191924] border border-[#353535] rounded-full p-2 hover:bg-[#2a2a3a] transition-all duration-300 ease-in-out ${
            effectiveIsHeaderOpen ? 'top-22' : 'top-4'
          }`}
          aria-label="헤더 토글"
        >
          <svg 
            className={`w-5 h-5 text-white transition-transform duration-300 ${effectiveIsHeaderOpen ? '' : 'rotate-180'}`}
            fill="none" 
            stroke="currentColor" 
            viewBox="0 0 24 24"
          >
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 15l7-7 7 7" />
          </svg>
        </button>
      )}

      {/* 헤더 */}
      <div 
        className={`fixed top-0 right-0 w-full z-40 transition-transform duration-300 ease-in-out ${
          effectiveIsHeaderOpen 
            ? 'translate-y-0' 
            : '-translate-y-full'
        } ${isScrolledStyle ? 'border border-[#353535] bg-[#191924]' : 'translate-y-4'}`}
      >
        <div 
          className={`header relative rounded-xl default-container flex justify-between items-center transition-all duration-300 ease-in-out ${
            isScrolledStyle 
              ? 'py-3' 
              : 'py-6'
          }`}
        >
          <NavigationMenu className='flex gap-4 items-center'>
            <NavigationMenuItem className="list-none">
            <NavigationMenuTrigger className="flex gap-1 items-center cursor-pointer hover:text-[#f2b90c]"><Image src="/icon/hbg-btn.svg" alt="btn" width={25} height={25} /></NavigationMenuTrigger>
              <NavigationMenuContent>
                <ul className="grid grid-cols-1 w-[200px] p-3">
                  {majorCategories.map((component) => (
                    <li key={component.id}>
                      <NavigationMenuLink asChild>
                        <Link className="rounded-md flex flex-col items-center flex-1 py-6 hover:bg-[#f2b90c]/10"
                        href={`/category/${getCategoryId(component.name)}?categoryName=${component.name}`}>
                          
                          {/* <Image src={component.image} alt={component.title} width={100} height={100} /> */}
                          {component.name}
                        </Link>
                      </NavigationMenuLink>
                    </li>
                  ))}
                </ul>
              </NavigationMenuContent>
            </NavigationMenuItem>
            <SearchInput onSearch={(keyword) => {
              // 검색 기능 구현 예정
              console.log('Search keyword:', keyword);
            }} />
          </NavigationMenu>
          <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2">
            <Link href="/" style={{ fontFamily: 'Pinkfong-B' }}>
              <h1>
                <Image 
                  src="/logo.png" 
                  alt="logo" 
                  width={isScrolledStyle ? 100 : 140} 
                  height={60}
                  className="transition-all duration-300 ease-in-out"
                />
              </h1>
            </Link>
          </div>
          <HeaderNavigationMenu onLoginClick={() => setIsLoginModalOpen(true)} />
        </div>
      </div>
      
      {isLoginModalOpen && <LoginModal onClose={() => setIsLoginModalOpen(false)} />}
    </>
  );
}