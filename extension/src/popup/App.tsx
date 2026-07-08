import { useState } from 'react'
import { Layout } from './Layout'
import { useAnalysis } from '../hooks/useAnalysis'
import { useJobOffer } from '../hooks/useJobOffer'
import { useQuestionAnswers } from '../hooks/useQuestionAnswers'
import { UploadPage } from '../pages/UploadPage'
import { LoadingPage } from '../pages/LoadingPage'
import { ResultsPage } from '../pages/ResultsPage'
import { QuestionsDetectPage } from '../pages/QuestionsDetectPage'
import { AnswerResultsPage } from '../pages/AnswerResultsPage'
import { ModeTabs, type AppMode } from '../components/ModeTabs'
import type { UploadFormValues } from '../schemas/upload.schema'
import type { QuestionField } from '../schemas/question.schema'

export function App() {
  const [mode, setMode] = useState<AppMode>('analysis')
  const [cvFile, setCvFile] = useState<File | null>(null)

  const analysis = useAnalysis()
  const answers = useQuestionAnswers()
  const offerState = useJobOffer()

  const [submitError, setSubmitError] = useState<string>()
  const [answerSubmitError, setAnswerSubmitError] = useState<string>()

  const handleAnalysisSubmit = async (values: UploadFormValues): Promise<void> => {
    if (offerState.status !== 'found') return
    setSubmitError(undefined)
    setCvFile(values.cv)
    try {
      await analysis.start(offerState.offer, values.cv, values.coverLetter)
    } catch (error) {
      setSubmitError(error instanceof Error ? error.message : 'No se pudo iniciar el análisis')
    }
  }

  const handleQuestionsSubmit = async (questions: QuestionField[], cv: File): Promise<void> => {
    setCvFile(cv)
    setAnswerSubmitError(undefined)
    try {
      await answers.start(questions, {
        cvFile: cv,
        jobOffer:
          offerState.status === 'found'
            ? {
                title: offerState.offer.title,
                company: offerState.offer.company,
                description: offerState.offer.description,
              }
            : undefined,
      })
    } catch (error) {
      setAnswerSubmitError(
        error instanceof Error ? error.message : 'No se pudo iniciar la generación de respuestas',
      )
    }
  }

  return (
    <Layout>
      <div className="mb-4">
        <ModeTabs mode={mode} onChange={setMode} />
      </div>
      {mode === 'analysis' ? renderAnalysis() : renderAnswers()}
    </Layout>
  )

  function renderAnalysis() {
    if (analysis.loading) {
      return <p className="text-sm text-slate-400">Cargando…</p>
    }

    if (analysis.job) {
      if (analysis.job.status === 'DONE' && analysis.job.result) {
        return <ResultsPage result={analysis.job.result} onReset={analysis.reset} />
      }
      if (analysis.job.status === 'ERROR') {
        return renderJobError(analysis.job.error, analysis.reset)
      }
      return (
        <LoadingPage
          message="Analizando tu candidatura…"
          subtitle={`${analysis.job.offer.title} · ${analysis.job.offer.company}`}
        />
      )
    }

    if (offerState.status === 'loading') {
      return <p className="text-sm text-slate-400">Detectando oferta…</p>
    }
    if (offerState.status === 'error') {
      return <p className="text-sm text-rose-400">Error al leer la página: {offerState.message}</p>
    }
    if (offerState.status === 'not-found') {
      return (
        <p className="text-sm text-slate-400">
          No se ha detectado ninguna oferta en esta página. Abre una oferta de empleo (por
          ejemplo, en LinkedIn) y vuelve a intentarlo.
        </p>
      )
    }

    return (
      <UploadPage offer={offerState.offer} onSubmit={handleAnalysisSubmit} errorMessage={submitError} />
    )
  }

  function renderAnswers() {
    if (answers.loading) {
      return <p className="text-sm text-slate-400">Cargando…</p>
    }

    if (answers.job) {
      if (answers.job.status === 'DONE' && answers.job.answers) {
        return <AnswerResultsPage answers={answers.job.answers} onReset={answers.reset} />
      }
      if (answers.job.status === 'ERROR') {
        return renderJobError(answers.job.error, answers.reset)
      }
      return <LoadingPage message="Generando y rellenando las respuestas…" />
    }

    return (
      <QuestionsDetectPage
        cvFile={cvFile}
        onCvSelected={setCvFile}
        onSubmit={handleQuestionsSubmit}
        errorMessage={answerSubmitError}
      />
    )
  }

  function renderJobError(message: string | undefined, onReset: () => void) {
    return (
      <div className="space-y-3">
        <p className="text-sm text-rose-400">{message ?? 'Ha ocurrido un error.'}</p>
        <button
          type="button"
          onClick={onReset}
          className="w-full rounded-md border border-slate-700 px-3 py-2 text-sm text-slate-200 transition hover:bg-slate-800"
        >
          Volver a empezar
        </button>
      </div>
    )
  }
}
