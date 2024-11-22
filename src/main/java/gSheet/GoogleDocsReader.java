package gSheet;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GoogleDocsReader {

    private static final String CREDENTIALS_FILE_PATH = "/oauth2.json";
    // need to change this with ticket service account once api for doc is enabled , Need to raise a request to the tiket IT team .
    private static Set<Integer> processedNodes = new HashSet<>();


    public static String extractDocumentId(String url) {
        String regex = "https://docs\\.google\\.com/document/d/([a-zA-Z0-9-_]+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            throw new IllegalArgumentException("Invalid Google Docs URL: " + url);
        }
    }

    public static String getAccessToken() throws IOException {
        InputStream credentialsStream = SheetsServiceUtil.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (credentialsStream == null) {
            throw new FileNotFoundException("Credentials file not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream)
                .createScoped(Collections.singletonList("https://www.googleapis.com/auth/documents.readonly"));
        credentials.refreshIfExpired();
        AccessToken token = credentials.getAccessToken();
        return token.getTokenValue();
    }

    public static String readGoogleDoc(String documentUrl) throws IOException {
        Response response = getResponse(documentUrl);
        if (response.statusCode() == 200) {
            return parseContent(response.asString());
        } else {
            throw new IOException("Failed to retrieve document: " + response.statusLine());
        }
    }

    private static Response getResponse(String documentUrl) throws IOException {
        String documentId = extractDocumentId(documentUrl);
        System.out.println("document id is " + documentId);
        String accessToken = getAccessToken();
        String url = "https://docs.googleapis.com/v1/documents/" + documentId;
        Response response = RestAssured
                .given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .header("Accept", "application/json")
                .get(url).then().log().all().extract().response();
        return response;
    }

    public static String parseContent(String response) {
        JSONObject jsonResponse = new JSONObject(response);
        StringBuilder documentText = new StringBuilder();
        // Start the recursive search for 'content' and 'textRun'
        extractContentFromNode(jsonResponse, documentText);

        return documentText.toString();
    }

    private static void extractContentFromNode(JSONObject node, StringBuilder documentText) {
        // If the node has already been processed, skip it to avoid duplication
        if (processedNodes.contains(node.hashCode())) {
            return;
        }
        processedNodes.add(node.hashCode());

        // Iterate through all keys in the current JSON object
        Iterator<String> keys = node.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            Object value = node.get(key);

            // If the key is 'content' and the value is a JSON array, process it
            if (key.equals("content") && value instanceof JSONArray) {
                JSONArray contentArray = (JSONArray) value;
                for (int i = 0; i < contentArray.length(); i++) {
                    extractContentFromNode(contentArray.getJSONObject(i), documentText);
                }
            }

            // If the key is 'textRun' and the value is a JSON object, extract 'content'
            if (key.equals("textRun") && value instanceof JSONObject) {
                JSONObject textRun = (JSONObject) value;
                if (textRun.has("content")) {
                    documentText.append(textRun.getString("content"));
                }
            }

            // If the value is another JSONObject, recurse into it
            if (value instanceof JSONObject) {
                extractContentFromNode((JSONObject) value, documentText);
            }

            // If the value is a JSONArray, recurse into each of its elements
            if (value instanceof JSONArray) {
                JSONArray array = (JSONArray) value;
                for (int i = 0; i < array.length(); i++) {
                    Object arrayElement = array.get(i);
                    if (arrayElement instanceof JSONObject) {
                        extractContentFromNode((JSONObject) arrayElement, documentText);
                    }
                }
            }
        }
    }
}
