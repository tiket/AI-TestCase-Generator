package jira.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import util.ConfigReader;

public class ConfluenceService {

    public static String getTheRequirement(String pageId) throws JsonProcessingException {
        //call the jira api to get all the content
        String jsonResponse = getContent(pageId);
        return getRequirementTextFromTheApiResponse(jsonResponse);

    }


    private static String getContent(String pageId) {
        // Set base URI
        RestAssured.baseURI = "https://borobudur.atlassian.net/wiki/rest/api/content/";
        // Authorization token
        String authToken = getAuthToken();//"cmFrZXNoLnNpbmdoQHRpa2V0LmNvbTpBVEFUVDN4RmZHRjBoSlNjRXIzajdrYWtpSFZlb3FjRVZQSk5nWUFCby02NVVUUVBjbVlWUHE2b1Ytd2M4eV80WnlVcUJYN2RsSWpwaC1GS1ZsVFJMYVQzYlhHNWZFbnlLcmFEdl90WFFiM1FFT3lGbi1ZcDdUMGZfOGNPZlJUX0FHX0FVZ09sSXZ6aVhGTzBMYmhLZEQ1RUloOWJyUVRFcmFDZTRPZy1KRHJYZkprbGNaREdzVk09OEIxNzUyNzM=";
        if (authToken == null || authToken.isEmpty()) {
            throw new IllegalArgumentException("Missing or empty Jira Auth token key :" + authToken);
        }
        // Send request and get response
        return RestAssured.given().queryParam("expand", "body.storage").log().all()
                .header("singleRequest.Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Basic " + authToken)
                .header("Cookie", "atlassian.xsrf.token=dfadd0249b3f13070f62043f3746fe5e4fb843c5_lin")
                .when()
                .get("/"+pageId).then().log().all()
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


    private static String getRequirementTextFromTheApiResponse(String jsonResponse) throws JsonProcessingException {
        JSONObject jsonObject = new JSONObject(jsonResponse);
        String title = jsonObject.getString("title");
        String storageValue = jsonObject.getJSONObject("body")
                .getJSONObject("storage")
                .getString("value");

        // Parse the HTML content using Jsoup
        Document doc = Jsoup.parse(storageValue);

       /* Elements paragraphs = doc.select("p");

        // Extract and concatenate the text from all <p> tags
        StringBuilder extractedText = new StringBuilder();
        extractedText.append(title);
        for (Element paragraph : paragraphs) {
            extractedText.append(paragraph.text()).append("\n");
        }*/

        return title+"\n"+doc.text();
        //return extractedText.toString();
    }


}
