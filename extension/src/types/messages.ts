import type { JobOffer } from '../schemas/jobOffer.schema'

/** Mensajes que el popup envía al content script (vía `chrome.tabs.sendMessage`). */
export type ExtensionMessage = { type: 'EXTRACT_OFFER' }

/** Respuestas que el content script devuelve al popup. */
export type ExtensionResponse =
  | { type: 'OFFER_EXTRACTED'; offer: JobOffer }
  | { type: 'OFFER_NOT_FOUND' }

/** Mensajes que el popup envía al background service worker (vía `chrome.runtime.sendMessage`). */
export type BackgroundMessage = { type: 'START_POLLING'; jobId: string }
