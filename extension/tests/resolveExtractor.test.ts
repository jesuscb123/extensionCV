import { describe, expect, it } from 'vitest'
import { resolveExtractor } from '../src/content/extractors'

describe('resolveExtractor', () => {
  it('selecciona el extractor de LinkedIn en una URL de oferta de LinkedIn', () => {
    const extractor = resolveExtractor('https://www.linkedin.com/jobs/view/123456')
    expect(extractor.name).toBe('linkedin')
  })

  it('usa el extractor genérico fuera de los sitios soportados', () => {
    const extractor = resolveExtractor('https://example.com/careers/42')
    expect(extractor.name).toBe('generic')
  })

  it('usa el extractor genérico en LinkedIn fuera de la vista de empleo', () => {
    const extractor = resolveExtractor('https://www.linkedin.com/feed/')
    expect(extractor.name).toBe('generic')
  })
})
