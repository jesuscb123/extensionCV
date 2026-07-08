# JobMatch AI — Backend (Spring Boot)

Backend que recibe una oferta de empleo + CV y carta en PDF, extrae el texto, construye
un prompt y pide a la IA (Groq, API compatible con OpenAI) un análisis completo. El flujo
es **asíncrono con polling**: `POST` crea un job y `GET` consulta su estado/resultado.

## Stack

Java 21 · Spring Boot 3.3 · Spring Web · Spring Validation · Apache PDFBox · Maven (wrapper)

## Requisitos

- JDK 21 (`JAVA_HOME` apuntando a él).
- No necesitas Maven instalado: usa el wrapper (`./mvnw` / `mvnw.cmd`).

## Ejecución

```bash
./mvnw spring-boot:run        # Linux/macOS
.\mvnw.cmd spring-boot:run    # Windows
```

Arranca en `http://localhost:8080`. Por defecto usa `ai.mode=stub`, que devuelve un
análisis de ejemplo **sin llamar a ninguna API** (ideal para desarrollo y para probar la
extensión de punta a punta).

### Usar Groq (IA real)

Define variables de entorno y activa el modo `groq`:

```bash
AI_MODE=groq
GROQ_API_KEY=tu_api_key
AI_MODEL=llama-3.3-70b-versatile   # opcional
```

### Proteger el endpoint (opcional en local)

```bash
APP_API_KEY=un_secreto_compartido        # exige cabecera X-Api-Key
APP_ALLOWED_ORIGINS=chrome-extension://<id-de-tu-extension>
```

## Endpoints (`/api/v1`)

| Método | Ruta                    | Descripción                                  |
|--------|-------------------------|----------------------------------------------|
| POST   | `/analyses`             | Multipart: `request` (JSON) + `cv` + `coverLetter` (PDF). Devuelve `202 { jobId, status }`. |
| GET    | `/analyses/{jobId}`     | Estado/resultado del job (`PENDING`/`PROCESSING`/`DONE`/`ERROR`). |
| GET    | `/health`               | Healthcheck (sin auth).                      |

Todas las respuestas usan el envelope `{ success, data, error }`.

## Tests

```bash
./mvnw test
```

Incluye la extracción de PDF y un test de integración del flujo completo (POST → polling →
DONE) usando el `StubAiClient`, más los casos 401 (sin API key) y 404 (job inexistente).

## Arquitectura (paquetes)

```
api/         controllers + DTOs (envelope)
analysis/    servicio, processor async, modelo de job
job/         JobStore (en memoria; sustituible por Redis)
pdf/         extracción con PDFBox
prompt/      construcción del prompt
ai/          AiClient (interfaz) + GroqClient + StubAiClient
mapper/      JSON de la IA -> DTO de salida
security/    filtros API key + rate limit
config/      propiedades, CORS, registro de filtros
exception/   excepciones de dominio + manejador global
```
