package com.biblioteca.dto;

import com.biblioteca.entity.Usuario;

public record UsuarioDto(Long id, String username, String nombre, String apellido,
                         String email, String rol, boolean activo) {

    public static UsuarioDto from(Usuario u) {
        return new UsuarioDto(
                u.getId(),
                u.getUsername(),
                u.getNombre(),
                u.getApellido(),
                u.getEmail(),
                u.getRol(),
                Boolean.TRUE.equals(u.getActivo()));
    }
}