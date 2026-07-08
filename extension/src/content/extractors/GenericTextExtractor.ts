import type { Extractor } from './Extractor'
import type { JobOffer } from '../../schemas/jobOffer.schema'

const MAX_DESCRIPTION_LENGTH = 12_000
const MIN_DESCRIPTION_LENGTH = 50

/**
 * Fallback genérico: extrae el título visible y el texto principal de la página.
 * Menos preciso que un extractor por sitio, pero resistente a cambios de DOM.
 */
export class GenericTextExtractor implements Extractor {
  readonly name = 'generic'

  canHandle(): boolean {
    return true
  }

  extract(): JobOffer | null {
    const title = document.querySelector('h1')?.textContent?.trim() || document.title.trim()
    const container = document.querySelector('main, article') ?? document.body
    const description = (container.textContent ?? '')
      .replace(/\s+/g, ' ')
      .trim()
      .slice(0, MAX_DESCRIPTION_LENGTH)

    if (!title || description.length < MIN_DESCRIPTION_LENGTH) return null

    return {
      title,
      company: 'Desconocida',
      description,
      sourceUrl: window.location.href,
    }
  }
}
