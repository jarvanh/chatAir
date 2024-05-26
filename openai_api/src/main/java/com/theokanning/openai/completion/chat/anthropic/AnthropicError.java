package com.theokanning.openai.completion.chat.anthropic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class AnthropicError {

    public  AnthropicDetails error;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnthropicDetails {
        String message;

        String status;

        String code;
    }
}
