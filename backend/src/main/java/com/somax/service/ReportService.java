package com.somax.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;
import com.somax.model.Horario;
import com.somax.model.ReservaEstado;
import com.somax.model.Valoracion;
import com.somax.repository.HorarioRepository;
import com.somax.repository.ReservaRepository;
import com.somax.repository.ValoracionRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final HorarioRepository horarioRepository;
    private final ReservaRepository reservaRepository;
    private final ValoracionRepository valoracionRepository;

    public byte[] exportCsv() {
        List<ReportRow> rows = buildRows();
        StringBuilder csv = new StringBuilder();
        csv.append("Clase,Fecha,Monitor,Ocupadas,Aforo,Porcentaje,ValoracionMedia\n");
        for (ReportRow row : rows) {
            csv.append(escape(row.clase)).append(",").append(escape(row.fecha)).append(",")
                    .append(escape(row.monitor)).append(",").append(row.ocupadas).append(",")
                    .append(row.aforo).append(",").append(String.format("%.2f", row.porcentaje))
                    .append(",").append(String.format("%.2f", row.valoracionMedia)).append("\n");
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] exportPdf() {
        List<ReportRow> rows = buildRows();
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                float left = 50;
                float top = 770;
                float width = page.getMediaBox().getWidth() - 100;

                content.setNonStrokingColor(11 / 255f, 13 / 255f, 16 / 255f);
                content.addRect(0, 742, page.getMediaBox().getWidth(), 100);
                content.fill();

                byte[] logoBytes = loadResource("/static/img/logo-simbolo-fondo-blanco.png");
                if (logoBytes != null) {
                    var img = org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
                            .createFromByteArray(document, logoBytes, "somax-logo");
                    content.drawImage(img, left, 755, 34, 34);
                }

                content.beginText();
                content.setNonStrokingColor(1f, 1f, 1f);
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
                content.newLineAtOffset(left + 44, 768);
                content.showText("Reporte de ocupación");
                content.endText();

                content.beginText();
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                content.newLineAtOffset(left + 44, 750);
                content.showText("Somax · Exportación CSV/PDF");
                content.endText();

                float y = 715;
                content.setNonStrokingColor(16 / 255f, 24 / 255f, 39 / 255f);
                content.addRect(left, y, width, 22);
                content.fill();

                content.beginText();
                content.setNonStrokingColor(1f, 1f, 1f);
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 9);
                content.newLineAtOffset(left + 8, y + 7);
                content.showText("Clase");
                content.newLineAtOffset(190, 0);
                content.showText("Fecha");
                content.newLineAtOffset(110, 0);
                content.showText("Ocup.");
                content.newLineAtOffset(45, 0);
                content.showText("Aforo");
                content.newLineAtOffset(45, 0);
                content.showText("%");
                content.endText();

                y -= 16;
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 9);
                for (ReportRow row : rows) {
                    if (y < 70) {
                        break;
                    }
                    content.beginText();
                    content.setNonStrokingColor(16 / 255f, 24 / 255f, 39 / 255f);
                    content.newLineAtOffset(left + 8, y);
                    content.showText(truncate(row.clase, 28));
                    content.newLineAtOffset(190, 0);
                    content.showText(truncate(row.fecha, 16));
                    content.newLineAtOffset(110, 0);
                    content.showText(String.valueOf(row.ocupadas));
                    content.newLineAtOffset(45, 0);
                    content.showText(String.valueOf(row.aforo));
                    content.newLineAtOffset(45, 0);
                    content.showText(String.format("%.1f", row.porcentaje));
                    content.endText();
                    y -= 14;
                }

                content.beginText();
                content.setNonStrokingColor(107 / 255f, 114 / 255f, 128 / 255f);
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE), 8);
                content.newLineAtOffset(left, 40);
                content.showText("Generado por Somax · " + java.time.LocalDateTime.now());
                content.endText();
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            return outputStream.toByteArray();
        } catch (IOException ex) {
            return new byte[0];
        }
    }

    private List<ReportRow> buildRows() {
        List<Horario> horarios = horarioRepository.findAllWithClaseAndMonitor();
        List<ReportRow> rows = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (Horario horario : horarios) {
            int aforo = horario.getClase().getAforoMaximo();
            int ocupadas = (int) reservaRepository.countByHorario_IdAndEstado(horario.getId(),
                    ReservaEstado.CONFIRMADA);
            double porcentaje = aforo == 0 ? 0 : (ocupadas * 100.0) / aforo;

            List<Valoracion> valoraciones =
                    valoracionRepository.findByHorario_Id(horario.getId());
            double promedio =
                    valoraciones.stream().mapToInt(Valoracion::getPuntuacion).average().orElse(0.0);

            rows.add(new ReportRow(horario.getClase().getNombre(),
                    horario.getFechaHoraInicio().format(formatter),
                    horario.getMonitor() == null ? "" : horario.getMonitor().getNombre(), ocupadas,
                    aforo, porcentaje, promedio));
        }

        return rows;
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            String escaped = value.replace("\"", "\"\"");
            return "\"" + escaped + "\"";
        }
        return value;
    }

    private record ReportRow(String clase, String fecha, String monitor, int ocupadas, int aforo,
            double porcentaje, double valoracionMedia) {
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return "";
        }
        if (value.length() <= max) {
            return value;
        }
        return value.substring(0, Math.max(0, max - 1)) + "…";
    }

    private static byte[] loadResource(String path) {
        try (var is = ReportService.class.getResourceAsStream(path)) {
            if (is == null) {
                return null;
            }
            return is.readAllBytes();
        } catch (IOException ex) {
            return null;
        }
    }
}
