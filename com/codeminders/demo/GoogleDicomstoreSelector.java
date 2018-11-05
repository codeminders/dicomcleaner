package com.codeminders.demo;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.tree.DefaultTreeCellEditor.DefaultTextField;

import com.codeminders.demo.model.Location;
import com.codeminders.demo.model.ProjectDescriptor;
import com.google.api.client.util.Lists;
import com.pixelmed.display.DicomCleaner;
import com.pixelmed.display.SafeProgressBarUpdaterThread;
import com.pixelmed.slf4j.Logger;
import com.pixelmed.slf4j.LoggerFactory;

import javafx.scene.layout.Border;

public class GoogleDicomstoreSelector extends JPanel {

	private static final Logger logger = LoggerFactory.getLogger(DicomCleaner.class);

	private final JFrame parent;
	private final DicomCleaner dicomCleaner;
	
    private final GoogleAPIClient googleAPIClient;
    
    private boolean isNewDicomStore;

    private JComboBox projectComboBox;
    private JComboBox locationComboBox;
    private JComboBox datasetComboBox;
    private JComboBox dicomStoreComboBox;
    private JTextField dicomStoreTextField;
    
	public GoogleDicomstoreSelector(JFrame frame, DicomCleaner dicomCleaner, boolean isNewDicomStore) {
		this.dicomCleaner = dicomCleaner;
		this.parent = frame;
		this.isNewDicomStore = isNewDicomStore;
		googleAPIClient = GoogleAPIClientFactory.getInstance().getGoogleClient(dicomCleaner.getGoogleSecretPath());
		initComponents();
	}
	
	private String getProjectId() {
		if (projectComboBox.getSelectedItem() != null) {
			String projectId = ((ProjectDescriptor)projectComboBox.getSelectedItem()).getId();
			if (!projectId.equals("Choose project")) {
				return projectId;
			}
		}
		return "";
	}
	
	private String getLocationId() {
		if (locationComboBox.getSelectedItem() != null) {
			String locationId = ((Location)locationComboBox.getSelectedItem()).getId();
			if (!locationId.equals("Choose location")) {
				return locationId;
			}
		}
		return "";
	}
	
	private String getDataset() {
		if (datasetComboBox.getSelectedItem() != null) {
			String dataset = datasetComboBox.getSelectedItem().toString();
			if (!dataset.equals("Choose dataset")) {
				return dataset;
			}
		}
		return "";
	}
	
	private String getDicomStore() {
		if (isNewDicomStore) {
			return dicomStoreTextField.getText();
		} else {
			if (dicomStoreComboBox.getSelectedItem() != null) {
				String dicomStore = dicomStoreComboBox.getSelectedItem().toString();
				if (!dicomStore.equals("Choose dicomstore")) {
					return dicomStore;
				}
			}
		}
		return "";
	}
	
	private class LoadProject extends SwingWorker<List<ProjectDescriptor>, Void> {
		@Override
		protected List<ProjectDescriptor> doInBackground() throws Exception {
			return googleAPIClient.fetchProjects();
		}
		@Override
		protected void done() {
			try {
				projectComboBox.removeAllItems();
				projectComboBox.addItem(new ProjectDescriptor("", "Choose project"));
				get().forEach(projectComboBox::addItem);
			} catch(Exception e) {
				JOptionPane.showMessageDialog(null, "Error during fetching data from Google:" + e.getMessage());
			}
		}
	}
	
	private class LoadLocation extends SwingWorker<List<Location>, Void> {
		@Override
		protected List<Location> doInBackground() throws Exception {
			String id = getProjectId();
			return id.isEmpty() ? Lists.newArrayList() : googleAPIClient.fetchLocations(getProjectId());
		}
		@Override
		protected void done() {
			try {
				locationComboBox.removeAllItems();
				locationComboBox.addItem(new Location("", get().size()==0 ? "Please wait" : "Choose location"));
				get().forEach(locationComboBox::addItem);
			} catch(Exception e) {
				JOptionPane.showMessageDialog(null, "Error during fetching data from Google:" + e.getMessage());
			}
		}
	}

	private class LoadDataset extends SwingWorker<List<String>, Void> {
		@Override
		protected List<String> doInBackground() throws Exception {
			String projectId = getProjectId();
			String locationId = getLocationId();
			if (!projectId.isEmpty() && !locationId.isEmpty()) {
				return googleAPIClient.fetchDatasets(projectId, locationId);
			}
			return new ArrayList<>();
		}
		@Override
		protected void done() {
			try {
				datasetComboBox.removeAllItems();
				datasetComboBox.addItem(get().size()==0 ? "Please wait" : "Choose dataset");
				get().forEach(datasetComboBox::addItem);
			} catch(Exception e) {
				JOptionPane.showMessageDialog(null, "Error during fetching data from Google:" + e.getMessage());
			}
		}
	}

	private class LoadDicomstore extends SwingWorker<List<String>, Void> {
		@Override
		protected List<String> doInBackground() throws Exception {
			String projectId = getProjectId();
			String locationId = getLocationId();
			String dataset = getDataset();
			if (!projectId.isEmpty() && !locationId.isEmpty() && !dataset.isEmpty()) {
				return googleAPIClient.fetchDicomstores(projectId, locationId, dataset);
			}
			return new ArrayList<>();
		}
		@Override
		protected void done() {
			try {
				dicomStoreComboBox.removeAllItems();
				dicomStoreComboBox.addItem(get().size()==0 ? "Please wait" : "Choose dicomstore");
				get().forEach(dicomStoreComboBox::addItem);
			} catch(Exception e) {
				JOptionPane.showMessageDialog(null, "Error during fetching data from Google:" + e.getMessage());
			}
		}
	}
	
