package com.biblioteca.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import com.biblioteca.entity.Libro;
import com.biblioteca.entity.Usuario;
import com.biblioteca.repository.LibroRepository;
import com.biblioteca.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UsuarioRepository usuarioRepository;
    private final LibroRepository libroRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminUsername;
    private final String adminPassword;

    public DataInitializer(UsuarioRepository usuarioRepository,
                           LibroRepository libroRepository,
                           PasswordEncoder passwordEncoder,
                           @Value("${app.admin.username:}") String adminUsername,
                           @Value("${app.admin.password:}") String adminPassword) {
        this.usuarioRepository = usuarioRepository;
        this.libroRepository = libroRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        // El usuario administrador NO tiene credenciales por defecto.
        // Deben inyectarse con las variables de entorno APP_ADMIN_USERNAME y
        // APP_ADMIN_PASSWORD. Si no se definen, no se crea ningun usuario y el
        // registro debera realizarse por otra via (API o consola).
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
                usuarioRepository.save(admin);
                log.info("Usuario administrador creado con el usuario indicado en APP_ADMIN_USERNAME.");
            }
        }

        if (libroRepository.count() == 0) {
            libroRepository.save(libro("Cien años de soledad", "Gabriel García Márquez",
                    "978-0307474728", "Novela", 1967, 5));
            libroRepository.save(libro("Don Quijote de la Mancha", "Miguel de Cervantes",
                    "978-8420412146", "Clásico", 1605, 3));
            libroRepository.save(libro("El Principito", "Antoine de Saint-Exupéry",
                    "978-0156012195", "Infantil", 1943, 4));
            libroRepository.save(libro("1984", "George Orwell",
                    "978-0451524935", "Ciencia ficción", 1949, 2));
        }
    }

    private Libro libro(String titulo, String autor, String isbn, String categoria,
                        int anio, int cantidad) {
        Libro l = new Libro();
        l.setTitulo(titulo);
        l.setAutor(autor);
        l.setIsbn(isbn);
        l.setCategoria(categoria);
        l.setAnioPublicacion(anio);
        l.setCantidad(cantidad);
        l.setDisponibles(cantidad);
        return l;
    }
}