package com.yash.log.serviceImpl;

import com.yash.log.constants.AIFixConstants;
import com.yash.log.dto.AIFixMessage;
import com.yash.log.dto.AIFixRequest;
import com.yash.log.dto.AIFixResponse;
import com.yash.log.service.impl.AiFixServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AiFixServiceImplTest {


    private AiFixServiceImpl aiFixService;

    @Mock
    private RestTemplate mockRestTemplate;

    @BeforeEach
    void setup() throws Exception {

        aiFixService = new AiFixServiceImpl(
                "gpt-4",
                "dummy-key",
                "http://test-ai.com",
                "user",
                0.7,
                false,
                200,
                10
        );

        mockRestTemplate = mock(RestTemplate.class);

        // Inject mock RestTemplate using reflection
        Field field = AiFixServiceImpl.class.getDeclaredField("restTemplate");
        field.setAccessible(true);
        field.set(aiFixService, mockRestTemplate);
    }

    // ✅ SUCCESS TEST
    @Test
    void testAnalyse_success() throws Exception {

        AIFixRequest req = new AIFixRequest();
        req.setMessages(List.of(new AIFixMessage("user", "Fix this error")));

        String mockJson = """
                {
                  "choices": [
                    {
                      "message": {
                        "content": "AI fixed your issue",
                        "status": "success"
                      }
                    }
                  ]
                }
                """;

        ResponseEntity<String> mockResponse =
                new ResponseEntity<>(mockJson, HttpStatus.OK);

        when(mockRestTemplate.exchange(
                eq("http://test-ai.com"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(mockResponse);

        AIFixResponse response = aiFixService.analyse(req);

        assertNotNull(response);
        assertEquals(AIFixConstants.AI_API_SUCCESS,
                response.getChoices().get(0).getMessage().getStatus());
        assertTrue(response.getChoices().get(0).getMessage().getContent()
                .contains("AI fixed your issue"));
    }

    // ✅ ERROR TEST (fallback)
    @Test
    void testAnalyse_errorFallback() {

        AIFixRequest req = new AIFixRequest();
        req.setMessages(List.of(new AIFixMessage("user", "Fix this error")));

        when(mockRestTemplate.exchange(
                anyString(),
                any(),
                any(),
                eq(String.class)
        )).thenThrow(new RuntimeException("API down"));

        AIFixResponse response = aiFixService.analyse(req);

        assertNotNull(response);
        assertEquals(AIFixConstants.AI_API_FAILED,
                response.getChoices().get(0).getMessage().getStatus());
        assertTrue(response.getChoices().get(0).getMessage().getContent()
                .contains("API down"));
    }

    // ✅ PRIVATE METHOD TEST: buildUserContent()
    @Test
    void testBuildUserContent() throws Exception {

        AIFixRequest req = new AIFixRequest();
        req.setMessages(List.of(new AIFixMessage("user", "Original error")));

        Method method = AiFixServiceImpl.class
                .getDeclaredMethod("buildUserContent", AIFixRequest.class);
        method.setAccessible(true);

        String result = (String) method.invoke(aiFixService, req);

        assertTrue(result.contains("Original error"));
        assertTrue(result.contains(AIFixConstants.AI_API_SUGGESTION));
    }

    // ✅ PRIVATE METHOD TEST: toDomainResponse()
    @Test
    void testToDomainResponse_success() throws Exception {

        AIFixResponse aiResponse = new AIFixResponse();
        AIFixResponse.Choice choice = new AIFixResponse.Choice();
        AIFixResponse.Choice.Message msg = new AIFixResponse.Choice.Message();
        msg.setContent("Fixed content");
        choice.setMessage(msg);
        aiResponse.setChoices(List.of(choice));

        Method method = AiFixServiceImpl.class
                .getDeclaredMethod("toDomainResponse", AIFixResponse.class);
        method.setAccessible(true);

        AIFixResponse result =
                (AIFixResponse) method.invoke(aiFixService, aiResponse);

        assertEquals(AIFixConstants.AI_API_SUCCESS,
                result.getChoices().get(0).getMessage().getStatus());
    }

}

