package gptAI.dto;

import java.util.List;

public class RequestBody {
    private String model;
    private List<Message> messages;

    // Constructors
    public RequestBody() {}

    public RequestBody(String model, List<Message> messages) {
        this.model = model;
        this.messages = messages;
    }

    // Getters and Setters
    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
}
