import { getAnswerJob } from './apiClient'
import { pollJob } from './jobPolling'
import { fillAnswersOnTab } from './questionService'
import { answerJobStorage } from './storage'

/**
 * Hace polling del job de "responder preguntas" hasta que termina (DONE/ERROR)
 * y persiste cada actualización en `chrome.storage`, igual que el polling de
 * análisis, para que sobreviva al cierre del popup. En cuanto llegan las
 * respuestas, las autocompleta en la pestaña de origen sin intervención del
 * usuario (nunca envía el formulario, solo rellena los campos).
 */
export async function pollAnswerJob(jobId: string): Promise<void> {
  await pollJob(
    jobId,
    getAnswerJob,
    async (view) => {
      const existing = await answerJobStorage.loadJob(jobId)
      if (!existing) return false
      await answerJobStorage.saveJob({
        ...existing,
        status: view.status,
        answers: view.answers ?? existing.answers,
        updatedAt: Date.now(),
      })

      if (view.status === 'DONE' && view.answers) {
        await fillAnswersOnTab(existing.tabId, view.answers)
      }
      return true
    },
    async (message) => {
      const existing = await answerJobStorage.loadJob(jobId)
      if (!existing) return
      await answerJobStorage.saveJob({ ...existing, status: 'ERROR', error: message, updatedAt: Date.now() })
    },
  )
}
