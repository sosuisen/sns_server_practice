package com.example.startup;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class DataSeederTest {

    @Test
    void prodAdminConfig_usesConfiguredValues() {
        var config = DataSeeder.prodAdminConfig(
                Optional.of("root"),
                Optional.of("root@example.com"),
                Optional.of("secret123"));

        assertEquals("root", config.username());
        assertEquals("root@example.com", config.email());
        assertEquals("secret123", config.password());
    }

    @Test
    void prodAdminConfig_usesDefaultUsername() {
        var config = DataSeeder.prodAdminConfig(
                Optional.empty(),
                Optional.of("root@example.com"),
                Optional.of("secret123"));

        assertEquals("admin", config.username());
    }

    @Test
    void prodAdminConfig_requiresEmail() {
        assertThrows(IllegalStateException.class, () -> DataSeeder.prodAdminConfig(
                Optional.of("root"),
                Optional.empty(),
                Optional.of("secret123")));
    }

    @Test
    void prodAdminConfig_requiresPassword() {
        assertThrows(IllegalStateException.class, () -> DataSeeder.prodAdminConfig(
                Optional.of("root"),
                Optional.of("root@example.com"),
                Optional.empty()));
    }
}
