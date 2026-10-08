package com.anjar.portfolio.service;

import com.anjar.portfolio.dto.DatabaseStatusDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseStatusService {

    private final DataSource dataSource;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${app.backup.mode:DOCKER}")
    private String backupMode;

    // ============================================================
    // CEK STATUS DB
    // ============================================================
    public DatabaseStatusDTO checkStatus() {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();

            return DatabaseStatusDTO.builder()
                    .connected(true)
                    .version(meta.getDatabaseProductVersion())
                    .databaseName(conn.getCatalog())
                    .host(extractHost())
                    .port(extractPort())
                    .backupMode(backupMode)
                    .checkedAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("❌ Database status check failed", e);
            return DatabaseStatusDTO.builder()
                    .connected(false)
                    .errorMessage(e.getMessage())
                    .host(extractHost())
                    .port(extractPort())
                    .backupMode(backupMode)
                    .checkedAt(LocalDateTime.now())
                    .build();
        }
    }

    // ============================================================
    // HELPERS
    // ============================================================
    private String extractHost() {
        String url = datasourceUrl;
        if (!url.contains("://")) return "unknown";

        String afterScheme = url.substring(url.indexOf("://") + 3);

        // Strip user:pass@
        int atIdx = afterScheme.indexOf('@');
        if (atIdx >= 0) afterScheme = afterScheme.substring(atIdx + 1);

        int colonIdx = afterScheme.indexOf(':');
        int slashIdx = afterScheme.indexOf('/');
        int end = (colonIdx > 0 && colonIdx < slashIdx) ? colonIdx : slashIdx;

        return afterScheme.substring(0, end);
    }

    private Integer extractPort() {
        try {
            String url = datasourceUrl;
            String afterScheme = url.substring(url.indexOf("://") + 3);

            int atIdx = afterScheme.indexOf('@');
            if (atIdx >= 0) afterScheme = afterScheme.substring(atIdx + 1);

            int colonIdx = afterScheme.indexOf(':');
            int slashIdx = afterScheme.indexOf('/');

            if (colonIdx < 0 || colonIdx > slashIdx) return 5432;
            return Integer.parseInt(afterScheme.substring(colonIdx + 1, slashIdx));
        } catch (Exception e) {
            return 5432;
        }
    }
}