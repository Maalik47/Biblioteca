-- ==========================================================================
--  Gestion de Biblioteca - Esquema de base de datos
--  Motor: H2 (archivo) - compatible con MySQL/MariaDB y PostgreSQL
--  Descripcion: este script crea las tablas que Spring Boot genera por JPA
--                (spring.jpa.hibernate.ddl-auto=update). Sirve para
--                reconstruir la base de datos desde cero.
--
--  MODELO: una persona es una fila de "usuarios". El rol (ADMIN |
--  BIBLIOTECARIO | USUARIO) y la bandera puede_acceder deciden si puede
--  entrar al panel. Los libros tienen "ejemplares" (copias fisicas); los
--  prestamos se hacen sobre un ejemplar concreto. Las "reservas" cubren la
--  lista de espera de libros sin ejemplares disponibles y "auditoria" guarda
--  el historial de acciones.
--
--  EJECUCION (consola H2):
--    java -cp h2.jar org.h2.tools.RunScript -url "jdbc:h2:file:./data/biblioteca" ^
--         -user sa -script bd/schema.sql
--
--  EJECUCION (MySQL):
--    mysql -u root -p < bd/schema.sql
-- ==========================================================================

DROP TABLE IF EXISTS auditoria;
DROP TABLE IF EXISTS reservas;
DROP TABLE IF EXISTS prestamos;
DROP TABLE IF EXISTS ejemplares;
DROP TABLE IF EXISTS libros;
DROP TABLE IF EXISTS usuarios;

-- ==========================================================================
-- 1. USUARIOS
--    Personas de la biblioteca. El campo password almacena el hash BCrypt.
--    Puede ser un lector (puede_acceder = FALSE) o tener acceso al panel.
-- ==========================================================================
CREATE TABLE usuarios (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    username        VARCHAR(50)  NOT NULL,
    password        VARCHAR(100) NOT NULL,
    nombre          VARCHAR(100) NOT NULL,
    apellido        VARCHAR(100),
    email           VARCHAR(120) NOT NULL,
    rol             VARCHAR(20)  NOT NULL DEFAULT 'USUARIO',
    activo          BOOLEAN      NOT NULL DEFAULT TRUE,
    fecha_registro  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- Datos de lector (aditivos) --
    codigo_lector   VARCHAR(30),
    identificacion  VARCHAR(40),
    telefono        VARCHAR(30),
    tipo_lector     VARCHAR(20)  DEFAULT 'GENERAL',
    puede_acceder   BOOLEAN      DEFAULT TRUE,
    restriccion     VARCHAR(255),
    suspendido_hasta DATE,
    CONSTRAINT pk_usuarios PRIMARY KEY (id),
    CONSTRAINT uk_usuarios_username UNIQUE (username),
    CONSTRAINT uk_usuarios_codigo_lector UNIQUE (codigo_lector)
);

-- ==========================================================================
-- 2. LIBROS
--    Catalogo. "cantidad" es el total de ejemplares y "disponibles" los
--    ejemplares libres (se sincroniza con la tabla ejemplares).
-- ==========================================================================
CREATE TABLE libros (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    titulo           VARCHAR(200) NOT NULL,
    autor            VARCHAR(150) NOT NULL,
    isbn             VARCHAR(30),
    categoria        VARCHAR(80),
    anio_publicacion INTEGER,
    cantidad         INTEGER      NOT NULL DEFAULT 1,
    disponibles      INTEGER      NOT NULL DEFAULT 1,
    -- Campos bibliograficos adicionales --
    editorial        VARCHAR(150),
    descripcion      VARCHAR(1200),
    ubicacion        VARCHAR(120),
    portada_url      VARCHAR(500),
    estado           VARCHAR(20)  DEFAULT 'ACTIVO',
    fecha_registro   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_libros PRIMARY KEY (id),
    CONSTRAINT uk_libros_isbn UNIQUE (isbn),
    CONSTRAINT ck_libros_cantidad  CHECK (cantidad >= 1),
    CONSTRAINT ck_libros_disponibles CHECK (disponibles >= 0 AND disponibles <= cantidad),
    CONSTRAINT ck_libros_estado CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);

-- ==========================================================================
-- 3. EJEMPLARES
--    Copias fisicas de un libro. estado:
--    DISPONIBLE | PRESTADO | RESERVADO | MANTENIMIENTO | PERDIDO | BAJA
-- ==========================================================================
CREATE TABLE ejemplares (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    libro_id   BIGINT       NOT NULL,
    codigo     VARCHAR(40)  NOT NULL,
    ubicacion  VARCHAR(120),
    estado     VARCHAR(20)  NOT NULL DEFAULT 'DISPONIBLE',
    fecha_alta TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_ejemplares PRIMARY KEY (id),
    CONSTRAINT uk_ejemplares_codigo UNIQUE (codigo),
    CONSTRAINT fk_ejemplares_libro FOREIGN KEY (libro_id) REFERENCES libros (id),
    CONSTRAINT ck_ejemplares_estado CHECK (estado IN
        ('DISPONIBLE', 'PRESTADO', 'RESERVADO', 'MANTENIMIENTO', 'PERDIDO', 'BAJA'))
);

