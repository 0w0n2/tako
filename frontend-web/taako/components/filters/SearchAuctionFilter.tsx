import Link from "next/link"

export default function SearchAuctionFilter(){
    return (
        <div>
            <div className="search-category w-full bg-[#191924] border-1 border-[#353535] rounded-xl overflow-hidden">
                <table className="w-full">
                    <tbody>
                        {/* 카테고리 헤더 행 */}
                        <tr className="border-b border-b-[#353535]">
                            <td className="flex justify-between items-center bg-[#262633] px-8 py-4 text-left">
                                <div className="">카테고리</div>
                                <div className="cursor-pointer text-lg">
                                    -
                                </div>
                            </td>
                            <td className="px-7 py-4 text-left">
                                <div className="flex items-center gap-2 text-sm">
                                    전체
                                </div>
                            </td>
                        </tr>
                        {/* 대분류 선택 행 */}
                        <tr className="category border-b border-[#353535]">
                            <td className="px-8 bg-[#262633]">대분류 선택</td>
                            <td>
                                <ul className="grid grid-cols-7 gap-2 pl-4">
                                    <li>
                                        <Link href="" className="text-sm text-[#a5a5a5] px-3 py-4 hover:text-[#f2b90c] cursor-pointer block">
                                            포켓몬
                                        </Link>
                                    </li>
                                    <li>
                                        <Link href="" className="text-sm text-[#a5a5a5] px-3 py-4 hover:text-[#f2b90c] cursor-pointer block">
                                            유희왕
                                        </Link>
                                    </li>
                                    <li>
                                        <Link href="" className="text-sm text-[#a5a5a5] px-3 py-4 hover:text-[#f2b90c] cursor-pointer block">
                                            쿠키런
                                        </Link>
                                    </li>
                                </ul>
                            </td>
                        </tr>

                        {/* 소분류 선택 행 */}
                        <tr className="category border-b border-[#353535]">
                            <td className="px-8 bg-[#262633]">소분류 선택</td>
                            <td className="">
                                <ul className="grid grid-cols-7 gap-2 pl-4">
                                    <li>
                                        <Link href="" className="text-sm text-[#a5a5a5] px-3 py-4 hover:text-[#f2b90c] cursor-pointer block">
                                            레귤레이션A
                                        </Link>
                                    </li>
                                    <li>
                                        <Link href="" className="text-sm text-[#a5a5a5] px-3 py-4 hover:text-[#f2b90c] cursor-pointer block">
                                            레귤레이션B
                                        </Link>
                                    </li>
                                    <li>
                                        <Link href="" className="text-sm text-[#a5a5a5] px-3 py-4 hover:text-[#f2b90c] cursor-pointer block">
                                            레귤레이션C
                                        </Link>
                                    </li>
                                </ul>
                            </td>
                        </tr>

                        {/* 가격 필터 행 */}
                        <tr className="">
                            <td className="px-8 bg-[#262633]">가격</td>
                            <td className="px-6 py-3 flex justify-start gap-6 items-center">
                                <div className="flex items-center gap-2">
                                    <input type="text" placeholder="최소 가격" className="w-[150px] px-3 py-2 border border-[#353535] rounded-sm text-sm text-[#a5a5a5] focus:outline-none"/>
                                    <span>~</span>
                                    <input type="text" placeholder="최대 가격" className="w-[150px] px-3 py-2 border border-[#353535] rounded-sm text-sm text-[#a5a5a5] focus:outline-none"/>
                                    <button
                                    className="bg-[#3E4C63] text-white text-sm rounded-sm cursor-pointer py-2 px-4"
                                    >적용</button>
                                </div>
                                <div className="flex items-center bg-[#3E4C63]/40 px-4 py-1 rounded-full text-sm gap-2">
                                    <span>1.000 ~ 2.000</span>
                                    <button className="text-gray-500 text-xl cursor-pointer">×</button>
                                </div>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
    )
}