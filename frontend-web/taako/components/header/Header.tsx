'use client';

import { useState, useEffect } from 'react'
import Link from 'next/link'
import SearchInput from "../atoms/Input/SearchInput"
import LoginModal from "../modals/LoginModal"
import { usePathname } from 'next/navigation'
import NavigationMenu from './HeaderNavigationMenu';


export default function Header() {
  const [isLoginModalOpen, setIsLoginModalOpen] = useState(false);
  const [isCategoryModalOpen, setIsCategoryModalOpen] = useState(false);

  const handleLoginClick = () => {
    setIsLoginModalOpen(true);
  };
  
  const handleCategoryClick = () => {
    setIsCategoryModalOpen(prev => !prev);
  };

  const pathname = usePathname()
  
  useEffect(() => {
    setIsCategoryModalOpen(false)
  }, [pathname])

  return (
    <div className="bg-[#141420] border-b border-[#353535] fixed top-0 right-0 w-full z-90">
      <div className="default-container flex justify-between items-center py-5">
        <div className="flex gap-10 items-center">
          <div className="flex items-center"><Link href="/" style={{ fontFamily: 'Pinkfong-B' }}><h1>TAKO</h1></Link></div>
          <SearchInput />
        </div>
        <NavigationMenu />
      </div>
      {isLoginModalOpen && <LoginModal onClose={() => setIsLoginModalOpen(false)} />}
    </div>
  );
}