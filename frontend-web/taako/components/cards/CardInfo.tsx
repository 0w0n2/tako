interface CardInfoProps {
  cardData: any;
  description: any;
  cardType: string;
}

export default function CardInfo({ cardData, description, cardType }: CardInfoProps) {
  const renderPokemonInfo = () => (
    <div className="bg-black rounded-lg shadow-md p-6">
      <h2 className="text-2xl font-bold mb-4 text-gray-800">{cardData.name}</h2>
      
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* 기본 정보 */}
        <div className="space-y-4">
          <div>
            <h3 className="text-lg font-semibold text-gray-700 mb-2">기본 정보</h3>
            <div className="space-y-2">
              <div className="flex justify-between">
                <span className="text-black">카드 코드:</span>
                <span className="text-black font-medium">{cardData.code}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-black">희귀도:</span>
                <span className="text-black font-medium">{cardData.rarity}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-black">타입:</span>
                <div className="flex gap-2">
                  {description.types?.map((type: string, index: number) => (
                    <span key={index} className="px-2 py-1 bg-red-100 text-red-800 rounded text-sm">
                      {type}
                    </span>
                  ))}
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* 공격 정보 */}
        <div>
          <h3 className="text-lg font-semibold text-gray-700 mb-2">공격 정보</h3>
          <div className="space-y-3">
            {description.attacks?.map((attack: any, index: number) => (
              <div key={index} className="border rounded-lg p-3 bg-gray-50">
                <div className="flex justify-between items-center mb-2">
                  <span className="font-medium text-gray-800">{attack.name}</span>
                  <span className="text-sm text-gray-600">비용: {attack.cost}</span>
                </div>
                <p className="text-sm text-gray-700">{attack.text}</p>
                {attack.damage && (
                  <div className="mt-2">
                    <span className="text-sm font-medium text-red-600">데미지: {attack.damage}</span>
                  </div>
                )}
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );

  const renderYuGiOhInfo = () => (
    <div className="bg-white rounded-lg shadow-md p-6">
      <h2 className="text-2xl font-bold mb-4 text-gray-800">{cardData.name}</h2>
      
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* 기본 정보 */}
        <div className="space-y-4">
          <div>
            <h3 className="text-lg font-semibold text-gray-700 mb-2">기본 정보</h3>
            <div className="space-y-2">
              <div className="flex justify-between">
                <span className="text-black">카드 코드:</span>
                <span className="text-black font-medium">{cardData.code}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-black">희귀도:</span>
                <span className="text-black font-medium">{cardData.rarity}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-black">속성:</span>
                <span className="px-2 py-1 bg-purple-100 text-purple-800 rounded text-sm">
                  {description.attribute}
                </span>
              </div>
              <div className="flex justify-between">
                <span className="text-black">타입:</span>
                <span className="text-black font-medium">{description.type}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-black">레벨:</span>
                <span className="text-black font-medium">{description.level}</span>
              </div>
            </div>
          </div>
        </div>

        {/* 스탯 정보 */}
        <div>
          <h3 className="text-lg font-semibold text-gray-700 mb-2">스탯 정보</h3>
          <div className="space-y-3">
            <div className="flex justify-between items-center p-3 bg-red-50 rounded-lg">
              <span className="text-gray-600">공격력:</span>
              <span className="font-bold text-red-600">{description.attack}</span>
            </div>
            <div className="flex justify-between items-center p-3 bg-blue-50 rounded-lg">
              <span className="text-gray-600">수비력:</span>
              <span className="font-bold text-blue-600">{description.deffence}</span>
            </div>
          </div>
        </div>
      </div>

      {/* 카드 텍스트 */}
      <div className="mt-6">
        <h3 className="text-lg font-semibold text-gray-700 mb-2">카드 효과</h3>
        <div className="bg-gray-50 rounded-lg p-4">
          <p className="text-sm text-gray-700 leading-relaxed whitespace-pre-line">
            {description.card_text}
          </p>
        </div>
      </div>
    </div>
  );

  const renderCookieRunInfo = () => (
    <div className="bg-white rounded-lg shadow-md p-6">
      <h2 className="text-2xl font-bold mb-4 text-gray-800">{cardData.name}</h2>
      
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* 기본 정보 */}
        <div className="space-y-4">
          <div>
            <h3 className="text-lg font-semibold text-gray-700 mb-2">기본 정보</h3>
            <div className="space-y-2">
              <div className="flex justify-between">
                <span className="text-black">카드 코드:</span>
                <span className="text-black font-medium">{cardData.code}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-black">희귀도:</span>
                <span className="text-black font-medium">{cardData.rarity}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-black">카드 타입:</span>
                <span className="px-2 py-1 bg-orange-100 text-orange-800 rounded text-sm">
                  {description.cardTypeTitle}
                </span>
              </div>
              <div className="flex justify-between">
                <span className="text-black">레벨:</span>
                <span className="text-black font-medium">{description.cardLevelTitle}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-black">에너지 타입:</span>
                <span className="px-2 py-1 bg-purple-100 text-purple-800 rounded text-sm">
                  {description.energyType}
                </span>
              </div>
            </div>
          </div>
        </div>

        {/* 스탯 정보 */}
        <div>
          <h3 className="text-lg font-semibold text-gray-700 mb-2">스탯 정보</h3>
          <div className="space-y-3">
            <div className="flex justify-between items-center p-3 bg-green-50 rounded-lg">
              <span className="text-gray-600">HP:</span>
              <span className="font-bold text-green-600">{description.field_hp_zbxcocvx}</span>
            </div>
          </div>
        </div>
      </div>

      {/* 카드 설명 */}
      <div className="mt-6">
        <h3 className="text-lg font-semibold text-gray-700 mb-2">카드 설명</h3>
        <div className="bg-gray-50 rounded-lg p-4">
          <div 
            className="text-sm text-gray-700 leading-relaxed"
            dangerouslySetInnerHTML={{ __html: description.field_cardDesc }}
          />
        </div>
      </div>
    </div>
  );

  return (
    <div className="w-full">
      {cardType === "Pokémon" && renderPokemonInfo()}
      {cardType === "YuGiOh" && renderYuGiOhInfo()}
      {cardType === "Cookierun" && renderCookieRunInfo()}
    </div>
  );
}
