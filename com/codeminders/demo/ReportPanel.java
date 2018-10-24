package com.codeminders.demo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Timer;
import javax.swing.text.DefaultCaret;

public class ReportPanel extends JPanel {

	private JEditorPane out;
	private JScrollPane jsp;

	public final static int INTERVAL = 1000;
	
	public ReportPanel(ResourceBundle resourceBundle) {
		super();
		out = new JEditorPane();
		out.setContentType("text/html");
		JPanel bp = new JPanel();
		bp.add(out, BorderLayout.CENTER);

		DefaultCaret caret = (DefaultCaret)out.getCaret();
		caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
		
		jsp = new JScrollPane();
		jsp.getVerticalScrollBar().setUnitIncrement(10);
		jsp.setViewportView(bp);
		jsp.getViewport().setBackground(Color.white);
		add(jsp, BorderLayout.CENTER);

		Timer timer = new Timer(INTERVAL, new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				ReportPanel.this.reload();
				ReportPanel.this.revalidate();
			}
		});
		timer.start();
		
		if (resourceBundle != null) {
			JButton reportButton = new JButton(resourceBundle.getString("exportReportButtonLabelText"));
			reportButton.setToolTipText(resourceBundle.getString("exportReportButtonToolTipText"));
			add(reportButton, BorderLayout.SOUTH);
			reportButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						GoogleAPIClient client = GoogleAPIClientFactory.getInstance().getGoogleClient();
						client.signIn();
						String name = client.exportStringAsGoogleDoc(
								"DICOM Cleaner Report", 
								"DICOM Cleaner Report", 
								// We can export full version of report using 'true' for parameter in getReportFile method
								// and 'false' for short version
								ReportService.getInstance().getReportFile(true));
						JOptionPane.showMessageDialog(null, "Report exported to Google:" + name);
					} catch(Exception ex) {
						JOptionPane.showMessageDialog(null, "Error export to Google: " + ex.getMessage());
					}
				}
			});
		}
	}

	public void reload() {
		String report = ReportService.getInstance().loadReport();
		String prevRep = out.getText();
		if (!report.equals(prevRep)) {
			out.setText(report);
			
		}
	}

}
