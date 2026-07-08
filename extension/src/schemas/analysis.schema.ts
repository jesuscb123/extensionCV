import { z } from 'zod'
import { jobStatusSchema } from './job.schema'

export { jobStatusSchema }
export type { JobStatus } from './job.schema'

/**
 * Resultado completo del análisis que devuelve el backend.
 * Debe mantenerse alineado con el DTO `AnalysisResult` del backend y con el
 * JSON Schema que se pasa a Groq como `response_format`.
 */
export const analysisResultSchema = z.object({
  cvScore: z.number().min(0).max(100),
  coverLetterScore: z.number().min(0).max(100),
  globalMatch: z.number().min(0).max(100),
  atsCompatibility: z.object({
    score: z.number().min(0).max(100),
    issues: z.array(z.string()),
  }),
  strengths: z.array(z.string()),
  weaknesses: z.array(z.string()),
  missingKeywords: z.array(z.string()),
  missingSkills: z.array(z.string()),
  grammarIssues: z.array(z.string()),
  formattingIssues: z.array(z.string()),
  recommendations: z.array(z.string()),
  improvedCv: z.string(),
  improvedCoverLetter: z.string(),
  meta: z.object({
    model: z.string(),
    language: z.string(),
    generatedAt: z.string(),
  }),
})

export type AnalysisResult = z.infer<typeof analysisResultSchema>

/** Vista del job que devuelve `GET /api/v1/analyses/{jobId}`. */
export const analysisJobViewSchema = z.object({
  jobId: z.string(),
  status: jobStatusSchema,
  result: analysisResultSchema.optional(),
})

export type AnalysisJobView = z.infer<typeof analysisJobViewSchema>
