"use client";

import Image from "next/image";
import { useMyInfo } from "@/hooks/useMyInfo";

export default function EditPage() {
	// myInfo 호출로 인증 상태(토큰 등) 로딩 트리거
	useMyInfo();

	return (
		<div className="space-y-6">
			<h2 className="text-xl font-semibold">내 정보 수정</h2>
			<div>
				<form className="flex flex-col gap-10 my-10">
					<div>
						<p className="mb-4">프로필 이미지</p>
						<div className="w-[120px] h-[120px] rounded-full overflow-hidden relative group">
							{/* 현재 프로필 이미지 */}
							<Image src="/no-image.jpg" width={120} height={120} className="w-full h-full object-cover" alt="profile-image" />
							{/* 프로필 이미지 수정 버튼 */}
							<button type="button" className="absolute cursor-pointer inset-0 bg-black/20 flex items-center justify-center">
								<div className="bg-black/20 rounded-full p-2">
									<svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
										<path d="M3 21H21M12 3L12 17M5 10L12 3L19 10" stroke="white" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
									</svg>
								</div>
							</button>
						</div>
					</div>

					<div>
						<p className="mb-4">이메일</p>
						<div className="flex flex-col gap-3">
							<div className="w-[350px] px-5 py-3 text-[#a5a5a5] bg-[#383838] rounded-lg border-1 border-[#353535] text-sm">doriconi@gmail.com</div>
						</div>
					</div>

					<div>
						<p className="mb-4">닉네임</p>
						<div className="flex flex-col gap-3">
							<div className="flex gap-4">
								<input className="w-[350px] px-5 py-3 bg-[#191924] rounded-lg border-1 border-[#353535] text-sm" type="text" placeholder="닉네임을 입력해주세요" />
								<button className="min-w-[140px] px-8 bg-[#3E4C63] rounded-lg text-md">중복체크</button>
							</div>
							<div className="text-[#40C057] text-md mt-1 flex gap-2 items-center">
								<Image src="/icon/correct.svg" width={18} height={18} alt="valid" />
								사용 가능한 닉네임입니다.
							</div>
						</div>
					</div>

					<div>
						<p className="mb-4">소개글</p>
						<div className="flex flex-col gap-3">
							<textarea className="w-[350px] px-5 py-3 bg-[#191924] rounded-lg border-1 border-[#353535] text-sm" placeholder="소개글을 작성해 주세요"></textarea>
						</div>
					</div>

					<div>
						<p className="mb-4">배송지등록</p>
						<div>
							<ul className="flex flex-col gap-3">
								<li className="flex items-center justify-between px-8 py-5 bg-[#191924] rounded-lg border-1 border-[#353535]">
									<div className="flex gap-4 items-center ">
										<div>
											<Image src="/icon/place.png" alt="" width={16} height={20} />
										</div>
										<div className="flex flex-col gap-1">
											<p>부산광역시 중앙대로 1134번길 34</p>
											<p className="text-[#a5a5a5] text-sm">상세주소</p>
										</div>
									</div>
									{/* 기본배송지 표시 */}
									<div>ㅇ</div>
								</li>
							</ul>
							<button className="mt-3 w-full flex gap-1 justify-center items-center py-2 bg-[#191924] rounded-lg border-1 border-[#353535] text-[#a5a5a5] cursor-pointer">
								<p>+</p>새 주소 추가
							</button>
						</div>
					</div>

					{/* 완료 버튼 */}
					<button
						type="submit"
						className="w-[150px] px-8 py-3 bg-[#364153] text-[#7DB7CD] border-1 border-[#7DB7CD] cursor-pointer rounded-lg
            hover:bg-[#3E4C63] transition-all duration-300"
					>
						정보수정
					</button>
				</form>
			</div>
		</div>
	);
}
