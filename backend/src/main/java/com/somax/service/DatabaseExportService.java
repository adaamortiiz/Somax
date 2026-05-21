package com.somax.service;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.stereotype.Service;

/**
 * Exporta la base de datos H2 a un fichero SQL legible (SCRIPT).
 *
 * <p>
 * H2 guarda el fichero {@code *.mv.db} en binario por diseño. Este export genera un
 * {@code .sql} en texto para inspección/backup.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DatabaseExportService {
    private final JdbcTemplate jdbcTemplate;

    @Value("${app.db.export-sql:true}")
    private boolean enabled;

    @Value("${app.db.export-sql-path:./data/somaxdb.sql}")
    private String path;

    @EventListener(ApplicationReadyEvent.class)
    public void exportOnStart() {
        export("startup");
    }

    @PreDestroy
    public void exportOnStop() {
        export("shutdown");
    }

    private void export(String reason) {
        if (!enabled) {
            return;
        }
        try {
            jdbcTemplate.execute("SCRIPT TO '" + escapeSqlString(path) + "'");
            log.info("H2 export SQL ({}) -> {}", reason, path);
        } catch (Exception ex) {
            log.warn("No se pudo exportar H2 a SQL ({}) -> {}", reason, path, ex);
        }
    }

    private static String escapeSqlString(String value) {
        return value.replace("'", "''");
    }
}

