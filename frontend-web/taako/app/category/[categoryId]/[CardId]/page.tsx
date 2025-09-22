import api from "@/lib/api"
import EffectCard from "@/components/cards/EffectCard"
import CardInfo from "@/components/cards/CardInfo"


export default async function CategoryItemPage({ params }: { params: { categoryId: string, CardId: string } }) {
  const response = await api.get(`/v1/cards/${params.CardId}`)
  const cardData = response.data.result
  const description = JSON.parse(cardData.description)
  if (params.categoryId !== "2") {
    description.types = "fire"
  }

  const cardTypes = {
    1 : "YuGiOh", 
    2 : "Pokémon", 
    3 : "Cookierun", 
  }

  const cardType = cardTypes[Number(params.categoryId) as 1 | 2 | 3]

  return (
    <div className="min-h-screen py-8">
      <div className="max-w-7xl mx-auto px-4">
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 items-start">
          <div className="flex justify-center lg:justify-start">
            <EffectCard
              types={description.types}
              rarity={cardData.rarity}
              img={cardData.imageUrls[0]}
              type={cardType}
            />
          </div>
          
          <div className="w-full">
            <CardInfo 
              cardData={cardData}
              description={description}
              cardType={cardType}
            />
          </div>
        </div>
      </div>
    </div>
  );
}