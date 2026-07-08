import type { ReactNode } from 'react'

interface LayoutProps {
  children: ReactNode
}

export function Layout({ children }: LayoutProps) {
  return (
    <div className="flex min-h-[480px] max-h-[600px] w-[380px] flex-col bg-slate-950 text-slate-100">
      <header className="border-b border-slate-800 px-5 py-4">
        <h1 className="text-lg font-semibold tracking-tight">
          JobMatch <span className="text-indigo-400">AI</span>
        </h1>
        <p className="text-xs text-slate-400">Adapta tu CV y carta a la oferta con IA</p>
      </header>

      <main className="flex-1 overflow-y-auto px-5 py-4">{children}</main>

      <footer className="border-t border-slate-800 px-5 py-3 text-center text-[11px] text-slate-500">
        MVP · JobMatch AI
      </footer>
    </div>
  )
}
