package ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gptAI.dto.Message;
import gptAI.dto.RequestBody;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import util.ConfigReader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChatGPTClient implements AIClient {

    private String apiKey;
    private  String baseUrl= ConfigReader.getValue("baseUrl.gpt");
    public ChatGPTClient(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public Response getResponse(String text) {
        // Create the message
        Message message = new Message("user", text);
        // Create the request body
        RequestBody requestBody = new RequestBody("gpt-4o", Arrays.asList(message));
        Response response = RestAssured.given()
                .baseUri(baseUrl)
                .header("Authorization", "Bearer " + apiKey)
                .urlEncodingEnabled(false)
                .contentType(ContentType.JSON)
                .body(requestBody).log().all()
                .post();
        return response;
    }

    @Override
    public Response getResponseMultipleReq(List<String> userRequests, List<String> previousResults) {
        ObjectMapper mapper = new ObjectMapper();
        // Construct the request body using POJO classes
        List<Message> messages = new ArrayList<>();
        // Add initial user message
        messages.add(new Message("user", userRequests.get(0)));
        // Add system responses and follow-up user messages
        for (int i = 0; i < previousResults.size(); i++) {
            messages.add(new Message("assistant", previousResults.get(i)));
            if (i + 1 < userRequests.size()) {
                messages.add(new Message("user", userRequests.get(i + 1)));
            }
        }
        // Create the request body
        RequestBody requestBody = new RequestBody("gpt-4", messages);
        // Convert the request body to JSON
        String updatedRequestBodyJson = null;
        try {
            updatedRequestBodyJson = mapper.writeValueAsString(requestBody);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        // Send the request
        Response response = RestAssured.given()
                .baseUri(baseUrl)
                .header("Authorization", "Bearer " + apiKey)
                .contentType(ContentType.JSON)
                .body(updatedRequestBodyJson).log().all()
                .post();
        return response;
    }

    @Override
    public String parseContent(String aiResult) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(aiResult);

        // Navigate to the content field
        JsonNode choicesNode = rootNode.path("choices");
        if (choicesNode.isArray() && choicesNode.size() > 0) {
            JsonNode firstChoiceNode = choicesNode.get(0);
            JsonNode messageNode = firstChoiceNode.path("message");
            JsonNode contentNode = messageNode.path("content");
            return contentNode.asText();
        }
        return null; // or handle accordingly if choices array is empty    }
    }
}