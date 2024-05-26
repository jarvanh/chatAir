package com.theokanning.openai.completion.chat.anthropic;


public class AnthropicHttpException extends RuntimeException {

    public final String code;

    public final String status;


    public AnthropicHttpException(AnthropicError error, Throwable parent) {
        super(error.error.message, parent);
        this.code = error.error.code;
        this.status = error.error.status;
    }
}
