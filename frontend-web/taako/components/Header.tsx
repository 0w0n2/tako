import SearchInput from "./atoms/Input/SearchInput";
import Image from "next/image";

export default function Header() {
  return (
    <div className="border-b border-[#353535]">
      <div className="default-container flex justify-between items-center py-6">
        <div className="flex gap-10">
          <div className="flex items-center"><a href="/"><h1>TAAKO</h1></a></div>
          <SearchInput />
        </div>
        <ul className="category-wrap flex gap-10 items-center">
          <li>
            <div>
              <a href="#" className="flex items-center gap-2">
                카테고리
              <div><Image src="/icon/arrow-down.svg" width="8" height="4" alt="arrow-down" /></div>
              </a>
            </div>
            </li>
          <li><a href="#" className="">미니게임</a></li>
          <li>로그인</li>
          <li><a href="/mypage" className="">마이페이지</a></li>
          <li><a href="/notification" className="">알림</a></li>
          <li><a href="/auction/new" className="px-10 py-4 bg-gray-700 text-white cursor-pointer rounded-md bg-gradient-to-r from-[#863BA9] to-[#487BD9]">경매등록</a></li>
        </ul>
      </div>
      
    </div>
  );
}