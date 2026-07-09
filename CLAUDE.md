# CLAUDE.md

# JobMatch AI

## Descripción del proyecto

JobMatch AI es una extensión para Google Chrome cuyo objetivo es ayudar a los usuarios a adaptar su CV y su carta de presentación a una oferta de trabajo concreta utilizando Inteligencia Artificial.

La extensión detecta automáticamente la oferta de empleo que el usuario está visualizando, extrae su información, permite subir un CV y una carta de presentación en formato PDF y envía toda la información a un backend desarrollado con Spring Boot.

El backend analiza los documentos mediante la API de OpenAI y devuelve un análisis completo con puntuaciones, fortalezas, debilidades, palabras clave faltantes y versiones mejoradas adaptadas específicamente a la oferta.

Este proyecto debe desarrollarse con estándares profesionales y estar preparado para crecer y mantenerse a largo plazo.

---

# Objetivos del proyecto

- Crear una extensión profesional para Google Chrome.
- Aplicar una arquitectura limpia y escalable.
- Integrar Inteligencia Artificial mediante la API de OpenAI.
- Analizar CV y cartas de presentación.
- Adaptar ambos documentos a una oferta específica.
- Ofrecer recomendaciones claras y útiles al usuario.
- Mantener un código limpio, mantenible y desacoplado.

---

# Funcionalidades principales

## Extensión

- Detectar automáticamente la oferta de empleo abierta.
- Extraer información relevante de la página.
- Permitir subir un CV en PDF.
- Permitir subir una carta de presentación en PDF.
- Mostrar el análisis recibido del backend.
- Mostrar puntuaciones visuales.
- Mostrar recomendaciones.
- **Modo "Responder preguntas"**: detectar los campos de texto/textarea/select de un
  formulario de candidatura en **cualquier portal de empleo** (no solo LinkedIn) y, a partir
  del CV del usuario, generar una respuesta sugerida por cada pregunta para que el usuario
  la copie manualmente (sin autocompletar el formulario de terceros).

## Backend

- Recibir la información enviada por la extensión.
- Extraer el texto de los PDF.
- Construir los prompts para OpenAI.
- Analizar el CV.
- Analizar la carta.
- Calcular el nivel de compatibilidad.
- Detectar habilidades faltantes.
- Detectar palabras clave ATS.
- Generar sugerencias.
- Generar versiones mejoradas.

---

# Stack tecnológico

## Extensión

- TypeScript
- React
- Vite
- Manifest V3
- Tailwind CSS
- React Hook Form
- Zod

## Backend

- Java 21
- Spring Boot 3
- Maven
- Spring Web
- Spring Validation
- Apache PDFBox
- Jackson
- Rate limiting propio en memoria por IP (Bucket4j como alternativa futura)
- Cliente HTTP hacia la IA vía RestClient (API compatible con OpenAI)

## Base de datos

Inicialmente:

No se utilizará base de datos.

En futuras versiones:

- PostgreSQL
- Flyway

## Inteligencia Artificial

- **Groq** como proveedor principal (API compatible con OpenAI, base URL `https://api.groq.com/openai/v1`).
- Modelo principal: `llama-3.3-70b-versatile`.
- El análisis se genera con **una única llamada** usando salida estructurada (`response_format` = JSON Schema).
- El proveedor se aísla tras una interfaz `AiClient` (DIP) para poder cambiar a OpenAI u otro por configuración, sin tocar el resto del código.
- La API key vive **solo** en el backend (variable de entorno / secrets), nunca en la extensión ni en el repositorio.

---

# Arquitectura

La arquitectura será Cliente - Servidor.

```
Usuario

↓

Extensión Chrome

↓

Backend Spring Boot

↓

Groq API (compatible con OpenAI)
```

## Modelo de comunicación

La comunicación entre la extensión y el backend es **asíncrona con polling** (el análisis tarda 30–90 s):

- `POST /api/v1/analyses` crea un job y devuelve `202 { jobId }` de inmediato.
- El backend procesa en segundo plano (extracción PDF → prompt → Groq → mapeo).
- La extensión consulta `GET /api/v1/analyses/{jobId}` cada ~2 s hasta `DONE`/`ERROR`.
- El **background service worker** (no el popup) es el dueño del polling y persiste el estado/resultado en `chrome.storage`, para que el análisis sobreviva al cierre del popup (ciclo de vida efímero de MV3).

## Responsabilidades de la extensión

La extensión únicamente debe:

