package com.somax.controller;

import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.somax.dto.ValoracionRequest;
import com.somax.dto.ValoracionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.somax.security.SomaxUserDetails;
import com.somax.service.ValoracionService;

@RestController
@RequestMapping("/api/valoraciones")
@RequiredArgsConstructor
public class ValoracionController {
    private final ValoracionService valoracionService;

    @PostMapping
    public ValoracionResponse crear(@Valid @RequestBody ValoracionRequest request,
            @AuthenticationPrincipal SomaxUserDetails userDetails) {
        return valoracionService.crear(userDetails.getUsername(), request);
    }

    @GetMapping("/horario/{horarioId}")
    public List<ValoracionResponse> listByHorario(@PathVariable Long horarioId) {
        return valoracionService.listByHorario(horarioId);
    }
}
