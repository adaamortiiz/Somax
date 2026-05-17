package com.somax.service;

import com.somax.dto.ClaseRequest;
import com.somax.dto.ClaseResponse;
import com.somax.exception.NotFoundException;
import com.somax.model.ClaseDirigida;
import com.somax.repository.ClaseDirigidaRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClaseService {
    private final ClaseDirigidaRepository claseRepository;

    @Transactional
    public ClaseResponse create(ClaseRequest request) {
        ClaseDirigida clase = ClaseDirigida.builder().nombre(request.getNombre())
                .descripcion(request.getDescripcion()).nivel(request.getNivel())
                .aforoMaximo(request.getAforoMaximo()).privada(request.isPrivada())
                .imagenUrl(request.getImagenUrl()).build();
        return toResponse(claseRepository.save(clase));
    }

    @Transactional
    public ClaseResponse update(Long id, ClaseRequest request) {
        ClaseDirigida clase = getEntity(id);
        clase.setNombre(request.getNombre());
        clase.setDescripcion(request.getDescripcion());
        clase.setNivel(request.getNivel());
        clase.setAforoMaximo(request.getAforoMaximo());
        clase.setPrivada(request.isPrivada());
        clase.setImagenUrl(request.getImagenUrl());
        return toResponse(clase);
    }

    @Transactional
    public void delete(Long id) {
        ClaseDirigida clase = getEntity(id);
        claseRepository.delete(clase);
    }

    public ClaseDirigida getEntity(Long id) {
        return claseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Clase no encontrada"));
    }

    public List<ClaseResponse> listAll() {
        return claseRepository.findAll().stream().map(this::toResponse).toList();
    }

    public ClaseResponse toResponse(ClaseDirigida clase) {
        return ClaseResponse.builder().id(clase.getId()).nombre(clase.getNombre())
                .descripcion(clase.getDescripcion()).nivel(clase.getNivel())
                .aforoMaximo(clase.getAforoMaximo()).privada(clase.isPrivada())
                .imagenUrl(clase.getImagenUrl()).build();
    }
}
