package com.yash.log.controller;

import com.yash.log.entity.Log;
import com.yash.log.service.impl.LogFileServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ErrorLogController.class)
class ErrorLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LogFileServiceImpl logFileServiceImpl;

    @Test
    void testGetAllLogs() throws Exception {
        // Mock returned list
        when(logFileServiceImpl.getAllLogs()).thenReturn(
                Arrays.asList(new Log(), new Log())
        );

        mockMvc.perform(get("/api/logs/all-logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}
