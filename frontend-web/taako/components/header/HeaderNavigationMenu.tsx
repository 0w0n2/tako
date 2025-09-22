"use client"

import * as React from "react"
import Link from "next/link";
import {
    NavigationMenu,
    NavigationMenuItem,
    NavigationMenuLink,
  } from "@/components/ui/navigation-menu"
import { Badge } from "@/components/ui/badge"

import { useLogin } from "@/hooks/useLogin";
import { useAuthStore } from "@/stores/useAuthStore";
import NotUserSideMenu from "../sidemenu/NotUserSideMenu";
import UserSideMenu from "../sidemenu/UserSideMenu";

export default function HeaderNavigationMenu(){
  const {
    handleLogout,
  } = useLogin();

  const token = useAuthStore((state) => state.token);
  const isLoggedIn = !!token; 

  return(
      <NavigationMenu className="gap-7">
          <NavigationMenuItem className="list-none">
              <NavigationMenuLink asChild>
                  <Link href="/search" className="hover:text-[#f2b90c]">전체경매</Link>
              </NavigationMenuLink>
          </NavigationMenuItem>
              <NavigationMenuItem className="list-none">
                  <NavigationMenuLink asChild>
                      <Link href="#" className="hover:text-[#f2b90c]">미니게임</Link>
                  </NavigationMenuLink>
              </NavigationMenuItem>
          {!isLoggedIn ? (
            <NavigationMenuItem className="list-none">
                {/* 로그인 sheet */}
                <NotUserSideMenu />
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
              <NavigationMenuItem className="list-none">
                  <NavigationMenuLink asChild>
                      <Link href="/notification" className="flex items-center gap-1 hover:text-[#f2b90c]">알림
                        <Badge className="h-4 min-w-4 rounded-full px-1 bg-[#f2b90c]">3</Badge>
                      </Link>
                  </NavigationMenuLink>
              </NavigationMenuItem>
              <NavigationMenuItem className="list-none">
                {/* 회원 sheet */}
                <UserSideMenu />
              </NavigationMenuItem>
            </>
          )}
      </NavigationMenu>
  )
}