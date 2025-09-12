"use client"
import * as React from "react"
import Image from "next/image";
import Link from "next/link";
import {
    NavigationMenu,
    NavigationMenuContent,
    NavigationMenuItem,
    NavigationMenuLink,
    NavigationMenuTrigger,
  } from "@/components/ui/navigation-menu"
  

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

export default function HeaderNavigationMenu(){
    return(
        <NavigationMenu>
            <NavigationMenuItem>
              <NavigationMenuTrigger>카테고리</NavigationMenuTrigger>
              <NavigationMenuContent>
                <ul className="grid w-[400px] gap-2 md:w-[500px] md:grid-cols-2 lg:w-[600px]">
                  {components.map((component) => (
                    <li
                      key={component.title}
                    >
                      <Link href={`/${component.title}`}>
                        <Image src={component.image} alt={component.title} width={30} height={30} />
                        {component.title}
                      </Link>
                    </li>
                  ))}
                </ul>
              </NavigationMenuContent>
            </NavigationMenuItem>
            <NavigationMenuItem>
                <NavigationMenuLink asChild>
                    <Link href="#">미니게임</Link>
                </NavigationMenuLink>
            </NavigationMenuItem>
            <NavigationMenuItem>
                  <button >로그인</button>
            </NavigationMenuItem>
            <NavigationMenuItem>
                <NavigationMenuLink asChild>
                    <Link href="/mypage">마이페이지</Link>
                </NavigationMenuLink>
            </NavigationMenuItem>
            <NavigationMenuItem>
                <NavigationMenuLink asChild>
                    <Link href="/notification">알림</Link>
                </NavigationMenuLink>
            </NavigationMenuItem>
            <NavigationMenuItem>
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