'use client'

import Image from "next/image"
import { useMemo, useState, useEffect } from "react";
import { useSpring, animated, to } from "@react-spring/web";
import { CARD_SIZE } from "@/types/card";

type SimpleCardProps = {
    imageUrl : string,
    cardType : keyof typeof CARD_SIZE,
    href?: string,
    onCardClick?: () => void
}

export default function SimpleCard(props: SimpleCardProps) {
  const cardSize = CARD_SIZE[props.cardType] || CARD_SIZE.Pokémon;
  const height = cardSize.height;
  const width = cardSize.width;

  const { basePng, hiresPng } = useMemo(() => {
    const baseUrl = props.imageUrl

    if (baseUrl.endsWith("_hires.png")) {
      return { basePng: baseUrl.replace("_hires.png", ".png"), hiresPng: baseUrl }
    }
    if (baseUrl.endsWith(".png")) {
      return {
        basePng: baseUrl,
        hiresPng: baseUrl.replace(/\.png$/, "_hires.png"),
      }
    }
    // 확장자 없는 예외 케이스는 원본만 사용
    return { basePng: baseUrl, hiresPng: baseUrl }
  }, [props.imageUrl])

  const [src, setSrc] = useState<string>(hiresPng)
  const [isActive, setIsActive] = useState(false)

  // 애니메이션 스프링 설정
  const [styles, api] = useSpring(() => ({
    translateX: 0,
    translateY: 0,
    rotateY: 0,
    scale: 1,
    zIndex: 1,
    config: { tension: 200, friction: 20 },
  }))

  // 클릭 핸들러
  const handleClick = () => {
    setIsActive(v => !v)
    
    // href가 있으면 애니메이션 후 1초 지연하고 링크 이동
    if (props.href) {
      setTimeout(() => {
        if (props.onCardClick) {
          props.onCardClick()
        } else if (props.href) {
          window.location.href = props.href
        }
      }, 1000)
    }
  }

  // 애니메이션 효과
  useEffect(() => {
    if (isActive) {
      // 클릭 시 Y축 360도 회전하면서 화면 중앙으로 이동
      api.start({ 
        translateX: 0,
        translateY: -50,
        rotateY: 360,
        scale: 2,
        zIndex: 1000,
        config: { tension: 200, friction: 20 }
      })
    } else {
      // 다시 클릭 시 원래 상태로 복원
      api.start({
        translateX: 0,
        translateY: 0,
        rotateY: 0,
        scale: 1,
        zIndex: 1,
        config: { tension: 200, friction: 40 },
      });
    }
  }, [isActive, api])

  // 애니메이션 스타일 변환
  const animatedStyles = {
    transform: to(
      [styles.translateX, styles.translateY, styles.rotateY, styles.scale],
      (x, y, rotateY, scale) => `translate(${x}px, ${y}px) rotateY(${rotateY}deg) scale(${scale})`
    ),
    zIndex: styles.zIndex,
  }

  return (
    <animated.div
      style={{
        position: 'relative',
        width: '100%',
        aspectRatio: `${width} / ${height}`,
        cursor: 'pointer',
        transformStyle: 'preserve-3d',
        ...animatedStyles
      }}
      onClick={handleClick}
    >
      <Image
        loader={() => src}
        src={src}
        alt="card-image"
        fill
        sizes="100vw"
        style={{ objectFit: 'contain' }}
        onError={() => {
          if (src !== basePng) setSrc(basePng)
        }}
      />
    </animated.div>
  )
}