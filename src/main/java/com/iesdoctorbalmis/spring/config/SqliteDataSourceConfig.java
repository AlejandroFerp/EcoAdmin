package com.iesdoctorbalmis.spring.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.zaxxer.hikari.HikariDataSource;

@Configuration
public class SqliteDataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(SqliteDataSourceConfig.class);
    private static final String SQLITE_PREFIX = "jdbc:sqlite:";

    @Bean
    @Primary
    public DataSource ecoadminDataSource(
            DataSourceProperties properties,
            @Value("${spring.datasource.url:}") String configuredUrl,
            @Value("${ecoadmin.db.path:}") String configuredPath,
            @Value("${ecoadmin.sqlite.reset-on-start:false}") boolean resetOnStart,
            @Value("${ecoadmin.sqlite.reset-on-legacy-schema:true}") boolean resetOnLegacySchema) {
        String resolvedUrl = resolveUrl(configuredUrl, configuredPath);

        if (resetOnStart) {
            resetSqliteFile(resolvedUrl, "reset-on-start habilitado");
        } else if (resetOnLegacySchema) {
            resetLegacySqliteIfNeeded(resolvedUrl);
        }

        if (resolvedUrl != null && resolvedUrl.startsWith(SQLITE_PREFIX)) {
            log.info("SQLite en uso: {}", resolvedUrl.substring(SQLITE_PREFIX.length()));
        }

        return properties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .url(resolvedUrl)
                .build();
    }

    static String resolveUrl(String configuredUrl, String configuredPath) {
        if (configuredUrl == null || configuredUrl.isBlank() || !configuredUrl.startsWith(SQLITE_PREFIX)) {
            return configuredUrl;
        }

        if (configuredPath != null && !configuredPath.isBlank()) {
            return SQLITE_PREFIX + Path.of(configuredPath).toAbsolutePath().normalize();
        }

        String relativeLocation = configuredUrl.substring(SQLITE_PREFIX.length());
        return SQLITE_PREFIX + resolveSqlitePath(relativeLocation);
    }

    static Path resolveSqlitePath(String relativeLocation) {
        Path configuredPath = Path.of(relativeLocation);
        if (configuredPath.isAbsolute()) {
            return configuredPath.normalize();
        }

        Path currentDir = Path.of("").toAbsolutePath().normalize();
        List<Path> candidates = List.of(
                currentDir.resolve("ServidorApiRest").resolve(relativeLocation).normalize(),
                currentDir.resolve(relativeLocation).normalize());

        for (Path candidate : candidates) {
            if (Files.exists(candidate)) {
                return candidate;
            }
        }

        if (Files.isDirectory(currentDir.resolve("ServidorApiRest"))) {
            return candidates.get(0);
        }

        return candidates.get(1);
    }

    static void resetSqliteFile(String resolvedUrl, String reason) {
        Optional<Path> sqlitePath = extractSqlitePath(resolvedUrl);
        if (sqlitePath.isEmpty() || !Files.exists(sqlitePath.get())) {
            return;
        }

        Path dbPath = sqlitePath.get();
        log.warn("Se elimina la base SQLite en {}: {}", dbPath, reason);
        deleteSqliteFile(dbPath);
        deleteSqliteFile(Path.of(dbPath + "-wal"));
        deleteSqliteFile(Path.of(dbPath + "-shm"));
    }

    static void resetLegacySqliteIfNeeded(String resolvedUrl) {
        Optional<Path> sqlitePath = extractSqlitePath(resolvedUrl);
        if (sqlitePath.isEmpty() || !Files.exists(sqlitePath.get())) {
            return;
        }

        Optional<String> resetReason = detectResetReason(sqlitePath.get());
        if (resetReason.isEmpty()) {
            return;
        }

        resetSqliteFile(resolvedUrl, resetReason.get());
    }

    static Optional<String> detectResetReason(Path sqlitePath) {
        try (Connection connection = DriverManager.getConnection(SQLITE_PREFIX + sqlitePath.toAbsolutePath().normalize())) {
            if (columnExists(connection, "residuos", "codigoler") || columnExists(connection, "residuos", "descripcion")) {
                return Optional.of("esquema legacy de residuos detectado");
            }
            if (columnExists(connection, "empresa", "direccion")
                    || columnExists(connection, "empresa", "ciudad")
                    || columnExists(connection, "empresa", "codigo_postal")
                    || columnExists(connection, "empresa", "codigoPostal")
                    || columnExists(connection, "empresa", "provincia")
                    || columnExists(connection, "empresa", "pais")) {
                return Optional.of("esquema legacy de empresa detectado");
            }
            if (columnExists(connection, "traslados", "fecha_ultimo_cambio_estado")
                    || columnExists(connection, "traslados", "fechaUltimoCambioEstado")) {
                return Optional.of("esquema legacy de traslados detectado");
            }
            if (columnExists(connection, "residuos", "codigo_ler") && !tableExists(connection, "lista_ler")) {
                return Optional.of("tabla lista_ler ausente para residuos canonicos");
            }
            if (hasNonCanonicalLerCodes(connection)) {
                return Optional.of("datos de residuos incompatibles con la lista LER canonica");
            }
            return Optional.empty();
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo validar la compatibilidad de la base SQLite en " + sqlitePath, ex);
        }
    }

    private static Optional<Path> extractSqlitePath(String resolvedUrl) {
        if (resolvedUrl == null || !resolvedUrl.startsWith(SQLITE_PREFIX)) {
            return Optional.empty();
        }

        String rawPath = resolvedUrl.substring(SQLITE_PREFIX.length());
        if (rawPath.isBlank() || ":memory:".equalsIgnoreCase(rawPath)) {
            return Optional.empty();
        }

        return Optional.of(Path.of(rawPath).toAbsolutePath().normalize());
    }

    private static boolean hasNonCanonicalLerCodes(Connection connection) throws Exception {
        if (!columnExists(connection, "residuos", "codigo_ler") || !tableExists(connection, "lista_ler")) {
            return false;
        }

        String sql = "SELECT COUNT(*) FROM residuos r WHERE trim(coalesce(r.codigo_ler, '')) <> '' "
                + "AND NOT EXISTS (SELECT 1 FROM lista_ler l WHERE upper(replace(l.codigo, ' ', '')) = upper(replace(r.codigo_ler, ' ', '')))";
        try (PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() && resultSet.getLong(1) > 0;
        }
    }

    private static boolean tableExists(Connection connection, String tableName) throws Exception {
        try (PreparedStatement statement = connection
                .prepareStatement("SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = ? LIMIT 1")) {
            statement.setString(1, tableName);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private static boolean columnExists(Connection connection, String tableName, String columnName) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement("PRAGMA table_info(" + tableName + ")");
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                if (columnName.equalsIgnoreCase(resultSet.getString("name"))) {
                    return true;
                }
            }
            return false;
        }
    }

    private static void deleteSqliteFile(Path filePath) {
        try {
            Files.deleteIfExists(filePath);
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo eliminar la base SQLite incompatible: " + filePath, ex);
        }
    }
}