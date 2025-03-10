package jira.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gSheet.GoogleSheetReader;
import io.restassured.RestAssured;
import io.restassured.response.Response;
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
        String authToken = getAuthToken();
        if (authToken == null || authToken.isEmpty()) {
            throw new IllegalArgumentException("Missing or empty Jira Auth token key: " + authToken);
        }

        int maxAttempts = 3;
        int delay = 2000; // Start with 2 seconds

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                // Send API request without body or cookie
                Response response = RestAssured.given()
                        .log().all()
                        .header("Content-Type", "application/json")
                        .header("Accept", "application/json")
                        .header("Authorization", "Basic " + authToken)
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:122.0) Gecko/20100101 Firefox/122.0")
                        .when()
                        .get("/issue/" + JiraId + "?fields=description")
                        .then()
                        .log().all()
                        .extract()
                        .response();

                int statusCode = response.getStatusCode();

                if (statusCode == 403) {
                    System.out.println("❌ Received 403 Forbidden. Attempt " + attempt + " of " + maxAttempts);
                    if (attempt == maxAttempts) {
                        System.out.println("❌ Maximum attempts reached. Skipping this Jira ID: " + JiraId);
                        return null;
                    }
                    // Exponential backoff delay
                    Thread.sleep(delay);
                    delay *= 2;
                } else if (statusCode == 200) {
                    System.out.println("✅ Successfully fetched requirement for: " + JiraId);
                    return response.asString();
                } else {
                    System.out.println("❌ Unexpected status code: " + statusCode);
                    return null;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("❌ Thread interrupted.");
                return null;
            } catch (Exception e) {
                System.out.println("❌ Failed due to: " + e.getMessage());
                return null;
            }
        }
        return null;
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
