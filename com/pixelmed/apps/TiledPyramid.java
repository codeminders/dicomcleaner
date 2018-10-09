/* Copyright (c) 2001-2018, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.apps;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.ClinicalTrialsAttributes;
import com.pixelmed.dicom.CompressedFrameEncoder;
import com.pixelmed.dicom.CodedSequenceItem;
import com.pixelmed.dicom.CodeStringAttribute;
import com.pixelmed.dicom.DecimalStringAttribute;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.FileMetaInformation;
import com.pixelmed.dicom.IntegerStringAttribute;
import com.pixelmed.dicom.OtherByteAttributeMultipleCompressedFrames;
import com.pixelmed.dicom.SequenceAttribute;
import com.pixelmed.dicom.SignedLongAttribute;
import com.pixelmed.dicom.TagFromName;
import com.pixelmed.dicom.TiledFramesIndex;
import com.pixelmed.dicom.UIDGenerator;
import com.pixelmed.dicom.UniqueIdentifierAttribute;
import com.pixelmed.dicom.UnsignedLongAttribute;
import com.pixelmed.dicom.VersionAndConstants;

import com.pixelmed.display.SourceImage;

import com.pixelmed.slf4j.Logger;
import com.pixelmed.slf4j.LoggerFactory;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
//import java.awt.image.ComponentSampleModel;

import java.io.File;
import java.io.IOException;

/**
 * <p>Take a single high resolution tiled image and downsample it by successive factors of two to produce a multi-resolution pyramid set of images.</p>
 *
 * @author	dclunie
 */
public class TiledPyramid {
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/apps/TiledPyramid.java,v 1.6 2018/05/18 19:43:27 dclunie Exp $";

	private static final Logger slf4jlogger = LoggerFactory.getLogger(TiledPyramid.class);
	
	protected UIDGenerator generator = new UIDGenerator();
	
