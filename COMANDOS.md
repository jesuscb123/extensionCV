# COMANDOS.md

Chuleta de comandos para arrancar y trabajar en JobMatch AI (backend + extensión) en Windows/PowerShell.

---

## Backend (Spring Boot)

Requisitos: JDK 21 (`JAVA_HOME` apuntando a él). No hace falta tener Maven instalado, se usa el wrapper.

### Arrancar (recomendado)

```powershell
cd backend
.\run.ps1
```

`run.ps1` empaqueta el jar y lo ejecuta con `java -jar`. Es la forma recomendada en este
proyecto porque `mvnw.cmd spring-boot:run` falla con rutas que contienen caracteres no-ASCII
(como la "ú" de esta carpeta: `Documents\proyecto-cv`), ya que el goal `run` del plugin pasa el
classpath completo como argumento a un proceso hijo.

Si PowerShell bloquea el script con `UnauthorizedAccess` / "la ejecución de scripts está
deshabilitada en este sistema" (política por defecto en Windows), tienes dos opciones:

```powershell
# A) Solo para esta ejecución, sin cambiar la configuración global
powershell -ExecutionPolicy Bypass -File .\run.ps1

# B) Permitir scripts locales para tu usuario de forma permanente
Set-ExecutionPolicy -Scope CurrentUser RemoteSigned
.\run.ps1   # ya funciona directamente a partir de ahora
```

### Alternativa (solo si la ruta no tiene caracteres especiales)

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Arranca en `http://localhost:8080`. Por defecto `ai.mode=stub` (respuestas de ejemplo, sin llamar a ninguna IA).

### Usar Groq (IA real)

```powershell
$env:AI_MODE = "groq"
$env:GROQ_API_KEY = "tu_api_key"
$env:AI_MODEL = "llama-3.3-70b-versatile"   # opcional
.\run.ps1
```

### Proteger el backend en local (opcional)

```powershell
$env:APP_API_KEY = "un_secreto_compartido"                          # exige X-Api-Key
$env:APP_ALLOWED_ORIGINS = "chrome-extension://<id-de-tu-extension>" # CORS
```

### Tests

```powershell
cd backend
.\mvnw.cmd test
```

### Compilar sin tests

```powershell
cd backend
.\mvnw.cmd package -DskipTests
```

---

## Extensión (Chrome, MV3)

Requisitos: Node 20+ y npm.

### Instalación inicial

```powershell
cd extension
npm install
Copy-Item .env.example .env   # luego ajusta VITE_API_BASE_URL / VITE_API_KEY si hace falta
```

### Desarrollo (con HMR)

```powershell
cd extension
npm run dev
```

Cargar en Chrome:

1. Abre `chrome://extensions`.
2. Activa **Modo de desarrollador**.
3. **Cargar descomprimida** → selecciona la carpeta `extension/dist/`.

### Build de producción

```powershell
cd extension
npm run build
```

### Comprobar tipos

```powershell
cd extension
npm run typecheck
```

### Tests (Vitest)

```powershell
cd extension
npm test
```

---

## Flujo típico de desarrollo

```powershell
# Terminal 1: backend
cd backend
.\run.ps1

# Terminal 2: extensión
cd extension
npm run dev
```

Luego recarga la extensión descomprimida en `chrome://extensions` cada vez que `npm run build`
genere un nuevo `dist/` (con `npm run dev` + `@crxjs/vite-plugin` el recargado suele ser automático).

---

## Endpoints útiles para probar con curl/Postman

Base: `http://localhost:8080/api/v1`

```powershell
# Healthcheck (sin auth)
curl http://localhost:8080/api/v1/health

# Subir CV (requiere X-Api-Key si APP_API_KEY está definido)
curl -X POST http://localhost:8080/api/v1/cv `
  -H "X-Api-Key: tu_secreto" `
  -F "cv=@C:\ruta\a\mi\cv.pdf"

# Ver metadatos del CV almacenado
curl http://localhost:8080/api/v1/cv -H "X-Api-Key: tu_secreto"

# Eliminar el CV almacenado
curl -X DELETE http://localhost:8080/api/v1/cv -H "X-Api-Key: tu_secreto"
```
