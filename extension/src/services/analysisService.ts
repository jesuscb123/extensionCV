import { getAnalysisJob } from './apiClient'
import { loadJob, saveJob, type StoredAnalysis } from './storage'
import type { JobStatus } from '../schemas/analysis.schema'

const POLL_INTERVAL_MS = 2000
const MAX_CONSECUTIVE_FAILURES = 5

const delay = (ms: number): Promise<void> => new Promise((resolve) => setTimeout(resolve, ms))

const isTerminal = (status: JobStatus): boolean => status === 'DONE' || status === 'ERROR'

/**
 * Hace polling del job hasta que termina (DONE/ERROR) y persiste cada
 * actualización en `chrome.storage`. Pensado para ejecutarse en el service
 * worker, de modo que el análisis sobreviva al cierre del popup.
 */
export async function pollAnalysis(jobId: string): Promise<void> {
  let failures = 0

  for (;;) {
    try {
      const view = await getAnalysisJob(jobId)
      failures = 0

      const existing = await loadJob(jobId)
      if (!existing) return // el usuario descartó el análisis

      const updated: StoredAnalysis = {
        ...existing,
        status: view.status,
        result: view.result ?? existing.result,
        updatedAt: Date.now(),
      }
      await saveJob(updated)

      if (isTerminal(view.status)) return
    } catch (error) {
      failures += 1
      if (failures >= MAX_CONSECUTIVE_FAILURES) {
        await markAsError(jobId, error)
        return
      }
    }

    await delay(POLL_INTERVAL_MS)
  }
}

async function markAsError(jobId: string, error: unknown): Promise<void> {
  const existing = await loadJob(jobId)
  if (!existing) return
  await saveJob({
    ...existing,
    status: 'ERROR',
    error: error instanceof Error ? error.message : 'Error de comunicación con el backend',
    updatedAt: Date.now(),
  })
}
