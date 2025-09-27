"use client";

import Image from "next/image";
import Link from "next/link";
import { Button } from "@/components/ui/button";
import { useState } from "react";

export default function ShopPage() {
  const tabs = [
    { id: "mySellAuction", label: "판매경매" },
    { id: "myReview", label: "리뷰" },
  ];

  const [status, setStatus] = useState(tabs[0].id);
  const activeIndex = tabs.findIndex((tab) => tab.id === status);

  return (
    <div className="default-container">
      <div className="flex flex-col gap-10">
        {/* 상단 프로필 영역 */}
        <div
          className="flex gap-10 p-8 rounded-xl relative bg-gradient-to-b from-[#073A4B] to-[#3B80FF]"
          style={{
            backgroundImage: "url(/demo-bg.jpg)",
            backgroundSize: "cover",
            backgroundPosition: "center",
          }}
        >
          <div className="absolute inset-0 rounded-xl bg-black/40 backdrop-brightness-90 pointer-events-none" />
          <div className="flex-2 aspect-square rounded-xl overflow-hidden relative z-1">
            <Image
              src="/basic-profile.png"
              alt="profile"
              fill
              style={{ objectFit: "cover" }}
            />
          </div>
          <div className="flex-7 pt-8 relative z-10">
            <p
              className="mb-1 text-lg font-semibold"
              style={{
                color: "#FFFFFF",
                textShadow: "0 2px 4px rgba(0,0,0,0.55)",
              }}
            >
              홍길동
            </p>
            <p
              className="text-sm whitespace-pre-line"
              style={{
                color: "#E5EAF0",
                textShadow: "0 1px 3px rgba(0,0,0,0.55)",
              }}
            >
              안녕하세요. 믿을 수 있는 판매자 홍길동입니다.
            </p>
          </div>
          {/* 신뢰도 */}
          <div className="absolute right-10 top-32 z-20 flex items-center gap-4">
            <div className="flex items-center gap-4 px-5 py-2.5 rounded-xl bg-white/10 backdrop-blur-md border border-white/25 shadow-[0_4px_12px_-2px_rgba(0,0,0,0.30)] relative overflow-hidden">
              <div className="pointer-events-none absolute inset-0 rounded-xl bg-gradient-to-br from-white/20 via-white/5 to-transparent" />
              <div className="relative flex flex-col items-center leading-tight min-w-[70px]">
                <span className="text-[14px] tracking-wide text-white/70 font-medium mb-0.5 select-none">
                  신뢰온도
                </span>
                <span
                  className="text-xl font-semibold tracking-tight drop-shadow-[0_1px_2px_rgba(0,0,0,0.6)] tabular-nums transition-colors duration-300"
                  style={{ color: "#4ade80" }}
                >
                  85.0ºC
                </span>
              </div>
              {/* eslint-disable-next-line @next/next/no-img-element */}
              <img
                alt="trust-level"
                src="/icon/trust-high.png"
                className="relative h-16 w-auto object-contain drop-shadow-[0_4px_10px_rgba(0,0,0,0.55)] contrast-125"
              />
            </div>
          </div>
          <div className="pl-8 flex gap-10 w-full bg-[#3D3D4D] absolute bottom-0 left-0 rounded-bl-xl rounded-br-xl overflow-hidden">
            <div className="flex-2"></div>
            <div className="flex-7 relative">
              <ul className={`grid grid-cols-2`}>
                {tabs.map((tab) => {
                  const active = status === tab.id;
                  return (
                    <li key={tab.id} className="text-center py-1">
                      <button
                        type="button"
                        onClick={() => setStatus(tab.id)}
                        className={`w-full py-3 rounded focus:outline-none focus-visible:ring-2 ring-[#7DB7CD] transition-colors hover:text-white ${
                          active ? "text-white font-medium" : "text-[#a5a5a5]"
                        }`}
                        aria-current={active ? "page" : undefined}
                      >
                        {tab.label}
                      </button>
                    </li>
                  );
                })}
              </ul>
              {activeIndex !== -1 && (
                <div
                  className="absolute -bottom-1.5 transition-all duration-300"
                  style={{
                    left: `calc(${activeIndex * (100 / tabs.length) + 50 / tabs.length}% - 15px)`,
                  }}
                >
                  <Image
                    src="/icon/current-arrow.svg"
                    alt="current"
                    width={30}
                    height={14}
                  />
                </div>
              )}
            </div>
          </div>
        </div>

        {status === "mySellAuction" && (
          <div>
            <h2>판매경매</h2>
          </div>
        )}
        {status === "myReview" && (
          <div>
            <h2>리뷰</h2>
          </div>
        )}
      </div>
    </div>
  );
}
