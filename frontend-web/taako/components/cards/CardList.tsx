'use client'

import TCGCard from './TCGCard'
import SimpleCard from './SimpleCard'
import cards from './cards.json'
import './cards.css'
import './css/base.css'
import './css/amazing-rare.css'
import './css/basic.css'
import './css/cosmos-holo.css'
import './css/radiant-holo.css'
import './css/rainbow-alt.css'
import './css/rainbow-holo.css'
import './css/regular-holo.css'
import './css/reverse-holo.css'
import './css/secret-rare.css'
import './css/shiny-rare.css'
import './css/shiny-v.css'
import './css/shiny-vmax.css'
import './css/swsh-pikachu.css'
import './css/trainer-full-art.css'
import './css/trainer-gallery-holo.css'
import './css/trainer-gallery-secret-rare.css'
import './css/trainer-gallery-v-max.css'
import './css/trainer-gallery-v-regular.css'
import './css/v-full-art.css'
import './css/v-max.css'
import './css/v-regular.css'
import './css/v-star.css'
import { useCardStore } from '@/stores/useCardStore'

interface CardListProps {
  column: number;
}

// rows랑 columns를 받아서 그에 맞게 카드를 보여준다.



export default function CardList({ column }: CardListProps) {
  const { activeCard, setActiveCard } = useCardStore()
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
  )
}