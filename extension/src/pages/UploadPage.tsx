import { Controller, useForm, type Control } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import type { JobOffer } from '../schemas/jobOffer.schema'
import { uploadFormSchema, type UploadFormValues } from '../schemas/upload.schema'

interface UploadPageProps {
  offer: JobOffer
  canSubmit: boolean
  onSubmit: (values: UploadFormValues) => Promise<void>
  errorMessage?: string
}

export function UploadPage({ offer, canSubmit, onSubmit, errorMessage }: UploadPageProps) {
  const {
    control,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<UploadFormValues>({ resolver: zodResolver(uploadFormSchema) })

  return (
    <form className="space-y-4" onSubmit={handleSubmit(onSubmit)}>
      <div>
        <h2 className="text-base font-medium leading-tight">{offer.title}</h2>
        <p className="text-sm text-slate-400">{offer.company}</p>
        {offer.location && <p className="text-xs text-slate-500">{offer.location}</p>}
      </div>

      <FileField
        name="coverLetter"
        label="Carta de presentación (PDF)"
        control={control}
        error={errors.coverLetter?.message}
      />

      {!canSubmit && <p className="text-xs text-slate-500">Sube tu CV arriba para poder analizar.</p>}
      {errorMessage && <p className="text-xs text-rose-400">{errorMessage}</p>}

      <button
        type="submit"
        disabled={isSubmitting || !canSubmit}
        className="w-full rounded-md bg-indigo-600 px-3 py-2 text-sm font-medium text-white transition hover:bg-indigo-500 disabled:opacity-50"
      >
        {isSubmitting ? 'Enviando…' : 'Analizar con IA'}
      </button>
    </form>
  )
}

interface FileFieldProps {
  name: keyof UploadFormValues
  label: string
  control: Control<UploadFormValues>
  error?: string
}

function FileField({ name, label, control, error }: FileFieldProps) {
  return (
    <Controller
      name={name}
      control={control}
      render={({ field: { onChange, name: fieldName } }) => (
        <label className="block space-y-1">
          <span className="text-xs text-slate-300">{label}</span>
          <input
            type="file"
            accept="application/pdf"
            name={fieldName}
            onChange={(event) => onChange(event.target.files?.[0])}
            className="block w-full text-xs text-slate-400 file:mr-3 file:rounded-md file:border-0 file:bg-slate-800 file:px-3 file:py-1.5 file:text-slate-200"
          />
          {error && <span className="text-[11px] text-rose-400">{error}</span>}
        </label>
      )}
    />
  )
}
