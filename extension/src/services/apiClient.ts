import { analysisJobViewSchema, type AnalysisJobView } from '../schemas/analysis.schema'
import { cvMetadataSchema, type CvMetadata } from '../schemas/cv.schema'
import { jobCreatedSchema, type JobCreated } from '../schemas/job.schema'
import type { JobOffer } from '../schemas/jobOffer.schema'
import { answerJobViewSchema, type AnswerJobView, type QuestionField } from '../schemas/question.schema'
import type { ApiError, ApiResponse } from '../types/api'

/**
 * Cliente HTTP hacia el backend Spring Boot.
 * Base URL y API key se inyectan por variables de entorno de Vite.
 */
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'
const apiKey = import.meta.env.VITE_API_KEY ?? ''
const API_VERSION_PATH = '/api/v1'

/** Error tipado que traslada el `error` del envelope de la API. */
export class ApiRequestError extends Error {
  constructor(readonly apiError: ApiError) {
    super(apiError.message)
    this.name = 'ApiRequestError'
  }
}

function url(path: string): string {
  return `${baseUrl}${API_VERSION_PATH}${path}`
}

function authHeaders(): Record<string, string> {
  return apiKey ? { 'X-Api-Key': apiKey } : {}
}

async function parseErrorBody(response: Response): Promise<ApiError> {
  let body: ApiResponse<unknown> | null = null
  try {
    body = (await response.json()) as ApiResponse<unknown>
  } catch {
    body = null
  }
  return body?.error ?? { code: 'INTERNAL_ERROR', message: `Error de red (HTTP ${response.status})` }
}

/** Lee el envelope exigiendo que `data` esté presente; lanza si falta o si `success` es falso. */
async function readEnvelope<T>(response: Response): Promise<T> {
  let body: ApiResponse<T> | null = null
  try {
    body = (await response.json()) as ApiResponse<T>
  } catch {
    body = null
  }

  if (body?.success && body.data !== null && body.data !== undefined) {
    return body.data
  }

  throw new ApiRequestError(body?.error ?? { code: 'INTERNAL_ERROR', message: `Error de red (HTTP ${response.status})` })
}

/** Como {@link readEnvelope}, pero `data: null` es un estado válido (no un error). */
async function readEnvelopeNullable<T>(response: Response): Promise<T | null> {
  let body: ApiResponse<T> | null = null
  try {
    body = (await response.json()) as ApiResponse<T>
  } catch {
    body = null
  }

  if (body?.success) {
    return body.data
  }

  throw new ApiRequestError(body?.error ?? { code: 'INTERNAL_ERROR', message: `Error de red (HTTP ${response.status})` })
}

export interface CreateAnalysisPayload {
  offer: JobOffer
  cvFile: File
  coverLetterFile: File
}

/** `POST /api/v1/analyses` — crea el job y devuelve `{ jobId, status }`. */
export async function createAnalysis({
  offer,
  cvFile,
  coverLetterFile,
}: CreateAnalysisPayload): Promise<JobCreated> {
  const form = new FormData()
  form.append('request', new Blob([JSON.stringify(offer)], { type: 'application/json' }))
  form.append('cv', cvFile)
  form.append('coverLetter', coverLetterFile)

  const response = await fetch(url('/analyses'), {
    method: 'POST',
    headers: authHeaders(),
    body: form,
  })

  const data = await readEnvelope<unknown>(response)
  return jobCreatedSchema.parse(data)
}

/** `GET /api/v1/analyses/{jobId}` — consulta estado/resultado del job. */
export async function getAnalysisJob(jobId: string): Promise<AnalysisJobView> {
  const response = await fetch(url(`/analyses/${encodeURIComponent(jobId)}`), {
    headers: authHeaders(),
  })

  const data = await readEnvelope<unknown>(response)
  return analysisJobViewSchema.parse(data)
}

export interface CreateAnswerJobPayload {
  questions: QuestionField[]
  cvFile: File
  jobOffer?: Pick<JobOffer, 'title' | 'company' | 'description'>
  language?: string
}

/** `POST /api/v1/answers` — crea el job de respuestas y devuelve `{ jobId, status }`. */
export async function createAnswerJob({
  questions,
  cvFile,
  jobOffer,
  language,
}: CreateAnswerJobPayload): Promise<JobCreated> {
  const requestBody = { questions, jobOffer: jobOffer ?? null, language: language ?? null }
  const form = new FormData()
  form.append('request', new Blob([JSON.stringify(requestBody)], { type: 'application/json' }))
  form.append('cv', cvFile)

  const response = await fetch(url('/answers'), {
    method: 'POST',
    headers: authHeaders(),
    body: form,
  })

  const data = await readEnvelope<unknown>(response)
  return jobCreatedSchema.parse(data)
}

/** `GET /api/v1/answers/{jobId}` — consulta estado/resultado del job de respuestas. */
export async function getAnswerJob(jobId: string): Promise<AnswerJobView> {
  const response = await fetch(url(`/answers/${encodeURIComponent(jobId)}`), {
    headers: authHeaders(),
  })

  const data = await readEnvelope<unknown>(response)
  return answerJobViewSchema.parse(data)
}

/** `POST /api/v1/cv` — sube y persiste el CV (sustituye al anterior). */
export async function uploadCv(file: File): Promise<CvMetadata> {
  const form = new FormData()
  form.append('cv', file)

  const response = await fetch(url('/cv'), {
    method: 'POST',
    headers: authHeaders(),
    body: form,
  })

  const data = await readEnvelope<unknown>(response)
  return cvMetadataSchema.parse(data)
}

/** `GET /api/v1/cv` — metadatos del CV almacenado, o `null` si no hay ninguno todavía. */
export async function getCvMetadata(): Promise<CvMetadata | null> {
  const response = await fetch(url('/cv'), { headers: authHeaders() })
  const data = await readEnvelopeNullable<unknown>(response)
  return data === null ? null : cvMetadataSchema.parse(data)
}

/** `GET /api/v1/cv/file` — descarga el CV almacenado como `File`. */
export async function downloadCv(fileName = 'cv.pdf'): Promise<File> {
  const response = await fetch(url('/cv/file'), { headers: authHeaders() })
  if (!response.ok) {
    throw new ApiRequestError(await parseErrorBody(response))
  }
  const blob = await response.blob()
  return new File([blob], fileName, { type: 'application/pdf' })
}

/** `DELETE /api/v1/cv` — elimina el CV almacenado. */
export async function deleteCv(): Promise<void> {
  const response = await fetch(url('/cv'), { method: 'DELETE', headers: authHeaders() })
  await readEnvelope<unknown>(response)
}
