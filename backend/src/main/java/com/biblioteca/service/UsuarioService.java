package com.biblioteca.service;

import com.biblioteca.dto.LectorRequest;
import com.biblioteca.dto.PaginaResponse;
import com.biblioteca.dto.RegistroRequest;
import com.biblioteca.dto.UsuarioDto;
import com.biblioteca.entity.Usuario;
import com.biblioteca.exception.ConflictoException;
import com.biblioteca.exception.RecursoNoEncontradoException;
import com.biblioteca.exception.SolicitudInvalidaException;
import com.biblioteca.repository.PrestamoRepository;
import com.biblioteca.repository.UsuarioRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PrestamoRepository prestamoRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaService auditoriaService;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          PrestamoRepository prestamoRepository,
                          PasswordEncoder passwordEncoder,
                          AuditoriaService auditoriaService) {
        this.usuarioRepository = usuarioRepository;
        this.prestamoRepository = prestamoRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditoriaService = auditoriaService;
    }

    public List<UsuarioDto> listar() {
        return usuarioRepository.findAll(Sort.by("nombre")).stream().map(UsuarioDto::from).toList();
    }

    public PaginaResponse<UsuarioDto> buscar(String q, String rol, String tipoLector,
                                             Boolean soloAcceso, int pagina, int tamano) {
        var page = usuarioRepository.buscar(normalizar(q), normalizar(rol), normalizar(tipoLector),
                soloAcceso,
                PageRequest.of(Math.max(0, pagina), Math.max(1, Math.min(tamano, 50)),
                        Sort.by("nombre")));
        return PaginaResponse.desde(page, UsuarioDto::from);
    }

    public UsuarioDto obtener(Long id) {
        return UsuarioDto.from(buscar(id));
    }

    public Usuario buscarEntidad(Long id) {
        return buscar(id);
    }

    public Usuario porUsername(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }

    // ------------------------------------------------------------------
    // Registro de una cuenta propia desde el login. La cuenta nace con
    // rol USUARIO y acceso al panel; un administrador puede elevar rol o
    // editar el perfil despues.
    // ------------------------------------------------------------------
    @Transactional
    public UsuarioDto registrarCuenta(RegistroRequest req) {
        if (normalizar(req.nombre()) == null) {
            throw new SolicitudInvalidaException("El nombre es obligatorio");
        }
        if (normalizar(req.email()) == null || !req.email().trim().contains("@")) {
            throw new SolicitudInvalidaException("El correo electrónico no es válido");
        }
        if (normalizar(req.username()) == null) {
            throw new SolicitudInvalidaException("El nombre de usuario es obligatorio");
        }
        if (req.password() == null || req.password().length() < 6) {
            throw new SolicitudInvalidaException("La contraseña debe tener al menos 6 caracteres");
        }

        Usuario u = new Usuario();
        u.setUsername(req.username().trim());
        u.setPassword(req.password());
        u.setNombre(req.nombre().trim());
        u.setApellido(req.apellido() == null ? null : req.apellido().trim());
        u.setEmail(req.email().trim());
        u.setTipoLector("GENERAL");
        return crear(u);
    }

    // ------------------------------------------------------------------
    // Alta de lector (puede o no tener acceso al panel).
    // ------------------------------------------------------------------
    @Transactional
    public UsuarioDto crearLector(LectorRequest req) {
        boolean puedeAcceder = Boolean.TRUE.equals(req.puedeAcceder())
                || (req.username() != null && !req.username().isBlank()
                    && req.password() != null && !req.password().isBlank());

        String username = req.username() != null && !req.username().isBlank()
                ? req.username().trim() : null;
        if (Boolean.TRUE.equals(puedeAcceder)) {
            if (username == null) {
                throw new SolicitudInvalidaException("Para dar acceso al panel se requiere un usuario y contraseña");
            }
            if (req.password() == null || req.password().isBlank()) {
                throw new SolicitudInvalidaException("Para dar acceso al panel se requiere una contraseña");
            }
            if (usuarioRepository.existsByUsername(username)) {
                throw new ConflictoException("El nombre de usuario ya existe");
            }
        }

        Usuario u = new Usuario();
        aplicarDatos(u, req);
        u.setUsername(username != null ? username : generarUsername(req));
        u.setPassword(passwordEncoder.encode(
                req.password() != null && !req.password().isBlank() ? req.password() : randomPassword()));
        u.setPuedeAcceder(puedeAcceder);
        u.setRol(req.rol() != null && !req.rol().isBlank() ? req.rol().toUpperCase() : "USUARIO");
        u.setActivo(req.activo() == null ? true : req.activo());
        u.setCodigoLector(codigoLectorUnico());
        Usuario guardado = usuarioRepository.save(u);
        auditoriaService.registrar("CREAR", "Lector", guardado.getId(),
                "Alta de lector '" + nombreCompleto(guardado) + "'");
        return UsuarioDto.from(guardado);
    }

    // ------------------------------------------------------------------
    // Compatibilidad con el alta de usuario existente.
    // ------------------------------------------------------------------
    @Transactional
    public UsuarioDto crear(Usuario usuario) {
        if (usuario.getUsername() == null || usuario.getUsername().isBlank()) {
            throw new SolicitudInvalidaException("El nombre de usuario es obligatorio");
        }
        if (usuarioRepository.existsByUsername(usuario.getUsername().trim())) {
            throw new ConflictoException("El nombre de usuario ya existe");
        }
        if (usuario.getPassword() == null || usuario.getPassword().isBlank()) {
            throw new SolicitudInvalidaException("La contraseña es obligatoria al crear un usuario");
        }
        usuario.setId(null);
        usuario.setUsername(usuario.getUsername().trim());
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuario.setRol(usuario.getRol() == null || usuario.getRol().isBlank() ? "USUARIO" : usuario.getRol().toUpperCase());
        usuario.setActivo(usuario.getActivo() == null ? true : usuario.getActivo());
        usuario.setPuedeAcceder(true);
        if (usuario.getTipoLector() == null || usuario.getTipoLector().isBlank()) {
            usuario.setTipoLector("GENERAL");
        }
        if (usuario.getCodigoLector() == null || usuario.getCodigoLector().isBlank()) {
            usuario.setCodigoLector(codigoLectorUnico());
        }
        Usuario guardado = usuarioRepository.save(usuario);
        auditoriaService.registrar("CREAR", "Usuario", guardado.getId(),
                "Alta de usuario '" + guardado.getUsername() + "'");
        return UsuarioDto.from(guardado);
    }

    @Transactional
    public UsuarioDto actualizar(Long id, Usuario usuario) {
        Usuario existente = buscar(id);
        if (usuario.getUsername() != null && !usuario.getUsername().isBlank()) {
            String nuevo = usuario.getUsername().trim();
            if (usuarioRepository.existsByUsernameAndIdNot(nuevo, id)) {
                throw new ConflictoException("El nombre de usuario ya existe");
            }
            existente.setUsername(nuevo);
        }
        if (usuario.getPassword() != null && !usuario.getPassword().isBlank()) {
            existente.setPassword(passwordEncoder.encode(usuario.getPassword()));
        }
        if (usuario.getNombre() != null && !usuario.getNombre().isBlank()) {
            existente.setNombre(usuario.getNombre());
        }
        existente.setApellido(usuario.getApellido());
        if (usuario.getEmail() != null && !usuario.getEmail().isBlank()) {
            existente.setEmail(usuario.getEmail());
        }
        if (usuario.getRol() != null && !usuario.getRol().isBlank()) {
            existente.setRol(usuario.getRol().toUpperCase());
        }
        if (usuario.getActivo() != null) {
            existente.setActivo(usuario.getActivo());
        }
        copiarCamposLector(existente, usuario);
        Usuario guardado = usuarioRepository.save(existente);
        auditoriaService.registrar("ACTUALIZAR", "Usuario", id,
                "Edición del usuario '" + guardado.getUsername() + "'");
        return UsuarioDto.from(guardado);
    }

    @Transactional
    public UsuarioDto actualizarLector(Long id, LectorRequest req) {
        Usuario existente = buscar(id);
        aplicarDatos(existente, req);
        if (req.rol() != null && !req.rol().isBlank()) {
            existente.setRol(req.rol().toUpperCase());
        }
        if (req.activo() != null) {
            existente.setActivo(req.activo());
        }
        if (req.puedeAcceder() != null) {
            if (req.puedeAcceder()) {
                if (req.username() == null || req.username().isBlank()) {
                    throw new SolicitudInvalidaException("Para dar acceso al panel se requiere un usuario");
                }
                existente.setUsername(req.username().trim());
            }
            existente.setPuedeAcceder(req.puedeAcceder());
        }
        if (req.password() != null && !req.password().isBlank()) {
            existente.setPassword(passwordEncoder.encode(req.password()));
        }
        if (existente.getUsername() == null || existente.getUsername().isBlank()) {
            existente.setUsername(generarUsername(req));
        }
        Usuario guardado = usuarioRepository.save(existente);
        auditoriaService.registrar("ACTUALIZAR", "Lector", id,
                "Edición del lector '" + nombreCompleto(guardado) + "'");
        return UsuarioDto.from(guardado);
    }

    @Transactional
    public void eliminar(Long id, Long idSesion) {
        if (id.equals(idSesion)) {
            throw new ConflictoException("No puedes eliminar tu propio usuario");
        }
        Usuario u = buscar(id);
        long activos = prestamoRepository.countByUsuarioIdAndEstado(id, "PENDIENTE");
        if (activos > 0) {
            throw new ConflictoException("No se puede eliminar: el lector tiene préstamos activos");
        }
        usuarioRepository.delete(u);
        auditoriaService.registrar("ELIMINAR", "Usuario", id, "Baja de '" + nombreCompleto(u) + "'");
    }

    private void aplicarDatos(Usuario u, LectorRequest req) {
        u.setNombre(req.nombre().trim());
        u.setApellido(req.apellido());
        u.setEmail(req.email().trim());
        u.setIdentificacion(req.identificacion());
        u.setTelefono(req.telefono());
        u.setTipoLector(req.tipoLector() != null && !req.tipoLector().isBlank() ? req.tipoLector() : "GENERAL");
        u.setRestriccion(req.restriccion());
    }

    private void copiarCamposLector(Usuario destino, Usuario origen) {
        if (origen.getIdentificacion() != null) destino.setIdentificacion(origen.getIdentificacion());
        if (origen.getTelefono() != null) destino.setTelefono(origen.getTelefono());
        if (origen.getTipoLector() != null && !origen.getTipoLector().isBlank()) destino.setTipoLector(origen.getTipoLector());
        if (origen.getPuedeAcceder() != null) destino.setPuedeAcceder(origen.getPuedeAcceder());
        if (origen.getRestriccion() != null) destino.setRestriccion(origen.getRestriccion());
        if (origen.getSuspendidoHasta() != null) destino.setSuspendidoHasta(origen.getSuspendidoHasta());
    }

    private Usuario buscar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }

    private String codigoLectorUnico() {
        long n = usuarioRepository.count() + 1;
        String codigo;
        do {
            codigo = String.format("LEC-%05d", n++);
        } while (usuarioRepository.findByCodigoLector(codigo).isPresent());
        return codigo;
    }

    private String generarUsername(LectorRequest req) {
        String base = (req.nombre() + (req.apellido() == null ? "" : req.apellido()))
                .toLowerCase().replaceAll("[^a-z0-9]", "");
        if (base.isBlank()) base = "lector";
        String candidato = base;
        int i = 1;
        while (usuarioRepository.existsByUsername(candidato)) {
            candidato = base + i++;
        }
        return candidato;
    }

    private String randomPassword() {
        return java.util.UUID.randomUUID().toString();
    }

    private String nombreCompleto(Usuario u) {
        return u.getNombre() + (u.getApellido() != null && !u.getApellido().isBlank() ? " " + u.getApellido() : "");
    }

    private String normalizar(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }
}
