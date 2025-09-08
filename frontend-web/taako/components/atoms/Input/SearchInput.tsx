import Image from "next/image";

export default function SearchInput() {
    return (
        <div className="relative">
            <input
                className="w-[350px] rounded-full border-1 border-[#353535] bg-[#191924] px-6 py-4"
                type="text"
                placeholder="검색어를 입력해주세요."
            />
            <Image src="/icon/search.svg" className="absolute right-6 top-1/2 -translate-y-1/2" width="15" height="15" alt="" />
        </div>
    );
}