package com.iesdoctorbalmis.spring.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

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
            @Value("${ecoadmin.db.path:}") String configuredPath) {
        String resolvedUrl = resolveUrl(configuredUrl, configuredPath);

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
}