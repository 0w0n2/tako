'use client';

import Image from 'next/image';
import { useSignupForm } from "@/hooks/useSignupForm";

export default function Signup(){
  const {
    isSocial, providerName, handleSignup,
    email, setEmail, isEmailAvailable, emailError, emailLoading,
    handleVerificationEmail, formatted, timeLeft, handleCheckEmail,
    password, passwordErrorMessage, setPassword, confirmPassword, setConfirmPassword,
    nickname, setNickname, isNicknameAvailable, nicknameError, nicknameLoading, handleCheckNickname,
  } = useSignupForm();

    return (
        <div className="small-container pb-20">
            <h2>일반 회원가입</h2>
            <div>
                <form
                className="flex flex-col gap-10"
                onSubmit={(e) => e.preventDefault()}>
                    <div>
                        <p className="mb-4">이메일</p>
                        <div className='flex flex-col gap-3'>
                            <div className="flex gap-4">
                                <input
                                className="w-[350px] px-5 py-3 bg-[#191924] rounded-lg border-1 border-[#353535] text-sm"
                                type="email"
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                placeholder="이메일을 입력해주세요"/>
                                <button
                                    type="button"
                                    onClick={handleCheckEmail}
                                    className="min-w-[140px] px-8 py-3 bg-[#3E4C63] rounded-lg text-md cursor-pointer"
                                >중복체크</button>
                            </div>
                            {emailError && (
                              <div className="text-[#FF3737] flex gap-2 items-center">
                                <Image src="/icon/error.svg" width={18} height={18} alt="error" />
                                {emailError}
                              </div>
                            )}
                            {isEmailAvailable === true && (
                              <div className="text-[#40C057] flex gap-2 items-center">
                                <Image src="/icon/correct.svg" width={18} height={18} alt="success" />
                                사용 가능한 이메일입니다.
                              </div>
                            )}
                            {isEmailAvailable === false && (
                              <div className="text-[#FF3737] flex gap-2 items-center">
                                <Image src="/icon/error.svg" width={18} height={18} alt="error" />
                                이미 사용중인 이메일입니다.
                              </div>
                            )}
                            <div className="flex gap-4">
                                <div className='relative'>
                                    <input
                                    className="w-[350px] px-5 py-3 bg-[#191924] rounded-lg border-1 border-[#353535] text-sm"
                                    type="email"
                                    placeholder="인증번호 입력해주세요"/>
                                    <p className='absolute top-1/2 right-5 -translate-y-1/2'>
                                      {timeLeft > 0 ? formatted : ''}
                                    </p>
                                </div>
                                <button
                                type="button"
                                className="min-w-[140px] px-6 bg-[#3E4C63] rounded-lg text-md cursor-pointer"
                                onClick={() => handleVerificationEmail("SIGN_UP_MAIL_VERIFICATION")}
                                >인증번호전송</button>
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
                            {passwordErrorMessage && (
                                <div className="text-[#FF3737] text-md mt-1 flex gap-2 items-center">
                                  <Image src="/icon/error.svg" width={18} height={18} alt="error" />
                                  {passwordErrorMessage}
                                </div>
                              )}
                              {!passwordErrorMessage && password && confirmPassword && (
                                <div className="text-[#40C057] text-md mt-1 flex gap-2 items-center">
                                  <Image src="/icon/correct.svg" width={18} height={18} alt="correct" />
                                  비밀번호가 유효합니다.
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
                                value={nickname}
                                onChange={(e) => setNickname(e.target.value)}
                                placeholder="닉네임을 입력해주세요"/>
                                <button
                                    type="button"
                                    className="min-w-[140px] px-8 bg-[#3E4C63] rounded-lg text-md cursor-pointer"
                                    onClick={handleCheckNickname}
                                >중복체크</button>
                            </div>
                            {/* 에러 메시지 */}
                            {nicknameError && (
                            <div className="text-[#FF3737] text-md mt-1 flex gap-2 items-center">
                                <Image src="/icon/error.svg" width={18} height={18} alt="error" />
                                {nicknameError}
                            </div>
                            )}

                            {!nicknameError && isNicknameAvailable === true && (
                            <div className="text-[#40C057] text-md mt-1 flex gap-2 items-center">
                                <Image src="/icon/correct.svg" width={18} height={18} alt="correct" />
                                사용 가능한 닉네임입니다.
                            </div>
                            )}
                            {!nicknameError && isNicknameAvailable === false && (
                            <div className="text-[#FF3737] text-md mt-1 flex gap-2 items-center">
                                <Image src="/icon/error.svg" width={18} height={18} alt="error" />
                                이미 존재하는 닉네임입니다.
                            </div>
                            )}
                        </div>
                    </div>
                    <button
                        type="submit"
                        className='w-[150px] px-8 py-3 cursor-pointer rounded-lg bg-[#364153] text-[#7DB7CD] border-1 border-[#7DB7CD] hover:bg-[#3E4C63]'
                        onClick={handleSignup}
                    >
                        회원가입
                    </button>
                </form>
            </div>
        </div>
    )
}