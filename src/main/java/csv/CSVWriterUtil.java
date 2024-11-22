package csv;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;


public class CSVWriterUtil {

    public static void appendListToCSV(List<String> dataList, String filePath,Map<String, String> rowData) {
        boolean fileExists = new File(filePath).exists();
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath, true))) {
            // Write header if file does not exist or is empty
            if (!fileExists || new File(filePath).length() == 0) {
                writer.writeNext(new String[]{"Test cases"});
            }
            // Write data
            for (String data : dataList) {
                writer.writeNext(new String[]{data});
            }
            System.out.println("Data has been appended to CSV file successfully.");
            rowData.put("AI TC Status", "Success");
        } catch (IOException e) {
            rowData.put("AI TC Status", "Failed , Something wrong while writing to the csv file");
        }
    }
}
