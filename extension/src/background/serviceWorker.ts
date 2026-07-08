import { pollAnalysis } from '../services/analysisService'
import type { BackgroundMessage } from '../types/messages'

/**
 * Background service worker: dueño del ciclo de vida del análisis.
 * Recibe `START_POLLING` del popup y hace polling del backend hasta que el
 * job termina, persistiendo el estado/resultado en `chrome.storage` para que
 * sobreviva al cierre del popup (ciclo de vida efímero de MV3).
 */
const activePolls = new Set<string>()

chrome.runtime.onInstalled.addListener((details) => {
  console.info('[JobMatch AI] Extension installed:', details.reason)
})

chrome.runtime.onMessage.addListener((message: BackgroundMessage) => {
  if (message.type === 'START_POLLING') {
    const { jobId } = message
    if (activePolls.has(jobId)) return
    activePolls.add(jobId)
    void pollAnalysis(jobId).finally(() => activePolls.delete(jobId))
  }
})
