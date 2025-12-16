package com.yash.log.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yash.log.dto.AIFixMessage;
import com.yash.log.dto.AIFixRequest;
import com.yash.log.dto.AIFixResponse;
import com.yash.log.service.services.AIFixService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;


import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ExtendWith(MockitoExtension.class)
class AIFixesControllerTest {


    private MockMvc mockMvc;

    @Mock
    private AIFixService aiFixService;


    private ObjectMapper mapper;

    @BeforeEach
    void setup() {
        mapper = new ObjectMapper();
    }

    @Test
    void testAnalyse_success() throws Exception {

        // Prepare request
        AIFixRequest request = new AIFixRequest();
        request.setMessages(List.of(new AIFixMessage("user", "Fix this error")));

        // Prepare mocked service response
        AIFixResponse response = new AIFixResponse();
        AIFixResponse.Choice choice = new AIFixResponse.Choice();
        AIFixResponse.Choice.Message msg = new AIFixResponse.Choice.Message();
        msg.setStatus("success");
        msg.setContent("AI fixed your issue");
        choice.setMessage(msg);
        response.setChoices(List.of(choice));

        Mockito.when(aiFixService.analyse(any(AIFixRequest.class)))
                .thenReturn(response);

        // Perform POST request
        mockMvc.perform(post("/api/ai/assitant")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.choices[0].message.status").value("success"))
                .andExpect(jsonPath("$.choices[0].message.content").value("AI fixed your issue"));

        // Verify service call
        verify(aiFixService, times(1)).analyse(any(AIFixRequest.class));
    }
}