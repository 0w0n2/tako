interface Rank {
  rank: string;
}

export default function RankElement({ rank }: Rank) {
  const getRankStyle = (rank: string) => {
    switch (rank) {
      case 'S+':
        return 'text-[#30E6F7] shadow-lg shadow-[#30E6F7]';
      case 'S':
        return 'text-[#837BFF] text-[40px] shadow-lg shadow-[#837BFF]';
      case 'A1':
        return 'text-[#FF5DA3] shadow-lg shadow-[#FF5DA3]';
      case 'A':
        return 'text-[#FF5DA3] shadow-lg shadow-[#FF5DA3]';
      case 'B':
        return 'shadow-lg shadow-[#BCBCBC]';
      default:
        return 'text-[#BCBCBC]';
    }
  };

  return (
    <div
      className={`relative flex justify-center items-center 
        w-[50px] h-[50px] border-4 rounded-lg overflow-hidden
        bg-[#191924] ${getRankStyle(rank)}`}
    >
      {/* 반짝임 효과 */}
      <div className="shine-effect"></div>

      {/* 숫자 */}
      <p className="relative z-10 text-[32px] font-bold -translate-y-0.5">
        {rank}
      </p>
    </div>
  );
}