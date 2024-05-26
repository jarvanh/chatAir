package com.theokanning.openai.completion.chat.anthropic;

import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.Data;

@Data
public class ChatACompletionChoice {

    Integer index;

    /**
     * stream类型
     * You may use {@link ChatAContentType} enum.
     */
    String type;

    /**
     * Must be either 'user', or 'assistant'.<br>
     * You may use {@link ChatAMessageRole} enum.
     */
    String role;

    String model;

    String stop_sequence;
    String stop_reason;

    ChatAUsage usage;

    @JsonAlias("delta")
    ChatAMessage message;

}
