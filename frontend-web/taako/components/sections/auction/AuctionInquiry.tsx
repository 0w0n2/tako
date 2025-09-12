"use client"

import { useState } from 'react'
import { AuctionDetailProps } from '@/types/auction'
import CreateInquiryModal from '../../modals/CreateInquiryModal'

interface AuctionInquiryProps {
    props: AuctionDetailProps
}

export default function AuctionInquiry({ props }: AuctionInquiryProps){
    const [showForm, setShowForm] = useState(false)

    return (
        <div>
            <div className='flex justify-center text-[#999] items-center py-10'>
                등록된 문의글이 없습니다.
            </div>
            <button 
                className="w-full py-3 text-sm text-[#999] border border-[#353535] rounded-lg cursor-pointer"
                onClick={() => setShowForm(true)}
            >
                판매자에게 문의하기
            </button>

            {showForm && (
                <CreateInquiryModal props={props} onClose={() => setShowForm(false)} />
            )}
        </div>
    )
}