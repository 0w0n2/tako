'use client'

import Image from "next/image"
import { useState } from "react";
import { CharacterCardProps, CARD_SIZE } from "@/types/card"



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