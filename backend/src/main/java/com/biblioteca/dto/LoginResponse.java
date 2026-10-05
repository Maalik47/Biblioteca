package com.biblioteca.dto;

public record LoginResponse(String token, Long id, String username, String nombre, String rol) {
}