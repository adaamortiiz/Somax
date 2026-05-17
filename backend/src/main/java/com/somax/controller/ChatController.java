package com.somax.controller;

import com.somax.dto.ChatRequest;
import com.somax.dto.ChatResponse;
import com.somax.security.SomaxUserDetails;
import com.somax.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @PostMapping
    public ChatResponse preguntar(@Valid @RequestBody ChatRequest request,
            @AuthenticationPrincipal SomaxUserDetails userDetails) {
        String respuesta = chatService.preguntar(userDetails.getUsername(), request.getMensaje());
        return new ChatResponse(respuesta);
    }
}
