import { useCallback, useEffect, useState } from 'react'
import type { JobOffer } from '../schemas/jobOffer.schema'
import type { JobStatus } from '../schemas/analysis.schema'
import { createAnalysis } from '../services/apiClient'
import {
  clearCurrentJob,
  jobStorageKey,
  loadCurrentJob,
  saveJob,
  setCurrentJobId,
  type StoredAnalysis,
} from '../services/storage'
import type { BackgroundMessage } from '../types/messages'

const isTerminal = (status: JobStatus): boolean => status === 'DONE' || status === 'ERROR'

export interface UseAnalysis {
  job: StoredAnalysis | null
  loading: boolean
  start: (offer: JobOffer, cvFile: File, coverLetterFile: File) => Promise<void>
  reset: () => Promise<void>
}

function startPolling(jobId: string): void {
  const message: BackgroundMessage = { type: 'START_POLLING', jobId }
  void chrome.runtime.sendMessage(message).catch(() => undefined)
}

/**
 * Fuente de verdad del análisis actual en el popup: rehidrata desde
 * `chrome.storage`, reacciona a las actualizaciones del service worker y expone
 * acciones para iniciar/descartar un análisis.
 */
export function useAnalysis(): UseAnalysis {
  const [job, setJob] = useState<StoredAnalysis | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    let active = true

    void loadCurrentJob().then((current) => {
      if (!active) return
      setJob(current)
      setLoading(false)
      if (current && !isTerminal(current.status)) startPolling(current.jobId)
    })

    const listener = (
      changes: Record<string, chrome.storage.StorageChange>,
      area: chrome.storage.AreaName,
    ): void => {
      if (area !== 'local') return
      setJob((previous) => {
        if (!previous) return previous
        const change = changes[jobStorageKey(previous.jobId)]
        return change?.newValue ? (change.newValue as StoredAnalysis) : previous
      })
    }
    chrome.storage.onChanged.addListener(listener)

    return () => {
      active = false
      chrome.storage.onChanged.removeListener(listener)
    }
  }, [])

  const start = useCallback(
    async (offer: JobOffer, cvFile: File, coverLetterFile: File) => {
      const created = await createAnalysis({ offer, cvFile, coverLetterFile })
      const stored: StoredAnalysis = {
        jobId: created.jobId,
        status: created.status,
        offer,
        updatedAt: Date.now(),
      }
      await saveJob(stored)
      await setCurrentJobId(created.jobId)
      setJob(stored)
      startPolling(created.jobId)
    },
    [],
  )

  const reset = useCallback(async () => {
    await clearCurrentJob()
    setJob(null)
  }, [])

  return { job, loading, start, reset }
}
