// 마이페이지 전용 사이드 메뉴
import Link from "next/link"

export default function MypageSideMenu() {
    return (
        <div className="w-[240px]">
            <ul className="mypage-side-menu flex flex-col gap-8">
                <li><h2><Link href="/mypage">마이페이지</Link></h2></li>
                <li>
                    <ul className="flex flex-col gap-2">
                        <h3>경매이력</h3>
                        <li><Link href="/mypage/buyAuction">입찰 경매 조회</Link></li>
                        <li><Link href="/mypage/sellAuction">판매 경매 조회</Link></li>
                        <li><Link href="/mypage/wishItem">관심 경매</Link></li>
                    </ul>
                </li>
                <li>
                    <ul className="flex flex-col gap-2">
                        <h3>카드</h3>
                        <li><Link href="">관심 카드</Link></li>
                    </ul>
                </li>
                <li>
                    <ul className="flex flex-col gap-2">
                        <h3>내 정보</h3>
                        <li><Link href="">내 정보 수정</Link></li>
                        <li><Link href="">회원탈퇴</Link></li>
                    </ul>
                </li>
            </ul>
            
        </div>
    )
}