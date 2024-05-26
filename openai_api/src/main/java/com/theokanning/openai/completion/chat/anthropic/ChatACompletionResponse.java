package com.theokanning.openai.completion.chat.anthropic;

import java.util.List;

import lombok.Data;

/**
 * Created by flyun on 2024/4/4.
 */

@Data
public class ChatACompletionResponse {
   String id;
   String type;
   /**
    * Must be either 'user', or 'assistant'.<br>
    * You may use {@link ChatAMessageRole} enum.
    */
   String role;

   String model;
   String stop_sequence;
   String stop_reason;

   // 非流式中的内容
   List<ChatAMessage> content;

   ChatAUsage usage;

}
