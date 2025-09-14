import { AuctionDetailProps } from "@/types/auction"
import { useState } from "react"
import RegisterImage from "../atoms/RegisterImage"

interface InquiryProps{
    props: AuctionDetailProps,
    onClose?: () => void
}

export default function CreateInquiryModal({ props, onClose }: InquiryProps){
    const [title, setTitle] = useState("")
    const [content, setContent] = useState("")
    const [isNotificationEnabled, setIsNotificationEnabled] = useState(true);
    const [images, setImages] = useState<File[]>([]) // ✅ 이미지 파일 상태 추가

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault()
        alert("문의가 등록되었습니다.")
        setTitle("")
        setContent("")
        onClose && onClose()
    }

    return(
        <div className="fixed inset-0 z-[200]">
            {/* 배경 */}
            <div className="absolute inset-0 bg-black/50" onClick={() => onClose && onClose()} />

            {/* 내용 */}
            <div className="absolute inset-0 flex items-center justify-center p-4">
                <div className="w-full max-w-[560px] max-h-[600px] bg-[#141420] border border-[#353535] rounded-xl shadow-xl overflow-hidden">
                    <div className="flex items-center justify-between px-5 py-4 border-b border-[#353535]">
                        <p className="text-[18px]">판매자에게 문의</p>
                        <button className="text-[#bbb] hover:text-white" onClick={() => onClose && onClose()}>✕</button>
                    </div>

                    <div className="p-5 overflow-y-auto scrollbar-hide" style={{maxHeight: "calc(600px - 57px)"}}>
                        <p className="block mb-2">기본정보</p>
                        <div className="flex flex-col gap-1 mb-4 border-b border-[#353535] pb-4">
                            <p className="text-sm text-[#a5a5a5]">경매번호: {props.code}</p>
                            <p className="text-sm text-[#a5a5a5]">판매자: {props.seller.nickname}</p>
                        </div>
                        <form onSubmit={handleSubmit} className="flex flex-col gap-8">
                            <div>
                                <label className="block mb-2">제목</label>
                                <input 
                                    type="text" 
                                    className="w-full p-3 border border-[#353535] rounded-md outline-none placeholder:text-sm"
                                    placeholder="제목을 입력하세요"
                                    value={title}
                                    onChange={(e) => setTitle(e.target.value)}
                                    required
                                    />
                            </div>
                            <div>
                                <label className="block mb-2">내용</label>
                                <textarea 
                                    className="w-full p-3 border border-[#353535] rounded-md outline-none placeholder:text-sm"
                                    placeholder="문의 내용을 입력해주세요"
                                    value={content}
                                    onChange={(e) => setContent(e.target.value)}
                                    required
                                    />
                            </div>

                            <div>
                                <label className="block mb-2">사진</label>
                                <RegisterImage onChange={(files) => setImages(files)} />
                            </div>

                            <div>
                                <p className="block mb-2">유의 사항</p>
                                <ul className="list-disc flex flex-col gap-1">
                                    <li className="text-sm text-[#999] ml-4 before:content">재입고, 사이즈, 배송 등 상품에 대하여 판매자에게 문의하는 게시판입니다.</li>
                                    <li className="text-sm text-[#999] ml-4 before:content">욕설, 비방, 거래 글, 분쟁 유발, 명예훼손, 허위 사실 유포, 타 쇼핑몰 언급, 광고성 등의 부적절한 게시글은 금지합니다. 더불어 문의 시 비밀글만 작성되도록 제한됩니다.</li>
                                    <li className="text-sm text-[#999] ml-4 before:content">주문번호, 연락처, 계좌번호 등의 개인 정보 관련 내용은 공개되지 않도록 비밀글로 문의해 주시기 바랍니다. 공개된 글은 비밀글로 전환될 수 있으며, 개인 정보 노출로 인한 피해는 TAKO가 책임지지 않습니다.</li>
                                </ul>
                            </div>

                            <div>
                                <p className="block mb-2">비밀글 여부</p>
                                <div className="flex items-center gap-2">
                                <input
                                    type="checkbox"
                                    id="notification"
                                    checked={isNotificationEnabled}
                                    onChange={(e) => setIsNotificationEnabled(e.target.checked)}
                                    className="w-4 h-4"
                                />
                                <label htmlFor="notification" className="text-sm text-[#a5a5a5] cursor-pointer">
                                    비밀글 작성
                                </label>
                                </div>
                            </div>

                            <div className="grid grid-cols-2 mt-3 gap-2">
                                <button type="button" className="px-4 py-3 border border-[#353535] rounded cursor-pointer" onClick={() => {setTitle(""); setContent(""); onClose && onClose()}}>취소</button>
                                <button type="submit" className="px-4 py-3 bg-[#3e4c63] text-[#7db7cd] rounded cursor-pointer hover:bg-[#7db7cd] hover:text-[#3e4c63] transition-colors">등록</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    )
}