package adris.altoclef.baritone.brain.utils;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;

import java.util.List;

public class ChatGPT {
    private final OpenAiService service;

    public ChatGPT(String apiKey) {
        this.service = new OpenAiService(apiKey);
    }

    public String generateTask(WorldState worldState) {
        // Create user message
        ChatMessage userMessage = new ChatMessage(
                "user",
                String.format("Given my current situation in an anarchy Minecraft server, what should my next goal be? Here's my current state:\n%s", worldState.toString())
        );

        // Create system message
        ChatMessage systemMessage = new ChatMessage(
                "system",
                "You are an AI playing on an anarchy Minecraft server. Your ultimate goals are to escape spawn, survive, and establish a thriving base in this challenging environment. Use the information provided to decide the best next task."
        );

        // Create chat completion request
        ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
                .messages(List.of(systemMessage, userMessage))
                .model("gpt-3.5-turbo")
                .build();

        // Get completion from API
        return service.createChatCompletion(completionRequest)
                .getChoices().get(0).getMessage().getContent();
    }
}

