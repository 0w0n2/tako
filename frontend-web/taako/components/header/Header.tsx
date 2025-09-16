'use client';

import { useState, useEffect } from 'react'
import Link from 'next/link'
import Image from 'next/image';
import SearchInput from "../atoms/Input/SearchInput"
import LoginModal from "../modals/LoginModal"
import HeaderNavigationMenu from './HeaderNavigationMenu';

import {
    NavigationMenu,
    NavigationMenuContent,
    NavigationMenuItem,
    NavigationMenuLink,
    NavigationMenuTrigger,
  } from "@/components/ui/navigation-menu"

import { useAuthStore } from "@/stores/useAuthStore";

const components: { image: string; title:string}[] = [
  {
    image:"/logo/PokemonCardGame.webp",
    title:"Pokemon",
  },
  {
    image:"/logo/YuGiOh.webp",
    title:"YuGiOh",
  },
  {
    image:"/logo/cookierunBRAVERSE.webp",
    title:"CookieRun",
  },
]

export default function Header() {
  const [isLoginModalOpen, setIsLoginModalOpen] = useState(false);
  const [currentScrollY, setCurrentScrollY] = useState(0);
  const [isHeaderOpen, setIsHeaderOpen] = useState(true);
  const { isLoggedIn } = useAuthStore();

  // 로그인 시 모달 닫기
  useEffect(() => {
    if (isLoggedIn) {
      setIsLoginModalOpen(false);
    }
  }, [isLoggedIn]);

  // 스크롤 이벤트 핸들러
  useEffect(() => {
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
  }, [isHeaderOpen]);

  // 헤더 토글 함수
  const toggleHeader = () => {
    setIsHeaderOpen(!isHeaderOpen);
  };

  return (
    <>
      {/* 토글 버튼 - 스크롤이 0 이상일 때만 표시 */}
      {currentScrollY > 0 && (
        <button
          onClick={toggleHeader}
          className={`fixed left-1/2 -translate-x-1/2 z-50 bg-[#191924] border border-[#353535] rounded-full p-2 hover:bg-[#2a2a3a] transition-all duration-300 ease-in-out ${
            isHeaderOpen ? 'top-20' : 'top-4'
          }`}
          aria-label="헤더 토글"
        >
          <svg 
            className={`w-5 h-5 text-white transition-transform duration-300 ${isHeaderOpen ? '' : 'rotate-180'}`}
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
          isHeaderOpen 
            ? 'translate-y-0' 
            : '-translate-y-full'
        } ${currentScrollY > 0 ? 'border border-[#353535] bg-[#191924]' : 'translate-y-4'}`}
      >
        <div 
          className={`header rounded-xl default-container flex justify-between items-center transition-all duration-300 ease-in-out ${
            currentScrollY > 0 
              ? 'py-3' 
              : 'py-6'
          }`}
        >
          <NavigationMenu className='flex gap-3 items-center'>
            <NavigationMenuItem className="list-none">
            <NavigationMenuTrigger className="flex gap-1 items-center cursor-pointer hover:text-[#f2b90c]">카테고리</NavigationMenuTrigger>
              <NavigationMenuContent>
                <ul className="grid grid-cols-3 w-[600px] p-3">
                  {components.map((component) => (
                    <li
                      key={component.title}
                    >
                      <NavigationMenuLink asChild>
                        <Link className="rounded-md flex flex-col items-center flex-1 py-6 hover:bg-[#f2b90c]/10" href={`/category/${component.title}`}>
                          <Image src={component.image} alt={component.title} width={100} height={100} />
                          {/* {component.title} */}
                        </Link>
                      </NavigationMenuLink>
                    </li>
                  ))}
                </ul>
              </NavigationMenuContent>
            </NavigationMenuItem>
            <SearchInput />
          </NavigationMenu>
          <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2">
            <Link href="/" style={{ fontFamily: 'Pinkfong-B' }}>
              <h1>
                <Image 
                  src="/logo.png" 
                  alt="logo" 
                  width={currentScrollY > 0 ? 100 : 140} 
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