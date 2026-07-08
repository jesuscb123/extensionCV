interface ScoreBarProps {
  label: string
  score: number
}

export function ScoreBar({ label, score }: ScoreBarProps) {
  const clamped = Math.max(0, Math.min(100, Math.round(score)))
  const color =
    clamped >= 75 ? 'bg-emerald-500' : clamped >= 50 ? 'bg-amber-500' : 'bg-rose-500'

  return (
    <div className="space-y-1">
      <div className="flex items-center justify-between text-xs">
        <span className="text-slate-300">{label}</span>
        <span className="font-medium text-slate-100">{clamped}</span>
      </div>
      <div className="h-1.5 w-full overflow-hidden rounded-full bg-slate-800">
        <div className={`h-full ${color}`} style={{ width: `${clamped}%` }} />
      </div>
    </div>
  )
}
