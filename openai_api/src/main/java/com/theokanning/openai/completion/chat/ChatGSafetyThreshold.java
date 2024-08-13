package com.theokanning.openai.completion.chat;

/**
 * Created by flyun on 2024/8/9.
 */
public enum ChatGSafetyThreshold {

   // 无论是否存在不安全内容，一律显示
   BLOCK_NONE("BLOCK_NONE"),
   // 屏蔽高概率不安全的内容时
   BLOCK_ONLY_HIGH("BLOCK_ONLY_HIGH"),
   // 当不安全内容的可能性为中等或较高时屏蔽
   BLOCK_MEDIUM_AND_ABOVE("BLOCK_MEDIUM_AND_ABOVE"),
   // 在出现不安全内容的可能性为“低”“中”或“高”时屏蔽
   BLOCK_LOW_AND_ABOVE("BLOCK_LOW_AND_ABOVE"),
   // 阈值未指定，使用默认阈值屏蔽
   HARM_BLOCK_THRESHOLD_UNSPECIFIED("HARM_BLOCK_THRESHOLD_UNSPECIFIED");


   private final String value;

   ChatGSafetyThreshold(final String value) {
      this.value = value;
   }

   public String value() {
      return value;
   }

}
