package com.somax.service;

import com.somax.model.Usuario;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final UsuarioService usuarioService;

    @Value("${app.stackai.base-url}")
    private String baseUrl;

    @Value("${app.stackai.api-key}")
    private String apiKey;

    @Value("${app.stackai.workflow-id}")
    private String workflowId;

    public String preguntar(String email, String mensaje) {
        Usuario usuario = usuarioService.getByEmail(email);

        if (apiKey == null || apiKey.startsWith("CHANGE_ME") || workflowId == null
                || workflowId.startsWith("CHANGE_ME")) {
            return "El asistente IA esta en modo demo. Configura la clave de Stack.ai para obtener respuestas reales.";
        }

        String url = baseUrl + "/v1/workflows/" + workflowId + "/invoke";
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> payload = Map.of("input", mensaje, "user",
                Map.of("email", usuario.getEmail(), "nombre", usuario.getNombre()));

        try {
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            Map response = restTemplate.postForObject(url, entity, Map.class);
            if (response != null) {
                Object output = response.get("output");
                if (output != null) {
                    return output.toString();
                }
                Object data = response.get("data");
                if (data != null) {
                    return data.toString();
                }
            }
        } catch (Exception ex) {
            return "No se pudo contactar con la IA en este momento. Intentalo mas tarde.";
        }

        return "No se recibio respuesta del asistente IA.";
    }
}
