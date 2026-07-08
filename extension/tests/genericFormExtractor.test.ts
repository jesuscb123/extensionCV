import { describe, expect, it } from 'vitest'
import {
  resolveLabel,
  resolveFieldType,
  resolveMaxLength,
  resolveOptions,
} from '../src/content/questions/GenericFormExtractor'

describe('resolveLabel', () => {
  it('usa el label[for] asociado por id', () => {
    document.body.innerHTML = `
      <label for="email">Correo electrónico</label>
      <input id="email" type="text" />
    `
    const input = document.getElementById('email') as HTMLInputElement
    expect(resolveLabel(input)).toBe('Correo electrónico')
  })

  it('usa el label que envuelve el campo', () => {
    document.body.innerHTML = `<label>Años de experiencia <input type="text" /></label>`
    const input = document.querySelector('input') as HTMLInputElement
    expect(resolveLabel(input)).toContain('Años de experiencia')
  })

  it('usa aria-label si no hay <label>', () => {
    document.body.innerHTML = `<input type="text" aria-label="Disponibilidad" />`
    const input = document.querySelector('input') as HTMLInputElement
    expect(resolveLabel(input)).toBe('Disponibilidad')
  })

  it('usa aria-labelledby si no hay label ni aria-label', () => {
    document.body.innerHTML = `
      <span id="q1">¿Por qué quieres este puesto?</span>
      <textarea aria-labelledby="q1"></textarea>
    `
    const textarea = document.querySelector('textarea') as HTMLTextAreaElement
    expect(resolveLabel(textarea)).toBe('¿Por qué quieres este puesto?')
  })

  it('usa el placeholder como último recurso', () => {
    document.body.innerHTML = `<input type="text" placeholder="Escribe tu respuesta" />`
    const input = document.querySelector('input') as HTMLInputElement
    expect(resolveLabel(input)).toBe('Escribe tu respuesta')
  })

  it('devuelve null si no hay ninguna etiqueta resoluble', () => {
    document.body.innerHTML = `<input type="text" />`
    const input = document.querySelector('input') as HTMLInputElement
    expect(resolveLabel(input)).toBeNull()
  })
})

describe('resolveFieldType', () => {
  it('distingue text/textarea/select', () => {
    document.body.innerHTML = `
      <input type="text" />
      <textarea></textarea>
      <select></select>
    `
    const [input, textarea, select] = Array.from(document.querySelectorAll('input, textarea, select'))
    expect(resolveFieldType(input as HTMLInputElement)).toBe('text')
    expect(resolveFieldType(textarea as HTMLTextAreaElement)).toBe('textarea')
    expect(resolveFieldType(select as HTMLSelectElement)).toBe('select')
  })
})

describe('resolveMaxLength', () => {
  it('devuelve el maxlength si está definido', () => {
    document.body.innerHTML = `<textarea maxlength="200"></textarea>`
    const textarea = document.querySelector('textarea') as HTMLTextAreaElement
    expect(resolveMaxLength(textarea)).toBe(200)
  })

  it('devuelve undefined si no hay maxlength', () => {
    document.body.innerHTML = `<input type="text" />`
    const input = document.querySelector('input') as HTMLInputElement
    expect(resolveMaxLength(input)).toBeUndefined()
  })
})

describe('resolveOptions', () => {
  it('extrae el texto de las opciones visibles de un select', () => {
    document.body.innerHTML = `
      <select>
        <option value="">Selecciona una opción</option>
        <option value="si">Sí</option>
        <option value="no">No</option>
      </select>
    `
    const select = document.querySelector('select') as HTMLSelectElement
    expect(resolveOptions(select)).toEqual(['Selecciona una opción', 'Sí', 'No'])
  })

  it('devuelve undefined para campos que no son select', () => {
    document.body.innerHTML = `<input type="text" />`
    const input = document.querySelector('input') as HTMLInputElement
    expect(resolveOptions(input)).toBeUndefined()
  })
})
