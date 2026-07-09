import { useCallback, useEffect, useState } from 'react'
import type { CvMetadata } from '../schemas/cv.schema'
import { deleteCv, downloadCv, getCvMetadata, uploadCv } from '../services/apiClient'

export interface UseStoredCv {
  metadata: CvMetadata | null
  loading: boolean
  upload: (file: File) => Promise<void>
  remove: () => Promise<void>
  /** Devuelve el `File` a usar: el recién subido en esta sesión, o lo descarga del backend. */
  ensureFile: () => Promise<File | null>
}

/**
 * Fuente de verdad del CV persistido en el backend: se sube una vez y se
 * reutiliza en análisis y respuestas sin tener que volver a seleccionarlo,
 * tanto en esta sesión del popup como en las siguientes.
 */
export function useStoredCv(): UseStoredCv {
  const [metadata, setMetadata] = useState<CvMetadata | null>(null)
  const [loading, setLoading] = useState(true)
  const [cachedFile, setCachedFile] = useState<File | null>(null)

  useEffect(() => {
    let active = true
    getCvMetadata()
      .then((current) => {
        if (!active) return
        setMetadata(current)
        setLoading(false)
      })
      .catch(() => {
        if (active) setLoading(false)
      })
    return () => {
      active = false
    }
  }, [])

  const upload = useCallback(async (file: File) => {
    const stored = await uploadCv(file)
    setMetadata(stored)
    setCachedFile(file)
  }, [])

  const remove = useCallback(async () => {
    await deleteCv()
    setMetadata(null)
    setCachedFile(null)
  }, [])

  const ensureFile = useCallback(async (): Promise<File | null> => {
    if (cachedFile) return cachedFile
    if (!metadata) return null
    const file = await downloadCv(metadata.fileName)
    setCachedFile(file)
    return file
  }, [cachedFile, metadata])

  return { metadata, loading, upload, remove, ensureFile }
}
