package org.uvhnael.ktal.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAiConfig {

    private final OpenAiChatModel openAiChatModel;

    public OpenAiConfig(OpenAiChatModel openAiChatModel) {
        this.openAiChatModel = openAiChatModel;
    }

    @Bean
    public ChatClient chatClient() {
        return ChatClient.builder(openAiChatModel).build();
    }
}
