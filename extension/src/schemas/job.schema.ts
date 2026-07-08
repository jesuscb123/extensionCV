import { z } from 'zod'

/** Estado de un job asíncrono, compartido por cualquier feature (análisis, respuestas, ...). */
export const jobStatusSchema = z.enum(['PENDING', 'PROCESSING', 'DONE', 'ERROR'])
export type JobStatus = z.infer<typeof jobStatusSchema>

/** Respuesta de la creación de un job asíncrono: `202 { jobId, status }`. */
export const jobCreatedSchema = z.object({
  jobId: z.string(),
  status: jobStatusSchema,
})

export type JobCreated = z.infer<typeof jobCreatedSchema>
