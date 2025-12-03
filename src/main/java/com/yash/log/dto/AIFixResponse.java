package com.yash.log.dto;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString

public class AIFixResponse {

    private List<Choice> choices;
    private Usage usage;

    @Data
    @ToString
    public static class Choice {
        private Message message;

        @Data
        @ToString
        public static class Message {
            private String status;
            private String role;
            private String content;
        }
    }

    @Data
    @ToString
    public static class Usage {
        private Integer promptTokens;
        private Integer completionTokens;
        private Integer totalTokens;
    }
}
