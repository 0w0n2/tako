'use client'

import Image from "next/image"
import { useState } from "react";

type CharacterCardProps = {
    imageUrl : string,
    cardType : keyof typeof CARD_SIZE
}

export const CARD_SIZE = {
  YuGiOh: { width: 59, height: 86 },
  Pokemon: { width: 63, height: 88 },
  MTG: { width: 63, height: 88 },
} as const;

export default function CharacterCard(props: CharacterCardProps) {
  const [isMouseOver, handleMouseOver] = useState<boolean>(false)
  const height = CARD_SIZE[props.cardType].height
  const width = CARD_SIZE[props.cardType].height
    return (
        <div onMouseOver={() => handleMouseOver(true)} onMouseOut={() => handleMouseOver(false)}>
            <Image src={props.imageUrl} alt="card-image" width={width} height={height}></Image>
            {isMouseOver && (
              <Image src={props.imageUrl} alt="card-image" width={width * 2} height={height * 2} />
            )}
        </div>
    )
}