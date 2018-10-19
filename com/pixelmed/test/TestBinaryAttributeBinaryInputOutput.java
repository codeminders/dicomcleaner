/* Copyright (c) 2001-2018, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.dicom.*;
import com.pixelmed.utils.HexDump;

import junit.framework.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class TestBinaryAttributeBinaryInputOutput extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestBinaryAttributeBinaryInputOutput(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestBinaryAttributeBinaryInputOutput.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestBinaryAttributeBinaryInputOutput");
		
		suite.addTest(new TestBinaryAttributeBinaryInputOutput("TestBinaryAttributeBinaryInputOutput_ReadAndWriteOtherLongValuesLittleEndian"));
		suite.addTest(new TestBinaryAttributeBinaryInputOutput("TestBinaryAttributeBinaryInputOutput_ReadAndWriteOtherLongValuesBigEndian"));
		
		return suite;
	}
	
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	public void TestBinaryAttributeBinaryInputOutput_ReadAndWriteOtherLongValuesLittleEndian() throws Exception {
		{
			int values [] = {
				0x01020304,
				0x11223344,
				0x04030201,
				0x44332211,
				0x00000000,
				0x7fffffff,
				0x80000000,
				0x8fffffff
			};
			
			AttributeTag tag = new AttributeTag(0x0011,0x1010);
			
			OtherLongAttribute a = new OtherLongAttribute(tag);
			a.setValues(values);

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DicomOutputStream dos = new DicomOutputStream(bos,null/*meta ts*/,TransferSyntax.ExplicitVRLittleEndian);
			a.write(dos);
			dos.close();
			
			byte[] bytes = bos.toByteArray();
			//{
			//	StringBuffer dumpOfBytes = new StringBuffer();
			//	for (byte b : bytes) {
			//		dumpOfBytes.append(Integer.toHexString(b&0xff));
			//	}
			//	System.err.println("dump of bytes: "+dumpOfBytes.toString());
			//}
			
			ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
			AttributeList list = new AttributeList();
			list.read(new DicomInputStream(bis,TransferSyntax.ExplicitVRLittleEndian,false/*meta*/));
			Attribute aRead = list.get(tag);
			assertEquals("VR",ValueRepresentation.OL,aRead.getVR());
			int[] readValues = aRead.getIntegerValues();
			assertEquals("Same number of values",values.length,readValues.length);
			for (int i=0; i<values.length; ++i) {
				assertEquals("Checking hex value["+i+"]",Integer.toHexString(values[i]),Integer.toHexString(readValues[i]));
			}
		}
	}
	
	public void TestBinaryAttributeBinaryInputOutput_ReadAndWriteOtherLongValuesBigEndian() throws Exception {
		{
			int values [] = {
				0x01020304,
				0x11223344,
				0x04030201,
				0x44332211,
				0x00000000,
				0x7fffffff,
				0x80000000,
				0x8fffffff
			};
			
			AttributeTag tag = new AttributeTag(0x0011,0x1010);
			
			OtherLongAttribute a = new OtherLongAttribute(tag);
			a.setValues(values);

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DicomOutputStream dos = new DicomOutputStream(bos,null/*meta ts*/,TransferSyntax.ExplicitVRBigEndian);
			a.write(dos);
			dos.close();
			
			byte[] bytes = bos.toByteArray();
			//{
			//	StringBuffer dumpOfBytes = new StringBuffer();
			//	for (byte b : bytes) {
			//		dumpOfBytes.append(Integer.toHexString(b&0xff));
			//	}
			//	System.err.println("dump of bytes: "+dumpOfBytes.toString());
			//}
			
			ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
			AttributeList list = new AttributeList();
			list.read(new DicomInputStream(bis,TransferSyntax.ExplicitVRBigEndian,false/*meta*/));
			Attribute aRead = list.get(tag);
			assertEquals("VR",ValueRepresentation.OL,aRead.getVR());
			int[] readValues = aRead.getIntegerValues();
			assertEquals("Same number of values",values.length,readValues.length);
			for (int i=0; i<values.length; ++i) {
				assertEquals("Checking hex value["+i+"]",Integer.toHexString(values[i]),Integer.toHexString(readValues[i]));
			}
		}
	}
	
}
