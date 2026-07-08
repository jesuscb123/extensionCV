import { Spinner } from '../components/Spinner'

interface LoadingPageProps {
  message: string
  subtitle?: string
}

export function LoadingPage({ message, subtitle }: LoadingPageProps) {
  return (
    <div className="flex flex-col items-center gap-3 py-10 text-center">
      <Spinner />
      <p className="text-sm text-slate-300">{message}</p>
      {subtitle && <p className="text-xs text-slate-500">{subtitle}</p>}
      <p className="text-[11px] text-slate-600">Esto puede tardar hasta 1–2 minutos.</p>
    </div>
  )
}
