'use client';
import { useState } from "react";
import { useAuthStore } from "@/stores/useAuthStore";

export function useLogin() {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const { login, logout, loading, error } = useAuthStore();

    const handleLogin = async() => {
        try{
            await login(email, password);
        }catch(err:any){
            console.log(err.message);
        }
    }

    const handleLogout = async() => {
        try{
            await logout()
            alert("로그아웃에 성공했습니다.")
        }catch(err:any){
            console.log(err.message);
        }
    }

    return {
        email, setEmail, password, setPassword,
        handleLogin, handleLogout,
    }
}