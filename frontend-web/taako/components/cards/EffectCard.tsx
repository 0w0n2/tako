"use client"

import { useSpring, animated, to } from "@react-spring/web"
import { useEffect, useState, useRef, useMemo } from "react"
import { clamp, round, adjust } from "./lib/math"
import rarityData from "./rarity.json"


type EffectCardProps = {
  types: string[] | string;
  rarity: string;
  img: string;
  type?: string;
}

export default function EffectCard({
  types: initialTypes,
  rarity: initialRarity,
  img,
  type,
}: EffectCardProps) {
  const [loading, setLoading] = useState(true)
  const [frontSrc, setFrontSrc] = useState<string>("")
  const [interacting, setInteracting] = useState(false)
  const thisCard = useRef<HTMLDivElement>(null);

  const [styles, api] = useSpring(() => ({
    translateX: 0,
    translateY: 0,
    rotateX: 0,
    rotateY: 0,
    scale: 1,
    pointerX: 50,
    pointerY: 50,
    backgroundX: 50,
    backgroundY: 50,
    opacity: 0,
    config: { tension: 300, friction: 30 },
  }));

  const types = useMemo(
    () =>
      Array.isArray(initialTypes)
        ? initialTypes.join(" ").toLowerCase()
        : initialTypes.toLowerCase(),
    [initialTypes]
  );


  const finalRarity = useMemo(() => {
    // rarity.json의 rarityMapping에서 직접 매핑된 값이 있는지 확인
    const mappedRarity = (rarityData.rarityMapping as any)[initialRarity];
    if (mappedRarity) {
      return mappedRarity;
    }
    
    // 매핑되지 않은 경우 패턴 매칭으로 fallback
    const validRarities = Object.values(rarityData.rarityMapping);
    let r = initialRarity.toLowerCase();
    
    if (!validRarities.includes(r)) {
      // rarity.json의 patternMapping을 사용하여 패턴 매칭
      const matchedPattern = (rarityData.patternMapping as any[]).find(
        item => r.includes(item.pattern)
      );
      
      if (matchedPattern) {
        r = matchedPattern.rarity;
      } else {
        r = rarityData.defaultRarity;
      }
    }
    
    return r;
  }, [initialRarity]);




  function getRarityEffect(rarity: string, type: "foil" | "mask") {
    const mapping = type === 'mask' ? rarityData.maskMapping : rarityData.foilMapping;
    return (mapping as any)[rarity] || '';
  }

  const getCardBackImage = (type?: string) => {
    const backMapping: { [key: string]: string } = {
      'pokemon': '/card-back/pokemon-back.jpg',
      'yugioh': '/card-back/yugioh-back.jpg',
      'cookierun': '/card-back/cookierun-back.png'
    };

    return type && backMapping[type.toLowerCase()] 
      ? backMapping[type.toLowerCase()] 
      : '/card-back/pokemon-back.jpg';
  };

  const cardBackImage = useMemo(() => getCardBackImage(type), [type]);

  useEffect(() => {
    setFrontSrc(img);
  }, [img]);

  const interact = (e: React.PointerEvent<HTMLButtonElement>) => {
    setInteracting(true);

    const rect = e.currentTarget.getBoundingClientRect();
    const absolute = {
      x: e.clientX - rect.left,
      y: e.clientY - rect.top,
    };
    const percent = {
      x: clamp(round((100 / rect.width) * absolute.x)),
      y: clamp(round((100 / rect.height) * absolute.y)),
    };
    const center = {
      x: percent.x - 50,
      y: percent.y - 50,
    };

    api.start({
      rotateX: round(center.y / 2),
      rotateY: round(-(center.x / 3.5)),
      pointerX: round(percent.x),
      pointerY: round(percent.y),
      backgroundX: adjust(percent.x, 0, 100, 37, 63),
      backgroundY: adjust(percent.y, 0, 100, 33, 67),
      opacity: 1,
    });
  };

  const interactEnd = () => {
    if (isActive) return;
    setInteracting(false);
    api.start({
      rotateX: 0,
      rotateY: 0,
      pointerX: 50,
      pointerY: 50,
      backgroundX: 50,
      backgroundY: 50,
      opacity: 0,
      config: { tension: 200, friction: 40 },
    });
  };

  const animatedStyles = {
    transform: to(
      [styles.translateX, styles.translateY, styles.scale],
      (x, y, s) => `translate(${x}px, ${y}px) scale(${s})`
    ),
  };

  const rotatorStyles = {
    transform: to(
      [styles.rotateX, styles.rotateY],
      (x, y) => `rotateY(${y}deg) rotateX(${x}deg)`
    ),
  };

  const dynamicStyles = {
    "--pointer-x": styles.pointerX.to((x) => `${x}%`),
    "--pointer-y": styles.pointerY.to((y) => `${y}%`),
    "--background-x": styles.backgroundX.to((x) => `${x}%`),
    "--background-y": styles.backgroundY.to((y) => `${y}%`),
    "--card-opacity": styles.opacity,
    "--pointer-from-center": to([styles.pointerX, styles.pointerY], (x, y) =>
      clamp(Math.sqrt((y - 50) ** 2 + (x - 50) ** 2) / 50, 0, 1)
    ),
    "--pointer-from-top": styles.pointerY.to((y) => y / 100),
    "--pointer-from-left": styles.pointerX.to((x) => x / 100),
  } as React.CSSProperties;

  const foilStyles = useMemo(() => {
    const foilUrl = getRarityEffect(finalRarity, "foil");
    const maskUrl = getRarityEffect(finalRarity, "mask");
    
    return {
      "--foil": foilUrl ? `url("${foilUrl}")` : "none",
      "--mask": maskUrl ? `url("${maskUrl}")` : "none",
    } as React.CSSProperties;
  }, [finalRarity]);

  const isActive = false
  return (
    <animated.div
      ref={thisCard}
      className={`card-container`}
      style={animatedStyles}
    >
      <animated.div
        className={`card ${types} ${loading ? "loading" : ""} ${
          isActive ? "active" : ""
        } ${interacting ? "interacting" : ""} ${getRarityEffect(finalRarity, "mask") ? "masked" : ""}`}
        data-rarity={finalRarity}
        data-trainer-gallery={finalRarity === "trainer gallery rare holo"}
        style={dynamicStyles}
      >
        <animated.button
          className="card__rotator"
          onPointerMove={interact}
          onPointerLeave={interactEnd}
          style={{ ...rotatorStyles, ...dynamicStyles }}
        >
          <img
            className="card__back"
            src={cardBackImage}
            alt="Card Back"
            loading="lazy"
            width="660"
            height="921"
          />
          <div className="card__front" style={foilStyles}>
            <img
              src={frontSrc}
              alt=""
              onLoad={() => setLoading(false)}
              loading="lazy"
              width="660"
              height="921"
            />
            <div className="card__shine"></div>
            <div className="card__glare"></div>
          </div>
        </animated.button>
      </animated.div>
    </animated.div>
  );
}