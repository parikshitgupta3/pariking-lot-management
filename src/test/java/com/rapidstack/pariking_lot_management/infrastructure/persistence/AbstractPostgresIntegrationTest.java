package com.rapidstack.pariking_lot_management.infrastructure.persistence;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Base class for tests that need the full Spring context against a real
 * PostgreSQL, provided by Testcontainers. The {@code @ServiceConnection}
 * container replaces the datasource configuration, so tests exercise the
 * same JPA/Hibernate stack production uses. The schema is created fresh and
 * dropped per context.
 *
 * <p>The container is started in a static initializer rather than via the
 * Testcontainers JUnit extension on purpose: extension-managed static
 * containers are stopped after each test class, while Spring caches the
 * application context across classes — a cached context would then point at
 * a dead container's port. A JVM-lifetime container keeps every cached
 * context valid. Ryuk reclaims it after the build.
 */
@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
public abstract class AbstractPostgresIntegrationTest {

    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

    static {
        POSTGRES.start();
    }
}
