import { z } from 'zod'

export const MAX_FILE_SIZE = 5 * 1024 * 1024 // 5 MB

const pdfFileSchema = z
  .instanceof(File, { message: 'Selecciona un archivo' })
  .refine((file) => file.type === 'application/pdf', 'El archivo debe ser un PDF')
  .refine((file) => file.size > 0, 'El archivo está vacío')
  .refine((file) => file.size <= MAX_FILE_SIZE, 'El archivo supera los 5 MB')

/** Formulario de subida del popup: CV + carta de presentación en PDF. */
export const uploadFormSchema = z.object({
  cv: pdfFileSchema,
  coverLetter: pdfFileSchema,
})

export type UploadFormValues = z.infer<typeof uploadFormSchema>
