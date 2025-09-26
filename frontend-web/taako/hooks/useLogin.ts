'use client';
import { useState } from "react";
import { useAuthStore } from "@/stores/useAuthStore";
import { useRouter, useSearchParams } from "next/navigation";

export function useLogin() {
    const [email, setEmail] = useState<string>('');
    const [password, setPassword] = useState<string>('');
    const { login, logout } = useAuthStore();
    const router = useRouter();
    const searchParams = useSearchParams();

    const handleLogin = async () => {
        try {
            const success = await login(email, password);
            if (success) {       // 성공일 때만 이동
                // 리다이렉트 파라미터가 있으면 해당 경로로, 없으면 홈으로
                const redirectPath = searchParams.get('redirect') || '/';
                router.push(redirectPath);
            }
        } catch (err: any) {
            console.log(err.message);
        }
    };

    const handleLogout = async () => {
        try {
            await logout();
            alert("로그아웃에 성공했습니다.");
        } catch (err: any) {
            console.log(err.message);
        }
    };

    return {
        email, setEmail, password, setPassword,
        handleLogin, handleLogout,
    };
}
