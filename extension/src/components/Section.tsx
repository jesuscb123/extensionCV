import type { ReactNode } from 'react'

interface SectionProps {
  title: string
  children: ReactNode
}

export function Section({ title, children }: SectionProps) {
  return (
    <section className="space-y-1.5">
      <h3 className="text-xs font-semibold uppercase tracking-wide text-slate-400">{title}</h3>
      {children}
    </section>
  )
}
