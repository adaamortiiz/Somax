package com.somax.service;

import java.time.Year;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailTemplateService {
    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    public String wrap(String title, String bodyHtml) {
        String logoUrl = frontendBaseUrl + "/img/logo-simbolo-fondo-blanco.png";
        return """
                <div style="background:#0b0d10;padding:24px 12px;font-family:Arial,Helvetica,sans-serif;">
                  <div style="max-width:680px;margin:0 auto;background:#ffffff;border-radius:12px;overflow:hidden;">
                    <div style="background:#0b0d10;color:#fff;padding:18px 20px;display:flex;align-items:center;gap:12px;">
                      <img src="%s" alt="Somax" style="height:40px;width:auto;display:block;" />
                      <div style="font-size:16px;font-weight:700;letter-spacing:0.04em;">SOMAX</div>
                    </div>
                    <div style="padding:22px 20px;color:#101827;">
                      <h2 style="margin:0 0 12px 0;font-size:18px;">%s</h2>
                      %s
                    </div>
                    <div style="padding:14px 20px;border-top:1px solid #eef2f6;color:#6b7280;font-size:12px;">
                      © %d Somax · Mensaje automático, no responder.
                    </div>
                  </div>
                </div>
                """
                .formatted(logoUrl, escape(title), bodyHtml, Year.now().getValue());
    }

    public String button(String href, String label) {
        return """
                <p style="text-align:center;margin:18px 0;">
                  <a href="%s" style="display:inline-block;padding:12px 18px;border-radius:10px;background:#2f80ff;color:#fff;text-decoration:none;font-weight:600;">
                    %s
                  </a>
                </p>
                """.formatted(href, escape(label));
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}

