import { Spinner } from '../components/Spinner'
import type { JobOffer } from '../schemas/jobOffer.schema'

interface LoadingPageProps {
  offer: JobOffer
}

export function LoadingPage({ offer }: LoadingPageProps) {
  return (
    <div className="flex flex-col items-center gap-3 py-10 text-center">
      <Spinner />
      <p className="text-sm text-slate-300">Analizando tu candidatura…</p>
      <p className="text-xs text-slate-500">
        {offer.title} · {offer.company}
      </p>
      <p className="text-[11px] text-slate-600">Esto puede tardar hasta 1–2 minutos.</p>
    </div>
  )
}
