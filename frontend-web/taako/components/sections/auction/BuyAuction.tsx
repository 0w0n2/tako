export default function BuyAuction(){
    return(
        <div>
         <table className="w-full">
           {/* 헤더 */}
           <thead>
             <tr className="flex items-center py-3 px-5">
               <th className="w-[130px] text-left font-light text-[#d5d5d5]">경매번호</th>
               <th className="w-[240px] text-left font-light text-[#d5d5d5]">상품명</th>
               <th className="w-[110px] text-left font-light text-[#d5d5d5]">현재입찰가</th>
               <th className="w-[120px] text-left font-light text-[#d5d5d5]">내 입찰 금액</th>
               <th className="w-[150px] text-left font-light text-[#d5d5d5]">남은시간</th>
               <th className="w-[120px] font-light text-[#d5d5d5]">낙찰 결과</th>
               <th className="w-[270px]"></th>
             </tr>
           </thead>

           <tbody>
             <tr className="flex items-center py-3 px-5 bg-[#191924] border border-[#353535] rounded-xl">
               <td className="w-[130px] text-[#eaeaea] text-sm">O-OR30296405</td>
               <td className="w-[240px] text-md">피카츄 사세요 백만볼트 짱짱맨...</td>
               <td className="w-[110px] text-md font-semibold">1.054 BTC</td>
               <td className="w-[120px] text-md">1.054 BTC</td>
               <td className="w-[150px] text-md">3일 12시간 32분 52초</td>
               <td className="w-[120px] text-md text-center">진행 중</td>
               <td className="w-[270px]">
                 <div className="flex gap-3">
                   <button className="flex-1 py-3 bg-[#272732] border border-[#353535] rounded-lg text-[#6d6d6d] text-sm">재입찰</button>
                   <button className="flex-1 py-3 bg-[#3E4C63] rounded-lg text-white text-sm">즉시구매</button>
                 </div>
               </td>
             </tr>
           </tbody>
         </table>
       </div>
    )
}