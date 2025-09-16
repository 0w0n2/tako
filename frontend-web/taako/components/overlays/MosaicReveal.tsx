'use client'

import { useEffect, useMemo, useState } from 'react'
import { usePathname } from 'next/navigation'

type MosaicRevealProps = {
  columns?: number
  rows?: number
  durationMs?: number
}

// A fullscreen mosaic of colored squares that fade away on mount and on route change
export default function MosaicReveal({ columns = 10, rows = 3, durationMs = 500 }: MosaicRevealProps) {
  const pathname = usePathname()
  const [visible, setVisible] = useState(true)
  const [showBadge, setShowBadge] = useState(true)
  const [shapeTick, setShapeTick] = useState(0)
  const [letterSlots, setLetterSlots] = useState<number[]>([])
  const [letterOrder, setLetterOrder] = useState<string[]>([])

  // Generate grid squares once for a given grid size
  const squares = useMemo(() => {
    // Fisher–Yates shuffle for random order
    const total = columns * rows
    const order = Array.from({ length: total }, (_, i) => i)
    for (let i = total - 1; i > 0; i -= 1) {
      const j = Math.floor(Math.random() * (i + 1))
      ;[order[i], order[j]] = [order[j], order[i]]
    }

    const arr: { key: string; delayMs: number; color: string }[] = []
    const step = 15 // ms between tiles (for ~2s total)
    const color = '#CA3813' // Tailwind blue-500
    let idx = 0
    for (let y = 0; y < rows; y += 1) {
      for (let x = 0; x < columns; x += 1) {
        const position = order[idx++]
        arr.push({ key: `${x}-${y}`, delayMs: position * step, color })
      }
    }
    return arr
  }, [columns, rows])

  // Show animation on first mount and whenever route changes
  useEffect(() => {
    setVisible(true)
    setShowBadge(true)
    setShapeTick(0)
    // choose 4 random slots for letters T,A,K,O and shuffle their order
    const idxs = [0,1,2,3,4,5]
    for (let i = idxs.length - 1; i > 0; i -= 1) {
      const j = Math.floor(Math.random() * (i + 1))
      ;[idxs[i], idxs[j]] = [idxs[j], idxs[i]]
    }
    const chosen = idxs.slice(0, 4)
    const letters = ['T','A','K','O']
    for (let i = letters.length - 1; i > 0; i -= 1) {
      const j = Math.floor(Math.random() * (i + 1))
      ;[letters[i], letters[j]] = [letters[j], letters[i]]
    }
    setLetterSlots(chosen)
    setLetterOrder(letters)
    // compute last tile finish time: max delay + duration
    const maxDelay = squares.reduce((m, s) => (s.delayMs > m ? s.delayMs : m), 0)
    const totalMs = maxDelay + durationMs + 50
    const t = setTimeout(() => setVisible(false), totalMs)
    const b = setTimeout(() => setShowBadge(false), totalMs)
    // cycle shapes while badge is visible
    const iv = setInterval(() => setShapeTick((v) => v + 1), 220)
    const stopIv = setTimeout(() => clearInterval(iv), totalMs)
    return () => {
      clearTimeout(t)
      clearTimeout(b)
      clearTimeout(stopIv)
      clearInterval(iv)
    }
  }, [pathname, durationMs, squares])

  if (!visible) return null

  return (
    <div className="pointer-events-none fixed inset-0 z-[9999]">
      <div
        className="grid h-full w-full"
        style={{
          gridTemplateColumns: `repeat(${columns}, 1fr)`,
          gridTemplateRows: `repeat(${rows}, 1fr)`,
        }}
      >
        {squares.map((sq) => (
          <div
            key={sq.key}
            className="mosaic-square"
            style={{
              animationDuration: `${durationMs}ms`,
              animationDelay: `${sq.delayMs}ms`,
              backgroundColor: sq.color,
            }}
          />
        ))}
      </div>
    </div>
  )
}


