import { NextResponse } from "next/server";
import type { NextRequest } from "next/server";

export function middleware(req: NextRequest) {
  const token = req.cookies.get("Authorization")?.value;

  const protectedPaths = [
    "/mypage",
    // 추가 라우트가드
  ];

  if (!token && protectedPaths.some((path) => req.nextUrl.pathname.startsWith(path))) {
    return NextResponse.redirect(new URL("/", req.url));
  }

  return NextResponse.next();
}

export const config = {
  matcher: [
    "/mypage/:path*",
    // 추가 라우트가드
  ],
};