import { extractOffer } from './extractors'
import { detectQuestions } from './questions/GenericFormExtractor'
import type { ExtensionMessage, ExtensionResponse } from '../types/messages'

/**
 * Content script: escucha peticiones del popup y devuelve la oferta u otros
 * datos extraídos de la página actual usando el extractor adecuado.
 */
chrome.runtime.onMessage.addListener(
  (message: ExtensionMessage, _sender, sendResponse: (response: ExtensionResponse) => void) => {
    if (message.type === 'EXTRACT_OFFER') {
      const offer = extractOffer(window.location.href)
      sendResponse(
        offer ? { type: 'OFFER_EXTRACTED', offer } : { type: 'OFFER_NOT_FOUND' },
      )
    }

    if (message.type === 'DETECT_QUESTIONS') {
      const questions = detectQuestions()
      sendResponse(
        questions.length > 0
          ? { type: 'QUESTIONS_DETECTED', questions }
          : { type: 'QUESTIONS_NOT_FOUND' },
      )
    }

    return true
  },
)
