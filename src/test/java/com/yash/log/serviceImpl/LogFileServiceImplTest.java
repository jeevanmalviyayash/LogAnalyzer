package com.yash.log.serviceImpl;

import com.yash.log.entity.Log;
import com.yash.log.repository.ErrorLogRepository;
import com.yash.log.service.impl.LogFileServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class LogFileServiceImplTest {

    @Mock
    private ErrorLogRepository errorLogRepository;

    @InjectMocks
    private LogFileServiceImpl logFileServiceImpl;

    @Test
    void testGetAllLogs() {
        // Prepare mock data
        Log log1 = new Log();
        Log log2 = new Log();
        List<Log> mockList = Arrays.asList(log1, log2);

        // Mock repository call
        when(errorLogRepository.findAll()).thenReturn(mockList);

        // Call service method
        List<Log> result = logFileServiceImpl.getAllLogs();

        // Verify result
        assertEquals(2, result.size());
        assertEquals(mockList, result);
    }
}
