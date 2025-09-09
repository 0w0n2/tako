'use client'

import TCGCard from './TCGCard'
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

export default function CardList() {
  return (
    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(240px, 1fr))', gap: '1rem', padding: '1rem' }}>
      {cards.map((card) => (
        <TCGCard
          key={card.id}
          id={card.id}
          name={card.name}
          number={card.number}
          set={card.set}
          types={card.types || []}
          subtypes={card.subtypes}
          supertype={card.supertype}
          rarity={card.rarity}
          img={card.images.large}
          foil={card.images.foil}
          mask={card.images.mask}
        />
      ))}
    </div>
  )
}