package gDriveSynch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.googleapis.media.MediaHttpDownloader;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files.Create;
import com.google.api.services.drive.Drive.Files.Get;
import com.google.api.services.drive.model.FileList;

public class DriveClient {

	private final JsonFactory jsonFactory;
	private final HttpTransport httpTransport;
	private final Credential credential;
	private final List<String> scopes;
	private final Drive drive;

	public DriveClient(JsonFactory jsonFactory, HttpTransport httpTransport, Credential credential, String appName,
			List<String> scopes) {
		super();
		this.jsonFactory = jsonFactory;
		this.httpTransport = httpTransport;
		this.credential = credential;
		this.scopes = scopes;
		this.drive = new Drive.Builder(httpTransport, jsonFactory, credential).setApplicationName(appName).build();
	}

	public FileList getFileList() throws IOException {
		com.google.api.services.drive.Drive.Files.List list = drive.files().list().setFields("files(id, name, md5Checksum)");
		return list.execute();
	}
	
	public com.google.api.services.drive.model.File createFolder(String name, String... parentsIDS) throws IOException {
		if(null == name) {
			throw new IllegalArgumentException("Folder must have a valid name");
		}
		com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
		
		fileMetadata.setName(name);
		if(null != parentsIDS && parentsIDS.length > 0) {
			fileMetadata.setParents(Arrays.asList(parentsIDS));
		}
		
		fileMetadata.setMimeType("application/vnd.google-apps.folder");

		return drive.files().create(fileMetadata)
		    .setFields("id")
		    .execute();
	}

	public com.google.api.services.drive.model.File uploadFile(File file, String mimeType) throws GoogleJsonResponseException, IOException {

		com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();

		fileMetadata.setName(file.getName());
		FileContent fileContent = new FileContent(mimeType, file);
		Create create = drive.files().create(fileMetadata, fileContent);
		GenericUrl genericUrl = create.buildHttpRequestUrl();
		MediaHttpUploader uploader = create.getMediaHttpUploader();
		uploader.setProgressListener(new CustomProgressListener());
		HttpResponse response = uploader.upload(genericUrl);
		if(!response.isSuccessStatusCode()){
			throw GoogleJsonResponseException.from(jsonFactory, response);
		}
		
		try (InputStream in = response.getContent()){
			return jsonFactory.fromInputStream(in, com.google.api.services.drive.model.File.class);
		}
	}

	public File downloadFile(String id, File outputDir) throws IOException {
		
		Get get = drive.files().get(id);
		com.google.api.services.drive.model.File metadata = get.setFields("name").execute();
		File output = new File(outputDir, metadata.getName());
		
		MediaHttpDownloader downloader = get.getMediaHttpDownloader();
//		downloader.setProgressListener(progressListener);
		try(OutputStream out = new FileOutputStream(output)){
			downloader.download(get.buildHttpRequestUrl(), out);
		}
		
		return output;
	}
}
