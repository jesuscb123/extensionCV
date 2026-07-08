import { getAnalysisJob } from './apiClient'
import { pollJob } from './jobPolling'
import { analysisJobStorage } from './storage'

/**
 * Hace polling del análisis hasta que termina (DONE/ERROR) y persiste cada
 * actualización en `chrome.storage`. Pensado para ejecutarse en el service
 * worker, de modo que el análisis sobreviva al cierre del popup.
 */
export async function pollAnalysis(jobId: string): Promise<void> {
  await pollJob(
    jobId,
    getAnalysisJob,
    async (view) => {
      const existing = await analysisJobStorage.loadJob(jobId)
      if (!existing) return false // el usuario descartó el análisis
      await analysisJobStorage.saveJob({
        ...existing,
        status: view.status,
        result: view.result ?? existing.result,
        updatedAt: Date.now(),
      })
      return true
    },
    async (message) => {
      const existing = await analysisJobStorage.loadJob(jobId)
      if (!existing) return
      await analysisJobStorage.saveJob({ ...existing, status: 'ERROR', error: message, updatedAt: Date.now() })
    },
  )
}