- Detectar la oferta de trabajo.
- Extraer la información de la página.
- Permitir subir documentos.
- Mostrar resultados.
- Comunicarse con el backend.

La extensión NO debe:

- Tener la API Key de OpenAI.
- Analizar documentos.
- Procesar PDFs.
- Contener lógica de negocio.

---

## Responsabilidades del backend

El backend será el responsable de:

- Leer PDFs.
- Extraer texto.
- Crear prompts.
- Comunicarse con OpenAI.
- Procesar respuestas.
- Devolver un JSON limpio a la extensión.

---

# Arquitectura del backend

Organizar el proyecto por capas.

```
controller/  (api)

service/     (analysis, answer: modo "responder preguntas", cv: almacenamiento del CV)

job/         (JobStore<T> async genérico: crear/consultar/expirar trabajos)

ai/          (AiClient + QuestionAnswerAiClient + GroqClient/GroqChatClient)

prompt/

pdf/

dto/

mapper/

config/

security/    (ApiKeyFilter + RateLimitFilter + CORS)

exception/
```

Cada clase debe tener una única responsabilidad.

---

# Arquitectura de la extensión

```
popup/

background/

content/

services/

hooks/

components/

pages/

utils/

types/
```

Separar claramente:

- UI
- Comunicación
- Extracción de datos
- Lógica de negocio

---

# Principios de desarrollo

Aplicar siempre:

- Clean Architecture
- SOLID
- DRY
- KISS
- YAGNI cuando sea necesario

Priorizar:

- Legibilidad
- Escalabilidad
- Mantenibilidad
- Seguridad

---

# Convenciones de código

Todo el código debe estar en inglés.

Ejemplos:

Variables

```
jobOffer
coverLetter
analysisResult
cvText
```

Métodos

```
extractOffer()

analyzeCv()

buildPrompt()

rewriteCoverLetter()

calculateScore()
```

Clases

```
JobOfferExtractor

PromptBuilder

OpenAiService

PdfExtractor

AnalysisController
```

---

# TypeScript

Siempre utilizar:

- strict mode
- interfaces
- async/await
- React Hooks
- Componentes funcionales

Evitar:

- any
- callbacks anidados
- componentes de clase

---

# Java

Utilizar Java 21.

Preferir:

- Records
- Optional
- Constructor Injection
- Streams cuando mejoren la legibilidad

Evitar:

- Field Injection
- Métodos enormes
- Servicios gigantes
- Lógica de negocio en los Controllers

---

# API REST

Todas las respuestas deberán seguir un formato consistente.

Ejemplo:

```json
{
  "success": true,
  "data": {},
  "error": null
}
```

Los errores deberán ser claros y descriptivos.

Endpoints (base `/api/v1`):

- `POST /api/v1/analyses` — crea el análisis (multipart: oferta JSON + cv.pdf + coverLetter.pdf). Devuelve `202 { jobId, status }`.
- `GET /api/v1/analyses/{jobId}` — consulta estado/resultado (`PENDING`/`PROCESSING`/`DONE`/`ERROR`).
- `POST /api/v1/answers` — modo "responder preguntas" (multipart: JSON con la lista de preguntas detectadas + contexto opcional de la oferta + cv.pdf). Devuelve `202 { jobId, status }`.
- `GET /api/v1/answers/{jobId}` — consulta estado/resultado del job de respuestas.
- `POST /api/v1/cv` — sube y persiste el CV (multipart: `cv.pdf`), sustituye al anterior. Devuelve `200 { fileName, storedAt, sizeBytes }`.
- `GET /api/v1/cv` — metadatos del CV almacenado, o `data: null` si no hay ninguno todavía.
- `GET /api/v1/cv/file` — descarga el CV almacenado (`application/pdf`, sin envelope JSON); `404 CV_NOT_FOUND` si no hay ninguno.
- `DELETE /api/v1/cv` — elimina el CV almacenado.
- `GET /api/v1/health` — healthcheck (sin auth).

Los códigos de error del envelope son un enum estable: `VALIDATION_ERROR`, `UNAUTHORIZED`, `PAYLOAD_TOO_LARGE`, `UNSUPPORTED_MEDIA_TYPE`, `PDF_UNPROCESSABLE`, `RATE_LIMITED`, `JOB_NOT_FOUND`, `CV_NOT_FOUND`, `AI_PROVIDER_ERROR`, `INTERNAL_ERROR`.

## Almacenamiento del CV

