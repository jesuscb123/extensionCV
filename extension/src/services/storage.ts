import type { JobOffer } from '../schemas/jobOffer.schema'
import type { AnalysisResult, JobStatus } from '../schemas/analysis.schema'

/** Estado persistido de un análisis en curso o terminado. */
export interface StoredAnalysis {
  jobId: string
  status: JobStatus
  offer: JobOffer
  result?: AnalysisResult
  error?: string
  updatedAt: number
}

const CURRENT_JOB_KEY = 'currentJobId'

export function jobStorageKey(jobId: string): string {
  return `job:${jobId}`
}

export async function saveJob(job: StoredAnalysis): Promise<void> {
  await chrome.storage.local.set({ [jobStorageKey(job.jobId)]: job })
}

export async function loadJob(jobId: string): Promise<StoredAnalysis | null> {
  const key = jobStorageKey(jobId)
  const stored = await chrome.storage.local.get(key)
  return (stored[key] as StoredAnalysis | undefined) ?? null
}

export async function setCurrentJobId(jobId: string): Promise<void> {
  await chrome.storage.local.set({ [CURRENT_JOB_KEY]: jobId })
}

export async function getCurrentJobId(): Promise<string | null> {
  const stored = await chrome.storage.local.get(CURRENT_JOB_KEY)
  return (stored[CURRENT_JOB_KEY] as string | undefined) ?? null
}

export async function loadCurrentJob(): Promise<StoredAnalysis | null> {
  const jobId = await getCurrentJobId()
  return jobId ? loadJob(jobId) : null
}

export async function clearCurrentJob(): Promise<void> {
  await chrome.storage.local.remove(CURRENT_JOB_KEY)
}
