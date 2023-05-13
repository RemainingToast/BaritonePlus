package adris.altoclef.baritone.brain.utils;

import adris.altoclef.Debug;
import com.theokanning.openai.OpenAiApi;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.finetune.FineTuneRequest;
import com.theokanning.openai.service.OpenAiService;

import java.time.Duration;
import java.util.List;

public class ChatGPT {
    private final OpenAiApi api;

    public ChatGPT(String apiKey) {
        this.api = OpenAiService.buildApi(apiKey, Duration.ofMinutes(1));
//        this.api.createFineTune(new FineTuneRequest(
//
//        ))
//        this.service = new OpenAiService(apiKey);
    }

    public String generateTaskNaturalLanguage(WorldState worldState) {
        // Create user message
        ChatMessage userMessage = new ChatMessage(
                "user",
                String.format("Given my current situation in an anarchy Minecraft server, what should my next goal be? Here's my current state:\n%s", worldState.toString())
        );

        // Create system message
        ChatMessage systemMessage = new ChatMessage(
                "system",
                "You are an AI playing on an anarchy Minecraft server. Use the information provided to decide the best next task. Max 50 Words."
        );

        // Create chat completion request
        ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
                .messages(List.of(systemMessage, userMessage))
                .model("gpt-3.5-turbo")
                .build();

        // Get completion from API
        return api.createChatCompletion(completionRequest).blockingGet()
                .getChoices().get(0).getMessage().getContent();
    }

    public String generateTask(WorldState worldState) {
        // Generate natural language task with ChatGPT as before...
        String task = generateTaskNaturalLanguage(worldState);

        Debug.logMessageBrain(task);

        // Create user message asking to convert task to command
        ChatMessage userMessage = new ChatMessage(
                "user",
                String.format("Given this task: '%s', what would be the appropriate Alto Clef bot command to accomplish it?", task)
        );

        // Create system message
        ChatMessage systemMessage = new ChatMessage(
                "system",
                "You are an AI assistant tasked with understanding and converting user inputs into valid tasks for the Alto Clef bot in Minecraft. " +
                        "Ensure to convert inputs into the most specific tasks possible and fill out required parameters. " +
                        "If no valid task can be derived from the input, return the Idle task." +
                        "Format: e.g 'mine wood' 'collect food 5' 'collect item'"
        );

        // Create chat completion request
        ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
                .messages(List.of(systemMessage, userMessage))
                .model("gpt-3.5-turbo")
                .temperature(0D)
                .build();

        // Get completion from API and return it as the command
        return api.createChatCompletion(completionRequest).blockingGet()
                .getChoices().get(0).getMessage().getContent();
    }
}

