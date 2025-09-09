'use client';

import Image from 'next/image';
import { useState } from 'react';

export default function Signup(){
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');

    const isPasswordMismatch = password !== confirmPassword && password !== '' && confirmPassword !== '';

    return (
        <div className="default-container">
            <h1 className="font-bold">일반 회원가입</h1>
            <div>
                <form className="flex flex-col gap-10 my-10">
                    <div>
                        <p className="mb-4">이메일</p>
                        <div className='flex flex-col gap-3'>
                            <div className="flex gap-4">
                                <input
                                className="w-[350px] px-5 py-3 bg-[#191924] rounded-lg border-1 border-[#353535] text-sm"
                                type="email"
                                placeholder="이메일을 입력해주세요"/>
                                <button className="min-w-[140px] px-8 py-3 bg-[#3A468C] rounded-lg text-md">중복체크</button>
                            </div>
                            <div className="flex gap-4">
                                <div className='relative'>
                                    <input
                                    className="w-[350px] px-5 py-3 bg-[#191924] rounded-lg border-1 border-[#353535] text-sm"
                                    type="email"
                                    placeholder="인증번호 입력해주세요"/>
                                    <p className='absolute top-1/2 right-5 -translate-y-1/2'>10:00</p>
                                </div>
                                <button className="min-w-[140px] px-6 bg-[#3A468C] rounded-lg text-md">인증번호전송</button>
                            </div>
                        </div>
                    </div>
                    <div>
                        <p className="mb-4">비밀번호</p>
                        <div className="flex flex-col gap-3">
                            <input
                            className="w-[350px] px-5 py-3 bg-[#191924] rounded-lg border-1 border-[#353535] text-sm"
                            type="password"
                            placeholder="비밀번호"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}/>
                            <input
                            className="w-[350px] px-5 py-3 bg-[#191924] rounded-lg border-1 border-[#353535] text-sm"
                            type="password"
                            placeholder="비밀번호확인"
                            value={confirmPassword}
                            onChange={(e) => setConfirmPassword(e.target.value)}/>
                            {isPasswordMismatch && (
                                <div className="text-[#FF3737] text-md mt-1 flex gap-2 items-center">
                                    <Image src="/icon/error.svg" width={18} height={18} alt="error" />
                                    비밀번호가 일치하지 않습니다.
                                </div>
                            )}
                            {!isPasswordMismatch && password !== '' && confirmPassword !== '' && (
                                <div className="text-[#40C057] text-md mt-1 flex gap-2 items-center">
                                    <Image src="/icon/correct.svg" width={18} height={18} alt="error" />
                                    비밀번호가 일치합니다.
                                </div>
                            )}
                        </div>
                    </div>
                    <div>
                        <p className="mb-4">닉네임</p>
                        <div className="flex flex-col gap-3">
                            <div className='flex gap-4'>
                                <input
                                className="w-[350px] px-5 py-3 bg-[#191924] rounded-lg border-1 border-[#353535] text-sm"
                                type="text"
                                placeholder="닉네임을 입력해주세요"/>
                                <button className="min-w-[140px] px-8 bg-[#3A468C] rounded-lg text-md">중복체크</button>
                            </div>
                            { false && (
                                <div className="text-[#FF3737] text-md mt-1 flex gap-2 items-center">
                                    <Image src="/icon/error.svg" width={18} height={18} alt="error" />
                                    이미 존재하는 닉네임입니다.
                                </div>
                            ) || (
                                <div className="text-[#40C057] text-md mt-1 flex gap-2 items-center">
                                    <Image src="/icon/correct.svg" width={18} height={18} alt="error" />
                                    사용 가능한 닉네임입니다.
                                </div>
                            )}
                        </div>
                    </div>
                    <button
                        type="submit"
                        className='w-[150px] px-8 py-3 cursor-pointer rounded-lg'
                        style={{
                            background: 'linear-gradient(137deg, #4557BF 20%, #3A468C 100%)'
                        }}
                    >
                        회원가입
                    </button>
                </form>
            </div>
        </div>
    )
}