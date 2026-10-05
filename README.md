# Gestión de Biblioteca

Sistema web completo de administración de biblioteca con **login backend Spring
Boot + JWT**, consumido desde **JavaScript genérico con Fetch**.

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

| Usuario | Contraseña | Rol |
|---------|-----------|-----|
| `admin` | `admin123` | ADMIN |
| `jperez` | `123456` | USUARIO |
| `mgomez` | `123456` | USUARIO |

## Documentación

- **[INFORME.md](INFORME.md)** — informe completo: arquitectura, base de datos,
  seguridad JWT, proceso de desarrollo y pruebas.
- **[bd/schema.sql](bd/schema.sql)** — código SQL para crear la base de datos.
- **[bd/diagrama-bd.mmd](bd/diagrama-bd.mmd)** — diagrama entidad-relación (Mermaid).
- **[bd/diagrama-flujo-jwt.mmd](bd/diagrama-flujo-jwt.mmd)** — diagrama de secuencia del login.

## Servicios

| Servicio | URL |
|----------|-----|
| Frontend | http://localhost:5500/index.html |
| API | http://localhost:8080/api |
| Consola H2 | http://localhost:8080/h2-console |

Consola H2: URL `jdbc:h2:file:./data/biblioteca`, usuario `sa`, contraseña vacía.
