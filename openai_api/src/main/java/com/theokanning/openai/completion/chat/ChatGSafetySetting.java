package com.theokanning.openai.completion.chat;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by flyun on 2023/12/16.
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatGSafetySetting {

    /**
     * see {@link ChatGSafetyCategory} documentation.
     */
    @JsonProperty("category")
    String category;

    /**
     * see {@link ChatGSafetyThreshold} documentation.
     */
    @JsonProperty("threshold")
    String threshold;

}
