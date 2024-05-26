package com.theokanning.openai.completion.chat.anthropic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatARequestMessage {

   /**
    * Must be either 'user', or 'assistant'.<br>
    * You may use {@link ChatAMessageRole} enum.
    */
   String role;
   String content;
}
