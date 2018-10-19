package com.codeminders.demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import com.codeminders.demo.ReportService.Status;
import com.pixelmed.slf4j.Logger;
import com.pixelmed.slf4j.LoggerFactory;

public class GoogleDICOMExport {
		
	private static final Logger logger = LoggerFactory.getLogger(GoogleDICOMExport.class);
	private static final int oneSecond = 1000;
	
	private final int connectionTimeout = 20 * oneSecond;
	private final int readTimeout = 120 * oneSecond;

	private boolean includeContentDispositionHeader = false;

	private DICOMStoreDescriptor dicomStoreDecriptor;
	private GoogleAPIClient apiClient;
	
	public GoogleDICOMExport(DICOMStoreDescriptor dicomStoreDecriptor, GoogleAPIClient apiClient) {
		this.dicomStoreDecriptor = dicomStoreDecriptor;
		this.apiClient = apiClient;
	}
	
	public Status export(File fileToExport) {
		// Do not export zero-length files
		long fileLength = fileToExport.length();
		if (fileLength == 0) {
			return Status.FAIL;
		}

		try {
			apiClient.signIn();

			// Establish the connection
			apiClient.checkDicomstore(dicomStoreDecriptor);
			URL url = new URL(apiClient.getGHCDicomstoreUrl(dicomStoreDecriptor) + "/dicomWeb/studies");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(connectionTimeout);
			conn.setConnectTimeout(readTimeout);
			conn.setRequestProperty("Authorization", "Bearer " + apiClient.getAccessToken());

			// Send the file to the server
			DICOMGoogleClientHttpRequest req = new DICOMGoogleClientHttpRequest(conn,
					"multipart/related; type=application/dicom;");
			if (!includeContentDispositionHeader) {
				req.addFilePart(fileToExport, "application/dicom");
			} else {
				String ctHeader = "Content-Type: application/dicom";
				String cdHeader = "Content-Disposition: form-data; name=\"stowrs\"; filename=\""
						+ fileToExport.getName() + "\";";
				String[] headers = { cdHeader, ctHeader };
				req.addFilePart(fileToExport, headers);
			}
			InputStream is = req.post();
			String response = new BufferedReader(new InputStreamReader(is)).lines().collect(Collectors.joining("\n"));
			conn.disconnect();

			logger.info("POST file("+fileToExport.getAbsolutePath()+") to dicomstore("+url+"). Response:" + response);
			
			// Get the response code and log Unauthorized responses
			int responseCode = conn.getResponseCode();
			
			if (responseCode == 200) {
				conn.disconnect();
				return reportStatus(fileToExport, Status.SUCCESS);
			} else {
				conn.disconnect();
				return reportStatus(fileToExport, Status.FAIL);
			}

		} catch (Exception e) {
			logger.error("Export failed: " + e.getMessage());
			return reportStatus(fileToExport, Status.FAIL);
		}
	}

	private Status reportStatus(File file, Status status) {
		ReportService.getInstance().addExported(file.getAbsolutePath(), status);
		return status;
	}

}
