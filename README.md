# Gestión de Biblioteca

Sistema web completo de gestión bibliotecaria: dashboard, catálogo con
ejemplares, lectores, préstamos con devolución y renovación, reservas, reportes
y auditoría, con **backend Spring Boot + JWT** consumido desde **JavaScript
puro con `fetch`** (sin frameworks ni librerías de frontend).

## Descripción del proyecto

La aplicación permite gestionar el inventario de libros y sus **ejemplares**
(copias físicas), los **lectores**, los préstamos con control de ejemplares
disponibles, las **reservas** (lista de espera) y los **usuarios del sistema**,
con **auditoría** de acciones y **reportes**. Los usuarios se autentican contra
una API REST que emite un token JWT; ese token se guarda en `localStorage` y se
envía en la cabecera `Authorization: Bearer` en cada petición posterior. El
frontend es HTML/CSS/JS estático y responsive (desktop, tablet y móvil), por lo
que puede alojarse en cualquier hosting estático.

**Características principales**

- Autenticación con JWT (login, perfil `/me`, token en `localStorage`)
- **Tres roles** `ADMIN`, `BIBLIOTECARIO` y `USUARIO`; el menú y las secciones
  se muestran según el rol
- **Dashboard** con métricas reales (títulos, ejemplares, lectores, préstamos
  activos, ranking, actividad reciente)
- Catálogo con **ejemplares** individuales (código, ubicación, estado), búsqueda
  y filtros paginados
- Gestión de **lectores** (alta/baja/modificación, código de lector, suspensión)
  y de **usuarios del sistema** (acceso al panel)
- Préstamos con estados, fecha límite, **devolución** y **renovación**
  (`PUT /{id}/devolver`, `PUT /{id}/renovar`), con reglas de negocio
- **Reservas** con lista de espera y expiración automática
- **Reportes** (resumen + exportación CSV) y **auditoría** de acciones
- Respuestas JSON uniformes (`success`, `message`, `data`) y manejo global de
  errores (400/401/403/404/409)
- API REST desplegable en la nube (Render/Fly/Koyeb con Dockerfile) y base de
  datos H2 local o PostgreSQL en producción
- Frontend responsive con navegación por secciones y modales accesibles

## Requisitos previos

| Herramienta | Versión |
|-------------|---------|
| Java (JDK)  | 17 o superior |
| Maven       | 3.9+ (`mvn -v`) |
| Node.js     | 18+ (para `npx http-server`) |
| PowerShell  | 5.1+ (solo para el script de arranque) |

## Arranque rápido

```powershell
powershell -ExecutionPolicy Bypass -File .\iniciar-proyecto.ps1
```

El script levanta el backend (puerto 8080) y el frontend (puerto 5500) y
verifica que ambos respondan.

> Si el puerto 8080 está ocupado por Apache/httpd, ejecútalo como Administrador:
> `Stop-Service PEMHTTPD-x64`

### Arranque manual

```powershell
# Terminal 1 - Backend
cd backend
mvn spring-boot:run

# Terminal 2 - Frontend
npx http-server frontend -p 5500 -c-1
```

Abrir <http://localhost:5500/index.html>

## Credenciales

En local, el primer arranque crea automáticamente un administrador **la primera
vez** (cuando la base está vacía):

| Usuario | Contraseña | Rol |
|---------|------------|-----|
| `admin` | `admin123` | ADMIN |

También se crean **3 lectores de demostración** (rol `USUARIO`, sin acceso al
panel) con sus préstamos y una reserva, más **4 libros** con sus ejemplares.

Estos valores se pueden cambiar antes de arrancar:

```powershell
$env:APP_ADMIN_USERNAME = "admin"
$env:APP_ADMIN_PASSWORD = "TU_CONTRASEÑA_FUERTE"
```

**En producción no hay credenciales por defecto visibles en el repo**: define
`APP_ADMIN_USERNAME` y `APP_ADMIN_PASSWORD` en el panel del proveedor (Render,
Koyeb…). Esas variables de entorno sobrescriben los valores locales, por lo que
el servicio desplegado sigue usando su propia contraseña.

