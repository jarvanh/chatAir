package com.theokanning.openai.completion.chat.anthropic;

/**
 * see {@link ChatAMessage} documentation.
 */
public enum ChatAContentType {
    MESSAGE_START("message_start"),
    CONTENT_BLOCK_START("content_block_start"),
    PING("ping"),
    CONTENT_BLOCK_DELTA("content_block_delta"),
    CONTENT_BLOCK_STOP("content_block_stop"),
    MESSAGE_DELTA("message_delta"),
    MESSAGE_STOP("message_stop");

    private final String value;

    ChatAContentType(final String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
