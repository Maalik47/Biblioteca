package com.biblioteca.dto;

/** Datos minimos para crear una cuenta propia desde el login. */
public record RegistroRequest(
        String username,
        String password,
        String nombre,
        String apellido,
        String email) {
}