	// NB. reuses various attributes when copied to new list, so their value in the old list becomes unreliable.
	protected static void copyFunctionalGroupsSequenceWithDownsampledValues(AttributeList list,AttributeList newList,
			TiledFramesIndex index,int oldNumberOfFrames,int newNumberOfFrames,int newNumberOfColumnsOfTiles,int newNumberOfRowsOfTiles,
			int columns,int rows
		) throws DicomException {
		SequenceAttribute sharedFunctionalGroupsSequence = (SequenceAttribute)list.get(TagFromName.SharedFunctionalGroupsSequence);
		if (sharedFunctionalGroupsSequence == null || sharedFunctionalGroupsSequence.getNumberOfItems() != 1) {
			throw new DicomException("Missing SharedFunctionalGroupsSequence or incorrect number of items");
		}
		else {
			newList.put(sharedFunctionalGroupsSequence);
			SequenceAttribute pixelMeasuresSequence = (SequenceAttribute)SequenceAttribute.getNamedAttributeFromWithinSelectedItemWithinSequence(sharedFunctionalGroupsSequence,0,TagFromName.PixelMeasuresSequence);
			if (pixelMeasuresSequence == null || pixelMeasuresSequence.getNumberOfItems() != 1) {
				throw new DicomException("Missing PixelMeasuresSequence in SharedFunctionalGroupsSequence or incorrect number of items");
			}
			else {
				AttributeList pixelMeasuresSequenceItemList = SequenceAttribute.getAttributeListFromSelectedItemWithinSequence(pixelMeasuresSequence,0);
				Attribute aPixelSpacing = pixelMeasuresSequenceItemList.get(TagFromName.PixelSpacing);
				if (aPixelSpacing == null || aPixelSpacing.getVM() != 2) {
					throw new DicomException("Missing PixelSpacing in PixelMeasuresSequence in SharedFunctionalGroupsSequence or incorrect number of values");
				}
				else {
					double[] vPixelSpacing = aPixelSpacing.getDoubleValues();
					slf4jlogger.debug("copyFunctionalGroupsSequenceWithDownsampledValues(): PixelSpacing[0] = {}",vPixelSpacing[0]);
					slf4jlogger.debug("copyFunctionalGroupsSequenceWithDownsampledValues(): PixelSpacing[1] = {}",vPixelSpacing[1]);
					vPixelSpacing[0] = vPixelSpacing[0] * 2;
					vPixelSpacing[1] = vPixelSpacing[1] * 2;
					slf4jlogger.debug("copyFunctionalGroupsSequenceWithDownsampledValues(): new PixelSpacing[0] = {}",vPixelSpacing[0]);
					slf4jlogger.debug("copyFunctionalGroupsSequenceWithDownsampledValues(): new PixelSpacing[1] = {}",vPixelSpacing[1]);
					{ Attribute a = new DecimalStringAttribute(TagFromName.PixelSpacing); a.addValue(vPixelSpacing[0]); a.addValue(vPixelSpacing[1]); pixelMeasuresSequenceItemList.put(a); }
				}
			}
			//slf4jlogger.debug("createDownsampledDICOMFile(): \n{}",sharedFunctionalGroupsSequence.toString());
		}

		SequenceAttribute oldPerFrameFunctionalGroupsSequence = (SequenceAttribute)list.get(TagFromName.PerFrameFunctionalGroupsSequence);
		if (oldPerFrameFunctionalGroupsSequence == null || oldPerFrameFunctionalGroupsSequence.getNumberOfItems() != oldNumberOfFrames) {
			throw new DicomException("Missing PerFrameFunctionalGroupsSequence or incorrect number of items");
		}
		else {
			SequenceAttribute newPerFrameFunctionalGroupsSequence = new SequenceAttribute(TagFromName.PerFrameFunctionalGroupsSequence);
			newList.put(newPerFrameFunctionalGroupsSequence);
			// the new PixelData will be encoded in raster scan order of the new rows and columns of tiles regardless of the original order
			// so iterate through the new tiles, pulling the top left of the four averaged source per-frame functional group information from the old
			for (int newRow=0; newRow<newNumberOfRowsOfTiles; ++newRow) {
				int oldRow = newRow * 2;
				for (int newColumn=0; newColumn<newNumberOfColumnsOfTiles; ++newColumn) {
					int oldColumn = newColumn * 2;
					int oldFrame = index.getFrameNumber(oldRow,oldColumn);	// returns frame numbered from 1
					//slf4jlogger.debug("createDownsampledDICOMFile(): making new PerFrameFunctionalGroupsSequence item  from old row {} column {} frame {} item {}",oldRow,oldColumn,oldFrame,oldFrame-1);
					AttributeList oldPerFrameList = SequenceAttribute.getAttributeListFromSelectedItemWithinSequence(oldPerFrameFunctionalGroupsSequence,oldFrame-1);	// items are number from 0
					newPerFrameFunctionalGroupsSequence.addItem(oldPerFrameList);
					SequenceAttribute planePositionSlideSequence = (SequenceAttribute)oldPerFrameList.get(TagFromName.PlanePositionSlideSequence);
					if (planePositionSlideSequence == null || planePositionSlideSequence.getNumberOfItems() != 1) {
						throw new DicomException("Missing PlanePositionSlideSequence in PerFrameFunctionalGroupsSequence item "+oldFrame+" or incorrect number of items");
					}
					else {
						AttributeList planePositionSlideSequenceItemList = SequenceAttribute.getAttributeListFromSelectedItemWithinSequence(planePositionSlideSequence,0);
						{ Attribute a = new SignedLongAttribute(TagFromName.RowPositionInTotalImagePixelMatrix);    a.addValue(newRow*rows+1);    planePositionSlideSequenceItemList.put(a); }
						{ Attribute a = new SignedLongAttribute(TagFromName.ColumnPositionInTotalImagePixelMatrix); a.addValue(newColumn*columns+1); planePositionSlideSequenceItemList.put(a); }
						// ?? need to update X and Y :(
					}
				}
			}
		}
	}

