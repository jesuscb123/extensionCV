import { useState } from 'react'
import type { AnsweredQuestion } from '../schemas/question.schema'

interface AnswerCardProps {
  item: AnsweredQuestion
}

export function AnswerCard({ item }: AnswerCardProps) {
  const [copied, setCopied] = useState(false)

  const copy = async (): Promise<void> => {
    try {
      await navigator.clipboard.writeText(item.answer)
      setCopied(true)
      window.setTimeout(() => setCopied(false), 1500)
    } catch {
      setCopied(false)
    }
  }

  return (
    <div className="space-y-1 rounded-md border border-slate-800 p-2.5">
      <p className="text-xs font-medium text-slate-300">{item.label}</p>
      <p className="text-xs leading-relaxed text-slate-400">{item.answer}</p>
      <button type="button" onClick={() => void copy()} className="text-[11px] text-indigo-400 hover:underline">
        {copied ? 'Copiado ✓' : 'Copiar'}
      </button>
    </div>
  )
}
