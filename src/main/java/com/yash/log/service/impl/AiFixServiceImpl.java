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
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Service
public class AiFixServiceImpl implements AIFixService {

    private static final Logger log = LoggerFactory.getLogger(AiFixServiceImpl.class);


    private final String model;
    private final String apiKey;
    private final String baseUrl;
    private final String role;
    private final Double temperature;
    private final Boolean stream;
    private final Integer token;
    private final Integer timeout;
    private RestTemplate restTemplate = new RestTemplate();

    public AiFixServiceImpl(
                            @Value("${ai.api.model:}") String model,
                            @Value("${ai.api.key:}") String keyFromProps,
                            @Value("${ai.api.base-url:}") String baseUrl,
                            @Value("${ai.api.role:}") String role,
                            @Value("${ai.api.temperature:}") Double temperature,
                            @Value("${ai.api.stream:}") Boolean stream,
                            @Value("${ai.api.token:}") Integer token,
                            @Value("${ai.api.timeout:9000}") Integer timeout
    ) {

        this.model = model;
        this.apiKey = keyFromProps;
        this.baseUrl = baseUrl;
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
    public AIFixResponse analyse(AIFixRequest req) {

        // Build payload
        AIFixRequest payload = new AIFixRequest();
        payload.setModel(model);
        payload.setTemperature(temperature);
        payload.setMax_tokens(token);
        payload.setStream(stream);
        payload.setMessages(List.of(new AIFixMessage(role, buildUserContent(req))));

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // Prepare headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.AUTHORIZATION, AIFixConstants.AI_API_BEARER + apiKey);
        HttpEntity<AIFixRequest> entity = new HttpEntity<>(payload, headers);

        log.info("AI Success response JSON  entity: {}", entity.toString());
        try {
            // Call external AI API
            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            String json = response.getBody();
            log.info("AI Success response JSON: {}", json);

            // Parse JSON into AIFixResponse
            AIFixResponse parsed = mapper.readValue(json, AIFixResponse.class);

            log.info("AI API Request Payload analyse(): {}", payload);

            return toDomainResponse(parsed);

        } catch (Exception ex) {
            log.error("AI call failed: {}", ex.getMessage(), ex);

            // Build fallback response
            AIFixResponse out = new AIFixResponse();
            AIFixResponse.Choice choice = new AIFixResponse.Choice();
            AIFixResponse.Choice.Message msg = new AIFixResponse.Choice.Message();

            msg.setStatus(AIFixConstants.AI_API_FAILED);
            msg.setContent(ex.getMessage());

            choice.setMessage(msg);
            out.setChoices(List.of(choice));

            return out;
        }
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
