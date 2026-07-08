import type { JobOffer } from '../schemas/jobOffer.schema'
import type { AnsweredQuestion, QuestionField } from '../schemas/question.schema'

/** Mensajes que el popup/background envían al content script (vía `chrome.tabs.sendMessage`). */
export type ExtensionMessage =
  | { type: 'EXTRACT_OFFER' }
  | { type: 'DETECT_QUESTIONS' }
  | { type: 'FILL_ANSWERS'; answers: AnsweredQuestion[] }

/** Respuestas que el content script devuelve al remitente. */
export type ExtensionResponse =
  | { type: 'OFFER_EXTRACTED'; offer: JobOffer }
  | { type: 'OFFER_NOT_FOUND' }
  | { type: 'QUESTIONS_DETECTED'; questions: QuestionField[] }
  | { type: 'QUESTIONS_NOT_FOUND' }
  | { type: 'ANSWERS_FILLED'; filledIds: string[] }

/** Mensajes que el popup envía al background service worker (vía `chrome.runtime.sendMessage`). */
export type BackgroundMessage =
  | { type: 'START_POLLING'; jobId: string }
  | { type: 'START_ANSWER_POLLING'; jobId: string }
