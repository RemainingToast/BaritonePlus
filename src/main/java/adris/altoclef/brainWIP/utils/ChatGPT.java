package adris.altoclef.brainWIP.utils;

import adris.altoclef.Debug;
import com.theokanning.openai.OpenAiApi;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;

import java.time.Duration;
import java.util.List;

public class ChatGPT {
    private final OpenAiApi api;

    protected ChatGPT(String apiKey) {
        this.api = OpenAiService.buildApi(apiKey, Duration.ofMinutes(1));
    }

    public static ChatGPT build() {
        return new ChatGPT("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");
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

        return prompt(List.of(systemMessage, userMessage));
    }

    public String prompt(List<ChatMessage> messages) {
        // Create chat completion request
        ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
                .messages(messages)
                .model("gpt-3.5-turbo")
                .build();

        // Get completion from API
        return api.createChatCompletion(completionRequest).blockingGet()
                .getChoices().get(0).getMessage().getContent();
    }

    public String generateTask(WorldState worldState) {
        // Generate natural language task with ChatGPT as before...
        String task = generateTaskNaturalLanguage(worldState);

        Debug.logMessage(task);

        // Create user message asking to convert task to command
        ChatMessage userMessage = new ChatMessage(
                "user",
                String.format("Given this task: '%s', what would be the appropriate command to accomplish it? Execute consecutve commands by separating them with ; (e.g @get iron_axe;get log 100;goto 0 0;give Player log 100)", task)
        );

        StringBuilder lines = new StringBuilder();
        /*for (PlusCommand c : AltoClef.getCommandExecutor().allCommands()) {
            lines.append("@").append(c.getHelpRepresentation()).append(" | ").append(c.getDescription());
            lines.append("\n");
        }*/

        // Create system message
        ChatMessage systemMessage = new ChatMessage(
                "system",
                "You are an AI assistant tasked with understanding and converting user inputs into valid commands for the Alto Clef bot in Minecraft.\n" +
                        "Ensure to convert inputs into the most specific commands possible and fill out required parameters. Including coordinates/locations\n" +
                        "If no valid task can be derived from the input, return the @idle command\n" +
                        "Respond only with the command chain - no explanations - only command\n" +
                        "Parameters requiring name assume specific item name e.g diamond_chestplate\n" +
                        "You must fill out all parameters with values. \n" +
                        "AltoClef Commands that you can use: " + lines
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

