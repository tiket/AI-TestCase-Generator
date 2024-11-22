package ai;

public class AIClientFactory {
    public static AIClient getAIClient(String type, String apiKey) {
        if ("gemini".equalsIgnoreCase(type)) {
            return new GeminiClient(apiKey);
        } else {
            return new ChatGPTClient(apiKey);
        }
    }
}
