package gSheet;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;

public class SheetsServiceUtil {

    private static final String APPLICATION_NAME = "AI-BOT-POC";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance(); // Using JacksonFactory here for backward compatibility
    private static final String CREDENTIALS_FILE_PATH = "/oauth2.json";

    public static Sheets getSheetsService() throws IOException, GeneralSecurityException {
        System.out.println("Working Directory: " + System.getProperty("user.dir"));
        InputStream credentialsStream = SheetsServiceUtil.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (credentialsStream == null) {
            throw new FileNotFoundException("Credentials file not found: " + CREDENTIALS_FILE_PATH);
        }

        GoogleCredentials credentials = ServiceAccountCredentials.fromStream(credentialsStream)
                .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));


        return new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY, new HttpCredentialsAdapter(credentials))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
}



