import { z } from 'zod'
import { questionFieldSchema, type AnsweredQuestion, type QuestionField } from '../schemas/question.schema'
import { sendToActiveTab, sendToTab } from './contentScriptBridge'

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

/**
 * Rellena en la pestaña indicada los campos que coincidan con las respuestas
 * generadas. Nunca envía el formulario. Devuelve los ids que se han podido
 * rellenar (best-effort: algunos campos pueden no encontrarse si la página
 * cambió entre la detección y la respuesta).
 */
export async function fillAnswersOnTab(tabId: number, answers: AnsweredQuestion[]): Promise<string[]> {
  const response = await sendToTab(tabId, { type: 'FILL_ANSWERS', answers })
  return response?.type === 'ANSWERS_FILLED' ? response.filledIds : []
}
