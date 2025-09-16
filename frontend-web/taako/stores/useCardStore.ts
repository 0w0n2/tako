import { create } from 'zustand';

type CardStore = {
  activeCard: string | null;
  setActiveCard: (id: string | null) => void;
};

export const useCardStore = create<CardStore>((set) => ({
  activeCard: null,
  setActiveCard: (id) => set({ activeCard: id }),
}));