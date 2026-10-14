package com.biblioteca.dto;

import com.biblioteca.entity.Auditoria;

import java.time.LocalDateTime;

public record AuditoriaDto(Long id, String usuario, String accion, String entidad,
                           Long entidadId, String detalle, LocalDateTime fecha) {

    public static AuditoriaDto from(Auditoria a) {
        return new AuditoriaDto(a.getId(), a.getUsuario(), a.getAccion(), a.getEntidad(),
                a.getEntidadId(), a.getDetalle(), a.getFecha());
    }
}
