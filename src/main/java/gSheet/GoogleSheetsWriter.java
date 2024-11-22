package gSheet;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;

public class GoogleSheetsWriter {

    // Define the spreadsheet ID and range
    private static final String SPREADSHEET_ID = "1BWQExYzU6qvYF2jgrX98S23HYNyTV2zXQRGKZs4Wjok";
    private static final String RANGE = "Sheet1"; // Change according to your sheet

    // Write data to Google Sheet
    public void writeToSheet(Map<String, String> data) throws IOException, GeneralSecurityException {
        Sheets service = SheetsServiceUtil.getSheetsService();
        ValueRange body = new ValueRange()
                .setValues(mapToValues(data));

        AppendValuesResponse result = service.spreadsheets().values()
                .append(SPREADSHEET_ID, RANGE, body)
                .setValueInputOption("RAW")
                .execute();
        System.out.printf("%d cells updated.", result.getUpdates().getUpdatedCells());
    }

    // Convert Map data to ValueRange
    private List<List<Object>> mapToValues(Map<String, String> data) {
        List<List<Object>> values = new java.util.ArrayList<>();
        for (Map.Entry<String, String> entry : data.entrySet()) {
            java.util.ArrayList<Object> row = new java.util.ArrayList<>();
            row.add(entry.getKey());
            row.add(entry.getValue());
            values.add(row);
        }
        return values;
    }



}
