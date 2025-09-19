'use client';

import { useEffect, useState } from 'react'
import { usePathname } from 'next/navigation'

type MosaicRevealProps = {
  columns?: number
  rows?: number
  durationMs?: number
}

export default function MosaicReveal({ columns = 10, rows = 3, durationMs = 500 }: MosaicRevealProps) {
  const pathname = usePathname()
  const [visible, setVisible] = useState(true)
  const [squares, setSquares] = useState<{ key: string; delayMs: number; color: string }[]>([])
  const [isMounted, setIsMounted] = useState(false)

  // 컴포넌트 마운트 상태 관리
  useEffect(() => {
    setIsMounted(true)
    return () => setIsMounted(false)
  }, [])

  // 클라이언트에서 delay 계산
  useEffect(() => {
    if (!isMounted) return
    
    const total = columns * rows
    const order = Array.from({ length: total }, (_, i) => i)
    for (let i = total - 1; i > 0; i -= 1) {
      const j = Math.floor(Math.random() * (i + 1))
      ;[order[i], order[j]] = [order[j], order[i]]
    }

    const arr: { key: string; delayMs: number; color: string }[] = []
    const step = 15
    const color = '#CA3813'

    let idx = 0
    for (let y = 0; y < rows; y += 1) {
      for (let x = 0; x < columns; x += 1) {
        const position = order[idx++]
        arr.push({ key: `${x}-${y}`, delayMs: position * step, color })
      }
    }

    setSquares(arr)
  }, [columns, rows, isMounted])

  useEffect(() => {
    setVisible(true)
    const maxDelay = squares.reduce((m, s) => (s.delayMs > m ? s.delayMs : m), 0)
    const totalMs = maxDelay + durationMs + 50

    const t = setTimeout(() => {
      setVisible(false)
    }, totalMs)
    
    return () => {
      clearTimeout(t)
    }
  }, [pathname, squares, durationMs])

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
