import { z } from 'zod'
import { questionFieldSchema, type QuestionField } from '../schemas/question.schema'
import { sendToActiveTab } from './contentScriptBridge'

const questionsArraySchema = z.array(questionFieldSchema)

/**
 * Pide al content script de la pestaña activa que detecte las preguntas del
 * formulario de candidatura visible (cualquier portal de empleo). Devuelve
 * `null` si no se detecta ningún campo válido.
 */
export async function requestQuestionsFromActiveTab(): Promise<QuestionField[] | null> {
  const response = await sendToActiveTab({ type: 'DETECT_QUESTIONS' })
  if (!response || response.type !== 'QUESTIONS_DETECTED') return null

  const parsed = questionsArraySchema.safeParse(response.questions)
  return parsed.success && parsed.data.length > 0 ? parsed.data : null
}
