'use client'

import SearchInput from "@/components/atoms/Input/SearchInput"
import Filter from "@/components/filters/Filter"
import { filterOptions, itemsMap } from "@/components/filters/data"
import TCGCard from '@/components/cards/TCGCard'
import SimpleCard from '@/components/cards/SimpleCard'
import cards from '@/components/cards/cards.json'
import '@/components/cards/cards.css'
import '@/components/cards/css/base.css'
import '@/components/cards/css/amazing-rare.css'
import '@/components/cards/css/basic.css'
import '@/components/cards/css/cosmos-holo.css'
import '@/components/cards/css/radiant-holo.css'
import '@/components/cards/css/rainbow-alt.css'
import '@/components/cards/css/rainbow-holo.css'
import '@/components/cards/css/regular-holo.css'
import '@/components/cards/css/reverse-holo.css'
import '@/components/cards/css/secret-rare.css'
import '@/components/cards/css/shiny-rare.css'
import '@/components/cards/css/shiny-v.css'
import '@/components/cards/css/shiny-vmax.css'
import '@/components/cards/css/swsh-pikachu.css'
import '@/components/cards/css/trainer-full-art.css'
import '@/components/cards/css/trainer-gallery-holo.css'
import '@/components/cards/css/trainer-gallery-secret-rare.css'
import '@/components/cards/css/trainer-gallery-v-max.css'
import '@/components/cards/css/trainer-gallery-v-regular.css'
import '@/components/cards/css/v-full-art.css'
import '@/components/cards/css/v-max.css'
import '@/components/cards/css/v-regular.css'
import '@/components/cards/css/v-star.css'
import { useCardStore } from '@/stores/useCardStore'
import { CategoryPageProps } from '@/types/category'

export default function CategoryPage({ params }: CategoryPageProps) {
  const { category_name } = params
  const { activeCard, setActiveCard } = useCardStore()
  
  const column = 5
  const gridStyle = {
    display: 'grid',
    gridTemplateColumns: `repeat(${column}, 1fr)`,
    gap: '2rem',
    padding: '1rem'
  }

  const displayCards = cards
  const active = activeCard ? displayCards.find((c) => c.id === activeCard) : null

  return (
    <div>
      <div className="default-container">
        <h1 className="text-center" style={{ marginBottom: '30px' }}>{category_name}</h1>
        <div className="flex items-center justify-between gap-4">
          <Filter filterOptions={filterOptions} itemsMap={itemsMap} />
          <SearchInput />
        </div>
        <div>
          <div style={gridStyle}>
            {displayCards.map((card) => (
              <button
                key={card.id}
                onClick={() => setActiveCard(card.id)}
                style={{
                  background: 'transparent',
                  border: 'none',
                  padding: 0,
                  cursor: 'pointer'
                }}
                aria-label={`open ${card.name}`}
              >
                <SimpleCard imageUrl={`/card-images/${card.set}/${card.number}_hires.png`} cardType={'Pokemon'} />
              </button>
            ))}
          </div>
          {active && (
            <div
              onClick={() => setActiveCard(null)}
              style={{
                position: 'fixed',
                inset: 0,
                background: 'rgba(0,0,0,0.35)',
                zIndex: 9998,
              }}
              aria-label="close active card"
            >
              <div
                style={{
                  position: 'absolute',
                  top: '50%',
                  left: '50%',
                  transform: 'translate(-50%, -50%) scale(0.5)',
                  transformOrigin: 'center',
                }}
                onClick={(e) => e.stopPropagation()}
              >
                <TCGCard
                  id={active.id}
                  name={active.name}
                  number={active.number}
                  set={active.set}
                  types={active.types || []}
                  subtypes={active.subtypes}
                  supertype={active.supertype}
                  rarity={active.rarity}
                  img={active.images.large}
                />
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}