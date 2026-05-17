package com.somax.controller;

import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.somax.dto.NotificacionResponse;
import com.somax.service.NotificationService;
import lombok.RequiredArgsConstructor;
import com.somax.security.SomaxUserDetails;

@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping
    public List<NotificacionResponse> list(@AuthenticationPrincipal SomaxUserDetails userDetails) {
        return notificationService.listByUsuario(userDetails.getUsuario().getId());
    }
}
