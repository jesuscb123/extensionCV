import type { JobOffer } from '../../schemas/jobOffer.schema'

/**
 * Estrategia de extracción de ofertas. Cada sitio soportado implementa esta
 * interfaz; `GenericTextExtractor` actúa como fallback para el resto.
 */
export interface Extractor {
  readonly name: string
  canHandle(url: string): boolean
  extract(): JobOffer | null
}
