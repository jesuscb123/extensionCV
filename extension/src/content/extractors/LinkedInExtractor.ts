import type { Extractor } from './Extractor'
import type { JobOffer } from '../../schemas/jobOffer.schema'

const readText = (selector: string): string =>
  document.querySelector(selector)?.textContent?.trim() ?? ''

/** Extractor específico para ofertas de LinkedIn (vistas de detalle de empleo). */
export class LinkedInExtractor implements Extractor {
  readonly name = 'linkedin'

  canHandle(url: string): boolean {
    return url.includes('linkedin.com') && url.includes('/jobs/')
  }

  extract(): JobOffer | null {
    const title = readText(
      '.job-details-jobs-unified-top-card__job-title, .top-card-layout__title, h1',
    )
    const company = readText(
      '.job-details-jobs-unified-top-card__company-name, .topcard__org-name-link, .topcard__flavor',
    )
    const description = readText('.jobs-description__content, .description__text, #job-details')
    const location = readText(
      '.job-details-jobs-unified-top-card__bullet, .topcard__flavor--bullet',
    )

    if (!title || !description) return null

    return {
      title,
      company: company || 'Desconocida',
      description,
      location: location || undefined,
      sourceUrl: window.location.href,
    }
  }
}
