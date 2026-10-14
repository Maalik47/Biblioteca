package com.biblioteca.config;

import com.biblioteca.entity.Libro;
import com.biblioteca.entity.Usuario;
import com.biblioteca.repository.LibroRepository;
import com.biblioteca.repository.UsuarioRepository;
import com.biblioteca.service.EjemplarService;
import com.biblioteca.service.MigracionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UsuarioRepository usuarioRepository;
    private final LibroRepository libroRepository;
    private final EjemplarService ejemplarService;
    private final MigracionService migracionService;
    private final PasswordEncoder passwordEncoder;
    private final String adminUsername;
    private final String adminPassword;

    public DataInitializer(UsuarioRepository usuarioRepository,
                           LibroRepository libroRepository,
                           EjemplarService ejemplarService,
                           MigracionService migracionService,
                           PasswordEncoder passwordEncoder,
                           @Value("${app.admin.username:}") String adminUsername,
                           @Value("${app.admin.password:}") String adminPassword) {
        this.usuarioRepository = usuarioRepository;
        this.libroRepository = libroRepository;
        this.ejemplarService = ejemplarService;
        this.migracionService = migracionService;
        this.passwordEncoder = passwordEncoder;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        migracionService.normalizar();
        // El usuario administrador se crea una sola vez (base vacia) con los
        // valores de app.admin.username / app.admin.password. En local ambas
        // propiedades tienen por defecto admin/admin123; en produccion se
        // sobrescriben con APP_ADMIN_USERNAME y APP_ADMIN_PASSWORD.
        if (usuarioRepository.count() == 0) {
            if (adminUsername.isBlank() || adminPassword.isBlank()) {
                log.warn("No se creo el usuario administrador: faltan APP_ADMIN_USERNAME y/o "
                        + "APP_ADMIN_PASSWORD. Configuralas en el servidor para crear el primer usuario.");
            } else {
                Usuario admin = new Usuario();
                admin.setUsername(adminUsername);
                admin.setPassword(passwordEncoder.encode(adminPassword));
                admin.setNombre("Administrador");
                admin.setEmail("admin@biblioteca.com");
                admin.setRol("ADMIN");
                admin.setActivo(true);
                admin.setPuedeAcceder(true);
                admin.setCodigoLector("LEC-ADMIN");
                usuarioRepository.save(admin);
                log.info("Usuario administrador '{}' creado.", adminUsername);
            }
            sembrarLectores();
        }

        if (libroRepository.count() == 0) {
            Libro l1 = libro("Cien años de soledad", "Gabriel García Márquez",
                    "978-0307474728", "Novela", 1967, 5, "Estante A-1");
            Libro l2 = libro("Don Quijote de la Mancha", "Miguel de Cervantes",
                    "978-8420412146", "Clásico", 1605, 3, "Estante A-2");
            Libro l3 = libro("El Principito", "Antoine de Saint-Exupéry",
                    "978-0156012195", "Infantil", 1943, 4, "Estante B-1");
            Libro l4 = libro("1984", "George Orwell",
                    "978-0451524935", "Ciencia ficción", 1949, 2, "Estante B-2");

            for (Libro l : new Libro[]{l1, l2, l3, l4}) {
                Libro guardado = libroRepository.save(l);
                ejemplarService.generar(guardado, guardado.getCantidad());
            }
            log.info("Catálogo inicial sembrado con 4 libros y sus ejemplares.");
        }
    }

    /** Lectores de ejemplo SIN acceso al panel (puedeAcceder = false). */
    private void sembrarLectores() {
        lector("Ana", "Torres", "ana.torres@correo.com", "ESTUDIANTE", "1001", "3001112233");
        lector("Luis", "Ramírez", "luis.ramirez@correo.com", "DOCENTE", "1002", "3004445566");
        lector("Carmen", "Díaz", "carmen.diaz@correo.com", "EXTERNO", "1003", "3007778899");
        log.info("Lectores de ejemplo creados.");
    }

    private void lector(String nombre, String apellido, String email,
                        String tipo, String identificacion, String telefono) {
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setApellido(apellido);
        u.setEmail(email);
        u.setTipoLector(tipo);
        u.setIdentificacion(identificacion);
        u.setTelefono(telefono);
        u.setRol("USUARIO");
        u.setActivo(true);
        u.setPuedeAcceder(false);
        u.setUsername("lector_" + identificacion);
        u.setPassword(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
        usuarioRepository.save(u);
    }

    private Libro libro(String titulo, String autor, String isbn, String categoria,
                        int anio, int cantidad, String ubicacion) {
        Libro l = new Libro();
        l.setTitulo(titulo);
        l.setAutor(autor);
        l.setIsbn(isbn);
        l.setCategoria(categoria);
        l.setAnioPublicacion(anio);
        l.setCantidad(cantidad);
        l.setDisponibles(cantidad);
        l.setUbicacion(ubicacion);
        l.setEstado("ACTIVO");
        return l;
    }
}