-- ==========================================================================
-- 4. PRESTAMOS
--    Tabla puente entre USUARIOS y LIBROS, con ejemplar concreto.
--    estado: PENDIENTE | DEVUELTO
-- ==========================================================================
CREATE TABLE prestamos (
    id               BIGINT      NOT NULL AUTO_INCREMENT,
    usuario_id       BIGINT      NOT NULL,
    libro_id         BIGINT      NOT NULL,
    ejemplar_id      BIGINT,
    fecha_prestamo   DATE        NOT NULL,
    fecha_limite     DATE        NOT NULL,
    fecha_devolucion DATE,
    estado           VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    renovaciones     INTEGER     DEFAULT 0,
    observaciones    VARCHAR(300),
    CONSTRAINT pk_prestamos PRIMARY KEY (id),
    CONSTRAINT fk_prestamos_usuario  FOREIGN KEY (usuario_id)  REFERENCES usuarios (id),
    CONSTRAINT fk_prestamos_libro    FOREIGN KEY (libro_id)    REFERENCES libros (id),
    CONSTRAINT fk_prestamos_ejemplar FOREIGN KEY (ejemplar_id) REFERENCES ejemplares (id),
    CONSTRAINT ck_prestamos_estado CHECK (estado IN ('PENDIENTE', 'DEVUELTO')),
    CONSTRAINT ck_prestamos_renovaciones CHECK (renovaciones >= 0)
);

-- ==========================================================================
-- 5. RESERVAS
--    Lista de espera de un libro sin ejemplares disponibles.
--    estado: PENDIENTE | DISPONIBLE | CUMPLIDA | CANCELADA | EXPIRADA
-- ==========================================================================
CREATE TABLE reservas (
    id               BIGINT      NOT NULL AUTO_INCREMENT,
    usuario_id       BIGINT      NOT NULL,
    libro_id         BIGINT      NOT NULL,
    fecha_solicitud  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_expiracion DATE,
    estado           VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    posicion         INTEGER,
    CONSTRAINT pk_reservas PRIMARY KEY (id),
    CONSTRAINT fk_reservas_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id),
    CONSTRAINT fk_reservas_libro   FOREIGN KEY (libro_id)   REFERENCES libros (id),
    CONSTRAINT ck_reservas_estado CHECK (estado IN
        ('PENDIENTE', 'DISPONIBLE', 'CUMPLIDA', 'CANCELADA', 'EXPIRADA'))
);

-- ==========================================================================
-- 6. AUDITORIA
--    Historial de acciones: quien hizo que y sobre que entidad.
-- ==========================================================================
CREATE TABLE auditoria (
    id         BIGINT        NOT NULL AUTO_INCREMENT,
    usuario    VARCHAR(50)   NOT NULL,
    accion     VARCHAR(30)   NOT NULL,
    entidad    VARCHAR(40),
    entidad_id BIGINT,
    detalle    VARCHAR(1000),
    fecha      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_auditoria PRIMARY KEY (id)
);

-- ==========================================================================
-- Indices para las consultas mas frecuentes
-- ==========================================================================
CREATE INDEX idx_prestamos_usuario  ON prestamos (usuario_id);
CREATE INDEX idx_prestamos_libro    ON prestamos (libro_id);
CREATE INDEX idx_prestamos_estado   ON prestamos (estado);
CREATE INDEX idx_ejemplares_libro   ON ejemplares (libro_id);
CREATE INDEX idx_ejemplares_estado  ON ejemplares (estado);
CREATE INDEX idx_reservas_libro     ON reservas (libro_id);
CREATE INDEX idx_reservas_estado    ON reservas (estado);
CREATE INDEX idx_auditoria_fecha    ON auditoria (fecha);
CREATE INDEX idx_libros_categoria   ON libros (categoria);

-- ==========================================================================
-- DATOS INICIALES
--    IMPORTANTE: la columna password guarda un hash BCrypt, no texto plano.
--    Spring Boot los genera automaticamente en DataInitializer al arrancar
--    (solo si la tabla usuarios esta vacia) y crea los ejemplares de cada
--    libro:
--      admin / admin123          (rol ADMIN)
--      3 lectores de demostracion (rol USUARIO, puede_acceder = FALSE)
--      4 libros de ejemplo con sus ejemplares
--    En produccion la contrasena del administrador se toma de las variables
--    de entorno APP_ADMIN_USERNAME / APP_ADMIN_PASSWORD.
-- ==========================================================================
