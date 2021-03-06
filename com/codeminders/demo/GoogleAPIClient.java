package com.codeminders.demo;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.codeminders.demo.model.Location;
import com.codeminders.demo.model.ProjectDescriptor;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.EmptyContent;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.cloudresourcemanager.CloudResourceManager;
import com.google.api.services.cloudresourcemanager.model.ListProjectsResponse;
import com.google.api.services.cloudresourcemanager.model.Project;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Tokeninfo;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.pixelmed.slf4j.Logger;
import com.pixelmed.slf4j.LoggerFactory;

public class GoogleAPIClient {
    private final static Logger logger = LoggerFactory.getLogger(GoogleAPIClient.class);
    /**
     * Be sure to specify the name of your application. If the application name is
     * {@code null} or blank, the application will log a warning. Suggested format
     * is "MyCompany-ProductName/1.0".
     */
    private static final String APPLICATION_NAME = "Codeminders-DicomCleanerDEMO/1.0";

    private static final String GDRIVE_REPORT_FOLDER_NAME = "De-id";
    
    /**
     * Directory to store user credentials.
     */
    private static final java.io.File DATA_STORE_DIR = new java.io.File(System.getProperty("user.home"), ".store/google_dicom_cleaner_auth_with_drive");

    /**
     * Global instance of the {@link DataStoreFactory}. The best practice is to make
     * it a single globally shared instance across your application.
     */
    private static DataStoreFactory dataStoreFactory;

    /**
     * Global instance of the HTTP transport.
     */
    private static HttpTransport httpTransport;

    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    /**
     * OAuth 2.0 scopes.
     */
    private static final List<String> SCOPES = Arrays.asList(
            "https://www.googleapis.com/auth/cloud-healthcare",
            "https://www.googleapis.com/auth/cloud-platform",
            "https://www.googleapis.com/auth/userinfo.profile",
            "https://www.googleapis.com/auth/userinfo.email",
            "https://www.googleapis.com/auth/drive",
            "https://www.googleapis.com/auth/drive.appdata",
            "https://www.googleapis.com/auth/drive.apps.readonly",
            "https://www.googleapis.com/auth/drive.file");

    private static Oauth2 oauth2;
    private static GoogleClientSecrets clientSecrets;

    /**
     * Instance of Google Cloud Resource Manager
     */
    private static CloudResourceManager cloudResourceManager;
    private static Drive drive;

    private boolean isSignedIn = false;
    private String accessToken;
    
    private List<GoogleAuthListener> listeners = new ArrayList<>();
    
    private GoogleApplicationEntity applicationEntity = new GoogleApplicationEntity("Google Health Care");
    
    private SimpleDateFormat timeFormatter = new SimpleDateFormat("yyyyMMdd_HHmm");
    
    private String googleSecretPath;
    
    protected GoogleAPIClient(String googleSecretPath) {
    	this.googleSecretPath = googleSecretPath;
    }
    
    public void cleanAuth() {
    	if (!isSignedIn()) {
    		try {
        		Path rootPath = Paths.get(DATA_STORE_DIR.getAbsolutePath());     
        		final List<Path> pathsToDelete = Files.walk(rootPath).sorted(Comparator.reverseOrder()).collect(Collectors.toList());
        		for(Path path : pathsToDelete) {
        		    Files.deleteIfExists(path);
        		}
    		} catch(Exception e) {
    			logger.error("There is error during delteing directory", e);
    		}
    	}
    }

