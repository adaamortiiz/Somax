package com.somax.service;

import java.time.Year;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailTemplateService {
    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    /**
     * Envuelve un cuerpo HTML con un template corporativo.
     *
     * <p>Incluye logo por URL absoluta (compatible con Resend).</p>
     */
    public String wrap(String title, String bodyHtml) {
        String safeTitle = escape(title);
        String year = String.valueOf(Year.now().getValue());
        String base = frontendBaseUrl != null ? frontendBaseUrl : "";
        String logoUrl = base.endsWith("/") ? base + "img/logo-blanco-sin-bg.png"
                : base + "/img/logo-blanco-sin-bg.png";

        return """
                <div style="margin:0;padding:0;background:#0b0d10;">
                  <div style="margin:0 auto;max-width:680px;padding:28px 12px;font-family:Arial,Helvetica,sans-serif;">
                    <div style="background:linear-gradient(135deg,#2f80ff,#61a6ff);border-radius:16px 16px 0 0;padding:18px 20px;">
                      <div style="display:flex;align-items:center;gap:12px;">
                        <img src="%s" alt="Somax" style="height:40px;width:auto;display:block;" />
                        <div style="color:#ffffff;font-size:16px;font-weight:800;letter-spacing:0.08em;">SOMAX</div>
                      </div>
                    </div>

                    <div style="background:#ffffff;border-radius:0 0 16px 16px;overflow:hidden;border:1px solid rgba(255,255,255,0.08);">
                      <div style="padding:22px 20px;color:#101827;">
                        <h2 style="margin:0 0 12px 0;font-size:18px;line-height:1.35;">%s</h2>
                        <div style="font-size:14px;line-height:1.6;color:#111827;">
                          %s
                        </div>
                      </div>
                      <div style="padding:14px 20px;border-top:1px solid #eef2f6;color:#6b7280;font-size:12px;line-height:1.5;">
                        <div>© %s Somax · Mensaje automático, no responder.</div>
                        <div style="margin-top:6px;">
                          <a href="%s" style="color:#2f80ff;text-decoration:none;">Abrir Somax</a>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
                """
                .formatted(escapeUrl(logoUrl), safeTitle, bodyHtml, year, escapeUrl(base));
    }

    public String button(String href, String label) {
        return """
                <div style="text-align:center;margin:18px 0 10px;">
                  <a href="%s" style="display:inline-block;padding:12px 18px;border-radius:12px;background:#2f80ff;color:#fff;text-decoration:none;font-weight:700;">
                    %s
                  </a>
                </div>
                """.formatted(escapeUrl(href), escape(label));
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static String escapeUrl(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\"", "%22");
    }
}
