# JobMatch AI — Extensión (Chrome MV3)

Extensión de Chrome que detecta la oferta de empleo de la pestaña activa, permite subir
CV y carta de presentación en PDF y envía todo a un backend Spring Boot que usa IA (Groq)
para devolver un análisis completo.

## Stack

TypeScript · React 18 · Vite · Manifest V3 (`@crxjs/vite-plugin`) · Tailwind CSS · React Hook Form · Zod

## Requisitos

- Node 20+ y npm

## Puesta en marcha

```bash
npm install
cp .env.example .env   # ajusta VITE_API_BASE_URL / VITE_API_KEY
npm run dev            # servidor de desarrollo con HMR
```

Cargar en Chrome (modo desarrollador):

1. Abre `chrome://extensions`.
2. Activa **Modo de desarrollador**.
3. **Cargar descomprimida** → selecciona la carpeta `dist/`.

Para un build de producción:

```bash
npm run build          # genera dist/
npm run typecheck      # comprueba tipos (tsc --noEmit)
```

## Estructura

```
src/
├─ popup/       UI del popup (React)
├─ background/  service worker (orquestación de red + polling)
├─ content/     content scripts + extractores de ofertas por sitio
├─ services/    transporte hacia el backend, storage
├─ hooks/       hooks de React
├─ schemas/     esquemas Zod (fuente de verdad de los contratos)
└─ types/       tipos compartidos (mensajería, envelope de API)
```

## Estado

Fases 6–8 del roadmap completadas en cliente:

- Detección de oferta en LinkedIn + fallback genérico (content script).
- Subida y validación de CV y carta en PDF (React Hook Form + Zod, ≤ 5 MB, `application/pdf`).
- Cliente HTTP contra el contrato del backend (`createAnalysis` / `getAnalysisJob`).
- Polling asíncrono en el service worker con persistencia en `chrome.storage`
  (sobrevive al cierre del popup) y vistas Upload → Loading → Results.

Pendiente: backend Spring Boot (requiere Java 21 + Maven) para probar el flujo end-to-end.
El cliente ya está preparado para conectarse en cuanto el backend exponga `/api/v1/analyses`.
