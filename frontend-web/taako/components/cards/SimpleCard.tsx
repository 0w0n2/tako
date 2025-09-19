'use client'

import Image from "next/image"
import { useMemo, useState } from "react";

type SimpleCardProps = {
    imageUrl : string,
    cardType : keyof typeof CARD_SIZE
}

export const CARD_SIZE = {
  YuGiOh: { width: 59, height: 86 },
  Pokemon: { width: 63, height: 88 },
  MTG: { width: 63, height: 88 },
} as const;

export default function SimpleCard(props: SimpleCardProps) {
  const height = CARD_SIZE[props.cardType].height
  const width = CARD_SIZE[props.cardType].width

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