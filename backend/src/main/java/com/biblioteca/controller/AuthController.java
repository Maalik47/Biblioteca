package com.biblioteca.controller;

import com.biblioteca.api.ApiResponse;
import com.biblioteca.dto.LoginRequest;
import com.biblioteca.dto.LoginResponse;
import com.biblioteca.dto.RegistroRequest;
import com.biblioteca.dto.UsuarioDto;
import com.biblioteca.entity.Usuario;
import com.biblioteca.repository.UsuarioRepository;
import com.biblioteca.security.JwtUtil;
import com.biblioteca.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioService usuarioService;

    public AuthController(UsuarioRepository usuarioRepository, JwtUtil jwtUtil,
                          PasswordEncoder passwordEncoder, UsuarioService usuarioService) {
        this.usuarioRepository = usuarioRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.usuarioService = usuarioService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
        Usuario usuario = usuarioRepository.findByUsername(request.username())
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

        if (!passwordEncoder.matches(request.password(), usuario.getPassword())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }
        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new AccessDeniedException("El usuario está inactivo");
        }
        if (!Boolean.TRUE.equals(usuario.getPuedeAcceder())) {
            throw new AccessDeniedException("Esta cuenta no tiene acceso al panel");
        }
        if (usuario.getSuspendidoHasta() != null
                && usuario.getSuspendidoHasta().isAfter(java.time.LocalDate.now())) {
            throw new AccessDeniedException("Cuenta suspendida hasta " + usuario.getSuspendidoHasta());
        }

        String nombreCompleto = usuario.getNombre()
                + (usuario.getApellido() != null && !usuario.getApellido().isBlank()
                        ? " " + usuario.getApellido() : "");
        LoginResponse response = new LoginResponse(
                jwtUtil.generateToken(usuario),
                usuario.getId(),
                usuario.getUsername(),
                nombreCompleto,
                usuario.getRol());

        return ResponseEntity.ok(ApiResponse.ok("Bienvenido", response));
    }

    @PostMapping("/registro")
    public ResponseEntity<ApiResponse<String>> registro(@RequestBody RegistroRequest request) {
        UsuarioDto creado = usuarioService.registrarCuenta(request);
        return ResponseEntity.ok(ApiResponse.ok(
                "Cuenta creada como usuario. Ahora puedes iniciar sesión.", creado.username()));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UsuarioDto>> me(Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(UsuarioDto.from(usuario)));
    }
}