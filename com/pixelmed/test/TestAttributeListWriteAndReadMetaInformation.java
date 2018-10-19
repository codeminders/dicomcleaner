/* Copyright (c) 2001-2018, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.dicom.*;

import junit.framework.*;

import java.io.File;

public class TestAttributeListWriteAndReadMetaInformation extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestAttributeListWriteAndReadMetaInformation(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestAttributeListWriteAndReadMetaInformation.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestAttributeListWriteAndReadMetaInformation");
		
		suite.addTest(new TestAttributeListWriteAndReadMetaInformation("TestAttributeListWriteAndReadMetaInformation_Valid_EVRLE_Meta_IVRLE_Dataset"));
		suite.addTest(new TestAttributeListWriteAndReadMetaInformation("TestAttributeListWriteAndReadMetaInformation_Valid_EVRLE_Meta_EVRLE_Dataset"));

		suite.addTest(new TestAttributeListWriteAndReadMetaInformation("TestAttributeListWriteAndReadMetaInformation_NoTransferSyntax_EVRLE_Meta_IVRLE_Dataset"));
		suite.addTest(new TestAttributeListWriteAndReadMetaInformation("TestAttributeListWriteAndReadMetaInformation_NoTransferSyntax_EVRLE_Meta_EVRLE_Dataset"));

		return suite;
	}
		
	protected void setUp() {
	}
	
	protected void tearDown() {
	}

	private String studyID = "612386812";
	private String seriesNumber = "12";
	private String instanceNumber = "38";
	
	private AttributeList makeAttributeList() {
		AttributeList list = new AttributeList();
		try {
			UIDGenerator u = new UIDGenerator("9999");
			String sopInstanceUID = u.getNewSOPInstanceUID(studyID,seriesNumber,instanceNumber);
			String seriesInstanceUID = u.getNewSeriesInstanceUID(studyID,seriesNumber);
			String studyInstanceUID = u.getNewStudyInstanceUID(studyID);

			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPClassUID); a.addValue(SOPClass.CTImageStorage); list.put(a); }
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID); a.addValue(sopInstanceUID); list.put(a); }
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SeriesInstanceUID); a.addValue(seriesInstanceUID); list.put(a); }
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.StudyInstanceUID); a.addValue(studyInstanceUID); list.put(a); }

		}
		catch (DicomException e) {
		}
		return list;
	}
	
	public void TestAttributeListWriteAndReadMetaInformation_Valid_EVRLE_Meta_IVRLE_Dataset() throws Exception {
//System.err.println("TestAttributeListWriteAndReadMetaInformation_Valid_EVRLE_Meta_IVRLE_Dataset():");
		
		int wroteValue = 0x7010;
		AttributeTag useTag = TagFromName.ReferencedFrameNumbers;
		
		File testFile = File.createTempFile("TestAttributeListWriteAndReadMetaInformation_Valid_EVRLE_Meta_IVRLE_Dataset",".dcm");
		{
			AttributeList list = makeAttributeList();
			
			{ Attribute a = new UnsignedShortAttribute(useTag); a.addValue(wroteValue); list.put(a); }	// something byte order and explicit/implicit VR form dependent
			
			FileMetaInformation.addFileMetaInformation(list,TransferSyntax.ImplicitVRLittleEndian,"OURAETITLE");
			list.write(testFile,TransferSyntax.ImplicitVRLittleEndian,true/*useMeta*/,true/*useBufferedStream*/);
		}
		{
			AttributeList list = new AttributeList();
			
			list.read(testFile);
			
			int readValue = Attribute.getSingleIntegerValueOrDefault(list,useTag,0);
			
			assertEquals("Checking value read equals wrote",wroteValue,readValue);
		}
	}
	
	public void TestAttributeListWriteAndReadMetaInformation_Valid_EVRLE_Meta_EVRLE_Dataset() throws Exception {
//System.err.println("TestAttributeListWriteAndReadMetaInformation_Valid_EVRLE_Meta_EVRLE_Dataset():");
		
		int wroteValue = 0x7010;
		AttributeTag useTag = TagFromName.ReferencedFrameNumbers;
		
		File testFile = File.createTempFile("TestAttributeListWriteAndReadMetaInformation_Valid_EVRLE_Meta_IVRLE_Dataset",".dcm");
		{
			AttributeList list = makeAttributeList();
			
			{ Attribute a = new UnsignedShortAttribute(useTag); a.addValue(wroteValue); list.put(a); }	// something byte order and explicit/implicit VR form dependent
			
			FileMetaInformation.addFileMetaInformation(list,TransferSyntax.ExplicitVRLittleEndian,"OURAETITLE");
			list.write(testFile,TransferSyntax.ExplicitVRLittleEndian,true/*useMeta*/,true/*useBufferedStream*/);
		}
		{
			AttributeList list = new AttributeList();
			
			list.read(testFile);
			
			int readValue = Attribute.getSingleIntegerValueOrDefault(list,useTag,0);
			
			assertEquals("Checking value read equals wrote",wroteValue,readValue);
		}
	}

	
	public void TestAttributeListWriteAndReadMetaInformation_NoTransferSyntax_EVRLE_Meta_IVRLE_Dataset() throws Exception {
//System.err.println("TestAttributeListWriteAndReadMetaInformation_NoTransferSyntax_EVRLE_Meta_IVRLE_Dataset():");
		
		int wroteValue = 0x7010;
		AttributeTag useTag = TagFromName.ReferencedFrameNumbers;
		
		File testFile = File.createTempFile("TestAttributeListWriteAndReadMetaInformation_Valid_EVRLE_Meta_IVRLE_Dataset",".dcm");
		{
			AttributeList list = makeAttributeList();
			
			{ Attribute a = new UnsignedShortAttribute(useTag); a.addValue(wroteValue); list.put(a); }	// something byte order and explicit/implicit VR form dependent
			
			FileMetaInformation.addFileMetaInformation(list,TransferSyntax.ImplicitVRLittleEndian,"OURAETITLE");
			list.remove(TagFromName.TransferSyntaxUID);
			list.write(testFile,TransferSyntax.ImplicitVRLittleEndian,true/*useMeta*/,true/*useBufferedStream*/);
		}
		{
			AttributeList list = new AttributeList();
			
			String transferSyntaxUID = Attribute.getDelimitedStringValuesOrEmptyString(list,TagFromName.TransferSyntaxUID);
			assertEquals("Checking no TransferSyntaxUID","",transferSyntaxUID);
			
			list.read(testFile);
			
			int readValue = Attribute.getSingleIntegerValueOrDefault(list,useTag,0);
			
			assertEquals("Checking value read equals wrote",wroteValue,readValue);
		}
	}
	
	public void TestAttributeListWriteAndReadMetaInformation_NoTransferSyntax_EVRLE_Meta_EVRLE_Dataset() throws Exception {
//System.err.println("TestAttributeListWriteAndReadMetaInformation_NoTransferSyntax_EVRLE_Meta_EVRLE_Dataset():");
		
		int wroteValue = 0x7010;
		AttributeTag useTag = TagFromName.ReferencedFrameNumbers;
		
		File testFile = File.createTempFile("TestAttributeListWriteAndReadMetaInformation_Valid_EVRLE_Meta_IVRLE_Dataset",".dcm");
		{
			AttributeList list = makeAttributeList();
			
			{ Attribute a = new UnsignedShortAttribute(useTag); a.addValue(wroteValue); list.put(a); }	// something byte order and explicit/implicit VR form dependent
			
			FileMetaInformation.addFileMetaInformation(list,TransferSyntax.ExplicitVRLittleEndian,"OURAETITLE");
			list.remove(TagFromName.TransferSyntaxUID);
			list.write(testFile,TransferSyntax.ExplicitVRLittleEndian,true/*useMeta*/,true/*useBufferedStream*/);
		}
		{
			AttributeList list = new AttributeList();
			
			list.read(testFile);

			String transferSyntaxUID = Attribute.getDelimitedStringValuesOrEmptyString(list,TagFromName.TransferSyntaxUID);
			assertEquals("Checking no TransferSyntaxUID","",transferSyntaxUID);

			int readValue = Attribute.getSingleIntegerValueOrDefault(list,useTag,0);
			
			assertEquals("Checking value read equals wrote",wroteValue,readValue);
		}
	}

}
