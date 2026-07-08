import { useState } from 'react'
import { Layout } from './Layout'
import { useAnalysis } from '../hooks/useAnalysis'
import { useJobOffer } from '../hooks/useJobOffer'
import { UploadPage } from '../pages/UploadPage'
import { LoadingPage } from '../pages/LoadingPage'
import { ResultsPage } from '../pages/ResultsPage'
import type { UploadFormValues } from '../schemas/upload.schema'

export function App() {
  const { job, loading, start, reset } = useAnalysis()
  const offerState = useJobOffer()
  const [submitError, setSubmitError] = useState<string>()

  const handleSubmit = async (values: UploadFormValues): Promise<void> => {
    if (offerState.status !== 'found') return
    setSubmitError(undefined)
    try {
      await start(offerState.offer, values.cv, values.coverLetter)
    } catch (error) {
      setSubmitError(error instanceof Error ? error.message : 'No se pudo iniciar el análisis')
    }
  }

  return <Layout>{renderBody()}</Layout>

  function renderBody() {
    if (loading) {
      return <p className="text-sm text-slate-400">Cargando…</p>
    }

    if (job) {
      if (job.status === 'DONE' && job.result) {
        return <ResultsPage result={job.result} onReset={reset} />
      }
      if (job.status === 'ERROR') {
        return (
          <div className="space-y-3">
            <p className="text-sm text-rose-400">{job.error ?? 'El análisis ha fallado.'}</p>
            <button
              type="button"
              onClick={reset}
              className="w-full rounded-md border border-slate-700 px-3 py-2 text-sm text-slate-200 transition hover:bg-slate-800"
            >
              Volver a empezar
            </button>
          </div>
        )
      }
      return <LoadingPage offer={job.offer} />
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

    return <UploadPage offer={offerState.offer} onSubmit={handleSubmit} errorMessage={submitError} />
  }
}
