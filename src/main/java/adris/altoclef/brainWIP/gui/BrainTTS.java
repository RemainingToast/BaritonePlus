package adris.altoclef.brainWIP.gui;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.brainWIP.utils.ChatGPT;
import adris.altoclef.tasksystem.Task;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import javazoom.jl.player.Player;

import java.io.FileInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;

public class BrainTTS {

    private final String ELEVEN_LABS = "https://api.elevenlabs.io/v1/text-to-speech/%s";
    private final String DONALD_TRUMP = "mknJFtyZvEeGM4w0Tqk1";
    private final String FIT_MC = "9xpRL3WTA9FtDLHMul3v";

    private final BlockingQueue<Task> queue = new LinkedBlockingQueue<>();

    private final AltoClef mod;
    private final ChatGPT gpt;
    private final Gson gson;

    public BrainTTS(AltoClef mod) {
        this.mod = mod;
        this.gpt = ChatGPT.build();
        this.gson = new Gson();

        // Start a new thread to play audio
        new Thread(() -> {
            while (true) {
                try {
                    var task = queue.take(); // This will block until a task is available

                    Debug.logInternal("TTS: " + task);

                    var dialogue = taskToDialogue(task).get();

                    Debug.logInternal("TTS: " + dialogue);

                    var audio = generateAudioFromText(dialogue);

                    Debug.logInternal("TTS: " + audio);

                    playAudio(audio);
                } catch (Exception e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }).start();
    }

    public void narrateTask(Task task) {
//        Debug.logInternal("TTS: Task Added: %s", task.toString());
        this.queue.add(task);
    }

    public void clearDialogue() {
        this.queue.clear();
    }

    public boolean removeTask(Task task) {
        return this.queue.remove(task);
    }

    private void playAudio(String filePath) {
        try {
            FileInputStream fis = new FileInputStream(filePath);
            Player playMP3 = new Player(fis);
            playMP3.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String generateAudioFromText(String dialogue) throws Exception {
        var payload = new Payload(dialogue);
        var json = this.gson.toJson(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format(ELEVEN_LABS, FIT_MC)))
                .header("xi-api-key", "d755ebaffb9c30fba4400c9f4f9f0473")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<byte[]> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofByteArray());

        if(response.statusCode() == 200) {
            Path audioFilePath = Files.createTempFile("tts-", ".mp3");
            Files.write(audioFilePath, response.body());
            Debug.logInternal("TTS: " + audioFilePath.toAbsolutePath());
            return audioFilePath.toString();
        } else {
            throw new Exception("Failed to generate audio from text");
        }
    }

    private CompletableFuture<String> taskToDialogue(Task task) {
        return CompletableFuture.completedFuture(this.gpt.prompt(List.of(
                new ChatMessage(ChatMessageRole.USER.value(),
                        String.format("Generate engaging sentence for the following task: %s",
                                task.toString().replaceAll("Killing", "Hunting")
                        )
                ),
                new ChatMessage(ChatMessageRole.SYSTEM.value(),
                        "You are a 2b2t Historian playing Minecraft on a Live Stream - return only what he would say - do not include any text that isn't dialogue"
                )
        )));
    }

    protected static class Payload {
        @SerializedName("text")
        String dialogue;
        @SerializedName("model_id")
        String modelId = "eleven_monolingual_v1";
        @SerializedName("voice_settings")
        VoiceSettings voiceSettings = new VoiceSettings();

        Payload(String dialogue) {
            this.dialogue = dialogue;
        }

        protected static class VoiceSettings {
            double stability = 0.75;
            @SerializedName("similarity_boost")
            double similarityBoost = 0.75;
        }
    }
}
