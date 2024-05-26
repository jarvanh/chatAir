package com.theokanning.openai.completion.chat.anthropic;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by flyun on 2024/4/4.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatACompletionRequest {

    String model;

    List<ChatARequestMessage> messages;

    Double temperature;

    @JsonProperty("top_k")
    Double topK;

    @JsonProperty("top_p")
    Double topP;

    Boolean stream;

    @JsonProperty("max_tokens")
    Integer maxTokens;

    String system;

}
