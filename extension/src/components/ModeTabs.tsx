export type AppMode = 'analysis' | 'answers'

interface ModeTabsProps {
  mode: AppMode
  onChange: (mode: AppMode) => void
}

export function ModeTabs({ mode, onChange }: ModeTabsProps) {
  return (
    <div className="flex gap-1 rounded-md bg-slate-900 p-1 text-xs">
      <button
        type="button"
        onClick={() => onChange('analysis')}
        className={`flex-1 rounded px-2 py-1.5 transition ${
          mode === 'analysis' ? 'bg-slate-800 text-slate-100' : 'text-slate-400 hover:text-slate-200'
        }`}
      >
        Analizar candidatura
      </button>
      <button
        type="button"
        onClick={() => onChange('answers')}
        className={`flex-1 rounded px-2 py-1.5 transition ${
          mode === 'answers' ? 'bg-slate-800 text-slate-100' : 'text-slate-400 hover:text-slate-200'
        }`}
      >
        Responder preguntas
      </button>
    </div>
  )
}