Decisión explícita del usuario (evoluciona el MVP inicial, que no persistía nada):

- Se guarda en el **sistema de ficheros local** del backend (`app.storage.directory`, por defecto
  `./data/cv/`), sin base de datos. Solo se conserva **la última versión subida** (cada subida
  sustituye a la anterior, minimización de datos).
- La extensión **reutiliza automáticamente** el CV almacenado: si ya hay uno, no vuelve a pedirlo
  al analizar ni al generar respuestas; lo descarga del backend cuando hace falta. El usuario puede
  "Cambiar" (subir uno nuevo) o "Eliminar" desde el propio popup.
- La carpeta `data/` está excluida de git (igual que `config/` con la API key).

---

# Prompts

Los prompts deben construirse mediante clases específicas.

Nunca escribir prompts enormes directamente dentro de un Controller.

Crear un PromptBuilder dedicado.

---

# Seguridad

Nunca almacenar:

- API Keys
- Tokens
- Prompts internos

Validar siempre:

- Tamaño del archivo
- Tipo MIME y magic bytes del PDF
- PDFs corruptos o sin texto extraíble

No confiar nunca en datos enviados por el cliente.

Protección del backend (MVP):

- **API key compartida** obligatoria (cabecera `X-Api-Key`) en los endpoints de análisis.
- **Rate limiting** por IP (limitador propio en memoria; Bucket4j como alternativa) para proteger el coste de la IA.
- **CORS** restringido al origin de la extensión (`chrome-extension://<id>`).
- **Anti prompt-injection**: el contenido scrapeado de la oferta y el texto de los PDFs se tratan como **datos**, nunca como instrucciones; el system prompt lo deja explícito y la salida se fuerza vía JSON Schema.
- **Privacidad / RGPD**: el CV es dato personal. Se persiste (decisión explícita del usuario, ver
  sección "Almacenamiento del CV") pero nunca se loggea su contenido; el resultado del análisis y
  las respuestas generadas siguen sin persistir (viven solo en `chrome.storage` del navegador).

---

# Rendimiento

Evitar llamadas innecesarias a OpenAI.

Reducir el tamaño de los prompts.

Utilizar asincronía cuando aporte beneficios.

Preparar el backend para soportar múltiples usuarios.

---

# Diseño de la interfaz

La interfaz debe transmitir profesionalidad.

Características:

- Minimalista
- Moderna
- Responsive
- Accesible
- Modo oscuro como prioridad

Evitar interfaces sobrecargadas.

---

# Resultado esperado del análisis

El backend deberá devolver:

- Puntuación del CV
- Puntuación de la carta
- Compatibilidad global
- Fortalezas
- Debilidades
- Palabras clave faltantes
- Habilidades faltantes
- Errores gramaticales
- Problemas de formato
- Compatibilidad ATS
- Recomendaciones
- CV optimizado
- Carta optimizada

---

# Calidad del código

Todo el código generado debe cumplir:

- Sin duplicación.
- Sin imports innecesarios.
- Sin código comentado.
- Sin métodos excesivamente largos.
- Sin clases con múltiples responsabilidades.
- Nombres claros y descriptivos.
- Manejo correcto de excepciones.
- Código preparado para producción.

---

# Testing

Backend

- JUnit
- Mockito

Frontend

- Vitest
- React Testing Library

End to End

- Playwright

---

# Git

Ramas:

```
main
develop
feature/*
fix/*
```

Commits descriptivos.

---

# Filosofía del proyecto

Este proyecto debe desarrollarse como si fuese un producto real destinado a producción.

Cada decisión técnica debe priorizar:

- Calidad del código.
- Escalabilidad.
- Mantenibilidad.
- Seguridad.
- Experiencia del usuario.
- Buenas prácticas.

No implementar soluciones rápidas que comprometan el diseño del proyecto.

Siempre pensar como un desarrollador senior y construir un software preparado para evolucionar durante años.

# Reglas para Claude

- No introducir dependencias innecesarias.
- Antes de generar código, analizar la arquitectura existente.
- Mantener consistencia con el estilo del proyecto.
- No duplicar lógica.
- Explicar brevemente las decisiones importantes cuando se propongan cambios arquitectónicos.
- Si existen varias soluciones posibles, elegir la más mantenible a largo plazo.
- Priorizar la simplicidad sin sacrificar la escalabilidad.
- Todo el código generado debe estar listo para producción y seguir buenas prácticas profesionales.