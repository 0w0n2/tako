import { NextResponse } from "next/server"
import type { NextRequest } from "next/server"

export function middleware(req: NextRequest) {
  const token = req.cookies.get("accessToken")?.value

  if (!token && (
    req.nextUrl.pathname.startsWith("/mypage")
    // 다른 페이지 추가
  )){
    return NextResponse.redirect(new URL("/", req.url))
  }

  return NextResponse.next()
}

export const config = {
    matcher: [
        "/mypage/:path*",
        // 다른 페이지 추가
    ],
}