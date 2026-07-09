import { z } from 'zod'

/** Metadatos del CV almacenado en el backend (última versión subida). */
export const cvMetadataSchema = z.object({
  fileName: z.string(),
  storedAt: z.string(),
  sizeBytes: z.number(),
})

export type CvMetadata = z.infer<typeof cvMetadataSchema>
