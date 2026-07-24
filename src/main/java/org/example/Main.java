package org.example;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.example.web.WebServer;

import java.io.IOException;

import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {

    private static final String OPENAI_API_KEY = resolveApiKey();

    static ChatModel model = OpenAiChatModel.builder()
            .apiKey(OPENAI_API_KEY)
            .modelName(GPT_4_O_MINI)
            .build();

    /**
     * Reads the OpenAI API key from the OPENAI_API_KEY environment variable
     * (falling back to the "openai.api.key" system property). Never hardcode
     * the key in source code.
     */
    private static String resolveApiKey() {
        String key = System.getenv("OPENAI_API_KEY");
        if (key == null || key.isBlank()) {
            key = System.getProperty("openai.api.key");
        }
        if (key == null || key.isBlank()) {
            throw new IllegalStateException(
                    "Missing OpenAI API key. Set the OPENAI_API_KEY environment variable "
                            + "(or pass -Dopenai.api.key=... as a JVM argument) before running the app.");
        }
        return key;
    }


    private static final int PORT = 8090;

    public static void main(String[] args) throws IOException {

        WebServer webServer = new WebServer(model, PORT);
        webServer.start();
        System.out.println("Upwork Job Analyzer is running: http://localhost:" + PORT);
    }


}
