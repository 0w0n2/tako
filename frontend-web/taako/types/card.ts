export interface Card {
  id: number;
  name: string;
  grade: string;
  rarity: string;
  categoryMajorId: number;
  categoryMajorName: string;
  categoryMediumId: number;
  categoryMediumName: string,
}

export const CARD_SIZE = {
  YuGiOh: { width: 59, height: 86 },
  Pokemon: { width: 63, height: 88 },
  MTG: { width: 63, height: 88 },
} as const;

export type CharacterCardProps = {
  imageUrl: string,
  cardType: keyof typeof CARD_SIZE
}