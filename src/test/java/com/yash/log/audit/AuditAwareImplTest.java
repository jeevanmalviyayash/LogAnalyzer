package com.yash.log.audit;


import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AuditAwareImplTest {

    @Test
    void getCurrentAuditor_returnsExpectedValue() {
        // Arrange
        AuditAwareImpl auditorAware = new AuditAwareImpl();

        // Act
        Optional<String> result = auditorAware.getCurrentAuditor();

        // Assert
        assertNotNull(result, "Result should not be null");
        assertTrue(result.isPresent(), "Optional should be present");
        assertEquals("LOG_MS", result.get(), "Auditor should be 'LOG_MS'");
    }

    @Test
    void getCurrentAuditor_isAlwaysPresent() {
        AuditAwareImpl auditorAware = new AuditAwareImpl();

        Optional<String> first = auditorAware.getCurrentAuditor();
        Optional<String> second = auditorAware.getCurrentAuditor();

        assertTrue(first.isPresent(), "First call should be present");
        assertTrue(second.isPresent(), "Second call should be present");
        assertEquals("LOG_MS", first.orElse(null));
        assertEquals("LOG_MS", second.orElse(null));
    }
}