    private Credential authorize() throws Exception {
        // load client secrets
    	Path secrets = Paths.get(googleSecretPath).toAbsolutePath();
        clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(Files.newInputStream(secrets)));
        if (clientSecrets.getDetails().getClientId().startsWith("Enter")
                || clientSecrets.getDetails().getClientSecret().startsWith("Enter ")) {
            System.out.println("Enter Client ID and Secret from https://code.google.com/apis/console/ "
                    + "into client_secrets.json");
            System.exit(1);
        }
        // set up authorization code flow
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY,
                clientSecrets, SCOPES).setDataStoreFactory(dataStoreFactory).build();
        // authorize
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }
    
    public void addListener(GoogleAuthListener listener) {
    	listeners.add(listener);
    }
    
    public void removeListener(GoogleAuthListener listener) {
    	listeners.remove(listener);
    }

    public void signIn() throws Exception {
    	if (!isSignedIn) {
    		try {
    			signInInternal();
    		} catch(Exception e) {
        		DATA_STORE_DIR.delete();
        		signInInternal();
    		}
    	}
    }

    private void signInInternal() throws Exception {
        if (!isSignedIn) {
            int tryCount = 0;
            Exception error;
            do {
                try {
                    tryCount++;
                    httpTransport = GoogleNetHttpTransport.newTrustedTransport();
                    dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR); //new MemoryDataStoreFactory();
                    // authorization
                    Credential credential = authorize();
                    // set up global Oauth2 instance
                    oauth2 = new Oauth2.Builder(httpTransport, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME)
                            .build();

                    cloudResourceManager = new CloudResourceManager.Builder(httpTransport, JSON_FACTORY, credential)
                            .build();
                    drive = new Drive.Builder(httpTransport, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME)
                    		.build();
                    accessToken = credential.getAccessToken();
                    System.out.println("Token:" + accessToken);
                    // run commands
                    tokenInfo(accessToken);
                    error = null;
                    isSignedIn = true;
                    for (GoogleAuthListener listener: listeners) {
                    	listener.authorized();
                    }
                } catch (Exception e) {
                    logger.error("Error occurred during authorization", e);
                    error = e;
                    e.printStackTrace();
                    logger.info("Retry authorization");
                    System.out.println("Retry authorization:");
                }
            } while (!isSignedIn && tryCount < 4);
            if (error != null) {
                throw new IllegalStateException(error);
            }
        }
    }
    
    public GoogleApplicationEntity getApplicationEntity() {
		return applicationEntity;
	}
    
    public boolean isSignedIn() {
		return isSignedIn;
	}
    
    public String getAccessToken() {
		return accessToken;
	}

    private static void tokenInfo(String accessToken) throws IOException {
        System.out.println("Validating token");
        Tokeninfo tokeninfo = oauth2.tokeninfo().setAccessToken(accessToken).execute();
        System.out.println(tokeninfo.toString());
        if (!tokeninfo.getAudience().equals(clientSecrets.getDetails().getClientId())) {
            System.err.println("ERROR: audience does not match our client ID!");
        }
    }

    public List<ProjectDescriptor> fetchProjects() throws Exception {
        signIn();
        List<ProjectDescriptor> result = new ArrayList<ProjectDescriptor>();
        CloudResourceManager.Projects.List request = cloudResourceManager.projects().list();
        ListProjectsResponse response;
        do {
            response = request.execute();
            if (response.getProjects() == null) {
                continue;
            }
            for (Project project : response.getProjects()) {
                result.add(new ProjectDescriptor(project.getName(), project.getProjectId()));
            }
            request.setPageToken(response.getNextPageToken());
        } while (response.getNextPageToken() != null);
        return result;
    }

    private String parseName(String name) {
        return name.substring(name.lastIndexOf("/") + 1);
    }

    private HttpResponse googleRequest(String url) throws Exception {
        signIn();
        System.out.println("Google request url:" + url);
        HttpRequest request = httpTransport.createRequestFactory().buildGetRequest(new GenericUrl(url));
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", Arrays.asList(new String[]{"Bearer " + accessToken}));
        request.setHeaders(headers);
        return request.execute();
    }
    
    private HttpResponse googlePostRequest(String url, HttpContent content) throws Exception {
        signIn();
        System.out.println("Google request url:" + url);
        HttpRequest request = httpTransport.createRequestFactory().buildPostRequest(new GenericUrl(url), content);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", Arrays.asList(new String[]{"Bearer " + accessToken}));
        request.setHeaders(headers);
        return request.execute();
    }

    public List<Location> fetchLocations(String projectId) throws Exception {
        signIn();
        String url = "https://healthcare.googleapis.com/v1alpha/projects/" + projectId + "/locations";
        String data = googleRequest(url).parseAsString();
        JsonParser parser = new JsonParser();
        JsonElement jsonTree = parser.parse(data);
        JsonArray jsonObject = jsonTree.getAsJsonObject().get("locations").getAsJsonArray();
        List<Location> list = StreamSupport.stream(jsonObject.spliterator(), false)
                .map(obj -> obj.getAsJsonObject())
                .map(obj -> {
                    return new Location(obj.get("name").getAsString(), obj.get("locationId").getAsString());
                })
                .collect(Collectors.toList());
        return list;
    }

    public List<String> fetchDatasets(String projectId, String locationId) throws Exception {
        signIn();
        String url = "https://healthcare.googleapis.com/v1alpha/projects/" + projectId + "/locations/" + locationId + "/datasets";
        String data = googleRequest(url).parseAsString();
        JsonParser parser = new JsonParser();
        JsonElement jsonTree = parser.parse(data);
        JsonArray jsonObject = jsonTree.getAsJsonObject().get("datasets").getAsJsonArray();
        return StreamSupport.stream(jsonObject.spliterator(), false)
                .map(obj -> obj.getAsJsonObject().get("name").getAsString())
                .map(this::parseName)
                .collect(Collectors.toList());
    }

    public List<String> fetchDicomstores(String projectId, String locationId, String dataset) throws Exception {
        signIn();
        String url = "https://healthcare.googleapis.com/v1alpha/projects/" + projectId + "/locations/" + locationId + "/datasets/" + dataset + "/dicomStores";
        String data = googleRequest(url).parseAsString();
        JsonParser parser = new JsonParser();
        JsonElement jsonTree = parser.parse(data);
        JsonArray jsonObject = jsonTree.getAsJsonObject().get("dicomStores").getAsJsonArray();
        List<String> result = StreamSupport.stream(jsonObject.spliterator(), false)
                .map(obj -> obj.getAsJsonObject().get("name").getAsString())
                .map(this::parseName)
                .collect(Collectors.toList());
        return result;
    }

	public String getGHCDatasetUrl(DICOMStoreDescriptor descriptor) {
		return "https://healthcare.googleapis.com/v1alpha/projects/"+descriptor.getProjectId()+
				"/locations/"+descriptor.getLocationId()+
				"/datasets/"+descriptor.getDataSetName();
	}

	public String getGHCDicomstoreUrl(DICOMStoreDescriptor descriptor) {
		return getGHCDatasetUrl(descriptor) + "/dicomStores/"+descriptor.getDicomStoreName();
	}

	private String getDCMFileUrl(DICOMStoreDescriptor study, String dcmFileId) {
		return getGHCDicomstoreUrl(study)+"/dicomWeb/studies/"+dcmFileId;
	}

	public List<String> listDCMFileIds(DICOMStoreDescriptor descriptor) {
		try {
			signIn();
			String url = getGHCDicomstoreUrl(descriptor)+"/dicomWeb/studies";
			String data = googleRequest(url).parseAsString();
	        JsonElement jsonTree = new JsonParser().parse(data);
	        List<String> imageUrls = StreamSupport.stream(jsonTree.getAsJsonArray().spliterator(), false)
	        	.map(el -> el.getAsJsonObject().get("0020000D").getAsJsonObject().get("Value").getAsJsonArray().get(0).getAsString())
	        	.map(id -> getDCMFileUrl(descriptor, id))
	        	.collect(Collectors.toList());
	        return imageUrls;
		} catch(Exception e) {
			logger.error("Error during listDCMFileIds", e);
			throw new IllegalStateException(e);
		}
	}

	public String createDicomstore(DICOMStoreDescriptor descriptor) throws Exception {
		signIn();
		String url = getGHCDatasetUrl(descriptor)+"/dicomStores?dicomStoreId=" + descriptor.getDicomStoreName();
		String data = googlePostRequest(url, new EmptyContent()).parseAsString();
        JsonElement jsonTree = new JsonParser().parse(data);
        JsonElement errorEl = jsonTree.getAsJsonObject().get("error");
        if (errorEl != null) {
        	throw new IllegalStateException("Dicomstore save error: " + errorEl.getAsJsonObject().get("message").getAsString());
        }
        return jsonTree.getAsJsonObject().get("name").getAsString();
	}

	public void checkDicomstore(DICOMStoreDescriptor descriptor) throws Exception {
		signIn();
		List<String> dicomstores = fetchDicomstores(descriptor.getProjectId(), descriptor.getLocationId(), descriptor.getDataSetName());
		boolean isNewDicomStore = !dicomstores.contains(descriptor.getDicomStoreName());
		if (isNewDicomStore) {
			String dicomStorePath = createDicomstore(descriptor);
			logger.info("DICOM store created: " + dicomStorePath);
		}
	}

	private String findFolder(String folderName) throws IOException {
		FileList result = drive.files().list()
			.setQ("mimeType = 'application/vnd.google-apps.folder' and name = 'De-id'")
			.setSpaces("drive")
			.execute();
		if (result.getFiles().isEmpty()) {
			return null;
		}
		return result.getFiles().get(0).getId();
	}
	
	private String getParentFolder(String folderName) throws IOException {
		String folderId = findFolder(folderName);
		if (folderId == null) {
			String mimeType = "application/vnd.google-apps.folder";
			File body = new File();
			body.setName(folderName);
			body.setMimeType(mimeType);
			File file = drive.files().create(body)
			  	.setFields("id")
			  	.execute();
			folderId = file.getId();
			logger.info("Folder ID: " + folderId);
		}
		return folderId;
	}
	
	public String exportStringAsGoogleDoc(String title, String description, String localFilename) {
		if (!isSignedIn()) {
			throw new IllegalStateException("User not authorized in Google cloud");
		}
		String result;
	    try {
	    	String reportFilename = title + "_" + timeFormatter.format(new Date());
			String mimeType = "text/html";
			String parentId = getParentFolder(GDRIVE_REPORT_FOLDER_NAME);
			File body = new File();
		    body.setName(reportFilename);
		    body.setDescription(description);
		    body.setMimeType(mimeType);
		    if (parentId != null && parentId.length() > 0) {
		    	body.setParents(Arrays.asList(parentId));
		    }
		    FileContent mediaContent = new FileContent(mimeType, new java.io.File(localFilename));
		    File file = drive.files().create(body, mediaContent).execute();
		    logger.info("File ID: " + file.getId());
		    result = "\"\\" + GDRIVE_REPORT_FOLDER_NAME + "\\" + reportFilename + "\" with id: " + file.getId();
	    } catch (IOException e) {
	    	result = "An error occurred: " + e.getMessage();
	    	logger.info(result, e);
	    }
	    return result;
	}
}
