package com.yash.log.dto;

import java.util.List;

public class AIFixRequest {

    private String model;
    private List<AIFixMessage> messages;
    private Double temperature;
    private Integer max_tokens;
    private Boolean stream;


    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<AIFixMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<AIFixMessage> messages) {
        this.messages = messages;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Integer getMax_tokens() {
        return max_tokens;
    }

    public void setMax_tokens(Integer max_tokens) {
        this.max_tokens = max_tokens;
    }

    public Boolean getStream() {
        return stream;
    }

    public void setStream(Boolean stream) {
        this.stream = stream;
    }

    @Override
    public String toString() {
        return "AIFixRequest{" +
                "model='" + model + '\'' +
                ", messages=" + messages +
                ", temperature=" + temperature +
                ", max_tokens=" + max_tokens +
                ", stream=" + stream +
                '}';
    }
}
