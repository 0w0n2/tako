"use client";

import { useSpring, animated, to } from "@react-spring/web";
import { useEffect, useState, useRef, useMemo } from "react";
import { useCardStore } from "@/stores/useCardStore";
import { clamp, round, adjust } from "./lib/math";
import altArts from "./lib/alternate-arts.json";
import promos from "./lib/promos.json";

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
  foil?: string;
  mask?: string;
};

export default function EffectCard({
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
  isReverse: initialIsReverse = false,
  foil: initialFoil,
  mask: initialMask,
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

  const supertype = useMemo(
    () => initialSupertype.toLowerCase(),
    [initialSupertype]
  );
  const numberL = useMemo(() => number.toLowerCase(), [number]);
  const types = useMemo(
    () =>
      Array.isArray(initialTypes)
        ? initialTypes.join(" ").toLowerCase()
        : initialTypes.toLowerCase(),
    [initialTypes]
  );
  const subtypes = useMemo(
    () =>
      Array.isArray(initialSubtypes)
        ? initialSubtypes.join(" ").toLowerCase()
        : initialSubtypes.toLowerCase(),
    [initialSubtypes]
  );

  const isShiny = useMemo(() => numberL.startsWith("sv"), [numberL]);
  const isGallery = useMemo(() => !!numberL.match(/^[tg]g/i), [numberL]);
  const isAlternate = useMemo(
    () => altArts.includes(id) && !isShiny && !isGallery,
    [id, isShiny, isGallery]
  );
  const isPromo = useMemo(() => set === "swshp", [set]);

  const finalRarity = useMemo(() => {
    let r = initialRarity.toLowerCase();
    if (initialIsReverse) {
      r = r + " reverse holo";
    }
    if (isGallery) {
      if (r.startsWith("trainer gallery")) {
        r = r.replace(/trainer gallery\s*/, "");
      }
      if (r.includes("rare holo v") && subtypes.includes("vmax")) {
        r = "rare holo vmax";
      }
      if (r.includes("rare holo v") && subtypes.includes("vstar")) {
        r = "rare holo vstar";
      }
    }
    if (isPromo) {
      if (id === "swshp-SWSH076" || id === "swshp-SWSH077") {
        r = "rare secret";
      } else if (subtypes.includes("v-union")) {
        r = "rare holo vunion";
      } else if (subtypes.includes("vmax")) {
        r = "rare holo vmax";
      } else if (subtypes.includes("vstar")) {
        r = "rare holo vstar";
      } else if (subtypes.includes("radiant")) {
        r = "radiant rare";
      } else if (subtypes.includes("v")) {
        r = "rare holo v";
      }
    }
    return r;
  }, [initialRarity, initialIsReverse, isGallery, isPromo, id, subtypes]);

  const isTrainerGallery = useMemo(
    () =>
      !!numberL.match(/^[tg]g/i) ||
      !!(id === "swshp-SWSH076" || id === "swshp-SWSH077"),
    [numberL, id]
  );

  const foilUrl = useMemo(() => {
    return foilMaskImage(initialFoil, "foils");
  }, [initialFoil, finalRarity, subtypes, supertype, set, numberL, isShiny, isGallery, isAlternate, isPromo, id]);

  const maskUrl = useMemo(() => {
    return foilMaskImage(initialMask, "masks");
  }, [initialMask, finalRarity, subtypes, supertype, set, numberL, isShiny, isGallery, isAlternate, isPromo, id]);

  function foilMaskImage(prop: string | undefined, type: "foils" | "masks") {
    if (type === 'masks') {
      return ''; // 마스크는 사용하지 않음
    }

    if (prop) {
      if (prop === "false") return "";
      return prop;
    }

    const fRarity = finalRarity;

    const mapping: { [key: string]: string } = {
      'rare secret': '/card-images/effects/geometric.png',
      'rare holo cosmos': '/card-images/effects/cosmos.png',
      'radiant rare': '/card-images/effects/angular.png',
      'trainer gallery rare holo': '/card-images/effects/trainerbg.png',
      'rare holo vmax': '/card-images/effects/vmaxbg.jpg',
      'rare holo vstar': '/card-images/effects/ancient.png',
      'rare holo v': '/card-images/effects/illusion.png',
      'rare ultra': '/card-images/effects/illusion.png',
      'rare rainbow': '/card-images/effects/rainbow.jpg',
      'amazing rare': '/card-images/effects/galaxy.jpg',
      'rare shiny': '/card-images/effects/illusion.png',
      'rare holo': '/card-images/effects/wave.png',
    };

    for (const key in mapping) {
      if (fRarity.includes(key)) {
        return mapping[key];
      }
    }

    if (fRarity.includes('reverse holo')) {
      return '/card-images/effects/wave.png';
    }

    return '';
  }

  const baseLocalPath = useMemo(
    () => `/card-images/${set}/${number}`,
    [set, number]
  );

  useEffect(() => {
    if (img.startsWith("http")) {
      setFrontSrc(img);
      return;
    }
    if (img.startsWith("/card-images/")) {
      setFrontSrc(img);
      return;
    }
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
    zIndex: isActive ? 9999 : "auto",
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
        data-number={numberL}
        data-set={set}
        data-subtypes={subtypes}
        data-supertype={supertype}
                data-rarity={finalRarity}
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
          <div className="card__front" style={foilStyles}>
            <img
              src={frontSrc}
              alt={name}
              onLoad={() => setLoading(false)}
              onError={(e) => {
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