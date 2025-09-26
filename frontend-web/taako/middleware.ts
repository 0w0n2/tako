import { NextResponse } from 'next/server'
import type { NextRequest } from 'next/server'

/**
 * Next.js 미들웨어 - 쿠키 기반 인증 처리
 * 
 * 기능:
 * 1. 리프레시 토큰 쿠키 존재 여부로 로그인 상태 판단
 * 2. 보호된 경로 접근 시 미인증 사용자를 로그인 페이지로 리다이렉트
 * 3. 인증된 사용자가 로그인/회원가입 페이지 접근 시 홈으로 리다이렉트
 * 4. 로그인 성공 후 원래 접근하려던 페이지로 자동 리다이렉트
 */

// 인증이 필요한 경로들
const protectedRoutes = [
  '/mypage',
  '/auction/new',
  '/auction/edit',
  '/change-password',
  '/notification',
  '/payment',
  '/wishItem',
]

// 인증된 사용자가 접근하면 안 되는 경로들 (로그인, 회원가입 등)
const authRoutes = [
  '/login',
  '/signup'
]

// 리프레시 토큰 쿠키 이름
const REFRESH_TOKEN_COOKIE = 'refreshToken'

export function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl

  // 리프레시 토큰 쿠키 확인
  const refreshToken = request.cookies.get(REFRESH_TOKEN_COOKIE)?.value
  const isAuthenticated = !!refreshToken

  // 디버깅을 위한 로그 (개발 환경에서만)
  if (process.env.NODE_ENV === 'development') {
    console.log(`[Middleware] Path: ${pathname}, Authenticated: ${isAuthenticated}`)
    if (refreshToken) {
      console.log(`[Middleware] Found refresh token cookie`)
    }
  }

  // 보호된 경로에 접근하는 경우
  const isProtectedRoute = protectedRoutes.some(route =>
    pathname.startsWith(route)
  )

  // 인증 관련 경로에 접근하는 경우
  const isAuthRoute = authRoutes.some(route =>
    pathname.startsWith(route)
  )

  // 보호된 경로에 접근하지만 인증되지 않은 경우
  if (isProtectedRoute && !isAuthenticated) {
    const loginUrl = new URL('/login', request.url)
    // 로그인 후 원래 페이지로 돌아가기 위한 파라미터 추가
    loginUrl.searchParams.set('redirect', pathname)

    if (process.env.NODE_ENV === 'development') {
      console.log(`[Middleware] Redirecting to login: ${loginUrl.toString()}`)
    }

    return NextResponse.redirect(loginUrl)
  }

  // 인증된 사용자가 로그인/회원가입 페이지에 접근하는 경우
  if (isAuthRoute && isAuthenticated) {
    if (process.env.NODE_ENV === 'development') {
      console.log(`[Middleware] Redirecting authenticated user to home`)
    }
    return NextResponse.redirect(new URL('/', request.url))
  }

  return NextResponse.next()
}

// 미들웨어가 실행될 경로 설정
export const config = {
  matcher: [
    /*
     * 다음 경로들을 제외한 모든 경로에서 미들웨어 실행:
     * - api (API routes)
     * - _next/static (static files)
     * - _next/image (image optimization files)
     * - favicon.ico (favicon file)
     * - public 폴더의 파일들
     */
    '/((?!api|_next/static|_next/image|favicon.ico|.*\\.(?:svg|png|jpg|jpeg|gif|webp)$).*)',
  ],
}
