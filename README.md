# Gestión de Biblioteca

Sistema web completo para administrar una biblioteca: usuarios, catálogo de
libros y préstamos, con **login backend Spring Boot + JWT** consumido desde
**JavaScript puro con `fetch`** (sin frameworks ni librerías de frontend).

## Descripción del proyecto

La aplicación permite a un administrador gestionar el inventario de libros
(alta, baja y modificación), los usuarios del sistema y los préstamos de
material, con control de ejemplares disponibles y devoluciones. Los usuarios
se autentican contra una API REST que emite un token JWT; ese token se guarda
en `localStorage` y se envía en la cabecera `Authorization: Bearer` en cada
petición posterior. El frontend es HTML/CSS/JS estático y responsive
(desktop, tablet y móvil), por lo que puede alojarse en cualquier hosting
estático.

**Características principales**

- Autenticación con JWT (login, perfil `/me`, token en `localStorage`)
- Gestión de libros: catálogo, ejemplares disponibles y búsqueda
- Gestión de usuarios con roles `ADMIN` y `USUARIO` (baja sin auto-eliminación)
- Préstamos con estados, fecha límite y devolución (`PUT /{id}/devolver`)
- Respuestas JSON uniformes (`success`, `message`, `data`) y manejo global de
  errores
- API REST desplegable en la nube (Render/Fly/Koyeb con Dockerfile) y base de
  datos H2 local o PostgreSQL en producción
- Frontend responsive con navegación por secciones y validaciones accesibles

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
│           ├── controller/    # Auth, Libros, Usuarios, Préstamos
│           ├── service/       # Lógica de negocio
│           ├── repository/    # Spring Data JPA
│           ├── entity/        # Usuario, Libro, Prestamo
│           ├── dto/           # LoginRequest/Response, UsuarioDto, etc.
│           ├── security/      # JwtUtil, JwtFilter
│           ├── exception/     # GlobalExceptionHandler
│           └── api/           # ApiResponse (envoltorio JSON)
│
├── frontend/
│   ├── index.html             # Login
│   ├── panel.html             # Panel de administración
│   ├── config.js              # URL de la API (editar antes de publicar)
│   ├── js/                    # api, auth, panel, libros, usuarios, prestamos
│   └── css/estilos.css        # Diseño responsive
│
└── bd/
    ├── schema.sql             # DDL de las 3 tablas
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

| Método | Endpoint | Auth | Descripción |
|--------|----------|------|-------------|
| POST | `/api/auth/login` | No | Body `{"username","password"}` → token + datos de usuario |
| GET | `/api/auth/me` | Sí | Perfil del usuario de la sesión |

### Libros

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/libros` | Listar catálogo |
| GET | `/api/libros/{id}` | Obtener uno |
| POST | `/api/libros` | Crear libro |
| PUT | `/api/libros/{id}` | Actualizar libro |
| DELETE | `/api/libros/{id}` | Eliminar libro |

### Usuarios

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/usuarios` | Listar usuarios |
| GET | `/api/usuarios/{id}` | Obtener uno |
| POST | `/api/usuarios` | Crear usuario |
| PUT | `/api/usuarios/{id}` | Actualizar usuario |
| DELETE | `/api/usuarios/{id}` | Eliminar (no permite auto-eliminación) |

### Préstamos

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/prestamos` | Listar préstamos |
| GET | `/api/prestamos/{id}` | Obtener uno |
| POST | `/api/prestamos` | Registrar préstamo |
| PUT | `/api/prestamos/{id}` | Actualizar préstamo |
| PUT | `/api/prestamos/{id}/devolver` | Devolver y liberar ejemplares |
| DELETE | `/api/prestamos/{id}` | Eliminar préstamo |

Body de préstamo (`PrestamoRequest`):

```json
{
  "usuarioId": 1,
  "libroId": 2,
  "fechaPrestamo": "2026-10-06",
  "fechaLimite": "2026-10-20",
  "estado": "PENDIENTE"
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
| `usuarios` | Credenciales (hash BCrypt), nombre, email, rol, estado activo |
| `libros` | Ficha del libro y ejemplares totales/disponibles |
| `prestamos` | Relación usuario↔libro, fechas, estado y devolución |

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
- Todas las rutas exigen token salvo `POST /api/auth/login` (y la consola H2,
  desactivable con `H2_CONSOLE=false` en producción).
- Sin credenciales por defecto: el primer admin nace de variables de entorno.
- En producción: usa `JWT_SECRET` propio, `H2_CONSOLE=false` y PostgreSQL.

## Pruebas realizadas

| Prueba | Resultado |
|--------|-----------|
| Login con credenciales válidas | ✅ 200 + token |
| Login con credenciales inválidas | ✅ 400 `Credenciales inválidas` |
| Sin token en `/api/libros` | ✅ 401 `No autorizado` |
| `/api/auth/me` con token | ✅ Devuelve el perfil y el rol |
| Frontend login → panel con datos reales | ✅ Libros, usuarios y préstamos cargan |
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