> La base local (H2) ya existente conserva sus usuarios. Si quieres partir de
> cero, borra `backend/data/biblioteca.mv.db` y vuelve a arrancar.

## Variables de entorno

Todas son opcionales en local (usan valores por defecto) excepto
`JWT_SECRET` cuando no está definida en el archivo de propiedades.

| Variable | Descripción | Valor por defecto |
|----------|-------------|-------------------|
| `PORT` | Puerto del backend | `8080` |
| `DB_URL` | URL JDBC. Si se define, se usa PostgreSQL; si no, H2 en archivo | `jdbc:h2:file:./data/biblioteca;DB_CLOSE_DELAY=-1` |
| `DB_USER` | Usuario de la base de datos | `sa` |
| `DB_PASSWORD` | Contraseña de la base de datos | *(vacío)* |
| `JWT_SECRET` | Clave HMAC para firmar los tokens | Clave de desarrollo incluida (cámbiala en producción) |
| `JWT_EXPIRATION` | Vigencia del token en milisegundos | `86400000` (24 h) |
| `H2_CONSOLE` | Habilita `/h2-console` (`true`/`false`) | `true` |
| `APP_ADMIN_USERNAME` | Usuario del primer administrador | `admin` (solo local) |
| `APP_ADMIN_PASSWORD` | Contraseña del primer administrador | `admin123` (solo local) |

## Estructura del proyecto

```
gestion_biblioteca/
├── README.md                  # Este archivo
├── INFORME.md                 # Informe académico (arquitectura, BD, JWT, pruebas)
├── .gitignore
├── iniciar-proyecto.ps1       # Arranque local automático
│
├── backend/
│   ├── Dockerfile             # Imagen multi-etapa (Maven + JRE 17)
│   ├── pom.xml                # Spring Boot 3.2.5, JJWT, PostgreSQL driver
│   └── src/main/
│       ├── resources/application.properties
│       └── java/com/biblioteca/
│           ├── GestionBibliotecaApplication.java
│           ├── config/        # SecurityConfig, DataInitializer
│           ├── controller/    # Auth, Libros, Ejemplares, Lectores,
│           │                  #   Prestamos, Reservas, Dashboard,
│           │                  #   Reportes, Auditoria, Usuarios
│           ├── service/       # Lógica de negocio (+ MigracionService)
│           ├── repository/    # Spring Data JPA
│           ├── entity/        # Usuario, Libro, Ejemplar, Prestamo, Reserva, Auditoria
│           ├── dto/           # Requests, Responses, PaginaResponse, DashboardDto…
│           ├── security/      # JwtUtil, JwtFilter
│           ├── exception/     # GlobalExceptionHandler + excepciones propias
│           └── api/           # ApiResponse (envoltorio JSON)
│
├── frontend/
│   ├── index.html             # Login
│   ├── panel.html             # Panel (secciones según rol)
│   ├── config.js              # URL de la API (editar antes de publicar)
│   ├── js/                    # api, auth, ui, panel, dashboard, catalogo,
│   │                          #   lectores, prestamos, reservas, reportes,
│   │                          #   auditoria, usuarios
│   └── css/estilos.css        # Diseño responsive
│
└── bd/
    ├── schema.sql             # DDL de referencia (6 tablas)
    ├── diagrama-bd.mmd        # Diagrama entidad-relación (Mermaid)
    └── diagrama-flujo-jwt.mmd # Diagrama de secuencia del login
```

## API REST

Todas las respuestas tienen la forma:

```json
{ "success": true, "message": "Bienvenido", "data": { } }
```

Autenticación: `Authorization: Bearer <token>` (excepto el login).

### Autenticación

| Método | Endpoint | Roles | Descripción |
|--------|----------|-------|-------------|
| POST | `/api/auth/login` | Público | Body `{"username","password"}` → token + datos de usuario |
| GET | `/api/auth/me` | Cualquiera | Perfil del usuario de la sesión |

