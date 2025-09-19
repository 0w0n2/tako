"use client"

import { useSpring, animated, to } from "@react-spring/web"
import { useEffect, useState, useRef, useMemo } from "react"
import { clamp, round, adjust } from "./lib/math"

type EffectCardProps = {
  types: string[] | string;
  rarity: string;
  img: string;
  type?: string;
  foil?: string;
  mask?: string;
}

export default function EffectCard({
  types: initialTypes,
  rarity: initialRarity,
  img,
  type,
  foil: initialFoil,
  mask: initialMask,
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
    const validRarities = [
      "radiant rare",
      "trainer gallery rare holo", 
      "rare holo cosmos",
      "rare holo v",
      "uncommon",
      "rare holo vstar",
      "rare holo vmax",
      "common",
      "rare secret",
      "pikachu",
      "rare shiny",
      "rare shiny v",
      "rare shiny vmax"
    ];
    
    let r = initialRarity.toLowerCase();
    
    if (!validRarities.includes(r)) {
      if (r.includes("pikachu")) {
        r = "pikachu";
      } else if (r.includes("rare secret")) {
        r = "rare secret"
      } else if (r.includes("rare shiny vmax")) {
        r = "rare shiny vmax"
      } else if (r.includes("rare shiny v")) {
        r = "rare shiny v"
      } else if (r.includes("rare shiny")) {
        r = "rare shiny"
      } else if (r.includes("rare holo vmax")) {
        r = "rare holo vmax"
      } else if (r.includes("rare holo vstar")) {
        r = "rare holo vstar"
      } else if (r.includes("rare holo v")) {
        r = "rare holo v";
      } else if (r.includes("rare holo cosmos")) {
        r = "rare holo cosmos"
      } else if (r.includes("trainer gallery")) {
        r = "trainer gallery rare holo"
      } else if (r.includes("radiant")) {
        r = "radiant rare"
      } else if (r.includes("uncommon")) {
        r = "uncommon"
      } else {
        r = "common"
      }
    }
    
    return r;
  }, [initialRarity]);

  const isTrainerGallery = useMemo(
    () => finalRarity === "trainer gallery rare holo",
    [finalRarity]
  );

  const foilUrl = useMemo(() => {
    return foilMaskImage(initialFoil, "foils")
  }, [initialFoil, finalRarity]);

  const maskUrl = useMemo(() => {
    return foilMaskImage(initialMask, "masks")
  }, [initialMask, finalRarity]);

  function foilMaskImage(prop: string | undefined, type: "foils" | "masks") {
    if (prop) {
      if (prop === "false") return "";
      if (prop.startsWith('/effects/') || prop.startsWith('effects/')) {
        return prop.startsWith('/') ? prop : `/${prop}`
      }
      return prop;
    }

    const foilMapping: { [key: string]: string } = {
      'radiant rare': '/effects/angular.png',
      'trainer gallery rare holo': '/effects/trainerbg.png',
      'rare holo cosmos': '/effects/cosmos.png',
      'rare holo v': '/effects/illusion.png',
      'uncommon': '/effects/wave.png',
      'rare holo vstar': '/effects/ancient.png',
      'rare holo vmax': '/effects/vmaxbg.jpg',
      'common': '/effects/wave.png',
    };

    const maskMapping: { [key: string]: string } = {
      'rare holo cosmos': '/effects/cosmos-middle-trans.png',
      'rare holo vmax': '/effects/vmaxbg.jpg',
      'rare holo vstar': '/effects/ancient.png',
      'rare holo v': '/effects/illusion-mask.png',
      'trainer gallery rare holo': '/effects/trainerbg.png',
      'radiant rare': '/effects/angular.png',
      'uncommon': '/effects/wave.png',
      'common': '/effects/wave.png',
    };

    const mapping = type === 'masks' ? maskMapping : foilMapping;
    
    return mapping[finalRarity] || '';
  }

  const getCardBackImage = (cardType?: string) => {
    const backMapping: { [key: string]: string } = {
      'pokemon': '/card-back/pokemon-back.jpg',
      'yugioh': '/card-back/yugioh-back.jpg',
      'cookierun': '/card-back/cookierun-back.png',
      'cookie run': '/card-back/cookierun-back.png',
    };

    return cardType && backMapping[cardType.toLowerCase()] 
      ? backMapping[cardType.toLowerCase()] 
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

  const foilStyles = {
    "--foil": foilUrl ? `url("${foilUrl}")` : "none",
    "--mask": maskUrl ? `url("${maskUrl}")` : "none",
  } as React.CSSProperties;

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
        } ${interacting ? "interacting" : ""} ${maskUrl ? "masked" : ""}`}
        data-rarity={finalRarity}
        data-trainer-gallery={isTrainerGallery}
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