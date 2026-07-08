import { z } from 'zod'
import { jobStatusSchema } from './job.schema'

/** Campo de un formulario de candidatura detectado en la página (cualquier portal). */
export const questionFieldSchema = z.object({
  id: z.string(),
  label: z.string().min(1),
  fieldType: z.enum(['text', 'textarea', 'select']),
  maxLength: z.number().optional(),
  options: z.array(z.string()).optional(),
})

export type QuestionField = z.infer<typeof questionFieldSchema>

/** Pregunta ya respondida, lista para mostrarse en el popup. */
export const answeredQuestionSchema = z.object({
  id: z.string(),
  label: z.string(),
  answer: z.string(),
})

export type AnsweredQuestion = z.infer<typeof answeredQuestionSchema>

/** Vista del job que devuelve `GET /api/v1/answers/{jobId}`. */
export const answerJobViewSchema = z.object({
  jobId: z.string(),
  status: jobStatusSchema,
  answers: z.array(answeredQuestionSchema).optional(),
})

export type AnswerJobView = z.infer<typeof answerJobViewSchema>
