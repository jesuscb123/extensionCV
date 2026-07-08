import { useEffect, useState } from 'react'
import type { JobOffer } from '../schemas/jobOffer.schema'
import { requestOfferFromActiveTab } from '../services/offerService'

export type JobOfferState =
  | { status: 'loading' }
  | { status: 'found'; offer: JobOffer }
  | { status: 'not-found' }
  | { status: 'error'; message: string }

/** Detecta y expone la oferta de empleo de la pestaña activa al abrir el popup. */
export function useJobOffer(): JobOfferState {
  const [state, setState] = useState<JobOfferState>({ status: 'loading' })

  useEffect(() => {
    let active = true

    requestOfferFromActiveTab()
      .then((offer) => {
        if (!active) return
        setState(offer ? { status: 'found', offer } : { status: 'not-found' })
      })
      .catch((error: unknown) => {
        if (!active) return
        const message = error instanceof Error ? error.message : 'Error desconocido'
        setState({ status: 'error', message })
      })

    return () => {
      active = false
    }
  }, [])

  return state
}
