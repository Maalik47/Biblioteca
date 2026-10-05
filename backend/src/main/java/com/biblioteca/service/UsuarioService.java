package com.biblioteca.service;

import com.biblioteca.dto.UsuarioDto;
import com.biblioteca.entity.Usuario;
import com.biblioteca.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UsuarioDto> listar() {
        return usuarioRepository.findAll().stream().map(UsuarioDto::from).toList();
    }

    public UsuarioDto obtener(Long id) {
        return UsuarioDto.from(buscar(id));
    }

    public UsuarioDto crear(Usuario usuario) {
        if (usuario.getUsername() == null || usuario.getUsername().isBlank()) {
            throw new RuntimeException("El nombre de usuario es obligatorio");
        }
        if (usuarioRepository.findByUsername(usuario.getUsername().trim()).isPresent()) {
            throw new RuntimeException("El nombre de usuario ya existe");
        }
        if (usuario.getPassword() == null || usuario.getPassword().isBlank()) {
            throw new RuntimeException("La contraseña es obligatoria al crear un usuario");
        }
        usuario.setId(null);
        usuario.setUsername(usuario.getUsername().trim());
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuario.setRol(usuario.getRol() == null || usuario.getRol().isBlank() ? "USUARIO" : usuario.getRol());
        usuario.setActivo(usuario.getActivo() == null ? true : usuario.getActivo());
        return UsuarioDto.from(usuarioRepository.save(usuario));
    }

    public UsuarioDto actualizar(Long id, Usuario usuario) {
        Usuario existente = buscar(id);
        if (usuario.getUsername() != null && !usuario.getUsername().isBlank()) {
            String nuevoUsername = usuario.getUsername().trim();
            usuarioRepository.findByUsername(nuevoUsername).ifPresent(other -> {
                if (!other.getId().equals(id)) {
                    throw new RuntimeException("El nombre de usuario ya existe");
                }
            });
            existente.setUsername(nuevoUsername);
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
            existente.setRol(usuario.getRol());
        }
        if (usuario.getActivo() != null) {
            existente.setActivo(usuario.getActivo());
        }
        return UsuarioDto.from(usuarioRepository.save(existente));
    }

    public void eliminar(Long id, Long idSesion) {
        if (id.equals(idSesion)) {
            throw new RuntimeException("No puedes eliminar tu propio usuario");
        }
        buscar(id);
        usuarioRepository.deleteById(id);
    }

    private Usuario buscar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }
}