package aiprocessor;
import ai.AICSVGenerator;
import ai.AIClient;
import ai.AIClientFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import data.PromptMapper;
import gSheet.GoogleDocsReader;
import gSheet.GoogleSheetReader;
import jira.core.ConfluenceService;
import jira.core.JiraService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import util.ConfigReader;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class VerticalAIProcessor {
    // Initialize the Logger
    private static final Logger logger = LogManager.getLogger(VerticalAIProcessor.class);


    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            logger.error("Usage: VerticalAIProcessor <verticalName> <tabName> <aiType>");
            System.exit(1);
        }
        String verticalName = args[0];
        String tabName = args[1];
        String aiType = args[2];

        // Read the Google Sheet and store the values in a data structure
        String sheetName =  tabName;
        Map<Integer, Map<String, String>> data = null;
        try {
            data = GoogleSheetReader.readData(sheetName);
        } catch (GoogleJsonResponseException e) {
            if (e.getDetails() != null) {
                int statusCode = e.getDetails().getCode();
                if(statusCode==404){
                    logger.error("Error Message: " + e.getDetails().getMessage());
                    throw new IllegalArgumentException("Incorrect g-sheet id is provided");
                }else if(statusCode==400){
                    logger.error("Error Message: " + e.getDetails().getMessage());
                    throw new IllegalArgumentException("Incorrect g-sheet tab name  is provided");
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Error in reading the master data sheet");
        }
        // Read the API key from the configuration
        String apiKey = ConfigReader.getValue("api.key." + aiType);
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalArgumentException("Missing or empty API key for type: " + aiType);
        }
        AIClient aiClient = AIClientFactory.getAIClient(aiType, apiKey);
        try {
            processRows(data, aiClient, verticalName);
        } finally {
            GoogleSheetReader.updateAIStatus(sheetName, data);
        }
        System.exit(0);
    }

    private static void processRows(Map<Integer, Map<String, String>> data, AIClient aiClient, String verticalName) throws Exception {
        for (Map.Entry<Integer, Map<String, String>> entry : data.entrySet()) {
            Map<String, String> rowData = entry.getValue();
            String jiraId = rowData.get("JIRA ID").trim();
            if (isReadyForAI(rowData)) {
                List<List<String>> prompts = getPrompts(verticalName, rowData);
                String requirement = null;
                try {
                    requirement = fetchRequirement(rowData, jiraId);
                } catch (Exception e) {
                    rowData.put("AI TC Status", "Error in reading the requirement from the source [Jira/Doc]");
                    e.printStackTrace();
                    continue;
                }
                if (rowData.get("TC Type").trim().equals("BE")) {
                    AICSVGenerator.getResultFromAIAndGenerateCsvFile(requirement, prompts.get(0), jiraId, rowData, aiClient);
                } else {
                    AICSVGenerator.getResultFromAIAndGenerateCsvFile(requirement, prompts.get(1), jiraId, rowData, aiClient);
                }
            } else {
                logger.info("Type not implemented or case is not ready for AI");
            }
        }
    }

    private static List<List<String>> getPrompts(String verticalName, Map<String, String> rowData) {
        String count = rowData.get("Tc Count").trim();
        PromptMapper mapper = new PromptMapper(count);
        List<List<String>> prompts = mapper.getPrompts(verticalName);
        return prompts;
    }

    private static boolean isReadyForAI(Map<String, String> rowData) {
        return rowData.get("Ready for AI").trim().equals("Yes");
    }

    private static String fetchRequirement(Map<String, String> rowData, String jiraId) throws Exception {
        String requirement;
        if (rowData.get("Type").trim().equals("Jira")) {
            requirement = JiraService.getTheRequirementFromJira(jiraId);
        } else if (rowData.get("Type").trim().equals("Doc")) {
            String url = rowData.get("Doc Link").trim();
            requirement = GoogleDocsReader.readGoogleDoc(url);
        } else if (rowData.get("Type").trim().equals("Confluence")) {
            String pageId = rowData.get("ConfluenceLinks").trim();
            requirement = ConfluenceService.getTheRequirement(pageId);
        } else if (rowData.get("Type").trim().equals("multipleSource")) {
            requirement = handleMultiSource(rowData, jiraId);
        } else {
            throw new IllegalArgumentException("Unsupported type: " + rowData.get("Type").trim());
        }
        return requirement;
    }

    private static String handleMultiSource(Map<String, String> rowData, String jiraId) throws IOException {
        StringBuilder finalRequirement = new StringBuilder();

        // Fetch WikiLink requirements
        String wikiRequirements = processWikiLinks(rowData.get("ConfluenceLinks"));
        if (!wikiRequirements.isEmpty()) {
            finalRequirement.append(wikiRequirements).append("\n");  // Append to final result
        }

        // Fetch Google Doc requirements
        String docRequirements = processGoogleDocs(rowData.get("Doc Link"));
        if (!docRequirements.isEmpty()) {
            finalRequirement.append(docRequirements).append("\n");
        }

        // Fetch Jira requirements from the row and jiraID param
        String jiraRequirements = processJiraIDs(rowData.get("Jiras"), jiraId);
        if (!jiraRequirements.isEmpty()) {
            finalRequirement.append(jiraRequirements).append("\n");
        }
        return finalRequirement.toString().trim();  // Return the concatenated requirement string
    }

    // Method to process WikiLink column and fetch data from each link
    private static String processWikiLinks(String wikiLinks) throws JsonProcessingException {
        StringBuilder wikiRequirements = new StringBuilder();

        if (wikiLinks != null && !wikiLinks.trim().isEmpty()) {
            String[] wikiLinkArray = wikiLinks.split(",");
            for (String wikiLink : wikiLinkArray) {
                wikiLink = wikiLink.trim();  // Remove extra spaces
                if (!wikiLink.isEmpty()) {
                    // Call the wikiLink API and append the result to the requirement
                    String requirement = ConfluenceService.getTheRequirement(wikiLink);
                    wikiRequirements.append(requirement).append("\n");
                }
            }
        }
        return wikiRequirements.toString().trim();  // Return concatenated WikiLink data
    }

    // Method to process Google Doc column and fetch data from each link
    private static String processGoogleDocs(String googleDocs) throws IOException {
        StringBuilder docRequirements = new StringBuilder();

        if (googleDocs != null && !googleDocs.trim().isEmpty()) {
            String[] googleDocArray = googleDocs.split(",");
            for (String googleDoc : googleDocArray) {
                googleDoc = googleDoc.trim();  // Remove extra spaces
                if (!googleDoc.isEmpty()) {
                    // Call the Google Doc API and append the result to the requirement
                    String requirement = GoogleDocsReader.readGoogleDoc(googleDoc);
                    docRequirements.append(requirement).append("\n");
                }
            }
        }
        return docRequirements.toString().trim();  // Return concatenated Google Doc data
    }

    // Method to process Jira IDs from both the column and the separate jiraID parameter
    private static String processJiraIDs(String jiraIds, String jiraID) throws JsonProcessingException {
        StringBuilder jiraRequirements = new StringBuilder();

        if (jiraIds != null && !jiraIds.trim().isEmpty()) {
            String[] jiraIdArray = jiraIds.split(",");
            for (String jira : jiraIdArray) {
                jira = jira.trim();  // Remove extra spaces
                if (!jira.isEmpty()) {
                    // Call the Jira API and append the result to the requirement
                    String requirement = JiraService.getTheRequirementFromJira(jira);
                    jiraRequirements.append(requirement).append("\n");
                }
            }
        }
            // Process the jiraID parameter itself
        return jiraRequirements.toString().trim();  // Return concatenated Jira data
    }
}