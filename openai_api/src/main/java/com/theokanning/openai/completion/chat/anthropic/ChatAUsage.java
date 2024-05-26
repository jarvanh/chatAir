package com.theokanning.openai.completion.chat.anthropic;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * Created by flyun on 2024/4/4.
 */
@Data
public class ChatAUsage {
    @JsonProperty("input_tokens")
    long inputTokens;

    @JsonProperty("output_tokens")
    long outputTokens;

}
