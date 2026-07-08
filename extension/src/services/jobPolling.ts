import type { JobStatus } from '../schemas/job.schema'

const POLL_INTERVAL_MS = 2000
const MAX_CONSECUTIVE_FAILURES = 5

const delay = (ms: number): Promise<void> => new Promise((resolve) => setTimeout(resolve, ms))

const isTerminal = (status: JobStatus): boolean => status === 'DONE' || status === 'ERROR'

/**
 * Hace polling de un job hasta que termina (DONE/ERROR), delegando en el
 * llamante cómo obtener la vista remota y cómo persistir cada actualización.
 * Compartido por el polling de análisis y el de respuestas.
 */
export async function pollJob<TView extends { status: JobStatus }>(
  jobId: string,
  fetchView: (jobId: string) => Promise<TView>,
  applyUpdate: (view: TView) => Promise<boolean>,
  markError: (message: string) => Promise<void>,
): Promise<void> {
  let failures = 0

  for (;;) {
    try {
      const view = await fetchView(jobId)
      failures = 0

      const stillTracked = await applyUpdate(view)
      if (!stillTracked || isTerminal(view.status)) return
    } catch (error) {
      failures += 1
      if (failures >= MAX_CONSECUTIVE_FAILURES) {
        await markError(error instanceof Error ? error.message : 'Error de comunicación con el backend')
        return
      }
    }

    await delay(POLL_INTERVAL_MS)
  }
}
