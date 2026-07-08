import { z } from 'zod'

/**
 * Contrato de la oferta de empleo extraída de la página.
 * Fuente única de verdad: el tipo `JobOffer` se infiere de este esquema.
 */
export const jobOfferSchema = z.object({
  title: z.string().min(1),
  company: z.string().min(1),
  description: z.string().min(1),
  location: z.string().optional(),
  sourceUrl: z.string().url().optional(),
  language: z.string().optional(),
})

export type JobOffer = z.infer<typeof jobOfferSchema>