	private class ImportDicomStore extends SwingWorker<File, Void> {
		@Override
		protected File doInBackground() throws Exception {
			String projectId = getProjectId();
			String locationId = getLocationId();
			String dataset = getDataset();
			String dicomStore = getDicomStore();
			parent.dispose();
			if (!projectId.isEmpty() && !locationId.isEmpty() && !dataset.isEmpty() && !dicomStore.isEmpty()) {
				DICOMStoreDescriptor descriptor = new DICOMStoreDescriptor(projectId, locationId, dataset, dicomStore);
				GoogleDICOMImport googleImport = new GoogleDICOMImport(googleAPIClient, dicomCleaner);
				File importTempDir = googleImport.downloadFileIntoTempDir(descriptor);
				return importTempDir;
			} else {
				JOptionPane.showMessageDialog(null, "Import failed. Wrong input.");
			}
			return null;
		}
		@Override
		protected void done() {
			try {
				File importDir = get();
				if (importDir != null) {
					dicomCleaner.importDirectory(importDir.getAbsolutePath());
				}
			} catch(Exception e) {
				JOptionPane.showMessageDialog(null, "Error during fetching data from Google:" + e.getMessage());
			}
		}
	}

	private class ExportDicomStore extends SwingWorker<String, Void> {
		@Override
		protected String doInBackground() throws Exception {
			try {
				String projectId = getProjectId();
				String locationId = getLocationId();
				String dataset = getDataset();
				String dicomStore = getDicomStore();
				parent.dispose();
				if (!projectId.isEmpty() && !locationId.isEmpty() && !dataset.isEmpty() && !dicomStore.isEmpty()) {
					DICOMStoreDescriptor descriptor = new DICOMStoreDescriptor(projectId, locationId, dataset, dicomStore);
					SafeProgressBarUpdaterThread.startProgressBar(dicomCleaner.getProgressBarUpdater());
					List<String> sourceFiles = dicomCleaner.getCurrentDestinationFilePathSelections();
					dicomCleaner.getProgressBarUpdater().updateProgressBar(0, sourceFiles.size());
					GoogleDICOMExport googleExport = new GoogleDICOMExport(descriptor, googleAPIClient);
					int i=0;
					for(String file : sourceFiles) {
						googleExport.export(new File(file));
						dicomCleaner.getProgressBarUpdater().updateProgressBar(i++);
					};
				} else {
					JOptionPane.showMessageDialog(null, "Import failed. Wrong input.");
				}
			} catch(Exception e) {
				logger.error("Error in googel export", e);
				SafeProgressBarUpdaterThread.endProgressBar(dicomCleaner.getProgressBarUpdater());
			}
			return null;
		}
		@Override
		protected void done() {
			SafeProgressBarUpdaterThread.endProgressBar(dicomCleaner.getProgressBarUpdater());
		}
	}
	
	private void createLabel(String title, int gridX, int gridY) {
		JLabel label = new JLabel(title);
		GridBagConstraints gbConstrains = new GridBagConstraints();
		gbConstrains.insets = new Insets(0, 0, 5, 5);
		gbConstrains.gridx = gridX;
		gbConstrains.gridy = gridY;
		add(label, gbConstrains);
	}

	private JComboBox createComboBox(int gridX, int gridY, ActionListener action) {
		JComboBox comboBox = new JComboBox();
		if (action != null) {
			comboBox.addActionListener(action);
		}
		GridBagConstraints comboBoxConstrain = new GridBagConstraints();
		comboBoxConstrain.insets = new Insets(0, 0, 5, 5);
		comboBoxConstrain.fill = GridBagConstraints.HORIZONTAL;
		comboBoxConstrain.gridx = gridX;
		comboBoxConstrain.gridy = gridY;
		add(comboBox, comboBoxConstrain);
		return comboBox;
	}
	
	private JTextField createTextField(int gridX, int gridY) {
		JTextField textField = new JTextField("", 20);
		GridBagConstraints comboBoxConstrain = new GridBagConstraints();
		comboBoxConstrain.insets = new Insets(0, 0, 5, 5);
		comboBoxConstrain.fill = GridBagConstraints.HORIZONTAL;
		comboBoxConstrain.gridx = gridX;
		comboBoxConstrain.gridy = gridY;
		add(textField, comboBoxConstrain);
		return textField;
	}


	private void initComponents() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{36, 276, 0, 0};
		gridBagLayout.rowHeights = new int[]{28, 36, 24, 34, 23, 36, 0, 0, 16, -32, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);

		createLabel("Google Project", 1, 0);
		projectComboBox = createComboBox(1, 1, event -> {
			new LoadLocation().execute();
		});
		
		createLabel("Location", 1, 2);
		locationComboBox = createComboBox(1, 3, event -> {
			new LoadDataset().execute();
		});
		
		createLabel("Dataset", 1, 4);
		datasetComboBox = createComboBox(1, 5, event -> {
			if (!isNewDicomStore) {
				new LoadDicomstore().execute();
			}
		});
		
		createLabel("DataStore", 1, 6);
		if (isNewDicomStore) {
			dicomStoreTextField = createTextField(1, 7);
		} else {
			dicomStoreComboBox = createComboBox(1, 7, null);
		}
		
		JButton btnNewButton = new JButton(isNewDicomStore ? "Export" : "Import");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (isNewDicomStore) {
					new ExportDicomStore().execute();
				} else {
					new ImportDicomStore().execute();
				}
			}
		});
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.fill = GridBagConstraints.VERTICAL;
		gbc_btnNewButton.insets = new Insets(0, 0, 0, 5);
		gbc_btnNewButton.gridx = 1;
		gbc_btnNewButton.gridy = 9;
		add(btnNewButton, gbc_btnNewButton);
		
		new LoadProject().execute();
	}
}
