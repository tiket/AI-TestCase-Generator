package ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.response.Response;

import java.util.List;

public interface AIClient {
    Response getResponse(String text);
    Response getResponseMultipleReq(List<String> userRequests, List<String> previousResults);
    String parseContent(String aiResult) throws JsonProcessingException;
}
