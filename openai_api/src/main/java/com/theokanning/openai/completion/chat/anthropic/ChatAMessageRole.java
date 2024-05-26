package com.theokanning.openai.completion.chat.anthropic;

/**
 * see {@link ChatAMessageRole} documentation.
 */
public enum ChatAMessageRole {
    USER("user"),
    ASSISTANT("assistant");

    private final String value;

    ChatAMessageRole(final String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
