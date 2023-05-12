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
                "You are an AI playing on an anarchy Minecraft server. Your ultimate goals are to escape spawn, survive, and establish a thriving base in this challenging environment. Use the information provided to decide the best next task."
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
                "You are an AI assistant trained to understand the Alto Clef bot command system in Minecraft. Here are the key commands:\n" +
                        "- @help: Lists all commands.\n" +
                        "- @coords: Prints the bot's current coordinates.\n" +
                        "- @equip {material}: Equips a `material` armor set.\n" +
                        "- @follow {player = <you>}: Follows a player.\n" +
                        "- @food {amount}: Collects `amount` units of food.\n" +
                        "- @get [items...]: Gets all items in `[items...]`.\n" +
                        "- @give {player = <you>} {item} {quantity=1}: Gives `player` `quantity` units of `item`.\n" +
                        "- @goto {x} {y} {z} {dimension=<current>}: Goes to (`x`,`y`, `z`) in a given `dimension`.\n" +
                        "Your task is to convert user tasks into specific commands that the Alto Clef bot can execute."
        );

        // Create chat completion request
        ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
                .messages(List.of(systemMessage, userMessage))
                .model("gpt-3.5-turbo")
                .build();

        // Get completion from API and return it as the command
        return api.createChatCompletion(completionRequest).blockingGet()
                .getChoices().get(0).getMessage().getContent();
    }
}

