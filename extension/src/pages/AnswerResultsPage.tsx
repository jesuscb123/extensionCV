import type { AnsweredQuestion } from '../schemas/question.schema'
import { AnswerCard } from '../components/AnswerCard'

interface AnswerResultsPageProps {
  answers: AnsweredQuestion[]
  onReset: () => void
}

export function AnswerResultsPage({ answers, onReset }: AnswerResultsPageProps) {
  return (
    <div className="space-y-3">
      {answers.map((item) => (
        <AnswerCard key={item.id} item={item} />
      ))}
      <button
        type="button"
        onClick={onReset}
        className="w-full rounded-md border border-slate-700 px-3 py-2 text-sm text-slate-200 transition hover:bg-slate-800"
      >
        Nueva detección
      </button>
    </div>
  )
}
