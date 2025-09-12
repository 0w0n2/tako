import Image from "next/image"

export default function RegisterImage(){
    const images = Array.from({ length: 2 }, () => "/no-image.jpg")

    return(
        <div className="flex gap-4">
            {/* 이미지 등록 */}
            <div className="w-25 h-25 flex flex-col justify-center items-center border border-[#a5a5a5] rounded-lg shrink-0 cursor-pointer">
                <div className="flex flex-col items-center">
                    <div className="">+</div>
                    <p className="text-xs text-[#a5a5a5]">0/5</p>
                </div>
            </div>

            {/* 이미지 목록 (가로 스크롤) */}
            <div className="flex-1 overflow-x-auto custom-scroll">
                <ul className="flex gap-3">
                    {images.map((src, idx) => (
                        <li key={idx} className="relative w-25 h-25 rounded-lg overflow-hidden border border-[#353535] shrink-0">
                            <Image src={src} alt={`image-item-${idx}`} fill className="object-cover" />
                        </li>
                    ))}
                </ul>
            </div>
        </div>
    )
}