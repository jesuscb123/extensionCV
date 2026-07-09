import { useState, type ChangeEvent } from 'react'
import type { UseStoredCv } from '../hooks/useStoredCv'
import { formatRelativeDate } from '../utils/formatRelativeDate'

interface CvStatusProps {
  cv: UseStoredCv
}

/**
 * Estado del CV persistido en el backend, compartido por los dos modos del
 * popup: muestra el CV actual (nombre + fecha) o, si no hay ninguno, el
 * selector para subirlo. Evita duplicar esta lógica en cada página.
 */
export function CvStatus({ cv }: CvStatusProps) {
  const [changing, setChanging] = useState(false)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string>()

  const handleFileChange = async (event: ChangeEvent<HTMLInputElement>): Promise<void> => {
    const file = event.target.files?.[0]
    if (!file) return
    setBusy(true)
    setError(undefined)
    try {
      await cv.upload(file)
      setChanging(false)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'No se pudo guardar el CV')
    } finally {
      setBusy(false)
    }
  }

  const handleRemove = async (): Promise<void> => {
    setBusy(true)
    setError(undefined)
    try {
      await cv.remove()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'No se pudo eliminar el CV')
    } finally {
      setBusy(false)
    }
  }

  if (cv.loading) {
    return <p className="text-xs text-slate-500">Comprobando CV guardado…</p>
  }

  if (cv.metadata && !changing) {
    return (
      <div className="space-y-1">
        <div className="flex items-center justify-between gap-2 rounded-md bg-slate-900 px-2.5 py-2 text-xs">
          <span className="truncate text-slate-300">
            CV: {cv.metadata.fileName} · {formatRelativeDate(cv.metadata.storedAt)}
          </span>
          <div className="flex shrink-0 gap-2">
            <button type="button" onClick={() => setChanging(true)} className="text-indigo-400 hover:underline">
              Cambiar
            </button>
            <button type="button" onClick={() => void handleRemove()} className="text-rose-400 hover:underline">
              Eliminar
            </button>
          </div>
        </div>
        {error && <p className="text-[11px] text-rose-400">{error}</p>}
      </div>
    )
  }

  return (
    <label className="block space-y-1">
      <span className="text-xs text-slate-300">CV (PDF)</span>
      <input
        type="file"
        accept="application/pdf"
        disabled={busy}
        onChange={(event) => void handleFileChange(event)}
        className="block w-full text-xs text-slate-400 file:mr-3 file:rounded-md file:border-0 file:bg-slate-800 file:px-3 file:py-1.5 file:text-slate-200"
      />
      {busy && <p className="text-[11px] text-slate-500">Guardando…</p>}
      {error && <p className="text-[11px] text-rose-400">{error}</p>}
    </label>
  )
}
