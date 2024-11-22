package gSheet;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import util.ConfigReader;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GoogleSheetReader {

    // Modify this data.constant with your spreadsheet ID
    private static final String SPREADSHEET_ID = ConfigReader.getValue("google.sheet.id").trim();

    public static Map<Integer, Map<String, String>> readData(String sheetName) throws IOException, GeneralSecurityException {
        Sheets service = SheetsServiceUtil.getSheetsService();
        String range = sheetName + "!A:J"; // Assuming data starts from A1 and includes "AI TC Status" in column G
        ValueRange response = service.spreadsheets().values()
                .get(SPREADSHEET_ID, range)
                .execute();

        List<List<Object>> values = response.getValues();
        Map<Integer, Map<String, String>> data = new HashMap<>();

        if (values == null || values.isEmpty()) {
            System.out.println("No data found.");
        } else {
            List<Object> headerRow = values.get(0);
            int jiraIdIndex = headerRow.indexOf("JIRA ID");

            if (jiraIdIndex != -1) {
                for (int rowIndex = 1; rowIndex < values.size(); rowIndex++) {
                    List<Object> row = values.get(rowIndex);
                    Map<String, String> rowData = new HashMap<>();
                    String jiraId = row.size() > jiraIdIndex ? (String) row.get(jiraIdIndex) : "";

                    for (int i = 0; i < headerRow.size(); i++) {
                        if (i < row.size()) {
                            rowData.put((String) headerRow.get(i), (String) row.get(i));
                        } else {
                            rowData.put((String) headerRow.get(i), "");
                        }
                    }

                    data.put(rowIndex, rowData);
                }
            } else {
                System.out.println("JIRA ID column not found.");
            }
        }
        return data;
    }

    public static void updateAIStatus(String sheetName, Map<Integer, Map<String, String>> data) throws IOException, GeneralSecurityException {
        Sheets service = SheetsServiceUtil.getSheetsService();
        List<List<Object>> values = new ArrayList<>();
        List<String> ranges = new ArrayList<>();

        for (Map.Entry<Integer, Map<String, String>> entry : data.entrySet()) {
            int rowIndex = entry.getKey();
            Map<String, String> row = entry.getValue();

            List<Object> rowValue = new ArrayList<>();
            rowValue.add(row.get("AI TC Status"));
            values.add(rowValue);

            String range = sheetName + "!J" + (rowIndex + 1); // Update specific row
            ranges.add(range);
        }

        for (int i = 0; i < ranges.size(); i++) {
            ValueRange body = new ValueRange().setValues(List.of(values.get(i)));
            service.spreadsheets().values()
                    .update(SPREADSHEET_ID, ranges.get(i), body)
                    .setValueInputOption("RAW")
                    .execute();
        }
    }

}
