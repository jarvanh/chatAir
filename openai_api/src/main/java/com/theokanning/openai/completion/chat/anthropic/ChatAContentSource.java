package com.theokanning.openai.completion.chat.anthropic;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by flyun on 2024/8/15.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
class ChatAContentSource {

   @JsonProperty("data")
   String data;
   @JsonProperty("media_type")
   String mediaType;
   @JsonProperty("type")
   String type;

}
