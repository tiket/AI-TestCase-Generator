package jira.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gSheet.GoogleSheetReader;
import io.restassured.RestAssured;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import util.ConfigReader;

public class JiraService {
    private static final Logger logger = LogManager.getLogger(JiraService.class);


    public static String getTheRequirementFromJira(String JiraId) throws JsonProcessingException {
        //call the jira api to get all the content
        String jsonResponse = getJiraContent(JiraId);
        return getRequiremntTextFromTheJiraApiResponse(jsonResponse);
    }

    private static String getJiraContent(String JiraId) {
        // Set base URI
        RestAssured.baseURI = "https://borobudur.atlassian.net/rest/api/3";
        // Authorization token
        String authToken = getAuthToken();//"cmFrZXNoLnNpbmdoQHRpa2V0LmNvbTpBVEFUVDN4RmZHRjBoSlNjRXIzajdrYWtpSFZlb3FjRVZQSk5nWUFCby02NVVUUVBjbVlWUHE2b1Ytd2M4eV80WnlVcUJYN2RsSWpwaC1GS1ZsVFJMYVQzYlhHNWZFbnlLcmFEdl90WFFiM1FFT3lGbi1ZcDdUMGZfOGNPZlJUX0FHX0FVZ09sSXZ6aVhGTzBMYmhLZEQ1RUloOWJyUVRFcmFDZTRPZy1KRHJYZkprbGNaREdzVk09OEIxNzUyNzM=";
        if (authToken == null || authToken.isEmpty()) {
            throw new IllegalArgumentException("Missing or empty Jira Auth token key :" + authToken);
        }
        // Send request and get response
        return RestAssured.given().log().all()
                .header("singleRequest.Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Basic " + authToken)
                .header("Cookie", "atlassian.xsrf.token=dfadd0249b3f13070f62043f3746fe5e4fb843c5_lin")
                .body("{\"fields\": [\"description\"]}")
                .when()
                .get("/issue/" + JiraId + "")
                .then().log().all()
                .extract().response().asString();
    }

    private static String getAuthToken() {
        // Attempt to read from environment variable
        String authToken = System.getenv("jira.auth.key");
        if (authToken != null && !authToken.isEmpty()) {
            return authToken;
        }
        // Fallback to reading from properties file
        return ConfigReader.getValue("jira.auth.key");
    }

    private static String getRequiremntTextFromTheJiraApiResponse(String jsonResponse) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(jsonResponse);
        // Extract the summary
        StringBuilder descriptionText = new StringBuilder();
        String summary = jsonNode.path("fields").path("summary").asText("");
        descriptionText.append("summary:" +summary).append("\n\n");
        JsonNode descriptionNode = jsonNode.path("fields").path("description");
        if (!descriptionNode.isMissingNode() && descriptionNode.isObject()) {
            extractTextFromContentArray(descriptionNode.path("content"), descriptionText);
            return descriptionText.toString().trim();
        } else {
            logger.info("Description not found or is empty.");
            return null;
        }
    }


    private static void extractTextFromContentArray(JsonNode contentArray, StringBuilder descriptionText) {
        if (contentArray.isArray()) {
            for (JsonNode item : contentArray) {
                extractTextRecursively(item, descriptionText);
            }
        }
    }

    private static void extractTextRecursively(JsonNode node, StringBuilder descriptionText) {
        // If the node contains a "text" field, append its value
        if (node.has("text") && node.get("text").isTextual()) {
            descriptionText.append(node.get("text").asText()).append(" ");
        }

        // Recursively handle nodes with "content" fields
        if (node.has("content") && node.get("content").isArray()) {
            for (JsonNode child : node.get("content")) {
                extractTextRecursively(child, descriptionText);
            }
        }
    }
}
