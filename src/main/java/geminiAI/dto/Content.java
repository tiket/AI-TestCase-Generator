package geminiAI.dto;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Content {
    private String role; // New attribute
    private List<ContentPart> parts;

    public Content(String role, List<ContentPart> parts) {
        this.role = role;
        this.parts = parts;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<ContentPart> getParts() {
        return parts;
    }

    public void setParts(List<ContentPart> parts) {
        this.parts = parts;
    }
}
