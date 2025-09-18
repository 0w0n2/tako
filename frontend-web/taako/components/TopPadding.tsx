'use client'

import { ReactNode } from 'react'
import { usePathname } from 'next/navigation'

type TopPaddingProps = {
  children: ReactNode
}

export default function TopPadding({ children }: TopPaddingProps) {
  const pathname = usePathname()
  const isHome = pathname === '/'
  const paddingClass = isHome ? 'pt-0' : 'py-[130px]'

  return (
    <div className={paddingClass}>
      {children}
    </div>
  )
}


