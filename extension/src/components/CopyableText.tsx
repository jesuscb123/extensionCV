import { useState } from 'react'

interface CopyableTextProps {
  text: string
}

export function CopyableText({ text }: CopyableTextProps) {
  const [copied, setCopied] = useState(false)

  const copy = async (): Promise<void> => {
    try {
      await navigator.clipboard.writeText(text)
      setCopied(true)
      window.setTimeout(() => setCopied(false), 1500)
    } catch {
      setCopied(false)
    }
  }

  return (
    <div className="space-y-1">
      <button
        type="button"
        onClick={() => void copy()}
        className="text-[11px] text-indigo-400 hover:underline"
      >
        {copied ? 'Copiado ✓' : 'Copiar'}
      </button>
      <pre className="max-h-40 overflow-auto whitespace-pre-wrap rounded-md bg-slate-900 p-2 text-[11px] leading-relaxed text-slate-300">
        {text}
      </pre>
    </div>
  )
}
