import type { ExtensionMessage, ExtensionResponse } from '../types/messages'

/**
 * Inyecta el content script en la pestaña si aún no está presente. Necesario
 * para pestañas que ya estaban abiertas antes de instalar/recargar la
 * extensión (Chrome no inyecta content scripts retroactivamente), y para
 * portales de empleo no listados en `content_scripts.matches`: el permiso
 * `activeTab` concedido al abrir el popup permite inyectar en la pestaña
 * activa sea cual sea su dominio.
 */
async function ensureContentScriptInjected(tabId: number): Promise<void> {
  const files = chrome.runtime.getManifest().content_scripts?.[0]?.js
  if (!files || files.length === 0) return
  await chrome.scripting.executeScript({ target: { tabId }, files }).catch(() => undefined)
}

async function sendMessage(tabId: number, message: ExtensionMessage): Promise<ExtensionResponse | null> {
  return (await chrome.tabs.sendMessage(tabId, message).catch(() => null)) as ExtensionResponse | null
}

/**
 * Envía un mensaje al content script de una pestaña concreta, inyectándolo
 * primero si todavía no responde. Devuelve `null` si el content script no
 * llega a responder tras la inyección (p. ej. la pestaña se cerró o navegó).
 */
export async function sendToTab(tabId: number, message: ExtensionMessage): Promise<ExtensionResponse | null> {
  let response = await sendMessage(tabId, message)
  if (!response) {
    await ensureContentScriptInjected(tabId)
    response = await sendMessage(tabId, message)
  }
  return response
}

/**
 * Envía un mensaje al content script de la pestaña activa. Devuelve `null` si
 * no hay pestaña activa.
 */
export async function sendToActiveTab(message: ExtensionMessage): Promise<ExtensionResponse | null> {
  const [tab] = await chrome.tabs.query({ active: true, currentWindow: true })
  if (!tab?.id) return null
  return sendToTab(tab.id, message)
}
