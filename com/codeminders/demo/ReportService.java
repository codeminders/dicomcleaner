package com.codeminders.demo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hsqldb.lib.FileUtil;

import com.pixelmed.slf4j.Logger;
import com.pixelmed.slf4j.LoggerFactory;

public class ReportService {

	public enum Status { SUCCESS, FAIL };

	public static final String REPORT_FILE = "report.html";
	private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

    private static ReportService instance;

    private List<String> downloaded = new ArrayList<>();
    private Map<String, String> importedFiles = new HashMap<>();
    private Map<String, String> anonymizedFiles = new HashMap<>();
    private Map<String, String> pixelAnonymizedFiles = new HashMap<>();
    private Map<String, String> exportedFiles = new HashMap<>();
    
    public static synchronized ReportService getInstance() {
        if (instance == null) {
            instance = new ReportService();
        }
        return instance;
    }

    private ReportService() {
    	dumpReport("");
    }

    public void addDownloaded(String url) {
    	downloaded.add(url);
    	dumpReport();
    }

    
    public void addImported(String imported, String qFile) {
    	importedFiles.put(imported, qFile);
    	dumpReport();
    }
    
    public void addAnonymized(String anonymized, String status) {
    	anonymizedFiles.put(anonymized, status);
    	dumpReport();
    }

    public void addPixelAnonymized(String anonymized, String status) {
    	pixelAnonymizedFiles.put(anonymized, status);
    	dumpReport();
    }

    public void addExported(String exported, Status status, String info) {
    	exportedFiles.put(exported, info);
    	dumpReport();
    }

    public String loadReport() {
    	//return FileUtil.getFileUtil().getText(new File(REPORT_FILE));
    	return null;
    }
    
    private void dumpReport() {
    	dumpReport(generateReport());
    }
    
    private void dumpReport(String data) {
    	try (FileOutputStream fos = new FileOutputStream(REPORT_FILE)) {
    		fos.write(data.getBytes());
    	} catch(Exception e) {
    		logger.error("Error writing report to file", e);
    	}
    }
    
    private String generateReport() {
    	ByteArrayOutputStream os = new ByteArrayOutputStream();
    	PrintStream ps = new PrintStream(os);
    	
    	ps.println("<html><head></head><body><b>REPORT</b><br><br>");

    	//REPORT for download
    	ps.println("<b>Files downloaded:</b><br>");
    	downloaded.forEach(str ->{
    		ps.println(str + "<br>");
    	});
    	ps.print("<b>TOTAL:" + downloaded.size() + "</b><br><br>");

    	//REPORT for import
    	ps.println("<b>Files imported:</b><br>");
    	importedFiles.keySet().forEach(str ->{
    		ps.println(str + "  IN  " + importedFiles.get(str) + "<br>");
    	});
    	ps.print("<b>TOTAL:" + importedFiles.size() + "</b><br><br>");
    	
    	//REPORT for anonymizer
    	ps.println("<b>Files anonymized:</b><br>");
    	anonymizedFiles.keySet().forEach(str -> {
//    		ps.println(str + ":" + anonymizedFiles.get(str).getStatus() + "<br>");
    	});
    	long successCount = 0;//anonymizedFiles.values().stream().filter(st -> st.isOK()).count();
    	ps.println("<b>TOTAL SUCCESS:" + successCount + "</b><br>");
    	ps.println("<b>TOTAL FAIL:" + (anonymizedFiles.size() - successCount) + "</b><br>");
    	ps.println("<b>TOTAL:" + anonymizedFiles.size() + "</b><br><br>");
    	
    	ps.println();
    	
    	//REPORT for pixel anonymizer
    	ps.println("<b>Files pixel-anonymized:</b><br>");
    	pixelAnonymizedFiles.keySet().forEach(str -> {
//    		ps.println(str + ":" + pixelAnonymizedFiles.get(str).getStatus() + "<br>");
    	});
//    	successCount = pixelAnonymizedFiles.values().stream().filter(st -> st.isOK()).count();
    	ps.println("<b>TOTAL SUCCESS:" + successCount + "</b><br>");
    	ps.println("<b>TOTAL FAIL:" + (pixelAnonymizedFiles.size() - successCount) + "</b><br>");
    	ps.println("<b>TOTAL:" + pixelAnonymizedFiles.size() + "</b><br><br>");
    	
    	ps.println();
    	
    	//REPORT for export
    	ps.println("<b>Files exported:</b><br>");
    	exportedFiles.keySet().forEach(str -> {
//    		ps.println(str + ":" + exportedFiles.get(str).getStatus() + "  IN  " + exportedFiles.get(str).getInfo() + "<br>");
    	});
//    	successCount = exportedFiles.values().stream().filter(st -> st.getStatus().equals(Status.OK)).count();
    	ps.println("<b>TOTAL SUCCESS:" + successCount + "</b><br>");
    	ps.println("<b>TOTAL FAIL:" + (exportedFiles.size() - successCount) + "</b><br>");
    	ps.println("<b>TOTAL:" + exportedFiles.size() + "</b><br><br>");
    	
    	ps.println("</body></html>");
    	
    	String output;
    	try {
    		output = os.toString("UTF8");
    	} catch(Exception e) {
    		logger.error(e.getMessage(), e);
    		output = "ERROR in report generating: " + e.getMessage();
    	}
    	return output;
    }
}
