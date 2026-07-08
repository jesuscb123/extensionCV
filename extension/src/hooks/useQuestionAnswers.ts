import { useCallback, useEffect, useState } from 'react'
import type { JobStatus } from '../schemas/analysis.schema'
import type { QuestionField } from '../schemas/question.schema'
import { createAnswerJob, type CreateAnswerJobPayload } from '../services/apiClient'
import { answerJobStorage, type StoredAnswerJob } from '../services/storage'
import type { BackgroundMessage } from '../types/messages'

const isTerminal = (status: JobStatus): boolean => status === 'DONE' || status === 'ERROR'

export interface UseQuestionAnswers {
  job: StoredAnswerJob | null
  loading: boolean
  start: (questions: QuestionField[], payload: Omit<CreateAnswerJobPayload, 'questions'>) => Promise<void>
  reset: () => Promise<void>
}

function startPolling(jobId: string): void {
  const message: BackgroundMessage = { type: 'START_ANSWER_POLLING', jobId }
  void chrome.runtime.sendMessage(message).catch(() => undefined)
}

async function currentTabId(): Promise<number | null> {
  const [tab] = await chrome.tabs.query({ active: true, currentWindow: true })
  return tab?.id ?? null
}

/**
 * Fuente de verdad del job de "responder preguntas" actual en el popup: rehidrata
 * desde `chrome.storage`, reacciona a las actualizaciones del service worker y
 * expone acciones para iniciar/descartar la generación de respuestas. Las
 * respuestas se autocompletan en la pestaña de origen desde el service worker
 * en cuanto el job termina (ver `answerService.pollAnswerJob`).
 */
export function useQuestionAnswers(): UseQuestionAnswers {
  const [job, setJob] = useState<StoredAnswerJob | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    let active = true

    void answerJobStorage.loadCurrentJob().then((current) => {
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
        const change = changes[answerJobStorage.jobStorageKey(previous.jobId)]
        return change?.newValue ? (change.newValue as StoredAnswerJob) : previous
      })
    }
    chrome.storage.onChanged.addListener(listener)

    return () => {
      active = false
      chrome.storage.onChanged.removeListener(listener)
    }
  }, [])

  const start = useCallback(
    async (questions: QuestionField[], payload: Omit<CreateAnswerJobPayload, 'questions'>) => {
      const tabId = await currentTabId()
      if (tabId === null) {
        throw new Error('No se ha podido identificar la pestaña activa')
      }

      const created = await createAnswerJob({ questions, ...payload })
      const stored: StoredAnswerJob = {
        jobId: created.jobId,
        status: created.status,
        questions,
        tabId,
        updatedAt: Date.now(),
      }
      await answerJobStorage.saveJob(stored)
      await answerJobStorage.setCurrentJobId(created.jobId)
      setJob(stored)
      startPolling(created.jobId)
    },
    [],
  )

  const reset = useCallback(async () => {
    await answerJobStorage.clearCurrentJob()
    setJob(null)
  }, [])

  return { job, loading, start, reset }
}
