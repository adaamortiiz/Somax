package com.somax.controller;

import com.somax.dto.ClaseRequest;
import com.somax.dto.ClaseResponse;
import com.somax.service.ClaseService;
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

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ClaseController {
    private final ClaseService claseService;

    @GetMapping("/public/clases")
    public List<ClaseResponse> listPublic() {
        return claseService.listAll();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/clases")
    public ClaseResponse create(@Valid @RequestBody ClaseRequest request) {
        return claseService.create(request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/clases/{id}")
    public ClaseResponse update(@PathVariable Long id, @Valid @RequestBody ClaseRequest request) {
        return claseService.update(id, request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/clases/{id}")
    public void delete(@PathVariable Long id) {
        claseService.delete(id);
    }
}
