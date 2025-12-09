package com.yash.log.service.impl;




import com.yash.log.dto.AIFixRequest;
import com.yash.log.dto.AIFixResponse;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;

class AiFixServiceImplWireMockTest {

    private WireMockServer wireMockServer;
    private AiFixServiceImpl service;

    @BeforeEach
    void setUp() {

        wireMockServer = new WireMockServer(8089);
        wireMockServer.start();
        wireMockServer.stubFor(post(urlEqualTo("/endpoint"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"choices\":[{\"message\":{\"status\":\"success\",\"content\":\"AI response\"}}]}")));
        WebClient webClient = WebClient.builder().baseUrl("http://localhost:8089").build();
        service = new AiFixServiceImpl(webClient, "model", "key", "/endpoint", "user", 0.1, false, 2000, 5);
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void testAnalyseSuccess() {

        AIFixRequest request = new AIFixRequest();
        StepVerifier.create(service.analyse(request))
                .assertNext(response -> {
                    AIFixResponse.Choice choice = response.getChoices().get(0);
                    assert choice.getMessage().getStatus().equalsIgnoreCase("success");
                    assert choice.getMessage().getContent().contains("AI response");
                })
                .verifyComplete();
    }

    @Test
    void testAnalyseHttpError() {
        wireMockServer.stubFor(post(urlEqualTo("/endpoint"))
                .willReturn(aResponse().withStatus(500).withBody("Internal Server Error")));

        StepVerifier.create(service.analyse(new AIFixRequest()))
                .assertNext(response -> {
                    assert response.getChoices().get(0).getMessage().getStatus().equalsIgnoreCase("failed");
                    assert response.getChoices().get(0).getMessage().getContent().contains("HTTP Error");
                })
                .verifyComplete();
    }

    @Test
    void testAnalyseInvalidJson() {
        wireMockServer.stubFor(post(urlEqualTo("/endpoint"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("INVALID_JSON")));

        StepVerifier.create(service.analyse(new AIFixRequest()))
                .assertNext(response -> {
                    if (response.getChoices() == null || response.getChoices().isEmpty()) {
                        // parsing failed, returned empty AIFixResponse
                    } else {
                        // fallback path: service returned a "failed" choice
                        assert response.getChoices().get(0).getMessage().getStatus().equalsIgnoreCase("failed");
                    }
                })
                .verifyComplete();
    }


    @Test
    void testAnalyseTimeout() {
        // Stub with a delay longer than the configured timeout
        wireMockServer.stubFor(post(urlEqualTo("/endpoint"))
                .willReturn(aResponse()
                        .withFixedDelay(5000) // delay > timeout
                        .withStatus(200)
                        .withBody("{\"choices\":[{\"message\":{\"status\":\"success\",\"content\":\"Delayed response\"}}]}")));

        StepVerifier.create(service.analyse(new AIFixRequest()))
                .assertNext(response -> {
                    // The service wraps TimeoutException in onErrorResume
                    assert response.getChoices() != null && !response.getChoices().isEmpty();

                    AIFixResponse.Choice.Message msg = response.getChoices().get(0).getMessage();

                    // Status should be "failed"
                    assert msg.getStatus().equalsIgnoreCase("failed");

                    // The content will contain the TimeoutException message
                    // e.g. "Did not observe any item or terminal signal within 2000ms"
                    assert msg.getContent().contains("Did not observe any item");
                })
                .verifyComplete();
    }


    @Test
    void testAnalyseRetryLogic() {
        wireMockServer.stubFor(post(urlEqualTo("/endpoint"))
                .inScenario("Retry Scenario")
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse().withStatus(500).withBody("Connection reset"))
                .willSetStateTo("Second Attempt"));

        wireMockServer.stubFor(post(urlEqualTo("/endpoint"))
                .inScenario("Retry Scenario")
                .whenScenarioStateIs("Second Attempt")
                .willReturn(aResponse().withStatus(200)
                        .withBody("{\"choices\":[{\"message\":{\"status\":\"success\",\"content\":\"Recovered after retry\"}}]}")));

        StepVerifier.create(service.analyse(new AIFixRequest()))
                .assertNext(response -> {
                    assert response.getChoices().get(0).getMessage().getStatus().equalsIgnoreCase("success");
                    assert response.getChoices().get(0).getMessage().getContent().contains("Recovered after retry");
                })
                .verifyComplete();
    }
}





