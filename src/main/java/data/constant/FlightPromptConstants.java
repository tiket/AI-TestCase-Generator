package data.constant;

import java.util.ArrayList;
import java.util.List;

public class FlightPromptConstants {

    private static List<String> inputPromptBE;
    private static List<String> inputPromptFE;

    // Method to initialize the inputPromptBE with dynamic values
    public static List<String> getInputPromptBE(String dynamicValue) {
        if (inputPromptBE == null) {
            if(dynamicValue.equalsIgnoreCase("NA")){
                dynamicValue="";
            }else if(dynamicValue.equalsIgnoreCase("")||dynamicValue.equalsIgnoreCase("Default")){
                dynamicValue="20";
            }
            inputPromptBE = new ArrayList<>();
            inputPromptBE.add("Suggest some test cases for this software feature");
            inputPromptBE.add("Add test cases related to non-functional scenarios");
            inputPromptBE.add("Add test cases related to the system failure");

        }
        return inputPromptBE;
    }

    // Method to initialize the inputPromptFE with dynamic values
    public static List<String> getInputPromptFE(String dynamicValue) {
        if (inputPromptFE == null) {
            if(dynamicValue.equalsIgnoreCase("NA")){
                dynamicValue="";
            }else if(dynamicValue.equalsIgnoreCase("")||dynamicValue.equalsIgnoreCase("Default")){
                dynamicValue="20";
            }
            inputPromptFE = new ArrayList<>();
            inputPromptFE.add("Suggest some test cases for this software feature");
            inputPromptFE.add("Add test cases related to non-functional scenarios");
        }

        return inputPromptFE;
    }

}
