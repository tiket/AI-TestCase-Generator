package geminiAI.dto;
import java.util.List;

public class RequestBody {

    private List<Content> contents;

    public RequestBody(List<Content> contents) {
        this.contents = contents;
    }

    public List<Content> getContents() {
        return contents;
    }

    public void setContents(List<Content> contents) {
        this.contents = contents;
    }

}
