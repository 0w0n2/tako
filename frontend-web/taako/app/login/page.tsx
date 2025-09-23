'use client'

import Link from "next/link";
import Image from "next/image";

import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Button } from "@/components/ui/button"

import { useLogin } from "@/hooks/useLogin";

export default function LoginPage() {
    const { email, setEmail, password, setPassword, handleLogin } = useLogin();

    const onSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        await handleLogin();
    };

    return (
        <div className="small-container !px-30">
            <h1><Link href="/" className="flex justify-center py-10">
                <Image src="/logo.png" alt="logo" width={100} height={60}/>
            </Link></h1>
            
            <form onSubmit={onSubmit} className="flex flex-col items-center gap-10">
                <div className="w-full flex flex-col gap-4">
                    <div className="">
                        <Label className="text-sm text-[#a5a5a5] mb-2" htmlFor="email">이메일 주소</Label>
                        <Input type="email" id="email" placeholder="예) tako@tako.co.kr"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)} />
                    </div>
                    <div className="">
                        <Label className="text-sm text-[#a5a5a5] mb-2" htmlFor="password">비밀번호</Label>
                        <Input type="password" id="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)} />
                    </div>
                </div>

                <div className="flex flex-col gap-4 w-full">
                    <Button type="submit" className="h-12" 
                        disabled={!email || !password}>로그인</Button>
                    <div className="flex justify-center gap-2 text-sm text-[#a5a5a5]">
                        <Link href="/signup" className="hover:text-blue-500">회원가입</Link>
                        <div>|</div>
                        <Link href="/signup" className="hover:text-blue-500">비밀번호 찾기</Link>
                    </div>
                </div>
            </form>

            {/* 구분선 */}
            <div className="relative my-6">
                <div className="absolute inset-0 flex items-center">
                    <div className="w-full border-t border-[#353535]"></div>
                </div>
                <div className="relative flex justify-center text-sm">
                    <span className="px-2 bg-background text-gray-400">또는</span>
                </div>
            </div>

            {/* 구글 로그인 버튼 */}
            <button type="button" className="w-full py-3 bg-white text-gray-800 rounded-lg font-medium hover:bg-gray-100 transition-colors flex items-center justify-center gap-3" >
                {/* 구글 아이콘 */}
                <svg className="w-5 h-5" viewBox="0 0 24 24">
                    <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
                    <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
                    <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
                    <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
                </svg>
                구글로 로그인
            </button>
        </div>
    );
}
