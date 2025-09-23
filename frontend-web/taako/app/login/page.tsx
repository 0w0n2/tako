import { useLogin } from "@/hooks/useLogin";

export default function LoginPage() {
    const {
        email, setEmail, password, setPassword, handleLogin,
    } = useLogin()

    return(
        <div className="small-container h-screen">
            <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
            <div 
                className="relative bg-[#191924] rounded-lg p-6 border border-[#353535]"
                onClick={(e) => e.stopPropagation()}
            >
                <button 
                    className="absolute top-4 right-4 text-white text-xl cursor-pointer hover:text-gray-300"
                >
                    ×
                </button>
                <div className="w-80">
                    <h2 className="text-xl mb-6">로그인</h2>
                    <form className="space-y-4" onSubmit={(e) => e.preventDefault()}>
                        {/* 이메일 입력 */}
                        <div>
                            <input
                                type="email"
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                placeholder="이메일"
                                className="w-full px-4 py-3 bg-[#2a2a3a] border border-[#353535] rounded-lg text-white placeholder-gray-400 focus:outline-none focus:border-[#863BA9]"
                            />
                        </div>
                        
                        {/* 패스워드 입력 */}
                        <div>
                            <input
                                type="password"
                                placeholder="비밀번호"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                className="w-full px-4 py-3 bg-[#2a2a3a] border border-[#353535] rounded-lg text-white placeholder-gray-400 focus:outline-none focus:border-[#863BA9]"
                            />
                        </div>
                        
                        {/* 비밀번호 찾기, 회원가입 링크 */}
                        <div className="flex justify-center gap-2 text-sm">
                            <a href="#" className="text-[#789EBF]">
                                비밀번호 찾기
                            </a>
                            <a href="/signup" className="text-[#789EBF]">
                                회원가입
                            </a>
                        </div>
                        
                        {/* 로그인 버튼 */}
                        <button
                            type="submit"
                            className="w-full py-3 bg-gradient-to-r rounded-lg"
                            onClick={handleLogin}
                        >
                            로그인
                        </button>
                        
                        {/* 구분선 */}
                        <div className="relative my-6">
                            <div className="absolute inset-0 flex items-center">
                                <div className="w-full border-t border-[#353535]"></div>
                            </div>
                            <div className="relative flex justify-center text-sm">
                                <span className="px-2 bg-[#191924] text-gray-400">또는</span>
                            </div>
                        </div>
                        
                        {/* 구글 로그인 버튼 */}
                        <button
                            type="button"
                            className="w-full py-3 bg-white text-gray-800 rounded-lg font-medium hover:bg-gray-100 transition-colors flex items-center justify-center gap-3"
                        >
                            <svg className="w-5 h-5" viewBox="0 0 24 24">
                                <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
                                <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
                                <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
                                <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
                            </svg>
                            구글로 로그인
                        </button>
                    </form>
                </div>
            </div>
        </div>
        </div>
    )
}