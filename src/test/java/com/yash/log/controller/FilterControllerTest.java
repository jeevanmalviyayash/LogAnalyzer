package com.yash.log.controller;

import com.yash.log.entity.Log;
import com.yash.log.service.services.LogService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FilterControllerTest {

    @Mock
    private LogService service;

    @InjectMocks
    private FilterController controller;

    @Test
    void testGetLogs_returnsList() {
        // sample log
        Log log = new Log();
        log.setErrorId(1L);
        log.setErrorMessage("Test message");
        log.setCreatedAt(LocalDateTime.now());

        when(service.filterLogs(null, null, null))
                .thenReturn(List.of(log));

        // call controller method
        List<Log> result = controller.getLogs(null, null, null);

        // verify normal Java behavior
        assertEquals(1, result.size());
        assertEquals("Test message", result.get(0).getErrorMessage());
    }
}