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
import com.pixelmed.utils.FileUtilities;

public class ReportService {

	public enum Status { SUCCESS, FAIL };

	public static final String REPORT_FILE = "report.html";
	public static final String REPORT_FILE_FULL = "report_full.html";
	private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

    private static ReportService instance;

    private List<String> downloaded = new ArrayList<>();
    private Map<String, Status> importedFiles = new HashMap<>();
    private Map<String, Status> anonymizedFiles = new HashMap<>();
    private Map<String, Status> pixelAnonymizedFiles = new HashMap<>();
    private Map<String, Status> exportedFiles = new HashMap<>();
    
    public static synchronized ReportService getInstance() {
        if (instance == null) {
            instance = new ReportService();
        }
        return instance;
    }

    private ReportService() {
    	dumpReport("", REPORT_FILE);
    }
    
    public void clear() {
        downloaded.clear();
        importedFiles.clear();
        anonymizedFiles.clear();
        pixelAnonymizedFiles.clear();
        exportedFiles.clear();
    }

    public void addDownloaded(String url) {
    	downloaded.add(url);
    	dumpReport();
    }

    
    public void addImported(String imported, Status status) {
    	importedFiles.put(imported, status);
    	dumpReport();
    }
    
    public void addAnonymized(String anonymized, Status status) {
    	anonymizedFiles.put(anonymized, status);
    	dumpReport();
    }

    public void addPixelAnonymized(String anonymized, Status status) {
    	pixelAnonymizedFiles.put(anonymized, status);
    	dumpReport();
    }

    public void addExported(String exported, Status status) {
    	exportedFiles.put(exported, status);
    	dumpReport();
    }

    public String getReportFile(boolean full) {
		return full ? REPORT_FILE_FULL : REPORT_FILE;
	}
    
    public String loadReport() {
    	try {
    		return FileUtilities.readFile(new File(REPORT_FILE));
    	} catch(Exception e) {
    		logger.error("Error reading report from file", e);
    	}
    	return "NO REPORT";
    }
    
    private void dumpReport() {
    	dumpReport(generateReport(false), REPORT_FILE);
    	dumpReport(generateReport(true), REPORT_FILE_FULL);
    }
    
    private void dumpReport(String data, String filename) {
    	try (FileOutputStream fos = new FileOutputStream(filename)) {
    		fos.write(data.getBytes());
    	} catch(Exception e) {
    		logger.error("Error writing report to file", e);
    	}
    }
    
    private String generateReport(boolean fullReport) {
    	ByteArrayOutputStream os = new ByteArrayOutputStream();
    	PrintStream ps = new PrintStream(os);
    	
    	ps.println("<html><head></head><body><b>REPORT</b><br><br>");

    	//REPORT for download
    	ps.println("<b>Files downloaded:</b><br>");
    	if (fullReport) {
	    	downloaded.forEach(str ->{
	    		ps.println(str + "<br>");
	    	});
    	}
    	ps.print("<b>TOTAL:" + downloaded.size() + "</b><br><br>");

    	//REPORT for import
    	ps.println("<b>Files imported:</b><br>");
    	if (fullReport) {
	    	importedFiles.keySet().forEach(str ->{
	    		ps.println(str + "  STATUS: " + importedFiles.get(str) + "<br>");
	    	});
    	}
    	long successCount = importedFiles.values().stream().filter(st -> st.equals(Status.SUCCESS)).count();
    	ps.println("<b>TOTAL SUCCESS:" + successCount + "</b><br>");
    	ps.println("<b>TOTAL FAIL:" + (importedFiles.size() - successCount) + "</b><br>");
    	ps.println("<b>TOTAL:" + importedFiles.size() + "</b><br><br>");
    	
    	//REPORT for anonymizer
    	ps.println("<b>Files anonymized:</b><br>");
    	if (fullReport) {
	    	anonymizedFiles.keySet().forEach(str -> {
	    		ps.println(str + ":" + anonymizedFiles.get(str) + "<br>");
	    	});
    	}
    	successCount = anonymizedFiles.values().stream().filter(st -> st.equals(Status.SUCCESS)).count();
    	ps.println("<b>TOTAL SUCCESS:" + successCount + "</b><br>");
    	ps.println("<b>TOTAL FAIL:" + (anonymizedFiles.size() - successCount) + "</b><br>");
    	ps.println("<b>TOTAL:" + anonymizedFiles.size() + "</b><br><br>");
    	
    	ps.println();
    	
    	//REPORT for pixel anonymizer
    	ps.println("<b>Files pixel-anonymized:</b><br>");
    	if (fullReport) {
	    	pixelAnonymizedFiles.keySet().forEach(str -> {
	    		ps.println(str + ":" + pixelAnonymizedFiles.get(str) + "<br>");
	    	});
    	}
    	successCount = pixelAnonymizedFiles.values().stream().filter(st -> st.equals(Status.SUCCESS)).count();
    	ps.println("<b>TOTAL SUCCESS:" + successCount + "</b><br>");
    	ps.println("<b>TOTAL FAIL:" + (pixelAnonymizedFiles.size() - successCount) + "</b><br>");
    	ps.println("<b>TOTAL:" + pixelAnonymizedFiles.size() + "</b><br><br>");
    	
    	ps.println();
    	
    	//REPORT for export
    	ps.println("<b>Files exported:</b><br>");
    	if (fullReport) {
	    	exportedFiles.keySet().forEach(str -> {
	    		ps.println(str + ":" + exportedFiles.get(str) + "<br>");
	    	});
    	}
    	successCount = exportedFiles.values().stream().filter(st -> st.equals(Status.SUCCESS)).count();
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
