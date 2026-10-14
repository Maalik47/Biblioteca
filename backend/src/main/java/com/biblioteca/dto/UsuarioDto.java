package com.biblioteca.dto;

import com.biblioteca.entity.Usuario;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UsuarioDto(Long id, String username, String nombre, String apellido,
                         String email, String rol, boolean activo,
                         String codigoLector, String identificacion, String telefono,
                         String tipoLector, boolean puedeAcceder, String restriccion,
                         LocalDate suspendidoHasta, LocalDateTime fechaRegistro) {

    public static UsuarioDto from(Usuario u) {
        return new UsuarioDto(
                u.getId(),
                u.getUsername(),
                u.getNombre(),
                u.getApellido(),
                u.getEmail(),
                u.getRol(),
                Boolean.TRUE.equals(u.getActivo()),
                u.getCodigoLector(),
                u.getIdentificacion(),
                u.getTelefono(),
                u.getTipoLector(),
                Boolean.TRUE.equals(u.getPuedeAcceder()),
                u.getRestriccion(),
                u.getSuspendidoHasta(),
                u.getFechaRegistro());
    }
}
