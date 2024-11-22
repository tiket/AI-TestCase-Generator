package data.constant;

import java.util.ArrayList;
import java.util.List;

public class B2BPromptConstants {

    private static List<String> inputPromptBE;
    private static List<String> inputPromptFE;

    // Method to initialize the inputPromptBE with dynamic values
    public static List<String> getInputPromptBE(String dynamicValue) {
        if (inputPromptBE == null) {
            if(dynamicValue.equalsIgnoreCase("NA")){
                dynamicValue="";
            }else if(dynamicValue.equalsIgnoreCase("")||dynamicValue.equalsIgnoreCase("Default")){
                dynamicValue="";
            }
            inputPromptBE = new ArrayList<>();
            inputPromptBE.add("This is product requirement for changes around Backend/API task. Act like a software testing specialist who is expert in OTA or travel domain, analyze below requirement and give me " + dynamicValue + " best quality distinct one liner valid test cases avoid giving headings/title");
            inputPromptBE.add("Can you give me additional  distinct one liner test cases related to positive, functional, business logic, data specific or any other conditions missed from requirement and don’t repeat those test cases which are already shared");
            inputPromptBE.add("Can you give me additional more one liner test cases related to error handling, negative, edge cases related to backend/API etc. and don’t repeat the cases which are already shared");
            inputPromptBE.add("Focus on the requirement again carefully and share  more effective test cases related to backward/forward compatibility or any other conditions mentioned in requirement which is not covered yet. and please don’t repeat cases which are already shared");
        }
        return inputPromptBE;
    }

    // Method to initialize the inputPromptFE with dynamic values
    public static List<String> getInputPromptFE(String dynamicValue) {
        if (inputPromptFE == null) {
            if(dynamicValue.equalsIgnoreCase("NA")){
                dynamicValue="";
            }else if(dynamicValue.equalsIgnoreCase("")||dynamicValue.equalsIgnoreCase("Default")){
                dynamicValue="";
            }
            inputPromptFE = new ArrayList<>();
            inputPromptFE.add("This is product requirement for changes around Frontend. Act like a software testing specialist who is expert in OTA or travel domain, analyze below requirement and give me " + dynamicValue + " best quality distinct one liner valid test cases avoid giving headings/title");
            inputPromptFE.add("Can you give me additional  one liner test cases related to positive, functional, UI, business logic, data specific or conditions mentioned in the requirements. and please don’t repeat those test cases which are already shared");
            inputPromptFE.add("Can you give me additional more one liner test cases related to Integration, error handling, negative, edge cases, usability etc and don’t repeat the cases which are already shared avoid giving performance, security cases and skip headings/title also");
            inputPromptFE.add("Focus on the requirement again carefully and share more effective test cases related to backward/forward compatibility, analytics or any other conditions mentioned in requirement which is not covered yet and please don’t repeat cases which are already shared");
        }
        return inputPromptFE;
    }
}

