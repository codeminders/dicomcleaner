package com.codeminders.demo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import com.pixelmed.slf4j.Logger;
import com.pixelmed.slf4j.LoggerFactory;

public class GoogleDICOMImport {
	
    private final static Logger logger = LoggerFactory.getLogger(GoogleDICOMImport.class);

    private GoogleAPIClient googleAPIClient;
    
    private static int TEMP_DIR_ATTEMPTS = 3;
    
    public GoogleDICOMImport(GoogleAPIClient googleAPIClient) {
    	this.googleAPIClient = googleAPIClient;
	}
    
	public static File createTempDir() {
		File baseDir = new File(System.getProperty("java.io.tmpdir"));
		String baseName = System.currentTimeMillis() + "-";

		for (int counter = 0; counter < TEMP_DIR_ATTEMPTS; counter++) {
			File tempDir = new File(baseDir, baseName + counter);
			if (tempDir.mkdir()) {
				return tempDir;
			}
		}
		throw new IllegalStateException("Failed to create directory within " + TEMP_DIR_ATTEMPTS + " attempts (tried "
				+ baseName + "0 to " + baseName + (TEMP_DIR_ATTEMPTS - 1) + ')');
	}
    
	public static String readLine(InputStream inputStream) throws IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		int c;
		for (c = inputStream.read(); c != '\n' && c != -1; c = inputStream.read()) {
			byteArrayOutputStream.write(c);
		}
		if (c == -1 && byteArrayOutputStream.size() == 0) {
			return null;
		}
		String line = byteArrayOutputStream.toString("UTF-8");
		return line;
	}

	private String downloadFile(String fileURL, String saveDir) {
		try {
			if (!googleAPIClient.isSignedIn()) {
				googleAPIClient.signIn();
			}
			
			URL url = new URL(fileURL);
			HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
			httpConn.setRequestProperty("Authorization", "Bearer " + googleAPIClient.getAccessToken());
			httpConn.setRequestProperty("Accept", "multipart/related; type=application/dicom; transfer-syntax=*");
			int responseCode = httpConn.getResponseCode();
	
			// always check HTTP response code first
			if (responseCode == HttpURLConnection.HTTP_OK) {
				String fileName = "";
				String disposition = httpConn.getHeaderField("Content-Disposition");
	
				if (disposition != null) {
					// extracts file name from header field
					int index = disposition.indexOf("filename=");
					if (index > 0) {
						fileName = disposition.substring(index + 10, disposition.length() - 1);
					}
				} else {
					// extracts file name from URL
					fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1, fileURL.length());
				}
	
				logger.info("Starting download file = " + fileName);
				String saveFilePath = saveDir + File.separator + fileName;
	
				try (InputStream inputStream = httpConn.getInputStream();
						FileOutputStream outputStream = new FileOutputStream(saveFilePath)) {
		
					readLine(inputStream);
					readLine(inputStream);
					readLine(inputStream);
		
					int bytesRead = -1;
					byte[] buffer = new byte[1024];
					while ((bytesRead = inputStream.read(buffer)) != -1) {
						outputStream.write(buffer, 0, bytesRead);
					}
		
					outputStream.close();
					inputStream.close();
					
					logger.info("File downloaded");
					ReportService.getInstance().addDownloaded(fileURL);
					return saveFilePath;
				}
			} else {
				logger.info("No file to download. Server replied HTTP code: " + responseCode);
			}
			httpConn.disconnect();
		} catch(Exception e) {
			logger.error("Error during file import", e);
		}
		return null;
	}

	private List<String> downloadFile(List<String> fileURLs, String saveDir) {
		return fileURLs.stream().map(url -> downloadFile(url, saveDir)).collect(Collectors.toList());
	}

	public List<String> downloadFile(DICOMStoreDescriptor descriptor) {
		File tempDir = createTempDir();
		return downloadFile(googleAPIClient.listDCMFileIds(descriptor), tempDir.getAbsolutePath());
	}
}
