package gDriveSynch;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.FileList;

public class GDriveSynchTest {

	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	private static final String APP_NAME = "g-drive-synch";
	private static final File DATA_STORE_DIR = new File(".");

	private static final List<String> SCOPES = Arrays.asList(DriveScopes.DRIVE_READONLY);

	private static HttpTransport httpTransport;
	private static FileDataStoreFactory dataStoreFactory;
	private static GoogleClientSecrets clientSecrets;

	@Before
	public void setup() throws GeneralSecurityException, IOException {
		httpTransport = GoogleNetHttpTransport.newTrustedTransport();
		dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);
		clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
				new InputStreamReader(GDriveSynchTest.class.getResourceAsStream("/client_secret.json")));

	}

	@Test
	public void fileList() throws IOException {

		// set up authorization code flow
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.//
				Builder(httpTransport, JSON_FACTORY, clientSecrets, SCOPES)//
						.setDataStoreFactory(dataStoreFactory)//
						.build();
		LocalServerReceiver receiver = new LocalServerReceiver.Builder().setHost("[::1]").setPort(8888).build();
		Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
		
		
		System.out.println(credential);
		
        Drive service = new Drive.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APP_NAME)
                .build();

        // Print the names and IDs for up to 10 files.
        FileList result = service.files().list()
                .setPageSize(10)
                .setFields("nextPageToken, files(id, name)")
                .execute();
        List<com.google.api.services.drive.model.File> files = result.getFiles();
        printFiles(files);
        printFiles(service.files().list().setPageSize(10).setPageToken(result.getNextPageToken()).execute().getFiles());
	}
	
	private void printFiles(List<com.google.api.services.drive.model.File> files) {
		if (files == null || files.isEmpty()) {
			System.out.println("No files found.");
		} else {
			System.out.println("Files:");
			for (com.google.api.services.drive.model.File file : files) {
				System.out.printf("%s (%s)\n", file.getName(), file.getId());
			}
		}		
	}

}
