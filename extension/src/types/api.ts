/** Códigos de error estables del envelope de la API (ver backend). */
export type ApiErrorCode =
  | 'VALIDATION_ERROR'
  | 'UNAUTHORIZED'
  | 'PAYLOAD_TOO_LARGE'
  | 'UNSUPPORTED_MEDIA_TYPE'
  | 'PDF_UNPROCESSABLE'
  | 'RATE_LIMITED'
  | 'JOB_NOT_FOUND'
  | 'AI_PROVIDER_ERROR'
  | 'INTERNAL_ERROR'

export interface ApiError {
  code: ApiErrorCode
  message: string
  details?: { field: string; issue: string }[]
}

/** Envelope consistente de todas las respuestas del backend. */
export interface ApiResponse<T> {
  success: boolean
  data: T | null
  error: ApiError | null
}