### Libros y ejemplares

| Método | Endpoint | Roles | Descripción |
|--------|----------|-------|-------------|
| GET | `/api/libros` | Cualquiera | Listar catálogo |
| GET | `/api/libros/buscar` | Cualquiera | Búsqueda paginada (filtros y orden) |
| GET | `/api/libros/categorias` | Cualquiera | Categorías distintas |
| GET | `/api/libros/{id}` | Cualquiera | Obtener uno |
| POST | `/api/libros` | ADMIN, BIBLIOTECARIO | Crear libro (genera ejemplares) |
| PUT | `/api/libros/{id}` | ADMIN, BIBLIOTECARIO | Actualizar libro |
| DELETE | `/api/libros/{id}` | ADMIN, BIBLIOTECARIO | Eliminar libro |
| GET | `/api/libros/{id}/ejemplares` | Cualquiera | Ejemplares del libro |
| POST | `/api/libros/{id}/ejemplares` | ADMIN, BIBLIOTECARIO | Añadir ejemplar |
| PUT | `/api/ejemplares/{id}/estado` | ADMIN, BIBLIOTECARIO | Cambiar estado del ejemplar |
| DELETE | `/api/ejemplares/{id}` | ADMIN, BIBLIOTECARIO | Eliminar ejemplar |

### Lectores

| Método | Endpoint | Roles | Descripción |
|--------|----------|-------|-------------|
| GET | `/api/lectores` | ADMIN, BIBLIOTECARIO | Listar lectores |
| GET | `/api/lectores/{id}` | ADMIN, BIBLIOTECARIO | Obtener uno |
| POST | `/api/lectores` | ADMIN, BIBLIOTECARIO | Crear lector |
| PUT | `/api/lectores/{id}` | ADMIN, BIBLIOTECARIO | Actualizar lector |
| DELETE | `/api/lectores/{id}` | ADMIN, BIBLIOTECARIO | Eliminar lector |

### Préstamos

| Método | Endpoint | Roles | Descripción |
|--------|----------|-------|-------------|
| GET | `/api/prestamos` | ADMIN, BIBLIOTECARIO | Listar préstamos |
| GET | `/api/prestamos/buscar` | ADMIN, BIBLIOTECARIO | Búsqueda paginada |
| GET | `/api/prestamos/vencidos` | ADMIN, BIBLIOTECARIO | Préstamos vencidos |
| GET | `/api/prestamos/mios` | Cualquiera | Préstamos del usuario autenticado |
| GET | `/api/prestamos/{id}` | ADMIN, BIBLIOTECARIO | Obtener uno |
| POST | `/api/prestamos` | ADMIN, BIBLIOTECARIO | Registrar préstamo |
| PUT | `/api/prestamos/{id}` | ADMIN, BIBLIOTECARIO | Actualizar préstamo |
| PUT | `/api/prestamos/{id}/devolver` | ADMIN, BIBLIOTECARIO | Devolver y liberar el ejemplar |
| PUT | `/api/prestamos/{id}/renovar` | ADMIN, BIBLIOTECARIO | Renovar (máx. 2, sin lista de espera) |
| DELETE | `/api/prestamos/{id}` | ADMIN, BIBLIOTECARIO | Eliminar préstamo |

### Reservas

| Método | Endpoint | Roles | Descripción |
|--------|----------|-------|-------------|
| GET | `/api/reservas` | ADMIN, BIBLIOTECARIO | Listar reservas |
| GET | `/api/reservas/mias` | Cualquiera | Reservas del usuario autenticado |
| POST | `/api/reservas` | Cualquiera | Crear reserva (lista de espera) |
| PUT | `/api/reservas/{id}/cancelar` | ADMIN, BIBLIOTECARIO | Cancelar reserva |
| PUT | `/api/reservas/{id}/cumplir` | ADMIN, BIBLIOTECARIO | Marcar como cumplida |
| DELETE | `/api/reservas/{id}` | ADMIN, BIBLIOTECARIO | Eliminar reserva |

