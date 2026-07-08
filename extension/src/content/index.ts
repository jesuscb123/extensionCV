import { extractOffer } from './extractors'
import type { ExtensionMessage, ExtensionResponse } from '../types/messages'

/**
 * Content script: escucha peticiones del popup y devuelve la oferta extraída
 * de la página actual usando el extractor adecuado (con fallback genérico).
 */
chrome.runtime.onMessage.addListener(
  (message: ExtensionMessage, _sender, sendResponse: (response: ExtensionResponse) => void) => {
    if (message.type === 'EXTRACT_OFFER') {
      const offer = extractOffer(window.location.href)
      sendResponse(
        offer ? { type: 'OFFER_EXTRACTED', offer } : { type: 'OFFER_NOT_FOUND' },
      )
    }
    return true
  },
)
