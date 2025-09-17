"use client"

import * as React from "react"
import Image from "next/image";
import Link from "next/link";
import {
    NavigationMenu,
    NavigationMenuItem,
    NavigationMenuLink,
  } from "@/components/ui/navigation-menu"
import { Badge } from "@/components/ui/badge"

import { useEffect } from "react";
import { useAuthStore } from "@/stores/useAuthStore";
import { useLogin } from "@/hooks/useLogin";

interface HeaderNavigationMenuProps {
  onLoginClick: () => void;
}

export default function HeaderNavigationMenu({ onLoginClick }: HeaderNavigationMenuProps){
  const {
    handleLogout,
  } = useLogin();

  const { isLoggedIn, checkAuth } = useAuthStore();
  useEffect(() => {
    checkAuth();
  }, []);

    return(
        <NavigationMenu className="gap-8">
            <NavigationMenuItem className="list-none">
                <NavigationMenuLink asChild>
                    <Link href="#" className="hover:text-[#f2b90c]">미니게임</Link>
                </NavigationMenuLink>
            </NavigationMenuItem>
            {!isLoggedIn ? (
              <NavigationMenuItem className="list-none">
                    <button className="cursor-pointer hover:text-[#f2b90c]" onClick={onLoginClick}>로그인</button>
              </NavigationMenuItem>
            ) : (
              <>
                <NavigationMenuItem className="list-none">
                      <button className="cursor-pointer hover:text-[#f2b90c]" onClick={handleLogout}>로그아웃</button>
                </NavigationMenuItem>
                <NavigationMenuItem className="list-none">
                    <NavigationMenuLink asChild>
                        <Link href="/mypage" className="hover:text-[#f2b90c]">마이페이지</Link>
                    </NavigationMenuLink>
                </NavigationMenuItem>
              </>
            )}
            <NavigationMenuItem className="list-none">
                <NavigationMenuLink asChild>
                    <Link href="/notification" className="flex items-center gap-1 hover:text-[#f2b90c]">알림
                      <Badge className="h-4 min-w-4 rounded-full px-1 bg-[#f2b90c]">3</Badge>
                    </Link>
                </NavigationMenuLink>
            </NavigationMenuItem>
            <NavigationMenuItem className="list-none">
                <NavigationMenuLink asChild>
                    <Link
                    href="/auction/new"
                    className="px-8 py-3 bg-[#364153] text-[#7DB7CD] border-1 border-[#7DB7CD] cursor-pointer rounded-lg
                    hover:bg-[#3E4C63] transition-all duration-300">경매등록</Link>
                </NavigationMenuLink>
            </NavigationMenuItem>
        </NavigationMenu>
    )
}