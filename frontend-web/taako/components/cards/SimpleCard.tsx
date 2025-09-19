'use client'

import Image from "next/image"
import { useMemo, useState } from "react";
import { CARD_SIZE } from "@/types/card";

type SimpleCardProps = {
    imageUrl : string,
    cardType : keyof typeof CARD_SIZE
}

export default function SimpleCard(props: SimpleCardProps) {
  const cardSize = CARD_SIZE[props.cardType] || CARD_SIZE.Pokémon;
  const height = cardSize.height;
  const width = cardSize.width;

  const { basePng, hiresPng } = useMemo(() => {
    if (props.imageUrl.endsWith("_hires.png")) {
      return { basePng: props.imageUrl.replace("_hires.png", ".png"), hiresPng: props.imageUrl }
    }
    if (props.imageUrl.endsWith(".png")) {
      return {
        basePng: props.imageUrl,
        hiresPng: props.imageUrl.replace(/\.png$/, "_hires.png"),
      }
    }
    // 확장자 없는 예외 케이스는 원본만 사용
    return { basePng: props.imageUrl, hiresPng: props.imageUrl }
  }, [props.imageUrl])

  const [src, setSrc] = useState<string>(hiresPng)

  return (
    <div
      style={{
        position: 'relative',
        width: '100%',
        aspectRatio: `${width} / ${height}`,
      }}
    >
      <Image
        src={src}
        alt="card-image"
        fill
        sizes="100vw"
        style={{ objectFit: 'contain' }}
        onError={() => {
          if (src !== basePng) setSrc(basePng)
        }}
      />
    </div>
  )
}