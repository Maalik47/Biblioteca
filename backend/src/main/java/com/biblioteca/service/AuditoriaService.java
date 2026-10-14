package com.biblioteca.service;

import com.biblioteca.entity.Auditoria;
import com.biblioteca.entity.Usuario;
import com.biblioteca.repository.AuditoriaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;

    public AuditoriaService(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    /** Registra una accion del usuario autenticado (o "sistema"). */
    public void registrar(String accion, String entidad, Long entidadId, String detalle) {
        auditoriaRepository.save(new Auditoria(usuarioActual(), accion, entidad, entidadId, detalle));
    }

    public Page<Auditoria> buscar(String q, String accion, int pagina, int tamano) {
        return auditoriaRepository.buscar(normalizar(q), normalizar(accion),
                PageRequest.of(Math.max(0, pagina), Math.max(1, Math.min(tamano, 100)),
                        Sort.by(Sort.Direction.DESC, "fecha")));
    }

    public List<Auditoria> ultimas() {
        return auditoriaRepository.findTop15ByOrderByFechaDesc();
    }

    private String usuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Usuario u) {
            return u.getUsername();
        }
        return "sistema";
    }

    private String normalizar(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }
}
