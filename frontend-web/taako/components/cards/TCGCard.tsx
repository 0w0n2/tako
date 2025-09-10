"use client";

import { useSpring, animated, to } from "@react-spring/web";
import { useEffect, useState, useRef, useMemo } from "react";
import { useCardStore } from "@/stores/useCardStore";
import { clamp, round, adjust } from "./lib/math";

type TCGCardProps = {
  id: string;
  name: string;
  number: string;
  set: string;
  types: string[] | string;
  subtypes: string[] | string;
  supertype: string;
  rarity: string;
  img: string;
  back?: string;
  showcase?: boolean;
  isReverse?: boolean;
};

export default function TCGCard({
  id,
  name,
  number,
  set,
  types: initialTypes,
  subtypes: initialSubtypes,
  supertype: initialSupertype,
  rarity: initialRarity,
  img,
  back = "https://tcg.pokemon.com/assets/img/global/tcg-card-back-2x.jpg",
}: TCGCardProps) {
  const [loading, setLoading] = useState(true);
  const [frontSrc, setFrontSrc] = useState<string>("");
  const [interacting, setInteracting] = useState(false);
  const [firstPop, setFirstPop] = useState(true);
  const thisCard = useRef<HTMLDivElement>(null);

  const { activeCard, setActiveCard } = useCardStore();
  const isActive = activeCard === id;

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

  const rarity = useMemo(() => initialRarity.toLowerCase(), [initialRarity]);
  const supertype = useMemo(() => initialSupertype.toLowerCase(), [initialSupertype]);
  const numberL = useMemo(() => number.toLowerCase(), [number]);
  const types = useMemo(() => Array.isArray(initialTypes) ? initialTypes.join(" ").toLowerCase() : initialTypes.toLowerCase(), [initialTypes]);
  const subtypes = useMemo(() => Array.isArray(initialSubtypes) ? initialSubtypes.join(" ").toLowerCase() : initialSubtypes.toLowerCase(), [initialSubtypes]);
  const isTrainerGallery = useMemo(() => !!numberL.match(/^[tg]g/i) || !!(id === "swshp-SWSH076" || id === "swshp-SWSH077"), [numberL, id]);

  const baseLocalPath = useMemo(() => `/card-images/${set}/${number}`, [set, number]);

  useEffect(() => {
    if (img.startsWith("http")) {
      setFrontSrc(img);
      return;
    }
    if (img.startsWith("/card-images/")) {
      setFrontSrc(img);
      return;
    }
    // 기본: 로컬 구조에 맞춰 hires 우선 사용, 실패 시 onError에서 일반으로 폴백
    setFrontSrc(`${baseLocalPath}_hires.png`);
  }, [img, baseLocalPath]);

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

  const toggleActive = () => {
    if (activeCard === id) {
      setActiveCard(null);
    } else {
      setActiveCard(id);
    }
  };

  useEffect(() => {
    const cardRef = thisCard.current;
    if (!cardRef) return;

    if (isActive) {
      setInteracting(true);
      const rect = cardRef.getBoundingClientRect();
      const view = document.documentElement;
      const scaleW = (view.clientWidth / rect.width) * 0.6;
      const scaleH = (view.clientHeight / rect.height) * 0.6;
      const scale = Math.min(scaleW, scaleH, 1.75);

      const delta = {
        x: round(view.clientWidth / 2 - rect.x - rect.width / 2),
        y: round(view.clientHeight / 2 - rect.y - rect.height / 2),
      };

      api.start({
        translateX: delta.x,
        translateY: delta.y,
        scale: scale,
        rotateX: 0,
        rotateY: firstPop ? 360 : 0,
        config: { tension: 120, friction: 30 },
      });
      setFirstPop(false);
    } else {
      api.start({
        translateX: 0,
        translateY: 0,
        scale: 1,
        rotateX: 0,
        rotateY: 0,
        config: { tension: 200, friction: 30 },
      });
      interactEnd();
    }
  }, [isActive, api, firstPop]);

  const animatedStyles = {
    transform: to(
      [styles.translateX, styles.translateY, styles.scale],
      (x, y, s) => `translate(${x}px, ${y}px) scale(${s})`
    ),
    zIndex: isActive ? 9999 : 'auto',
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
    "--pointer-from-center": to(
      [styles.pointerX, styles.pointerY],
      (x, y) => clamp(Math.sqrt((y - 50) ** 2 + (x - 50) ** 2) / 50, 0, 1)
    ),
    "--pointer-from-top": styles.pointerY.to((y) => y / 100),
    "--pointer-from-left": styles.pointerX.to((x) => x / 100),
  } as React.CSSProperties;

  return (
    <animated.div
      ref={thisCard}
      className={`card-container`}
      style={animatedStyles}
    >
      <animated.div
        className={`card ${types} ${loading ? "loading" : ""} ${
          isActive ? "active" : ""
        } ${interacting ? "interacting" : ""}`}
        data-number={numberL}
        data-set={set}
        data-subtypes={subtypes}
        data-supertype={supertype}
        data-rarity={rarity}
        data-trainer-gallery={isTrainerGallery}
        style={dynamicStyles}
      >
        <animated.button
          className="card__rotator"
          onClick={toggleActive}
          onPointerMove={interact}
          onPointerLeave={interactEnd}
          style={{ ...rotatorStyles, ...dynamicStyles }}
        >
          <img
            className="card__back"
            src={back}
            alt="Pokemon Card Back"
            loading="lazy"
            width="660"
            height="921"
          />
          <div className="card__front">
            <img
              src={frontSrc}
              alt={name}
              onLoad={() => setLoading(false)}
              onError={(e) => {
                // 폴백: hires가 없으면 일반 해상도로 시도
                const current = (e.currentTarget as HTMLImageElement).src;
                if (current.endsWith("_hires.png")) {
                  setFrontSrc(`${baseLocalPath}.png`);
                }
              }}
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