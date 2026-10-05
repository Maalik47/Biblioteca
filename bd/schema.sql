-- ==========================================================================
--  Gestion de Biblioteca - Esquema de base de datos
--  Motor: H2 (archivo) - compatible con MySQL/MariaDB y PostgreSQL
--  Descripcion: este script crea las tablas que Spring Boot genera por JPA
--                (spring.jpa.hibernate.ddl-auto=update). Sirve para
--                reconstruir la base de datos desde cero.
--
--  EJECUCION (consola H2):
--    java -cp h2.jar org.h2.tools.RunScript -url "jdbc:h2:file:./data/biblioteca" ^
--         -user sa -script bd/schema.sql
--
--  EJECUCION (MySQL):
--    mysql -u root -p < bd/schema.sql
-- ==========================================================================

DROP TABLE IF EXISTS prestamos;
DROP TABLE IF EXISTS libros;
DROP TABLE IF EXISTS usuarios;

-- ==========================================================================
-- 1. USUARIOS
--    Cuentas de acceso al sistema. El campo password almacena el hash
--    BCrypt (nunca la contraseña en texto plano).
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
    CONSTRAINT pk_usuarios PRIMARY KEY (id),
    CONSTRAINT uk_usuarios_username UNIQUE (username)
);

-- ==========================================================================
-- 2. LIBROS
--    Catalogo. "cantidad" es el total de ejemplares y "disponibles" los
--    ejemplares libres (disponibles <= cantidad, validado por la aplicacion).
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
    fecha_registro   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_libros PRIMARY KEY (id),
    CONSTRAINT uk_libros_isbn UNIQUE (isbn),
    CONSTRAINT ck_libros_cantidad  CHECK (cantidad >= 1),
    CONSTRAINT ck_libros_disponibles CHECK (disponibles >= 0 AND disponibles <= cantidad)
);

-- ==========================================================================
-- 3. PRESTAMOS
--    Tabla puente entre USUARIOS y LIBROS (relacion N:M).
--    estado: PENDIENTE | DEVUELTO
-- ==========================================================================
CREATE TABLE prestamos (
    id              BIGINT      NOT NULL AUTO_INCREMENT,
    usuario_id      BIGINT      NOT NULL,
    libro_id        BIGINT      NOT NULL,
    fecha_prestamo  DATE        NOT NULL,
    fecha_limite    DATE        NOT NULL,
    fecha_devolucion DATE,
    estado          VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    CONSTRAINT pk_prestamos PRIMARY KEY (id),
    CONSTRAINT fk_prestamos_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id),
    CONSTRAINT fk_prestamos_libro   FOREIGN KEY (libro_id)   REFERENCES libros (id),
    CONSTRAINT ck_prestamos_estado  CHECK (estado IN ('PENDIENTE', 'DEVUELTO'))
);

-- ==========================================================================
-- Indices para las consultas mas frecuentes
-- ==========================================================================
CREATE INDEX idx_prestamos_usuario ON prestamos (usuario_id);
CREATE INDEX idx_prestamos_libro   ON prestamos (libro_id);
CREATE INDEX idx_prestamos_estado  ON prestamos (estado);
CREATE INDEX idx_libros_categoria  ON libros (categoria);

-- ==========================================================================
-- 4. DATOS INICIALES
--    IMPORTANTE: la columna password guarda un hash BCrypt, no texto plano.
--    Spring Boot los genera automaticamente en DataInitializer al arrancar
--    (solo si la tabla usuarios esta vacia):
--      admin / admin123  (rol ADMIN)
--      jperez / 123456   (rol USUARIO)
--      mgomez / 123456   (rol USUARIO)
--    Si desea sembrar datos por SQL, genere el hash con PasswordEncoder
--    y luego INSERT. Ejemplo de hash BCrypt para "admin123":
--      $2a$10$....................(generar con Spring PasswordEncoder)
-- ==========================================================================

-- INSERT INTO usuarios (username, password, nombre, apellido, email, rol, activo)
-- VALUES ('admin', '<BCrypt de admin123>', 'Administrador', NULL,
--         'admin@biblioteca.com', 'ADMIN', TRUE);

-- INSERT INTO libros (titulo, autor, isbn, categoria, anio_publicacion, cantidad, disponibles)
-- VALUES ('Cien anos de soledad', 'Gabriel Garcia Marquez', '978-0307474728',
--         'Novela', 1967, 5, 5),
--        ('Don Quijote de la Mancha', 'Miguel de Cervantes', '978-8420412146',
--         'Clasico', 1605, 3, 3);
