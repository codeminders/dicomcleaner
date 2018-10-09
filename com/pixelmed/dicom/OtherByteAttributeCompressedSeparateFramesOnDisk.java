/* Copyright (c) 2001-2018, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.dicom;

import java.io.*;

import com.pixelmed.utils.CopyStream;

import com.pixelmed.slf4j.Logger;
import com.pixelmed.slf4j.LoggerFactory;

/**
 * <p>A concrete class specializing {@link com.pixelmed.dicom.Attribute Attribute} for
 * Other Byte (OB) attributes whose compressed frames are not memory resident.</p>
 *
 * <p>Though an instance of this class may be created
 * using its constructors, there is also a factory class, {@link com.pixelmed.dicom.AttributeFactory AttributeFactory}.</p>
 *
 * @see com.pixelmed.dicom.Attribute
 * @see com.pixelmed.dicom.AttributeFactory
 * @see com.pixelmed.dicom.AttributeList
 * @see com.pixelmed.dicom.OtherWordAttributeOnDisk
 *
 * @author	dclunie
 */
public class OtherByteAttributeCompressedSeparateFramesOnDisk extends OtherAttributeOnDisk implements ByteFrameSource {
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/dicom/OtherByteAttributeCompressedSeparateFramesOnDisk.java,v 1.7 2018/04/01 01:22:10 dclunie Exp $";

	private static final Logger slf4jlogger = LoggerFactory.getLogger(OtherByteAttributeCompressedSeparateFramesOnDisk.class);
	
	protected long[][] frameItemByteOffsets;
	protected long[][] frameItemLengths;

	/**
	 * <p>Construct an (empty) attribute.</p>
	 *
	 * @param	t	the tag of the attribute
	 */
	public OtherByteAttributeCompressedSeparateFramesOnDisk(AttributeTag t) {
		super(t);
	}

	/**
	 * <p>Create an attribute from a file that contains tghe compressed frames.</p>
	 *
	 * @param	t						the tag of the attribute
	 * @param	file					the file
	 * @param	frameItemByteOffsets	the byte offsets in the input stream of the start of the data for each item of each frame
	 * @param	frameItemLengths		the lengths in bytes of each item of each frame, with any trailing padding after EOI marker removed (i.e., may be odd)
	 * @param	deleteFilesWhenNoLongerNeeded		delete file when attribute goes out of scope
	 * @throws	IOException
	 * @throws	DicomException
	 */
	public OtherByteAttributeCompressedSeparateFramesOnDisk(AttributeTag t,File file,long[][] frameItemByteOffsets,long[][] frameItemLengths,boolean deleteFilesWhenNoLongerNeeded) throws IOException, DicomException {
		super(t,file,deleteFilesWhenNoLongerNeeded);
		valueLength = 0xffffffffl;
		this.frameItemByteOffsets = frameItemByteOffsets;
		this.frameItemLengths = frameItemLengths;
	}

	/**
	 * <p>Create an attribute from an input stream from which the encapsulated items have already been read.</p>
	 *
	 * @param	t						the tag of the attribute
	 * @param	i						the input stream
	 * @param	frameItemByteOffsets	the byte offsets in the input stream of the start of the data for each item of each frame
	 * @param	frameItemLengths		the lengths in bytes of each item of each frame
	 * @throws	IOException
	 * @throws	DicomException
	 */
	public OtherByteAttributeCompressedSeparateFramesOnDisk(AttributeTag t,DicomInputStream i,long[][] frameItemByteOffsets,long[][] frameItemLengths) throws IOException, DicomException {
		super(t,0/*vl: don't skip anything ... already read*/,i,frameItemByteOffsets[0][0]);
		valueLength = 0xffffffffl;
		this.frameItemByteOffsets = frameItemByteOffsets;
		this.frameItemLengths = frameItemLengths;
	}

	/***/
	public long getPaddedVL() {
		return 0xffffffffl;
	}
	
	// writeItemTag(), writeSequenceDelimitationItemTag() copied from OtherByteAttributeMultipleCompressedFrames.java - should refactor :(
	
	protected static final AttributeTag itemTag = TagFromName.Item;
	
	protected void writeItemTag(DicomOutputStream o,long length) throws IOException {
		o.writeUnsigned16(itemTag.getGroup());
		o.writeUnsigned16(itemTag.getElement());
		o.writeUnsigned32(length);
	}
	
	protected static final AttributeTag sequenceDelimitationItemTag = TagFromName.SequenceDelimitationItem;
	
	protected void writeSequenceDelimitationItemTag(DicomOutputStream o) throws IOException {
		o.writeUnsigned16(sequenceDelimitationItemTag.getGroup());
		o.writeUnsigned16(sequenceDelimitationItemTag.getElement());
		o.writeUnsigned32(0);
	}
	
