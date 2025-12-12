package com.yash.log.controller;



import com.yash.log.constants.AIFixConstants;
import com.yash.log.dto.AIFixMessage;
import com.yash.log.dto.AIFixRequest;
import com.yash.log.dto.AIFixResponse;
import com.yash.log.service.services.AIFixService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@Import(AIFixesController.class)
class AIFixesControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private AIFixService aiFixService;


    @Test
    void testAnalysePostRequest() {
        AIFixResponse mockResponse = new AIFixResponse();
        AIFixResponse.Choice choice = new AIFixResponse.Choice();
        AIFixResponse.Choice.Message msg = new AIFixResponse.Choice.Message();
        msg.setStatus("Success");
        msg.setContent("Mocked content");
        choice.setMessage(msg);
        mockResponse.setChoices(java.util.List.of(choice));
        when(aiFixService.analyse(any())).thenReturn(Mono.just(mockResponse));
        webTestClient.post()
                .uri("/api/ai-assitant")
                .bodyValue(new AIFixRequest())
                .exchange()
                .expectStatus().isOk()
                .expectBody(AIFixResponse.class)
                .value(res -> {
                    assertEquals("Mocked content", res.getChoices().get(0).getMessage().getContent());
                });
    }





     @Test
    void testAnalysePostRequest_BadRequest() {

        when(aiFixService.analyse(any())).thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/api/ai-assitant")
                .bodyValue(new AIFixRequest())
                .exchange()
                .expectStatus().isBadRequest();
    }
}
