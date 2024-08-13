package com.theokanning.openai.completion.chat;

/**
 * Created by flyun on 2024/8/9.
 */
public enum ChatGSafetyCategory {
   HARM_CATEGORY_HARASSMENT("HARM_CATEGORY_HARASSMENT"),
   HARM_CATEGORY_HATE_SPEECH("HARM_CATEGORY_HATE_SPEECH"),
   HARM_CATEGORY_SEXUALLY_EXPLICIT("HARM_CATEGORY_SEXUALLY_EXPLICIT"),
   HARM_CATEGORY_DANGEROUS_CONTENT("HARM_CATEGORY_DANGEROUS_CONTENT");


   private final String value;

   ChatGSafetyCategory(final String value) {
      this.value = value;
   }

   public String value() {
      return value;
   }

}