### Dashboard, reportes y auditoría

| Método | Endpoint | Roles | Descripción |
|--------|----------|-------|-------------|
| GET | `/api/dashboard` | Cualquiera | Métricas y actividad reciente |
| GET | `/api/reportes/resumen` | ADMIN, BIBLIOTECARIO | Resumen (activos, devueltos, ranking) |
| GET | `/api/reportes/libros.csv` | ADMIN, BIBLIOTECARIO | Exportar catálogo a CSV |
| GET | `/api/reportes/prestamos.csv` | ADMIN, BIBLIOTECARIO | Exportar préstamos a CSV |
| GET | `/api/auditoria` | ADMIN | Historial de acciones |

### Usuarios del sistema

| Método | Endpoint | Roles | Descripción |
|--------|----------|-------|-------------|
| GET | `/api/usuarios` | ADMIN | Listar usuarios con acceso |
| GET | `/api/usuarios/buscar` | ADMIN | Búsqueda (p. ej. `?soloAcceso=true`) |
| GET | `/api/usuarios/{id}` | ADMIN | Obtener uno |
| POST | `/api/usuarios` | ADMIN | Crear usuario |
| PUT | `/api/usuarios/{id}` | ADMIN | Actualizar usuario |
| DELETE | `/api/usuarios/{id}` | ADMIN | Eliminar (no permite auto-eliminación) |

Body de préstamo (`PrestamoRequest`):

```json
{
  "usuarioId": 1,
  "libroId": 2,
  "ejemplarId": 3,
  "fechaPrestamo": "2026-10-06",
  "fechaLimite": "2026-10-21",
  "observaciones": "Entrega en mostrador"
}
```

### Ejemplo con PowerShell

```powershell
$login = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" `
  -Method POST -ContentType "application/json" `
  -Body '{"username":"admin","password":"TU_CONTRASEÑA"}'

$h = @{ Authorization = "Bearer $($login.data.token)" }
Invoke-RestMethod -Uri "http://localhost:8080/api/libros" -Headers $h
```

## Base de datos

| Tabla | Contenido |
|-------|-----------|
| `usuarios` | Personas de la biblioteca: credenciales (hash BCrypt), rol, estado y datos de lector (código, tipo, acceso, suspensión) |
| `libros` | Ficha del libro y ejemplares totales/disponibles |
| `ejemplares` | Copias físicas: código único, ubicación y estado |
| `prestamos` | Relación usuario↔libro con ejemplar, fechas, estado y renovaciones |
| `reservas` | Lista de espera: usuario↔libro, posición, estado y expiración |
| `auditoria` | Historial de acciones (quién, qué, sobre qué entidad y cuándo) |

- **Local:** H2 en archivo (`backend/data/biblioteca.mv.db`), consola en
  <http://localhost:8080/h2-console> — URL `jdbc:h2:file:./data/biblioteca`,
  usuario `sa`, contraseña vacía.
- **Nube:** define `DB_URL` con una PostgreSQL (p. ej. Neon) y se usa el driver
  incluido en `pom.xml`. El esquema lo crea Hibernate (`ddl-auto=update`) y los
  datos de ejemplo los inserta `DataInitializer` en el primer arranque.
- `bd/schema.sql` es el DDL manual de referencia.

