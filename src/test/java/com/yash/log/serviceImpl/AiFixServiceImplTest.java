


package com.yash.log.serviceImpl;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yash.log.constants.AIFixConstants;
import com.yash.log.dto.AIFixMessage;
import com.yash.log.dto.AIFixRequest;
import com.yash.log.dto.AIFixResponse;
import com.yash.log.service.impl.AiFixServiceImpl;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class AiFixServiceImplWireMockTest {

    private WireMockServer wireMockServer;
    private AiFixServiceImpl service;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(8089);
        wireMockServer.start();
        configureWireMockStubs();

        WebClient webClient = WebClient.builder()
                .baseUrl("http://localhost:8089")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .build();

        // Test with all possible configurations
        service = new AiFixServiceImpl(
                webClient,
                "gpt-4",            // model
                "test-api-key",     // apiKey
                "/v1/chat/completions", // endpoint
                "user",             // role
                0.7,                // temperature
                false,              // stream
                2000,               // token
                5                   // timeout
        );

        objectMapper = new ObjectMapper();

        // Test warning log when API key is missing
        AiFixServiceImpl serviceNoKey = new AiFixServiceImpl(
                webClient,
                "gpt-4",
                "",  // Empty API key
                "/v1/chat/completions",
                "user",
                0.7,
                false,
                2000,
                5
        );
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    private void configureWireMockStubs() {
        // Reset all stubs
        wireMockServer.resetAll();

        // Default successful response
        wireMockServer.stubFor(post(urlEqualTo("/v1/chat/completions"))
                .withHeader("Authorization", equalTo("Bearer test-api-key"))
                .willReturn(okJson("{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":\"Fix the null pointer by checking if object is null\",\"status\":\"success\"}}]}")));
    }

    @Test
    void testAnalyseSuccess_WithValidRequest() {
        AIFixRequest request = new AIFixRequest();
        request.setMessages(List.of(new AIFixMessage("user", "ERROR: Null pointer exception")));

        StepVerifier.create(service.analyse(request))
                .assertNext(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.getChoices()).isNotEmpty();
                    assertThat(response.getChoices().get(0).getMessage().getStatus())
                            .isEqualToIgnoringCase("success");
                    assertThat(response.getChoices().get(0).getMessage().getContent())
                            .contains("Fix the null pointer");
                })
                .verifyComplete();
    }


    @Test
    void testAnalyseSuccess_WithNullMessages() {
        AIFixRequest request = new AIFixRequest();
        request.setMessages(null); // Null messages

        wireMockServer.stubFor(post(urlEqualTo("/v1/chat/completions"))
                .willReturn(okJson("{\"choices\":[{\"message\":{\"content\":\"Response for null messages\",\"status\":\"success\"}}]}")));

        StepVerifier.create(service.analyse(request))
                .assertNext(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.getChoices()).isNotEmpty();
                })
                .verifyComplete();
    }

    @Test
    void testAnalyseSuccess_WithMultipleMessages() {
        AIFixRequest request = new AIFixRequest();
        List<AIFixMessage> messages = new ArrayList<>();
        messages.add(new AIFixMessage("user", "First message"));
        messages.add(new AIFixMessage("assistant", "Previous response"));
        messages.add(new AIFixMessage("user", "ERROR: Stack overflow"));
        request.setMessages(messages);

        StepVerifier.create(service.analyse(request))
                .assertNext(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.getChoices()).isNotEmpty();
                })
                .verifyComplete();
    }

    @Test
    void testAnalyseHttpError_WithDifferentStatusCodes() {
        // Test with 400 Bad Request
        wireMockServer.stubFor(post(urlEqualTo("/v1/chat/completions"))
                .willReturn(badRequest().withBody("Bad request error")));

        AIFixRequest request = new AIFixRequest();
        request.setMessages(List.of(new AIFixMessage("user", "Test error")));

        StepVerifier.create(service.analyse(request))
                .assertNext(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.getChoices()).isNotEmpty();
                    assertThat(response.getChoices().get(0).getMessage().getStatus())
                            .isEqualToIgnoringCase("failed");
                    assertThat(response.getChoices().get(0).getMessage().getContent())
                            .contains("HTTP Error");
                })
                .verifyComplete();

        // Test with 502 Bad Gateway
        wireMockServer.stubFor(post(urlEqualTo("/v1/chat/completions"))
                .willReturn(serverError().withBody("Gateway error")));

        StepVerifier.create(service.analyse(request))
                .assertNext(response -> {
                    assertThat(response.getChoices().get(0).getMessage().getStatus())
                            .isEqualToIgnoringCase("failed");
                })
                .verifyComplete();
    }




    @Test
    void testAnalyseTimeout_ExactTimeoutScenario() {
        // Stub with delay exactly at timeout
        wireMockServer.stubFor(post(urlEqualTo("/v1/chat/completions"))
                .willReturn(aResponse()
                        .withFixedDelay(6000) // More than 5 second timeout
                        .withStatus(200)
                        .withBody("{\"choices\":[{\"message\":{\"content\":\"Timeout response\",\"status\":\"success\"}}]}")));

        AIFixRequest request = new AIFixRequest();
        request.setMessages(List.of(new AIFixMessage("user", "Timeout test")));

        StepVerifier.create(service.analyse(request))
                .assertNext(response -> {
                    assertThat(response.getChoices()).isNotEmpty();
                    assertThat(response.getChoices().get(0).getMessage().getStatus())
                            .isEqualToIgnoringCase("failed");
                    assertThat(response.getChoices().get(0).getMessage().getContent())
                            .contains("Did not observe");
                })
                .verifyComplete();
    }




    @Test
    void testAnalyse_NoRetryForNonRetryableErrors() {
        // Error that doesn't match retry filter conditions
        wireMockServer.stubFor(post(urlEqualTo("/v1/chat/completions"))
                .willReturn(aResponse()
                        .withStatus(403)
                        .withBody("Authentication failed")));

        AIFixRequest request = new AIFixRequest();
        request.setMessages(List.of(new AIFixMessage("user", "Auth test")));

        StepVerifier.create(service.analyse(request))
                .assertNext(response -> {
                    assertThat(response.getChoices()).isNotEmpty();
                    assertThat(response.getChoices().get(0).getMessage().getStatus())
                            .isEqualToIgnoringCase("failed");
                })
                .verifyComplete();
    }

    @Test
    void testToDomainResponse_WithFailedStatus() {
        AIFixResponse input = new AIFixResponse();
        AIFixResponse.Choice choice = new AIFixResponse.Choice();
        AIFixResponse.Choice.Message message = new AIFixResponse.Choice.Message();
        message.setStatus(AIFixConstants.AI_API_FAILED);
        message.setContent("Already failed response");
        choice.setMessage(message);
        input.setChoices(List.of(choice));

        // Use reflection to test private method, or test through public method
        AIFixRequest request = new AIFixRequest();
        request.setMessages(List.of(new AIFixMessage("user", "Test")));

        // Setup response that returns failed status
        wireMockServer.stubFor(post(urlEqualTo("/v1/chat/completions"))
                .willReturn(okJson("{\"choices\":[{\"message\":{\"status\":\"failed\",\"content\":\"Already failed\"}}]}")));

        StepVerifier.create(service.analyse(request))
                .assertNext(response -> {
                    assertThat(response.getChoices()).isNotEmpty();
                    assertThat(response.getChoices().get(0).getMessage().getStatus())
                            .isEqualToIgnoringCase("failed");
                })
                .verifyComplete();
    }


    @Test
    void testToDomainResponse_WithNullMessage() {
        // Test when choice has null message
        wireMockServer.stubFor(post(urlEqualTo("/v1/chat/completions"))
                .willReturn(okJson("{\"choices\": [{\"message\": null}]}")));

        AIFixRequest request = new AIFixRequest();
        request.setMessages(List.of(new AIFixMessage("user", "Null message test")));

        StepVerifier.create(service.analyse(request))
                .assertNext(response -> {
                    // Should handle null message gracefully
                    assertThat(response.getChoices()).isNotEmpty();
                })
                .verifyComplete();
    }

    @Test
    void testBuildUserContent_EdgeCases() {
        AIFixRequest request = new AIFixRequest();

        // Test with null messages
        request.setMessages(null);
        // The method should handle null gracefully

        // Test with empty messages list
        request.setMessages(Collections.emptyList());

        // Test with message having null content
        List<AIFixMessage> messages = new ArrayList<>();
        messages.add(new AIFixMessage("user", null));
        request.setMessages(messages);

        // These cases will be tested indirectly through analyse() method
        StepVerifier.create(service.analyse(request))
                .assertNext(response -> {
                    assertThat(response).isNotNull();
                })
                .verifyComplete();
    }



    @Test
    void testAnalyse_WithEmptyApiKey() {
        // Test service with empty API key (should log warning)
        WebClient webClient = WebClient.builder().baseUrl("http://localhost:8089").build();
        AiFixServiceImpl serviceEmptyKey = new AiFixServiceImpl(
                webClient,
                "model",
                "",  // Empty key
                "/endpoint",
                "user",
                0.1,
                false,
                2000,
                5
        );

        // Should still work but might fail at API level
        AIFixRequest request = new AIFixRequest();
        request.setMessages(List.of(new AIFixMessage("user", "Test")));

        StepVerifier.create(serviceEmptyKey.analyse(request))
                .assertNext(response -> {
                    // Will either get a response or error response
                    assertThat(response).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    void testAnalyse_WithNullApiKey() {
        // Test service with null API key
        WebClient webClient = WebClient.builder().baseUrl("http://localhost:8089").build();
        AiFixServiceImpl serviceNullKey = new AiFixServiceImpl(
                webClient,
                "model",
                null,  // Null key
                "/endpoint",
                "user",
                0.1,
                false,
                2000,
                5
        );

        AIFixRequest request = new AIFixRequest();
        request.setMessages(List.of(new AIFixMessage("user", "Test")));

        StepVerifier.create(serviceNullKey.analyse(request))
                .assertNext(response -> {
                    assertThat(response).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    void testAnalyse_MaxRetryAttemptsExceeded() {
        // Setup to always return retryable error
        wireMockServer.stubFor(post(urlEqualTo("/v1/chat/completions"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(AIFixConstants.AI_API_CON_RESET)));

        AIFixRequest request = new AIFixRequest();
        request.setMessages(List.of(new AIFixMessage("user", "Max retry test")));

        StepVerifier.create(service.analyse(request))
                .assertNext(response -> {
                    // After max retries, should return failed response
                    assertThat(response.getChoices()).isNotEmpty();
                    assertThat(response.getChoices().get(0).getMessage().getStatus())
                            .isEqualToIgnoringCase("failed");
                })
                .verifyComplete();
    }


    @Test
    void testAnalyse_WhenRetryFilterReturnsFalseForNonMatchingErrorMessage() {
        // Exception message doesn't contain timeout or connection reset
        wireMockServer.stubFor(post(urlEqualTo("/v1/chat/completions"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("Authentication failed error")));

        AIFixRequest request = new AIFixRequest();
        request.setMessages(List.of(new AIFixMessage("user", "Test")));

        StepVerifier.create(service.analyse(request))
                .assertNext(response -> {
                    assertThat(response.getChoices()).isNotEmpty();
                    assertThat(response.getChoices().get(0).getMessage().getStatus())
                            .isEqualToIgnoringCase("failed");
                })
                .verifyComplete();
    }











    @Test
    void testConstructor_WithBlankApiKey_ShouldNotThrow() {
        WebClient webClient = WebClient.builder().build();

        // Should not throw, only log warning
        AiFixServiceImpl service = new AiFixServiceImpl(
                webClient,
                "model",
                "   ",  // Blank API key with spaces
                "/endpoint",
                "user",
                0.1,
                false,
                2000,
                5
        );

        assertThat(service).isNotNull();
    }





    @Test
    void testConstructor_WithAllNullParameters_ShouldNotThrow() {
        WebClient webClient = WebClient.builder().build();

        // Test with all null parameters
        AiFixServiceImpl service = new AiFixServiceImpl(
                webClient,
                null,  // null model
                null,  // null apiKey
                null,  // null endpoint
                null,  // null role
                null,  // null temperature
                null,  // null stream
                null,  // null token
                null   // null timeout
        );

        assertThat(service).isNotNull();
    }
}