package gptAI.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class GPTResponseParser {

    public static String parseContent(String jsonResponse) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonResponse);

        // Navigate to the content field
        JsonNode choicesNode = rootNode.path("choices");
        if (choicesNode.isArray() && choicesNode.size() > 0) {
            JsonNode firstChoiceNode = choicesNode.get(0);
            JsonNode messageNode = firstChoiceNode.path("message");
            JsonNode contentNode = messageNode.path("content");
            return contentNode.asText();
        }
        return null; // or handle accordingly if choices array is empty
    }



}
