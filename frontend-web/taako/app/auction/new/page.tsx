"use client";

import { useState } from "react";
import Image from "next/image";
import RegisterImage from "@/components/atoms/RegisterImage";

export default function NewAuctionPage() {
  const [hasImmediatePurchase, setHasImmediatePurchase] = useState(false);

  return (
    <div className="default-container pb-[40px]">
      <h2 className="mb-8">경매 등록하기</h2>

      <form className="flex flex-col gap-10">
        {/* 사진 등록 */}
        <div className="flex items-start gap-5">
          <div className="flex-1 flex items-start gap-2 mt-2">
            <h3>사진 등록</h3>
            <span className="text-[#FF0000]">*</span>
          </div>
          <div className="flex-5 flex flex-col gap-2 overflow-hidden">
            <RegisterImage />
            <p className="text-[#a5a5a5] mt-1 text-sm">상품 이미지를 등록해주세요.<br/>
            첫 번째 사진이 대표 이미지로 사용됩니다.</p>
          </div>
        </div>

        {/* 카테고리 */}
        <div className="flex items-start gap-5">
          <div className="flex-1 flex items-start gap-2 mt-2">
            <h3>카테고리</h3>
            <span className="text-[#FF0000]">*</span>
          </div>
          <div className="flex flex-5">
            <div className="w-[250px] h-[250px] overflow-y-auto scrollbar-hide border border-[#353535] rounded-md">
              <ul className="bg-[#191924]">
                {['Pokemon','Yu-Gi-OH!','SSAFY','쿠키런','소닉'].map((label, i) => (
                  <li key={label} className={`px-4 py-4 cursor-pointer ${label==='SSAFY' ? 'bg-[#1c1c2a]' : 'hover:bg-[#1c1c2a]'}`}>{label}</li>
                ))}
              </ul>
            </div>
            <div className="w-[250px] h-[250px] overflow-y-auto scrollbar-hide border border-[#353535] -translate-x-[1px] rounded-md">
              <ul className="bg-[#191924]">
                {['피카츄','알로라 피카츄','라이츄','파이리','꼬부기','버터풀'].map((label) => (
                  <li key={label} className="px-4 py-4 hover:bg-[#1c1c2a] cursor-pointer">{label}</li>
                ))}
              </ul>
            </div>
          </div>
        </div>

        {/* 제목 */}
        <div className="flex items-start gap-5">
          <div className="flex-1 flex items-start gap-2 mt-2">
            <h3>제목</h3>
            <span className="text-[#FF0000]">*</span>
          </div>
          <input
            type="text"
            className="flex-5 px-4 py-3 border border-[#353535] bg-[#191924] rounded-md outline-none placeholder:text-sm"
            placeholder="제목"
          />
        </div>

        {/* 카드 감정하기(AI) */}
        <div className="flex items-start gap-5">
          <div className="flex-1 flex flex-col gap-1">
            <div className="flex items-center gap-2 mt-2">
              <h3>카드 감정하기(AI)</h3>
              <span className="text-[#FF0000]">*</span>
            </div>
            <div className="flex gap-2 items-center">
              <div className="w-[15px] h-[15px] rounded-full border border-[#c3c3c3] flex items-center justify-center text-[10px]">i</div>
              <span className="text-sm text-[#a3a3a3]">촬영가이드</span>
            </div>
          </div>

          <div className="flex-5 flex flex-col gap-4">
            <div className="flex justify-between gap-2 mt-3">
              <p className="text-sm text-[#a5a5a5]">카드 이미지를 등록해 주세요<br/>
              (촬영 가이드 참고)</p>
              <button type="button" className="px-6 py-3 rounded-md text-[#7DB7CD] bg-[#3E4C63] border border-[#7DB7CD]">AI 감정하기</button>
            </div>

            {/* 카드 */}
            <div className="grid grid-cols-4 gap-4">
              {["(앞면)","(뒷면)","(긴모서리)","(짧은모서리)"].map((label) => (
                <div key={label} className="flex flex-col items-center gap-2">
                  <div className="w-full aspect-[4/5] border border-[#353535] bg-[#191924] rounded" />
                  <span className="text-sm text-[#a5a5a5]">{label}</span>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* 카드등급 */}
        <div className="flex items-center gap-5">
          <div className="flex-1 flex flex-col gap-1">
            <h3 className="mt-2">카드등급</h3>
            <div className="flex items-center gap-2">
              <div className="w-[15px] h-[15px] rounded-full border border-[#c3c3c3] flex items-center justify-center text-[10px]">i</div>
              <span className="text-sm text-[#a5a5a5]">등급가이드</span>
            </div>
          </div>
          <div className="flex-5">
            <span className="text-sm text-[#a5a5a5]">AI 카드 감정을 통해 등급을 알 수 있어요!</span>
          </div>
        </div>

        {/* 상세설명 */}
        <div className="flex gap-5">
          <div className="flex-1 flex items-start gap-2 mt-2">
            <h3>상세설명</h3>
            <span className="text-[#ff0000]">*</span>
          </div>
          <textarea className="flex-5 px-4 py-3 border border-[#353535] bg-[#191924] h-40 rounded-md outline-none placeholder:text-sm" placeholder="상세설명을 입력해주세요." />
        </div>

        {/* 경매기간 */}
        <div className="flex items-start gap-5">
          <div className="flex-1 flex items-center gap-2 mt-2">
            <h3>경매기간</h3>
            <span className="text-[#ff0000]">*</span>
          </div>
          <div className="flex-5 flex flex-col md:flex-row md:items-center gap-4">
            <div className="relative w-full max-w-[180px]">
              <input type="date" className="pl-10 pr-4 py-3 border border-[#353535] bg-[#191924] rounded-md appearance-none outline-none" />
              <span className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2">
                <Image src="/icon/calender.svg" alt="calendar" width={18} height={18} />
              </span>
            </div>
            <span className="mx-1 text-[#a4a4a4]">~</span>
            <div className="relative max-w-[180px]">
              <input type="date" className="pl-10 pr-3 py-3 border border-[#353535] bg-[#191924] rounded-md appearance-none outline-none" />
              <span className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2">
                <Image src="/icon/calender.svg" alt="calendar" width={18} height={18} />
              </span>
            </div>
            <div className="max-w-[200px]">
              <select className="px-4 py-4 border border-[#353535] bg-[#191924] rounded-md outline-none text-[#7c7c7c]">
                <option className="bg-[#141420]">종료시간설정</option>
                {Array.from({ length: 24 }).map((_, i) => (
                  <option key={i} className="bg-[#141420]">{String(i).padStart(2,'0')}:00</option>
                ))}
              </select>
            </div>
          </div>
        </div>

        {/* 입찰가 */}
        <div className="flex items-start gap-5">
          <div className="flex-1 flex items-center gap-2 mt-2">
            <h3>입찰가</h3>
            <span className="text-[#ff0000]">*</span>
          </div>
          <div className="flex-5 flex flex-col gap-2">
            <p className="text-sm">입찰 단위</p>
            <div className="flex gap-3">
              <div>
                <select className="px-4 py-3 border border-[#a5a5a5] rounded-md outline-none text-[#a5a5a5]">
                  <option className="bg-[#141420]">100</option>
                  <option className="bg-[#141420]">500</option>
                  <option className="bg-[#141420]">1000</option>
                </select>
              </div>
              <div className="relative">
                <input type="number" className="w-full pl-4 pr-12 py-3 border border-[#a5a5a5] rounded-md bg-transparent outline-none" placeholder="0" min={0} />
                <span className="absolute right-4 top-1/2 -translate-y-1/2 text-sm">TKC</span>
              </div>
            </div>
          </div>
        </div>

      {/* 속성 */}
      <div className="flex items-start gap-5">
        <h3 className="flex-1 mb-4">속성</h3>
        <div className="flex-5 flex items-center gap-3">
          <input id="immediate" type="checkbox" className="w-5 h-5 border border-[#353535]" checked={hasImmediatePurchase} onChange={(e) => setHasImmediatePurchase(e.target.checked)} />
          <label htmlFor="immediate" className="text-sm text-[#7c7c7c]">즉시구매</label>
        </div>
      </div>

        {/* 즉시구매가 */}
        <div className="flex gap-5">
          <h3 className="flex-1 mt-2">즉시구매가</h3>
          <div className="flex-5 relative">
            <input type="number" className="w-full pl-4 pr-20 py-4 border border-[#353535] rounded-md bg-transparent outline-none disabled:opacity-50" placeholder="0" min={0} disabled={!hasImmediatePurchase} />
            <span className="absolute right-4 top-1/2 -translate-y-1/2 text-sm">BTC</span>
          </div>
        </div>

        {/* 제출 */}
        <div className="flex justify-end gap-3">
          <button type="button" className="px-6 py-4 border border-[#353535] rounded-md">취소</button>
          <button type="submit" className="px-6 py-4 rounded-md bg-gradient-to-r from-[#863ba9] to-[#487bd9]">경매 등록</button>
        </div>
      </form>
    </div>
  );
}
