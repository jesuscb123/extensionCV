import type { JobOffer } from '../schemas/jobOffer.schema'
import type { AnalysisResult, JobStatus } from '../schemas/analysis.schema'
import type { AnsweredQuestion, QuestionField } from '../schemas/question.schema'

/** Estado persistido de un análisis en curso o terminado. */
export interface StoredAnalysis {
  jobId: string
  status: JobStatus
  offer: JobOffer
  result?: AnalysisResult
  error?: string
  updatedAt: number
}

/** Estado persistido de un job de "responder preguntas" en curso o terminado. */
export interface StoredAnswerJob {
  jobId: string
  status: JobStatus
  questions: QuestionField[]
  answers?: AnsweredQuestion[]
  error?: string
  updatedAt: number
}

/**
 * Crea el conjunto de operaciones de `chrome.storage` para un tipo de job dado
 * (análisis, respuestas, ...), evitando duplicar la misma lógica por feature.
 */
function createJobStorage<T extends { jobId: string }>(keyPrefix: string, currentKey: string) {
  const jobStorageKey = (jobId: string): string => `${keyPrefix}:${jobId}`

  const saveJob = async (job: T): Promise<void> => {
    await chrome.storage.local.set({ [jobStorageKey(job.jobId)]: job })
  }

  const loadJob = async (jobId: string): Promise<T | null> => {
    const key = jobStorageKey(jobId)
    const stored = await chrome.storage.local.get(key)
    return (stored[key] as T | undefined) ?? null
  }

  const setCurrentJobId = async (jobId: string): Promise<void> => {
    await chrome.storage.local.set({ [currentKey]: jobId })
  }

  const getCurrentJobId = async (): Promise<string | null> => {
    const stored = await chrome.storage.local.get(currentKey)
    return (stored[currentKey] as string | undefined) ?? null
  }

  const loadCurrentJob = async (): Promise<T | null> => {
    const jobId = await getCurrentJobId()
    return jobId ? loadJob(jobId) : null
  }

  const clearCurrentJob = async (): Promise<void> => {
    await chrome.storage.local.remove(currentKey)
  }

  return { jobStorageKey, saveJob, loadJob, setCurrentJobId, getCurrentJobId, loadCurrentJob, clearCurrentJob }
}

export const analysisJobStorage = createJobStorage<StoredAnalysis>('job', 'currentJobId')
export const answerJobStorage = createJobStorage<StoredAnswerJob>('answerJob', 'currentAnswerJobId')
