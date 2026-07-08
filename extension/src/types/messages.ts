import type { JobOffer } from '../schemas/jobOffer.schema'
import type { QuestionField } from '../schemas/question.schema'

/** Mensajes que el popup envía al content script (vía `chrome.tabs.sendMessage`). */
export type ExtensionMessage = { type: 'EXTRACT_OFFER' } | { type: 'DETECT_QUESTIONS' }

/** Respuestas que el content script devuelve al popup. */
export type ExtensionResponse =
  | { type: 'OFFER_EXTRACTED'; offer: JobOffer }
  | { type: 'OFFER_NOT_FOUND' }
  | { type: 'QUESTIONS_DETECTED'; questions: QuestionField[] }
  | { type: 'QUESTIONS_NOT_FOUND' }

/** Mensajes que el popup envía al background service worker (vía `chrome.runtime.sendMessage`). */
export type BackgroundMessage =
  | { type: 'START_POLLING'; jobId: string }
  | { type: 'START_ANSWER_POLLING'; jobId: string }