	/**
	 * @param	o
	 * @throws	IOException
	 * @throws	DicomException
	 */
	public void write(DicomOutputStream o) throws DicomException, IOException {
		writeBase(o);
		writeItemTag(o,0);	// empty basic offset table
		byte[] buffer = null;
		int bufferSize = 0;
		BinaryInputStream i = new BinaryInputStream(new FileInputStream(file),false/*bigEndian - byte order is irrelevant*/);
		long lastPosition = 0l;
		int nFrames = frameItemByteOffsets.length;
		for (int frameNumber=0; frameNumber < nFrames; ++frameNumber) {
			long[] itemByteOffsetsThisFrame = frameItemByteOffsets[frameNumber];
			long[] itemLengthsThisFrame = frameItemLengths[frameNumber];
			int nItemsThisFrame = itemByteOffsetsThisFrame.length;
			for (int item=0; item<nItemsThisFrame; ++item) {
				slf4jlogger.debug("write(): reading item {}",item);
				slf4jlogger.debug("write(): lastPosition before skipping {}",lastPosition);
				long skipping = itemByteOffsetsThisFrame[item]-lastPosition;
				slf4jlogger.debug("write(): skipping {} before reading",skipping);
				i.skipInsistently(skipping);
				lastPosition+=skipping;
				slf4jlogger.debug("write(): lastPosition after skipping and before reading {}",lastPosition);
				int reading = (int)(itemLengthsThisFrame[item]);
				if (bufferSize < reading) {
					bufferSize=reading;
					buffer = new byte[bufferSize];
				}
				i.readInsistently(buffer,0,reading);
				lastPosition+=reading;
				slf4jlogger.debug("write(): lastPosition after reading {}",lastPosition);

				writeItemTag(o,reading);	// ?? should we check length is even and pad if not? cf. OtherByteAttributeMultipleCompressedFrames.java
				o.write(buffer,0,reading);
			}
		}
		writeSequenceDelimitationItemTag(o);
	}

	/**
	 * <p>Get the value of this attribute as a byte array for one selected frame.</p>
	 *
	 * @param	frameNumber		from 0
	 * @return					the values as an array of bytes
	 * @throws	DicomException	thrown if values cannot be read
	 */
	public byte[] getByteValuesForSelectedFrame(int frameNumber) throws DicomException {
		slf4jlogger.debug("getByteValuesForSelectedFrame(): lazy read of selected frame {} into heap allocated memory",frameNumber);
		byte[] buffer = null;
		int framesize = 0;
		long[] itemLengthsThisFrame = frameItemLengths[frameNumber];
		int nItemsThisFrame = itemLengthsThisFrame.length;
		slf4jlogger.debug("getByteValuesForSelectedFrame(): nItemsThisFrame = {}",nItemsThisFrame);
		for (int item=0; item<nItemsThisFrame; ++item) {
			framesize += itemLengthsThisFrame[item];
		}
		if (slf4jlogger.isDebugEnabled()) slf4jlogger.debug("getByteValuesForSelectedFrame(): framesize = 0x{} ({} dec) bytes",Integer.toHexString(framesize),framesize);
		long[] itemByteOffsetsThisFrame = frameItemByteOffsets[frameNumber];

		buffer = new byte[framesize];
		int offsetInBuffer = 0;
		long lastPosition = 0l;
		try {
			BinaryInputStream i = new BinaryInputStream(new FileInputStream(file),false/*bigEndian - byte order is irrelevant*/);
			for (int item=0; item<nItemsThisFrame; ++item) {
				slf4jlogger.debug("getByteValuesForSelectedFrame(): reading item {}",item);
				slf4jlogger.debug("getByteValuesForSelectedFrame(): lastPosition before skipping {}",lastPosition);
				long skipping = itemByteOffsetsThisFrame[item]-lastPosition;
				if (slf4jlogger.isDebugEnabled()) slf4jlogger.debug("getByteValuesForSelectedFrame(): skipping itemByteOffsetsThisFrame[] 0x{} ({} dec) bytes before reading",Long.toHexString(skipping),skipping);
				i.skipInsistently(skipping);
				lastPosition+=skipping;
				slf4jlogger.debug("getByteValuesForSelectedFrame(): lastPosition after skipping and before reading {}",lastPosition);
				int reading = (int)(itemLengthsThisFrame[item]);
				if (slf4jlogger.isDebugEnabled()) slf4jlogger.debug("getByteValuesForSelectedFrame(): reading itemLengthsThisFrame[] 0x{} ({} dec) bytes",Integer.toHexString(reading),reading);
				i.readInsistently(buffer,offsetInBuffer,reading);
				lastPosition+=reading;
				slf4jlogger.debug("getByteValuesForSelectedFrame(): lastPosition after reading {}",lastPosition);
				offsetInBuffer+=reading;
			}
			i.close();
		}
		catch (IOException e) {
			throw new DicomException("Failed to read compressed frame "+frameNumber+", offset "+lastPosition+" dec bytes in delayed read of "+ValueRepresentation.getAsString(getVR())+" attribute "+getTag());
		}
		if (slf4jlogger.isTraceEnabled()) slf4jlogger.trace("getByteValuesForSelectedFrame() frame bytes =\n{}",com.pixelmed.utils.HexDump.dump(buffer));
		return buffer;
	}
	
	/**
	 * <p>Get the number of frames.</p>
	 *
	 * @return	number of frame
	 */
	public int getNumberOfFrames() {
		return frameItemLengths.length;
	}

	/**
	 * <p>Get the value representation of this attribute (OB).</p>
	 *
	 * @return	'O','B' in ASCII as a two byte array; see {@link com.pixelmed.dicom.ValueRepresentation ValueRepresentation}
	 */
	public byte[] getVR() { return ValueRepresentation.OB; }
}

