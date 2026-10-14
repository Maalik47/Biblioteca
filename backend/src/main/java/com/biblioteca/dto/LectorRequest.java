package com.biblioteca.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Alta/edicion de un lector. El password es opcional: si no se indica,
 * el lector no podra iniciar sesion (puedeAcceder = false).
 */
public record LectorRequest(
        @NotBlank(message = "el nombre es obligatorio")
        @Size(max = 100) String nombre,
        @Size(max = 100) String apellido,
        @NotBlank(message = "el email es obligatorio")
        @Email(message = "el email no es valido")
        String email,
        String username,
        String password,
        String identificacion,
        String telefono,
        String tipoLector,
        String rol,
        Boolean activo,
        Boolean puedeAcceder,
        String restriccion) {
}
