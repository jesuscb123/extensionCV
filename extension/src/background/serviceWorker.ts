import { pollAnalysis } from '../services/analysisService'
import { pollAnswerJob } from '../services/answerService'
import type { BackgroundMessage } from '../types/messages'

/**
 * Background service worker: dueño del ciclo de vida de los jobs asíncronos
 * (análisis y respuestas). Recibe la orden de arrancar el polling del popup y
 * lo hace hasta que el job termina, persistiendo el estado/resultado en
 * `chrome.storage` para que sobreviva al cierre del popup (ciclo de vida
 * efímero de MV3).
 */
const activePolls = new Set<string>()

function runOnce(jobId: string, poll: (jobId: string) => Promise<void>): void {
  if (activePolls.has(jobId)) return
  activePolls.add(jobId)
  void poll(jobId).finally(() => activePolls.delete(jobId))
}

chrome.runtime.onInstalled.addListener((details) => {
  console.info('[JobMatch AI] Extension installed:', details.reason)
})

chrome.runtime.onMessage.addListener((message: BackgroundMessage) => {
  if (message.type === 'START_POLLING') {
    runOnce(message.jobId, pollAnalysis)
  }
  if (message.type === 'START_ANSWER_POLLING') {
    runOnce(message.jobId, pollAnswerJob)
  }
})
