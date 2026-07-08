import type { QuestionField } from '../../schemas/question.schema'

const MAX_QUESTIONS = 30
const MAX_LABEL_LENGTH = 300

const EXCLUDED_INPUT_TYPES = new Set([
  'hidden',
  'password',
  'submit',
  'button',
  'reset',
  'image',
  'file',
  'checkbox',
  'radio',
  'color',
  'range',
  'date',
  'month',
  'week',
  'time',
  'datetime-local',
  'search',
])

type Candidate = HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement

function isVisible(element: Element): boolean {
  return (element as HTMLElement).offsetParent !== null
}

function isEligible(element: Candidate): boolean {
  if (element.disabled || (element as HTMLInputElement).readOnly) return false
  if (!isVisible(element)) return false
  if (element instanceof HTMLInputElement) {
    const type = (element.type || 'text').toLowerCase()
    return !EXCLUDED_INPUT_TYPES.has(type)
  }
  return true
}

function escapeCssIdentifier(value: string): string {
  if (typeof CSS !== 'undefined' && typeof CSS.escape === 'function') {
    return CSS.escape(value)
  }
  return value.replace(/[^a-zA-Z0-9_-]/g, (char) => `\\${char}`)
}

function textFromLabelledBy(element: Element): string | null {
  const ids = element.getAttribute('aria-labelledby')
  if (!ids) return null
  const text = ids
    .split(/\s+/)
    .map((id) => document.getElementById(id)?.textContent?.trim() ?? '')
    .filter(Boolean)
    .join(' ')
  return text || null
}

function textFromFieldsetLegend(element: Element): string | null {
  const fieldset = element.closest('fieldset')
  const legend = fieldset?.querySelector('legend')
  return legend?.textContent?.trim() || null
}

export function resolveLabel(element: Candidate): string | null {
  const id = element.getAttribute('id')
  if (id) {
    const forLabel = document.querySelector(`label[for="${escapeCssIdentifier(id)}"]`)
    const text = forLabel?.textContent?.trim()
    if (text) return text
  }

  const wrappingLabel = element.closest('label')
  const wrappingText = wrappingLabel?.textContent?.trim()
  if (wrappingText) return wrappingText

  const ariaLabel = element.getAttribute('aria-label')?.trim()
  if (ariaLabel) return ariaLabel

  const labelledBy = textFromLabelledBy(element)
  if (labelledBy) return labelledBy

  const legend = textFromFieldsetLegend(element)
  if (legend) return legend

  const placeholder = (element as HTMLInputElement).placeholder?.trim()
  if (placeholder) return placeholder

  return null
}

export function resolveFieldType(element: Candidate): QuestionField['fieldType'] {
  if (element instanceof HTMLTextAreaElement) return 'textarea'
  if (element instanceof HTMLSelectElement) return 'select'
  return 'text'
}

export function resolveMaxLength(element: Candidate): number | undefined {
  if (element instanceof HTMLSelectElement) return undefined
  const maxLength = element.maxLength
  return maxLength && maxLength > 0 ? maxLength : undefined
}

export function resolveOptions(element: Candidate): string[] | undefined {
  if (!(element instanceof HTMLSelectElement)) return undefined
  const options = Array.from(element.options)
    .map((option) => option.textContent?.trim() ?? '')
    .filter((text) => text.length > 0)
  return options.length > 0 ? options : undefined
}

/**
 * Recuerda qué elemento del DOM corresponde a cada id detectado, para poder
 * rellenarlo más tarde (`fillFields`) sin tener que volver a resolverlo.
 * Vive en memoria del content script: sobrevive mientras la pestaña no navegue.
 */
const fieldRegistry = new Map<string, Candidate>()

/**
 * Detecta campos de texto/textarea/select con una etiqueta resoluble en cualquier
 * página (formularios de candidatura de cualquier portal de empleo, no solo LinkedIn).
 */
export function detectQuestions(): QuestionField[] {
  const candidates = Array.from(
    document.querySelectorAll<Candidate>('input, textarea, select'),
  ).filter(isEligible)

  const questions: QuestionField[] = []
  fieldRegistry.clear()

  for (const [index, element] of candidates.entries()) {
    const label = resolveLabel(element)
    if (!label) continue

    const id = element.id || `field-${index}`
    fieldRegistry.set(id, element)

    questions.push({
      id,
      label: label.slice(0, MAX_LABEL_LENGTH),
      fieldType: resolveFieldType(element),
      maxLength: resolveMaxLength(element),
      options: resolveOptions(element),
    })

    if (questions.length >= MAX_QUESTIONS) break
  }

  return questions
}

/**
 * Ajusta el valor de un input/textarea pasando por el setter nativo del prototipo.
 * Necesario porque frameworks como React sobrescriben el setter de `.value` en la
 * instancia para su propio seguimiento interno; escribir a través del prototipo
 * y disparar un evento `input` real es lo que hace que detecten el cambio.
 */
function setNativeValue(element: HTMLInputElement | HTMLTextAreaElement, value: string): void {
  const prototype = element instanceof HTMLTextAreaElement ? HTMLTextAreaElement.prototype : HTMLInputElement.prototype
  Object.getOwnPropertyDescriptor(prototype, 'value')?.set?.call(element, value)
}

function fillField(element: Candidate, answer: string): boolean {
  const trimmed = answer.trim()
  if (!trimmed) return false

  if (element instanceof HTMLSelectElement) {
    const normalized = trimmed.toLowerCase()
    const match = Array.from(element.options).find(
      (option) => option.textContent?.trim().toLowerCase() === normalized || option.value.toLowerCase() === normalized,
    )
    if (!match) return false
    element.value = match.value
  } else {
    setNativeValue(element, trimmed)
  }

  element.dispatchEvent(new Event('input', { bubbles: true }))
  element.dispatchEvent(new Event('change', { bubbles: true }))
  return true
}

/**
 * Rellena en la página los campos detectados previamente con las respuestas
 * generadas por la IA. Nunca envía el formulario: solo escribe los valores.
 * Devuelve los ids que se han podido rellenar (los demás quedan disponibles
 * para copiar manualmente desde el popup).
 */
export function fillFields(answers: { id: string; answer: string }[]): string[] {
  const filledIds: string[] = []

  for (const { id, answer } of answers) {
    const element = fieldRegistry.get(id)
    if (element && fillField(element, answer)) {
      filledIds.push(id)
    }
  }

  return filledIds
}