	protected static void downsamplePixelData(AttributeList list,AttributeList newList,
			TiledFramesIndex index,int oldNumberOfFrames,int newNumberOfFrames,
			int oldNumberOfColumnsOfTiles,int oldNumberOfRowsOfTiles,
			int newNumberOfColumnsOfTiles,int newNumberOfRowsOfTiles,
			int columns,int rows,String outputFormat,
			File[] frameFiles
		) throws DicomException, IOException {
		SourceImage sImg = new SourceImage(list);
		// reuse the raster arrays for each tile
		int[] downsampledcount =  new int[rows*columns*3];
		int[] downsampledtotal =  new int[rows*columns*3];
		byte[] downsampleddata =  new byte[rows*columns*3];
		ColorSpace colorSpace = ColorSpace.getInstance(ColorSpace.CS_sRGB);		// should really check DICOM ColorSpace attribute ... makes no difference to result though
		// the new PixelData will be encoded in raster scan order of the new rows and columns of tiles regardless of the original order ... must match the PerFrameFunctionalGroupsSequence item order
		int newFrameFromZero = 0;
		for (int newRow=0; newRow<newNumberOfRowsOfTiles; ++newRow) {
			int oldRowTLHCOfFour = newRow * 2;
			for (int newColumn=0; newColumn<newNumberOfColumnsOfTiles; ++newColumn) {
				int oldColumnTLHCOfFour = newColumn * 2;
				int oldFrameTLHC = oldRowTLHCOfFour <  oldNumberOfRowsOfTiles    && oldColumnTLHCOfFour <  oldNumberOfColumnsOfTiles    ? index.getFrameNumber(oldRowTLHCOfFour,  oldColumnTLHCOfFour  ) : 0;
				int oldFrameTRHC = oldRowTLHCOfFour <  oldNumberOfRowsOfTiles    && oldColumnTLHCOfFour < (oldNumberOfColumnsOfTiles-1) ? index.getFrameNumber(oldRowTLHCOfFour,  oldColumnTLHCOfFour+1) : 0;
				int oldFrameBLHC = oldRowTLHCOfFour < (oldNumberOfRowsOfTiles-1) && oldColumnTLHCOfFour <  oldNumberOfColumnsOfTiles    ? index.getFrameNumber(oldRowTLHCOfFour+1,oldColumnTLHCOfFour  ) : 0;
				int oldFrameBRHC = oldRowTLHCOfFour < (oldNumberOfRowsOfTiles-1) && oldColumnTLHCOfFour < (oldNumberOfColumnsOfTiles-1) ? index.getFrameNumber(oldRowTLHCOfFour+1,oldColumnTLHCOfFour+1) : 0;

				slf4jlogger.trace("downsamplePixelData(): Four old frames TLHC = {}, TRHC = {}, BLHC = {}, BRHC = {}",oldFrameTLHC,oldFrameTRHC,oldFrameBLHC,oldFrameBRHC);
				
				BufferedImage tlhcFrame = oldFrameTLHC != 0 ? sImg.getBufferedImage(oldFrameTLHC-1) : null;	// NB. getBufferedImage() takes argument numbered from 0 :(
				BufferedImage trhcFrame = oldFrameTRHC != 0 ? sImg.getBufferedImage(oldFrameTRHC-1) : null;
				BufferedImage blhcFrame = oldFrameBLHC != 0 ? sImg.getBufferedImage(oldFrameBLHC-1) : null;
				BufferedImage brhcFrame = oldFrameBRHC != 0 ? sImg.getBufferedImage(oldFrameBRHC-1) : null;

				// have encountered images (e.g., Aperio SVS TIFF) in which the last row is only partially encoded in the JPEG source, i.e., BufferedImage.getHeight() < rows (001067)
				int tlhcActualRows = (tlhcFrame != null) ? tlhcFrame.getHeight() : 0;
				int trhcActualRows = (trhcFrame != null) ? trhcFrame.getHeight() : 0;
				int blhcActualRows = (blhcFrame != null) ? blhcFrame.getHeight() : 0;
				int brhcActualRows = (brhcFrame != null) ? brhcFrame.getHeight() : 0;
				
				if (tlhcFrame != null && tlhcActualRows != rows) slf4jlogger.warn("Top row (TLHC) height {} is less than expected {}",tlhcActualRows,rows);
				if (trhcFrame != null && trhcActualRows != rows) slf4jlogger.warn("Top row (TRHC) height {} is less than expected {}",trhcActualRows,rows);
				if (blhcFrame != null && blhcActualRows != rows) slf4jlogger.warn("Bottom row (BLHC) height {} is less than expected {}",blhcActualRows,rows);
				if (brhcFrame != null && brhcActualRows != rows) slf4jlogger.warn("Bottom row (BRHC) height {} is less than expected {}",brhcActualRows,rows);
				
				int tlhcFramePixels[] = null; // to disambiguate SampleModel.getPixels() method signature
				int trhcFramePixels[] = null;
				int blhcFramePixels[] = null;
				int brhcFramePixels[] = null;
				
				tlhcFramePixels = tlhcFrame != null ? tlhcFrame.getSampleModel().getPixels(0,0,columns,tlhcActualRows,tlhcFramePixels,tlhcFrame.getRaster().getDataBuffer()) : null;
				trhcFramePixels = trhcFrame != null ? trhcFrame.getSampleModel().getPixels(0,0,columns,trhcActualRows,trhcFramePixels,trhcFrame.getRaster().getDataBuffer()) : null;
				blhcFramePixels = blhcFrame != null ? blhcFrame.getSampleModel().getPixels(0,0,columns,blhcActualRows,blhcFramePixels,blhcFrame.getRaster().getDataBuffer()) : null;
				brhcFramePixels = brhcFrame != null ? brhcFrame.getSampleModel().getPixels(0,0,columns,brhcActualRows,brhcFramePixels,brhcFrame.getRaster().getDataBuffer()) : null;
				
				// push rather than pull, by scanning source, adding to total and counting, then dividing total by count to produce byte pixel, for each of interlevaed RGB
				// zero our counts and totals since reusing arrays
				for (int i=0; i<downsampledcount.length; ++i) {
					downsampledcount[i]=0;
					downsampledtotal[i]=0;
				}
				
				if (tlhcFramePixels != null) {
					for (int r=0; r<tlhcActualRows; ++r) {
						int dstRow = r/2;
						for (int c=0; c<columns; ++c) {
							int dstColumn = c/2;
							for (int rgb=0; rgb<3; ++rgb) {
								int srcPixel = tlhcFramePixels[(r*columns+c)*3+rgb]&0xff;	// do not sign extend
								int dstIndex = (dstRow*columns+dstColumn)*3+rgb;
								++downsampledcount[dstIndex];
								downsampledtotal[dstIndex]+=srcPixel;
							}
						}
					}
				}
				if (trhcFramePixels != null) {
					int offsetalongrow = columns;	// may be odd
					
					for (int r=0; r<trhcActualRows; ++r) {
						int dstRow = r/2;
						for (int c=0; c<columns; ++c) {
							int dstColumn = (offsetalongrow+c)/2;
							for (int rgb=0; rgb<3; ++rgb) {
								int srcPixel = trhcFramePixels[(r*columns+c)*3+rgb]&0xff;	// do not sign extend
								int dstIndex = (dstRow*columns+dstColumn)*3+rgb;
								++downsampledcount[dstIndex];
								downsampledtotal[dstIndex]+=srcPixel;
							}
						}
					}
				}
				if (blhcFramePixels != null) {
					int offsetalongcolumn = rows;	// may be odd
					
					for (int r=0; r<blhcActualRows; ++r) {
						int dstRow = (offsetalongcolumn+r)/2;
						for (int c=0; c<columns; ++c) {
							int dstColumn = c/2;
							for (int rgb=0; rgb<3; ++rgb) {
								int srcPixel = blhcFramePixels[(r*columns+c)*3+rgb]&0xff;	// do not sign extend
								int dstIndex = (dstRow*columns+dstColumn)*3+rgb;
								++downsampledcount[dstIndex];
								downsampledtotal[dstIndex]+=srcPixel;
							}
						}
					}
				}
				if (brhcFramePixels != null) {
					int offsetalongrow = columns;	// may be odd
					int offsetalongcolumn = rows;	// may be odd
					
					for (int r=0; r<brhcActualRows; ++r) {
						int dstRow = (offsetalongcolumn+r)/2;
						for (int c=0; c<columns; ++c) {
							int dstColumn = (offsetalongrow+c)/2;
							for (int rgb=0; rgb<3; ++rgb) {
								int srcPixel = brhcFramePixels[(r*columns+c)*3+rgb]&0xff;	// do not sign extend
								int dstIndex = (dstRow*columns+dstColumn)*3+rgb;
								++downsampledcount[dstIndex];
								downsampledtotal[dstIndex]+=srcPixel;
							}
						}
					}
				}
				
				for (int i=0; i<downsampledcount.length; ++i) {
					downsampleddata[i] = (byte)(downsampledcount[i] == 0 ? 255 : downsampledtotal[i]/downsampledcount[i]);	// 0 count happens if no source of input
				}
				
				BufferedImage renderedImage = SourceImage.createPixelInterleavedByteThreeComponentColorImage(columns,rows,downsampleddata,0/*offset*/,colorSpace,false/*isChrominanceHorizontallyDownsampledBy2*/);
				File tmpFrameFile = File.createTempFile("TiledPyramid_tmp",".tmp");
				tmpFrameFile.deleteOnExit();
				CompressedFrameEncoder.getCompressedFrameAsFile(list,renderedImage,outputFormat,tmpFrameFile);
				frameFiles[newFrameFromZero] = tmpFrameFile;
				
				++newFrameFromZero;
			}
		}
		OtherByteAttributeMultipleCompressedFrames aPixelData = new OtherByteAttributeMultipleCompressedFrames(TagFromName.PixelData,frameFiles);
		newList.put(aPixelData);
	}
	
