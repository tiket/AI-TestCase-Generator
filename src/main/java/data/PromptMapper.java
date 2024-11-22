package data;

import data.constant.AccommodationPromptConstants;
import data.constant.B2BPromptConstants;
import data.constant.FlightPromptConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Main class for prompt mapping
public class PromptMapper {

    private Map<String, List<List<String>>> promptMapping;

    public PromptMapper(String tcCount) {
        promptMapping = new HashMap<>();
        initializeMappings(tcCount);
    }

    private void initializeMappings(String tcCount) {
        // Add mappings for Accommodation
        List<List<String>> accommodationPrompts = new ArrayList<>();
        accommodationPrompts.add(AccommodationPromptConstants.getInputPromptBE(tcCount));
        accommodationPrompts.add(AccommodationPromptConstants.getInputPromptFE(tcCount));
        promptMapping.put("Accommodation", accommodationPrompts);

        // Add mappings for Flight
        List<List<String>> flightPrompts = new ArrayList<>();
        flightPrompts.add(FlightPromptConstants.getInputPromptBE(tcCount));
        flightPrompts.add(FlightPromptConstants.getInputPromptFE(tcCount));
        promptMapping.put("Flight", flightPrompts);


        // Add mappings for B2B
        List<List<String>> b2bPrompts = new ArrayList<>();
        b2bPrompts.add(B2BPromptConstants.getInputPromptBE(tcCount));
        b2bPrompts.add(B2BPromptConstants.getInputPromptFE(tcCount));
        promptMapping.put("B2B", b2bPrompts);
    }

    public List<List<String>> getPrompts(String key) {
        return promptMapping.get(key);
    }

    public Map<String, List<List<String>>> getPromptMapping() {
        return promptMapping;
    }
}

