import { jobOfferSchema, type JobOffer } from '../schemas/jobOffer.schema'
import type { ExtensionResponse } from '../types/messages'

/**
 * Inyecta el content script en la pestaña si aún no está presente. Necesario
 * para pestañas que ya estaban abiertas antes de instalar/recargar la
 * extensión, ya que Chrome no inyecta content scripts retroactivamente.
 */
async function ensureContentScriptInjected(tabId: number): Promise<void> {
  const files = chrome.runtime.getManifest().content_scripts?.[0]?.js
  if (!files || files.length === 0) return
  await chrome.scripting.executeScript({ target: { tabId }, files }).catch(() => undefined)
}

async function sendExtractMessage(tabId: number): Promise<ExtensionResponse | null> {
  return (await chrome.tabs
    .sendMessage(tabId, { type: 'EXTRACT_OFFER' })
    .catch(() => null)) as ExtensionResponse | null
}

/**
 * Pide al content script de la pestaña activa que extraiga la oferta de empleo.
 * Devuelve `null` si la página no es soportada o si la oferta extraída no
 * supera la validación.
 */
export async function requestOfferFromActiveTab(): Promise<JobOffer | null> {
  const [tab] = await chrome.tabs.query({ active: true, currentWindow: true })
  if (!tab?.id) return null

  let response = await sendExtractMessage(tab.id)
  if (!response) {
    await ensureContentScriptInjected(tab.id)
    response = await sendExtractMessage(tab.id)
  }

  if (!response || response.type !== 'OFFER_EXTRACTED') return null

  const parsed = jobOfferSchema.safeParse(response.offer)
  return parsed.success ? parsed.data : null
}
