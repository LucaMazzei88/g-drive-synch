package gDriveSynch;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mortbay.jetty.MimeTypes;

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
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.FileList;
import com.google.common.base.Joiner;

import gDriveSynch.driveClient.DriveClient;

public class DriveClientTest {

	DriveClient sut;

	private static final String APP_NAME = "g-drive-synch";
	
	@Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Before
	public void setup() throws GeneralSecurityException, IOException {
		JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
		HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
		List<String> scopes = Arrays.asList(DriveScopes.DRIVE);
		sut = new DriveClient(jsonFactory, httpTransport,
				getCredential(httpTransport, jsonFactory, scopes, new File("."),
						GoogleClientSecrets.load(jsonFactory,
								new InputStreamReader(
										DriveClientTest.class.getResourceAsStream("/client_secret.json")))),
				APP_NAME, scopes);
	}

	private Credential getCredential(HttpTransport httpTransport, JsonFactory jsonFactory, List<String> scopes,
			File dataStorePath, GoogleClientSecrets clientSecrets) throws IOException {
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.//
				Builder(httpTransport, jsonFactory, clientSecrets, scopes)//
						.setDataStoreFactory(new FileDataStoreFactory(dataStorePath))//
						.build();
		LocalServerReceiver receiver = new LocalServerReceiver.Builder().setHost("[::1]").setPort(8888).build();
		return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
	}
	
	@Test
	public void uploadTest() throws Exception {
		
		File test = writeOnFile(temporaryFolder.newFile(), "TEST");
		
		com.google.api.services.drive.model.File actual = sut.uploadFile(test, MimeTypes.TEXT_PLAIN);
		Assert.assertNotNull(actual);
		System.out.println("File id " + actual.getId() + " md5 sum " + actual.getMd5Checksum());
	}
	
	@Test
	public void folderTest() throws Exception {
		com.google.api.services.drive.model.File actual = sut.createFolder("FolderTest");
		Assert.assertNotNull(actual);
		System.out.println("File id " + actual.getId() + " md5 sum " + actual.getMd5Checksum());
	}
	
	@Test
	public void downloadFile() throws Exception {
		
		File actual = sut.downloadFile("1WUvNf8376qRCtU1yf7bVvmnqvHATY-Qg", temporaryFolder.newFolder());
		
		Assert.assertTrue(actual.isFile());
		System.out.println(Joiner.on(System.lineSeparator()).join(Files.readAllLines(actual.toPath())));
	}
	
	private File writeOnFile(File file, String content) throws IOException {
		try(FileWriter writer = new FileWriter(file)){
			writer.write(content);
		}
		return file;
	}
	
	@Test
	public void listFile() throws Exception {
		FileList list = sut.getFileList();
		printFiles(list.getFiles());
	}
	
	private void printFiles(List<com.google.api.services.drive.model.File> files) {
		if (files == null || files.isEmpty()) {
			System.out.println("No files found.");
		} else {
			System.out.println("Files:");
			for (com.google.api.services.drive.model.File file : files) {
				System.out.printf("name=%s id=%s md5=%s\n", file.getName(), file.getId(), file.getMd5Checksum());
			}
		}		
	}
}
