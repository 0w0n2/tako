interface Rank {
  rank: string;
}

export default function RankElement({ rank }: Rank) {
  const getRankStyle = (rank: string) => {
    switch (rank) {
      case 'S+':
        return 'text-[#30E6F7] shadow-[#30E6F7]';
      case 'S':
        return 'text-[#837BFF] shadow-[#837BFF]';
      case 'A':
        return 'text-[#FF5DA3] shadow-[#FF5DA3]';
      case 'B':
        return 'shadow-[#BCBCBC]';
      default:
        return 'text-[#BCBCBC]';
    }
  };

  return (
    <div className={`flex justify-center items-center bg-[#191924]
        w-[50px] h-[50px] border-2 rounded-xl ${getRankStyle(rank)}`
    }
    >
        <p className="text-[32px] font-bold -translate-y-0.5"
            style={{ fontFamily: 'Pinkfong-L, sans-serif' }}
        >{rank}</p>
    </div>
  );
}