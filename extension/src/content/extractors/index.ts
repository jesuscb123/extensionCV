import type { Extractor } from './Extractor'
import type { JobOffer } from '../../schemas/jobOffer.schema'
import { LinkedInExtractor } from './LinkedInExtractor'
import { GenericTextExtractor } from './GenericTextExtractor'

const genericExtractor = new GenericTextExtractor()

/** Extractores por sitio (más específicos primero) + fallback genérico. */
const extractors: Extractor[] = [new LinkedInExtractor(), genericExtractor]

export function resolveExtractor(url: string): Extractor {
  return extractors.find((extractor) => extractor.canHandle(url)) ?? genericExtractor
}

/**
 * Extrae la oferta con el extractor del sitio y, si este no encuentra nada
 * (p. ej. por un cambio de DOM), reintenta con el extractor genérico.
 */
export function extractOffer(url: string): JobOffer | null {
  const extractor = resolveExtractor(url)
  const offer = extractor.extract()
  if (offer) return offer
  return extractor.name === genericExtractor.name ? null : genericExtractor.extract()
}