Diagramas Mermaid (se ven en GitHub, VS Code o [mermaid.live](https://mermaid.live)):

- `bd/diagrama-bd.mmd` — entidad-relación
- `bd/diagrama-flujo-jwt.mmd` — secuencia del login

## Despliegue en la nube

### Backend

**Opción A — Render (usada en este proyecto):**

1. Crea el servicio **Web Service** apuntando al repositorio (raíz: `backend`).
2. Build: `mvn clean package -DskipTests` · Start: `java -jar target/*.jar`
   (o usa el `Dockerfile`, Render lo detecta solo).
3. Define las variables de entorno: `APP_ADMIN_USERNAME`, `APP_ADMIN_PASSWORD`,
   `JWT_SECRET`, y `DB_URL`/`DB_USER`/`DB_PASSWORD` si usas PostgreSQL.
4. Ejemplo de URL: `https://biblioteca-0i0a.onrender.com/api`.

**Opción B — Contenedor (Koyeb, Fly.io, etc.):**

```bash
cd backend
docker build -t gestion-biblioteca .
docker run -p 8080:8080 -e JWT_SECRET=... -e APP_ADMIN_USERNAME=admin \
  -e APP_ADMIN_PASSWORD=... gestion-biblioteca
```

### Frontend

1. Edita `frontend/config.js` con la URL del backend **sin olvidar `/api`**:

   ```js
   window.__API_URL__ = 'https://TU-BACKEND.onrender.com/api';
   ```

   `api.js` concatena los paths (`API_URL + '/libros'`), por eso la base debe
   incluir `/api`.
2. Publica la carpeta `frontend/` en un hosting estático (Cloudflare Pages,
   Netlify, Vercel…).
3. En local, `config.js` vuelve a `http://localhost:8080/api`.

> El CORS del backend está abierto (`allowedOriginPatterns: *`), así que el
> frontend puede servirse desde otro dominio sin configuración adicional.

## Seguridad

- Contraseñas guardadas con **BCrypt**, nunca en texto plano.
- Tokens firmados con HMAC desde `JWT_SECRET`; expiran según `JWT_EXPIRATION`.
- Autorización por rol: `/api/usuarios/**` y `/api/auditoria/**` solo `ADMIN`;
  escritura de catálogo, lectores, préstamos y reportes para `ADMIN` y
  `BIBLIOTECARIO`; `USUARIO` accede a su propia información (`/mios`, `/mias`).
- Todas las rutas exigen token salvo `POST /api/auth/login` (y la consola H2,
  desactivable con `H2_CONSOLE=false` en producción).
- Sin credenciales por defecto: el primer admin nace de variables de entorno.
- En producción: usa `JWT_SECRET` propio, `H2_CONSOLE=false` y PostgreSQL.

## Pruebas realizadas

| Prueba | Resultado |
|--------|-----------|
| Login con credenciales válidas | ✅ 200 + token |
| Login con credenciales inválidas | ✅ 400/401 |
| Sin token en `/api/libros` | ✅ 401 `No autorizado` |
| `/api/auth/me` con token | ✅ Devuelve el perfil y el rol |
| Autorización por rol (USUARIO 403 en admin/auditoría) | ✅ Correcta |
| Ciclo préstamo → devolución → reserva | ✅ correcto |
| Renovación con lista de espera | ✅ 409 (regla de negocio) |
| Dashboard, reportes y auditoría | ✅ Datos reales y CSV |
| Frontend por rol (ADMIN y USUARIO) | ✅ Menú y secciones filtradas |
| Responsive 320 / 390 / 768 / 1280 px | ✅ Sin scroll horizontal |

## Documentación relacionada

- **[INFORME.md](INFORME.md)** — informe completo: arquitectura, base de datos,
  seguridad JWT, proceso de desarrollo y pruebas.
- **[bd/schema.sql](bd/schema.sql)** — código SQL para crear la base de datos.
- **[bd/diagrama-bd.mmd](bd/diagrama-bd.mmd)** — diagrama entidad-relación.
- **[bd/diagrama-flujo-jwt.mmd](bd/diagrama-flujo-jwt.mmd)** — diagrama de
  secuencia del login.

## Tecnologías

**Backend:** Java 17 · Spring Boot 3.2.5 · Spring Security · Spring Data JPA ·
JJWT · H2 / PostgreSQL · Maven  

**Frontend:** HTML5 · CSS3 · JavaScript (ES6, `fetch`) — sin frameworks  

**DevOps:** Docker · Render · Mermaid
