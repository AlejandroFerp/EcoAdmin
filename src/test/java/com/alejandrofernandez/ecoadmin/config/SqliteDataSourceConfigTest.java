package com.alejandrofernandez.ecoadmin.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SqliteDataSourceConfigTest {

    @TempDir
    Path tempDir;

    @Test
    void detectResetReasonReturnsLegacySchemaReason() throws Exception {
        Path dbPath = tempDir.resolve("legacy.db");

        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
                Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE residuos (id INTEGER PRIMARY KEY, codigoler TEXT)");
        }

        assertThat(SqliteDataSourceConfig.detectResetReason(dbPath))
                .contains("esquema legacy de residuos detectado");
    }

    @Test
    void detectResetReasonReturnsInvalidLerReason() throws Exception {
        Path dbPath = tempDir.resolve("invalid-ler.db");

        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
                Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE lista_ler (id INTEGER PRIMARY KEY, codigo TEXT)");
            statement.execute("CREATE TABLE residuos (id INTEGER PRIMARY KEY, codigo_ler TEXT)");
            statement.execute("INSERT INTO lista_ler (id, codigo) VALUES (1, '17 04 05')");
            statement.execute("INSERT INTO residuos (id, codigo_ler) VALUES (1, '99 99 99')");
        }

        assertThat(SqliteDataSourceConfig.detectResetReason(dbPath))
                .contains("datos de residuos incompatibles con la lista LER canonica");
    }

    @Test
    void detectResetReasonReturnsMissingLerCatalogReason() throws Exception {
        Path dbPath = tempDir.resolve("missing-ler-catalog.db");

        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
                Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE residuos (id INTEGER PRIMARY KEY, codigo_ler TEXT)");
            statement.execute("INSERT INTO residuos (id, codigo_ler) VALUES (1, '17 04 05')");
        }

        assertThat(SqliteDataSourceConfig.detectResetReason(dbPath))
                .contains("tabla lista_ler ausente para residuos canonicos");
    }

    @Test
    void resetLegacySqliteIfNeededDeletesLegacyDatabase() throws Exception {
        Path dbPath = tempDir.resolve("reset.db");

        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
                Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE empresa (id INTEGER PRIMARY KEY, direccion TEXT)");
        }

        SqliteDataSourceConfig.resetLegacySqliteIfNeeded("jdbc:sqlite:" + dbPath);

        assertThat(Files.exists(dbPath)).isFalse();
    }

    @Test
    void resetLegacySqliteIfNeededKeepsCanonicalDatabase() throws Exception {
        Path dbPath = tempDir.resolve("canonical.db");

        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
                Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE lista_ler (id INTEGER PRIMARY KEY, codigo TEXT)");
            statement.execute("CREATE TABLE residuos (id INTEGER PRIMARY KEY, codigo_ler TEXT)");
            statement.execute("INSERT INTO lista_ler (id, codigo) VALUES (1, '17 04 05')");
            statement.execute("INSERT INTO residuos (id, codigo_ler) VALUES (1, '17 04 05')");
        }

        SqliteDataSourceConfig.resetLegacySqliteIfNeeded("jdbc:sqlite:" + dbPath);

        assertThat(Files.exists(dbPath)).isTrue();
    }
}