package gptAI.parser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class CsvDataParser {

    public static List<String> parse(String text) {
        HashSet<String> testDataSet = new HashSet<>();
        ArrayList<String> testDataList = new ArrayList<>();

        String[] lines = text.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (testDataSet.add(line)) {
                testDataList.add(line);
            }
        }
        return testDataList;
    }



}
