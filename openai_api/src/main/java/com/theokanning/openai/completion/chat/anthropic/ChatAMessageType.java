package com.theokanning.openai.completion.chat.anthropic;

/**
 * see {@link ChatAMessageType} documentation.
 */
public enum ChatAMessageType {
    TEXT_DELTA("text_delta"),
    TEXT("text"),
    IMAGE("image");

    private final String value;

    ChatAMessageType(final String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
