import { jobOfferSchema, type JobOffer } from '../schemas/jobOffer.schema'
import { sendToActiveTab } from './contentScriptBridge'

/**
 * Pide al content script de la pestaña activa que extraiga la oferta de empleo.
 * Devuelve `null` si la página no es soportada o si la oferta extraída no
 * supera la validación.
 */
export async function requestOfferFromActiveTab(): Promise<JobOffer | null> {
  const response = await sendToActiveTab({ type: 'EXTRACT_OFFER' })
  if (!response || response.type !== 'OFFER_EXTRACTED') return null

  const parsed = jobOfferSchema.safeParse(response.offer)
  return parsed.success ? parsed.data : null
}
