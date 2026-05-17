package com.somax.controller;

import com.somax.dto.HorarioRequest;
import com.somax.dto.HorarioResponse;
import com.somax.service.HorarioService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.somax.security.SomaxUserDetails;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HorarioController {
    private final HorarioService horarioService;

    @GetMapping("/public/horarios")
    public List<HorarioResponse> listPublic() {
        return horarioService.listUpcoming();
    }

    @GetMapping("/horarios")
    public List<HorarioResponse> listPrivate() {
        return horarioService.listUpcoming();
    }

    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
    @GetMapping("/staff/horarios")
    public List<HorarioResponse> listForMonitor(
            @AuthenticationPrincipal SomaxUserDetails userDetails) {
        return horarioService.listByMonitor(userDetails.getUsuario().getId());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/horarios")
    public HorarioResponse create(@Valid @RequestBody HorarioRequest request) {
        return horarioService.create(request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/horarios/{id}")
    public HorarioResponse update(@PathVariable Long id,
            @Valid @RequestBody HorarioRequest request) {
        return horarioService.update(id, request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/horarios/{id}")
    public void delete(@PathVariable Long id) {
        horarioService.delete(id);
    }
}