	public static void createDownsampledDICOMAttributes(AttributeList list,AttributeList newList,TiledFramesIndex index,String outputformat,
				boolean populateunchangedimagepixeldescriptionmacroattributes,boolean populatefunctionalgroups) throws DicomException, IOException {
			int    rows = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.Rows,0);
			int columns = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.Columns,0);
			slf4jlogger.debug("createDownsampledDICOMAttributes(): rows = {}",rows);
			slf4jlogger.debug("createDownsampledDICOMAttributes(): columns = {}",columns);

			int totalPixelMatrixColumns = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.TotalPixelMatrixColumns,0);
			int    totalPixelMatrixRows = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.TotalPixelMatrixRows,0);
			slf4jlogger.debug("createDownsampledDICOMAttributes(): totalPixelMatrixColumns = {}",totalPixelMatrixColumns);
			slf4jlogger.debug("createDownsampledDICOMAttributes(): totalPixelMatrixRows = {}",totalPixelMatrixRows);
		
			int newTotalPixelMatrixColumns = (totalPixelMatrixColumns+2)/2-1;	// e.g, 512->256, 513->257, 514->257
			int newTotalPixelMatrixRows = (totalPixelMatrixRows+2)/2-1;
			slf4jlogger.debug("createDownsampledDICOMAttributes(): new TotalPixelMatrixColumns = {}",newTotalPixelMatrixColumns);
			slf4jlogger.debug("createDownsampledDICOMAttributes(): new TotalPixelMatrixRows = {}",newTotalPixelMatrixRows);
		
			if (totalPixelMatrixColumns == newTotalPixelMatrixColumns || totalPixelMatrixRows == newTotalPixelMatrixRows) {
				slf4jlogger.debug("createDownsampledDICOMAttributes(): stopping because unchanged");
			}
			else if (newTotalPixelMatrixColumns <= 0 || newTotalPixelMatrixRows <= 0) {
				slf4jlogger.error("createDownsampledDICOMAttributes(): stopping because matrix zero or less ... should not happen");
			}
		
			int numberOfColumnsOfTiles = index.getNumberOfColumnsOfTiles();
			int numberOfRowsOfTiles = index.getNumberOfRowsOfTiles();
			slf4jlogger.debug("createDownsampledDICOMAttributes(): numberOfColumnsOfTiles = {}",numberOfColumnsOfTiles);
			slf4jlogger.debug("createDownsampledDICOMAttributes(): numberOfRowsOfTiles = {}",numberOfRowsOfTiles);
		
			// the following computation is copied from TiledFramesIndex():

			int newNumberOfColumnsOfTiles = newTotalPixelMatrixColumns/columns;
			if (newTotalPixelMatrixColumns % columns != 0) {
				slf4jlogger.debug("createDownsampledDICOMAttributes(): New TotalPixelMatrixColumns {} is not an exact multiple of Columns {}",newTotalPixelMatrixColumns,columns);
				++newNumberOfColumnsOfTiles;
			}
			slf4jlogger.debug("createDownsampledDICOMAttributes(): New numberOfColumnsOfTiles = {}",newNumberOfColumnsOfTiles);
		
			int newNumberOfRowsOfTiles = newTotalPixelMatrixRows/rows;
			if (newTotalPixelMatrixRows % rows != 0) {
				slf4jlogger.debug("createDownsampledDICOMAttributes(): New TotalPixelMatrixRows {} is not an exact multiple of Rows {}",newTotalPixelMatrixRows,rows);
				++newNumberOfRowsOfTiles;
			}
			slf4jlogger.debug("createDownsampledDICOMAttributes(): New newNumberOfRowsOfTiles = {}",newNumberOfRowsOfTiles);

			int numberOfFrames = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.NumberOfFrames,1);	// default to 1 if missing
			slf4jlogger.debug("createDownsampledDICOMAttributes(): NumberOfFrames = {}",numberOfFrames);
		
			int newNumberOfFrames = newNumberOfColumnsOfTiles * newNumberOfRowsOfTiles;
			slf4jlogger.debug("createDownsampledDICOMAttributes(): new NumberOfFrames = {}",newNumberOfFrames);
			if (newNumberOfFrames == 0) {
				slf4jlogger.error("createDownsampledDICOMAttributes(): stopping because new NumberOfFrames zero or less ... should not happen");
			}
			{ Attribute a = new IntegerStringAttribute(TagFromName.NumberOfFrames); a.addValue(newNumberOfFrames); newList.put(a); }

			{ Attribute a = new UnsignedLongAttribute(TagFromName.TotalPixelMatrixColumns); a.addValue(newTotalPixelMatrixColumns); newList.put(a); }
			{ Attribute a = new UnsignedLongAttribute(TagFromName.TotalPixelMatrixRows); a.addValue(newTotalPixelMatrixRows); newList.put(a); }
		
			if (populateunchangedimagepixeldescriptionmacroattributes) {
				newList.put(list.get(TagFromName.Rows));
				newList.put(list.get(TagFromName.Columns));
				newList.put(list.get(TagFromName.BitsStored));
				newList.put(list.get(TagFromName.BitsAllocated));
				newList.put(list.get(TagFromName.HighBit));
				newList.put(list.get(TagFromName.SamplesPerPixel));
				newList.put(list.get(TagFromName.PlanarConfiguration));
			}
		
			{
				// override whatever source may have been
				slf4jlogger.debug("createDownsampledDICOMAttributes(): outputformat = {}",outputformat);
				String photometricInterpretation = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.PhotometricInterpretation);
				slf4jlogger.debug("createDownsampledDICOMAttributes(): source list PhotometricInterpretation = {}",photometricInterpretation);
				if (outputformat.equals("jpeg")) {
					photometricInterpretation = "YBR_FULL_422";		// even if input really was RGB, e.g., Aperio, we are recompressing using default lossy
				}
				if (outputformat.equals("jpeg2000")) {
					photometricInterpretation = "YBR_ICT";			// even if input really was RGB, e.g., Aperio, we are recompressing using default irreversible
				}
				else if (photometricInterpretation.length() == 0){
					photometricInterpretation = "RGB";
				}
				// else use value from existing list
				slf4jlogger.debug("createDownsampledDICOMAttributes(): new list PhotometricInterpretation = {}",photometricInterpretation);
				{ Attribute a = new CodeStringAttribute(TagFromName.PhotometricInterpretation); a.addValue(photometricInterpretation); newList.put(a); }
			}
			
			if (populatefunctionalgroups) {
				copyFunctionalGroupsSequenceWithDownsampledValues(list,newList,index,numberOfFrames,newNumberOfFrames,newNumberOfColumnsOfTiles,newNumberOfRowsOfTiles,columns,rows);
			}

			File[] frameFiles = new File[newNumberOfFrames];	// don't forget to delete these later :(

			downsamplePixelData(list,newList,index,numberOfFrames,newNumberOfFrames,numberOfColumnsOfTiles,numberOfRowsOfTiles,newNumberOfColumnsOfTiles,newNumberOfRowsOfTiles,columns,rows,outputformat,frameFiles);
	}
	
	public File createDownsampledDICOMFile(File inputFile,File outputFolder) throws DicomException, IOException {
		slf4jlogger.info("createDownsampledDICOMFile(): inputFile = {}",inputFile);
		File newFile = null;
		AttributeList list = new AttributeList();
		list.setDecompressPixelData(false);
		list.read(inputFile);

		String transferSyntaxUID = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.TransferSyntaxUID);
		if (transferSyntaxUID.length() == 0) {
			throw new DicomException("Missing TransferSyntaxUID");
		}
		slf4jlogger.debug("transferSyntaxUID = {}",transferSyntaxUID);
		String outputformat = CompressedFrameEncoder.chooseOutputFormatForTransferSyntax(transferSyntaxUID);
		slf4jlogger.debug("outputformat = {}",outputformat);

		list.removeGroupLengthAttributes();
		list.remove(TagFromName.DataSetTrailingPadding);
		// do NOT removeMetaInformationHeaderAttributes() yet because need TransferSyntaxUID left in the old list for deferred decmpression
						
		TiledFramesIndex index = new TiledFramesIndex(list,false/*physical*/,false/*buildInverseIndex*/,false/*ignorePlanePosition*/);
		int numberOfColumnsOfTiles = index.getNumberOfColumnsOfTiles();
		int numberOfRowsOfTiles = index.getNumberOfRowsOfTiles();
		if (numberOfColumnsOfTiles > 1 || numberOfRowsOfTiles > 1) {
			AttributeList newList = new AttributeList();
			newList.putAll(list);
			newList.remove(TagFromName.PixelData);
			
			// did putAll() so do not need to repeat populateunchangedimagepixeldescriptionmacroattributes
			createDownsampledDICOMAttributes(list,newList,index,outputformat,false/*populateunchangedimagepixeldescriptionmacroattributes*/,true/*populatefunctionalgroups*/);
			
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID); a.addValue(generator.getAnotherNewUID()); newList.put(a); }

			ClinicalTrialsAttributes.addContributingEquipmentSequence(newList,false/*retainExistingItems*/,
				new CodedSequenceItem("109106","DCM","Enhanced Multi-frame Conversion Equipment"),
				"PixelMed",														// Manufacturer
				"PixelMed",														// Institution Name
				"Software Development",											// Institutional Department Name
				"Bangor, PA",													// Institution Address
				null,															// Station Name
				"TiledPyramid",													// Manufacturer's Model Name
				null,															// Device Serial Number
				"Vers. "+VersionAndConstants.getBuildDate(),					// Software Version(s)
				"Tiled pyramid created from single high resolution image");
			
			newList.insertSuitableSpecificCharacterSetForAllStringValues();
			
			newList.removeMetaInformationHeaderAttributes();
			FileMetaInformation.addFileMetaInformation(newList,transferSyntaxUID,"OURAETITLE");

			newFile = new File(outputFolder,Attribute.getSingleStringValueOrDefault(newList,TagFromName.SOPInstanceUID,"NONAME"));
			newList.write(newFile,transferSyntaxUID,true,true);

			{
				Attribute aPixelData = newList.get(TagFromName.PixelData);
				if (aPixelData != null && aPixelData instanceof OtherByteAttributeMultipleCompressedFrames) {
					File[] frameFiles = ((OtherByteAttributeMultipleCompressedFrames)aPixelData).getFiles();
					for (int f=0; f<frameFiles.length; ++f) {
						slf4jlogger.debug("deleting = {}",outputformat);
						frameFiles[f].delete();
						frameFiles[f] = null;
					}
				}
			}
		}
		return newFile;
	}

	public TiledPyramid(String inputfilename,String outputPath) throws DicomException, IOException {
		File outputFolder = new File(outputPath);
		if (!outputFolder.isDirectory()) {
			throw new DicomException("Output folder "+outputFolder+" does not exist");
		}
		File lastfile = new File(inputfilename);
		do {
			lastfile = createDownsampledDICOMFile(lastfile,outputFolder);
		} while (lastfile != null);
	}

	/**
	 * <p>Take a single high resolution tiled image and downsample it by successive factors of two to produce multi-resolution pyramid set of images.</p>
	 *
	 * @param	arg	array of two strings - the source image and the target directory (which must already exist)
	 */
	public static void main(String arg[]) {
		try {
			new TiledPyramid(arg[0],arg[1]);
		}
		catch (Exception e) {
			e.printStackTrace(System.err);	// no need to use SLF4J since command line utility/test
			System.exit(0);
		}
	}

}
