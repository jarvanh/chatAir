package com.theokanning.openai.completion.chat.anthropic;

import java.util.List;

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
//   String content;

   List<ChatAMessage> content;

   public List<ChatAMessage> setContentText(List<ChatAMessage> contents, String message) {
      ChatAMessage content = new ChatAMessage();
      content.setType(ChatAMessageType.TEXT.value());
      content.setText(message);
      contents.add(content);
      this.content = contents;

      return contents;
   }

   public List<ChatAMessage> setContentImg(List<ChatAMessage> contents, String base64) {
      ChatAMessage content = new ChatAMessage();
      content.setType(ChatAMessageType.IMAGE.value());

      ChatAContentSource source = new ChatAContentSource();
      source.setData(base64);
      source.setType("base64");
      source.setMediaType("image/png");

      content.setSource(source);

      contents.add(content);
      this.content = contents;

      return contents;
   }
}
