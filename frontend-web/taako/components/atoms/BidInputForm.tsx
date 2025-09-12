import { AuctionDetailProps } from '@/types/auction';
import { useState } from 'react';

interface BidInput{
    props: AuctionDetailProps
}

export default function BidInputForm({ props }: BidInput){
    const [bidAmount, setBidAmount] = useState(props.currentPrice);
    const [inputValue, setInputValue] = useState(props.currentPrice.toString());

    const handleIncreaseBid = () => {
        const currentAmount = typeof bidAmount === 'number' ? bidAmount : parseFloat(bidAmount) || props.currentPrice;
        const newAmount = Math.round((currentAmount + props.bidUnit) * 1000) / 1000;
        setBidAmount(newAmount);
        setInputValue(newAmount.toString());
    };

    const handleDecreaseBid = () => {
        const currentAmount = typeof bidAmount === 'number' ? bidAmount : parseFloat(bidAmount) || props.currentPrice;
        if (currentAmount > props.bidUnit) {
            const newAmount = Math.round((currentAmount - props.bidUnit) * 1000) / 1000;
            setBidAmount(newAmount);
            setInputValue(newAmount.toString());
        }
    };

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const inputValue = e.target.value;
        
        // 숫자, 소수점, 빈 문자열만 허용
        if (inputValue === '' || /^[\d.]*$/.test(inputValue)) {
            // 소수점이 여러 개 있는 경우 방지
            if ((inputValue.match(/\./g) || []).length <= 1) {
                setInputValue(inputValue);
                // 유효한 숫자로 변환 가능한 경우에만 bidAmount 업데이트
                const numericValue = parseFloat(inputValue);
                if (!isNaN(numericValue)) {
                    setBidAmount(numericValue);
                }
            }
        }
    };

    const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
        if (e.key === 'ArrowUp') {
            e.preventDefault();
            handleIncreaseBid();
        } else if (e.key === 'ArrowDown') {
            e.preventDefault();
            handleDecreaseBid();
        }
    };

    return(
        <div className="w-full">
            <form className="w-full flex gap-3">
                <div className="w-full flex gap-3">
                    <div className='flex-1 flex items-center relative'>
                        <input
                            type="text"
                            value={inputValue}
                            onChange={handleInputChange}
                            onKeyDown={handleKeyDown}
                            placeholder="입찰금액을 입력해주세요"
                            className="text-right flex-1 pr-[90px] py-4 rounded-lg border border-[#353535] outline-none"
                        />

                        {/* 단위 및 화살표 버튼 */}
                        <div className='flex items-center gap-3 absolute top-1/2 -translate-y-1/2 right-0'>
                            TKC
                            <div className="flex flex-col gap-2 mr-4">
                                <button 
                                    type="button" 
                                    onClick={handleIncreaseBid}
                                    className="cursor-pointer"
                                ><svg width="12" height="10" viewBox="0 0 12 10" fill="none"><path d="M6 1L1 9H11L6 1Z" fill="#cacaca"/></svg>
                                </button>
                                <button 
                                    type="button" 
                                    onClick={handleDecreaseBid}
                                    className="cursor-pointer"
                                ><svg width="12" height="10" viewBox="0 0 12 10" fill="none"><path d="M6 9L1 1H11L6 9Z" fill="#cacaca"/></svg>
                                </button>
                            </div>
                        </div>
                    </div>
                    <button 
                        type="submit" 
                        disabled={bidAmount <= props.currentPrice || isNaN(bidAmount)}
                        className={`px-12 border rounded-lg transition-all duration-200 ${
                            bidAmount > props.currentPrice && !isNaN(bidAmount)
                                ? 'border-[#7db7cd] bg-[#3e4c63] text-[#7db7cd] cursor-pointer hover:bg-[#7db7cd] hover:text-[#3e4c63]'
                                : 'border-[#555555] bg-[#2a2a2a] text-[#666666] cursor-not-allowed'
                        }`}
                    >
                        입찰하기
                    </button>
                </div>
            </form>
            <p className="mt-3 text-[#a5a5a5] text-md text-right">
                입찰 단위: {props.bidUnit} TKC
            </p>
        </div>
    )
}