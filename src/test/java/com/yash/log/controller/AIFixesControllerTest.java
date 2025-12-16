package com.yash.log.controller;

import com.yash.log.dto.AIFixMessage;
import com.yash.log.dto.AIFixRequest;
import com.yash.log.dto.AIFixResponse;
import com.yash.log.service.services.AIFixService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class AIFixesControllerDirectTest {

    @Test
    void testAnalyse_direct() {

        // Mock service
        AIFixService mockService = Mockito.mock(AIFixService.class);

        // Prepare request
        AIFixRequest request = new AIFixRequest();
        request.setMessages(List.of(new AIFixMessage("user", "Fix this error")));

        // Prepare response
        AIFixResponse response = new AIFixResponse();
        AIFixResponse.Choice choice = new AIFixResponse.Choice();
        AIFixResponse.Choice.Message msg = new AIFixResponse.Choice.Message();
        msg.setStatus("success");
        msg.setContent("AI fixed your issue");
        choice.setMessage(msg);
        response.setChoices(List.of(choice));

        // Mock service behavior
        Mockito.when(mockService.analyse(any(AIFixRequest.class)))
                .thenReturn(response);

        // Create controller manually
        AIFixesController controller = new AIFixesController(mockService);

        // Call controller method directly
        ResponseEntity<AIFixResponse> result = controller.analyse(request);

        // Assertions
        assertEquals(200, result.getStatusCodeValue());
        assertEquals("success", result.getBody().getChoices().get(0).getMessage().getStatus());
        assertEquals("AI fixed your issue", result.getBody().getChoices().get(0).getMessage().getContent());

        // Verify service call
        Mockito.verify(mockService, Mockito.times(1)).analyse(any(AIFixRequest.class));
    }
}
