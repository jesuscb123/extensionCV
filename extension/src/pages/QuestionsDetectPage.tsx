import { useEffect, useState } from 'react'
import { requestQuestionsFromActiveTab } from '../services/questionService'
import type { QuestionField } from '../schemas/question.schema'

type DetectState =
  | { status: 'loading' }
  | { status: 'found'; questions: QuestionField[] }
  | { status: 'not-found' }
  | { status: 'error'; message: string }

interface QuestionsDetectPageProps {
  canSubmit: boolean
  onSubmit: (questions: QuestionField[]) => Promise<void>
  errorMessage?: string
}

export function QuestionsDetectPage({ canSubmit, onSubmit, errorMessage }: QuestionsDetectPageProps) {
  const [state, setState] = useState<DetectState>({ status: 'loading' })
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    let active = true

    requestQuestionsFromActiveTab()
      .then((questions) => {
        if (!active) return
        setState(questions ? { status: 'found', questions } : { status: 'not-found' })
      })
      .catch((error: unknown) => {
        if (!active) return
        const message = error instanceof Error ? error.message : 'Error desconocido'
        setState({ status: 'error', message })
      })

    return () => {
      active = false
    }
  }, [])

  const handleSubmit = async (): Promise<void> => {
    if (state.status !== 'found' || !canSubmit) return
    setSubmitting(true)
    try {
      await onSubmit(state.questions)
    } finally {
      setSubmitting(false)
    }
  }

  if (state.status === 'loading') {
    return <p className="text-sm text-slate-400">Buscando preguntas del formulario…</p>
  }
  if (state.status === 'error') {
    return <p className="text-sm text-rose-400">Error al leer la página: {state.message}</p>
  }
  if (state.status === 'not-found') {
    return (
      <p className="text-sm text-slate-400">
        No se han detectado preguntas de texto en esta página. Abre el formulario de
        candidatura (por ejemplo, tras pulsar "Solicitar" o "Aplicar") y vuelve a intentarlo.
      </p>
    )
  }

  return (
    <div className="space-y-4">
      <div>
        <h2 className="text-sm font-medium text-slate-200">
          {state.questions.length} pregunta{state.questions.length === 1 ? '' : 's'} detectada
          {state.questions.length === 1 ? '' : 's'}
        </h2>
        <ul className="mt-2 space-y-1 text-xs text-slate-400">
          {state.questions.map((question) => (
            <li key={question.id} className="truncate">
              • {question.label}
            </li>
          ))}
        </ul>
      </div>

      {!canSubmit && <p className="text-xs text-slate-500">Sube tu CV arriba para poder generar respuestas.</p>}
      {errorMessage && <p className="text-xs text-rose-400">{errorMessage}</p>}

      <button
        type="button"
        onClick={() => void handleSubmit()}
        disabled={!canSubmit || submitting}
        className="w-full rounded-md bg-indigo-600 px-3 py-2 text-sm font-medium text-white transition hover:bg-indigo-500 disabled:opacity-50"
      >
        {submitting ? 'Generando…' : 'Generar respuestas'}
      </button>
    </div>
  )
}
