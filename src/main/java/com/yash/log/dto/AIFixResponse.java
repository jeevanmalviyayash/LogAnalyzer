package com.yash.log.dto;

import java.util.List;

public class AIFixResponse {

    private List<Choice> choices;
    private Usage usage;


    public List<Choice> getChoices() {
        return choices;
    }

    public void setChoices(List<Choice> choices) {
        this.choices = choices;
    }

    public Usage getUsage() {
        return usage;
    }

    public void setUsage(Usage usage) {
        this.usage = usage;
    }


    @Override
    public String toString() {
        return "AIFixResponse{" +
                "choices=" + choices +
                ", usage=" + usage +
                '}';
    }


    public static class Choice {

        private Message message;


        public Message getMessage() {
            return message;
        }

        public void setMessage(Message message) {
            this.message = message;
        }


        @Override
        public String toString() {
            return "Choice{" +
                    "message=" + message +
                    '}';
        }


        public static class Message {
            private String status;
            private String role;
            private String content;


            public String getStatus() {
                return status;
            }

            public void setStatus(String status) {
                this.status = status;
            }

            public String getRole() {
                return role;
            }

            public void setRole(String role) {
                this.role = role;
            }

            public String getContent() {
                return content;
            }

            public void setContent(String content) {
                this.content = content;
            }


            @Override
            public String toString() {
                return "Message{" +
                        "status='" + status + '\'' +
                        ", role='" + role + '\'' +
                        ", content='" + content + '\'' +
                        '}';
            }
        }
    }


    public static class Usage {
        private Integer promptTokens;
        private Integer completionTokens;
        private Integer totalTokens;


        public Integer getPromptTokens() {
            return promptTokens;
        }

        public void setPromptTokens(Integer promptTokens) {
            this.promptTokens = promptTokens;
        }

        public Integer getCompletionTokens() {
            return completionTokens;
        }

        public void setCompletionTokens(Integer completionTokens) {
            this.completionTokens = completionTokens;
        }

        public Integer getTotalTokens() {
            return totalTokens;
        }

        public void setTotalTokens(Integer totalTokens) {
            this.totalTokens = totalTokens;
        }


        @Override
        public String toString() {
            return "Usage{" +
                    "promptTokens=" + promptTokens +
                    ", completionTokens=" + completionTokens +
                    ", totalTokens=" + totalTokens +
                    '}';
        }
    }
}
