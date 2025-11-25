package com.yash.log.service.impl;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yash.log.constants.AIFixConstants;
import com.yash.log.dto.AIFixMessage;
import com.yash.log.dto.AIFixRequest;
import com.yash.log.dto.AIFixResponse;
import com.yash.log.service.services.AIFixService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Service
public class AiFixServiceImpl implements AIFixService {

    private static final Logger log = LoggerFactory.getLogger(AiFixServiceImpl.class);

    private final WebClient webClient;
    private final String model;
    private final String apiKey;
    private final String endpoint;
    private final String role;
    private final Double temperature;
    private final Boolean stream;
    private final Integer token;
    private final Integer timeout;

    public AiFixServiceImpl(WebClient webClient,
                            @Value("${ai.api.model:}") String model,
                            @Value("${ai.api.key:}") String keyFromProps,
                            @Value("${ai.api.endPoint:}") String endpoint,
                            @Value("${ai.api.role:}") String role,
                            @Value("${ai.api.temperature:}") Double temperature,
                            @Value("${ai.api.stream:}") Boolean stream,
                            @Value("${ai.api.token:}") Integer token,
                            @Value("${ai.api.timeout:}") Integer timeout
    ) {
        this.webClient = webClient;
        this.model = model;
        this.apiKey = keyFromProps;
        this.endpoint = endpoint;
        this.role = role;
        this.temperature = temperature;
        this.stream = stream;
        this.token = token;
        this.timeout = timeout;
        if (this.apiKey == null || this.apiKey.isBlank()) {
            log.warn("AI API key is not configured. Set groq.api.key property.");
        }
    }


    @Override
    public Mono<AIFixResponse> analyse(AIFixRequest req) {

        AIFixRequest payload = new AIFixRequest();
        payload.setModel(model);
        payload.setTemperature(temperature);
        payload.setMax_tokens(token);
        payload.setStream(stream);
        payload.setMessages(List.of(new AIFixMessage(role, buildUserContent(req))));

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return webClient.post()
                .uri(endpoint)
                .header(HttpHeaders.AUTHORIZATION, AIFixConstants.AI_API_BEARER + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .doOnNext(res -> log.error("AI Failed error JSON: {}", res))
                                .map(body -> new RuntimeException("HTTP Error: " + body))
                )
                .bodyToMono(String.class)
                .doOnNext(res -> log.info("AI Success response JSON: {}", res))
                .map(res -> {
                    try {
                        return mapper.readValue(res, AIFixResponse.class);
                    } catch (Exception e) {
                        log.error("Failed to parse AI response: {}", e);

                        return new AIFixResponse();
                    }
                })
                .retryWhen(reactor.util.retry.Retry
                        .fixedDelay(AIFixConstants.AI_API_MAX_ATTEMPT, Duration.ofMillis(AIFixConstants.AI_API_DELAY))
                        .filter(ex -> {
                            log.error("Retry triggered due to exception: {}", ex.getMessage(), ex);
                            String msg = ex.getMessage();
                            return msg != null && (
                                            msg.contains(AIFixConstants.AI_API_TIMEOUT) ||
                                            msg.contains(AIFixConstants.AI_API_CON_RESET)
                            );
                        })
                )
                .map(res -> {
                    log.info("AI API Request Payload analyse() : {}", payload);
                    return toDomainResponse(res);
                })
                .timeout(Duration.ofSeconds(timeout))
                .onErrorResume(ex -> {
                    log.error("AI call failed: {}", ex.getMessage(), ex);

                    AIFixResponse out = new AIFixResponse();
                    AIFixResponse.Choice choice = new AIFixResponse.Choice();
                    AIFixResponse.Choice.Message msg = new AIFixResponse.Choice.Message();
                    msg.setStatus(AIFixConstants.AI_API_FAILED);
                    msg.setContent(ex.getMessage());
                    choice.setMessage(msg);
                    out.setChoices(List.of(choice));
                    return Mono.just(out);
                });
    }

    private String buildUserContent(AIFixRequest req) {
        StringBuilder sb = new StringBuilder();
        if (req.getMessages() != null) {
            sb.append(req.getMessages().get(0).getContent()).append("\n\n");
            sb.append(AIFixConstants.AI_API_SUGGESTION);
        }
        return sb.toString();
    }

    private AIFixResponse toDomainResponse(AIFixResponse aiResponse) {
        if (aiResponse.getChoices().get(0).getMessage().getStatus()!=null
                && aiResponse.getChoices().get(0).getMessage().getStatus()
                .equalsIgnoreCase(AIFixConstants.AI_API_FAILED)) {
            return aiResponse;
        }

        String content;
        if (aiResponse.getChoices() != null && !aiResponse.getChoices().isEmpty()
                && aiResponse.getChoices().get(0).getMessage() != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(aiResponse.getChoices().get(0).getMessage().getContent()).append("\n\n");
            content = sb.isEmpty() ? AIFixConstants.AI_API_NO_RES_MSG : sb.toString();
            AIFixResponse.Choice choice = new AIFixResponse.Choice();
            AIFixResponse.Choice.Message msg = new AIFixResponse.Choice.Message();
            msg.setStatus(AIFixConstants.AI_API_SUCCESS);
            msg.setContent(content);
            choice.setMessage(msg);
            aiResponse.setChoices(List.of(choice));
            log.info("AI Response toDomainResponse() : {}", aiResponse);
        }
        return aiResponse;

    }


}
