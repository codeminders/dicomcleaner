/* Copyright (c) 2001-2018, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.dicom;

import com.pixelmed.slf4j.Logger;
import com.pixelmed.slf4j.LoggerFactory;

/**
 * <p>The {@link com.pixelmed.dicom.TiledFramesIndex TiledFramesIndex} class ... .</p>
 *
 * @author	dclunie
 */
public class TiledFramesIndex {
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/dicom/TiledFramesIndex.java,v 1.12 2018/06/02 08:01:30 dclunie Exp $";

	private static final Logger slf4jlogger = LoggerFactory.getLogger(TiledFramesIndex.class);
	
	protected int[][] index;
	protected double[][] xOffsetInSlideCoordinateSystem;
	protected double[][] yOffsetInSlideCoordinateSystem;
	protected double[][] zOffsetInSlideCoordinateSystem;
	
	protected int numberOfColumnsOfTiles;
	protected int numberOfRowsOfTiles;
	
	protected int numberOfFrames;
	
	public int getNumberOfColumnsOfTiles() { return numberOfColumnsOfTiles; }
	public int getNumberOfRowsOfTiles() { return numberOfRowsOfTiles; }
	
	protected double totalPixelMatrixXOffsetInSlideCoordinateSystem;
	protected double totalPixelMatrixYOffsetInSlideCoordinateSystem;
	
	/**
	 * <p>Get the frame number for the tile at the specified row and column</p>
	 *
	 * @param	row	the number of the tile along the column direction (which row of tiles), numbered from 0
	 * @param	column	the number of the tile along the row direction (which column of tiles), numbered from 0
	 * @return			the frame number from 1, or 0 if no frame
	 * @throws	ArrayIndexOutOfBoundsException	if row or column beyond limits of tile array
	 */
	public int getFrameNumber(int row,int column) {
		slf4jlogger.trace("getFrameNumber(): index[{}][{}]",row,column);
		int frame = index[row][column];
		slf4jlogger.trace("getFrameNumber(): index[{}][{}] = {}",row,column,frame);
		return frame;
	}
	
	protected int[] rowForFrame;
	protected int[] columnForFrame;
	
	// delayed instantiation since may not need revese index
	protected void computeRowAndColumnForFrame() {
		slf4jlogger.trace("computeRowAndColumnForFrame(): numberOfRowsOfTiles = {}",numberOfRowsOfTiles);
		slf4jlogger.trace("computeRowAndColumnForFrame(): numberOfColumnsOfTiles = {}",numberOfColumnsOfTiles);
		rowForFrame = new int[numberOfRowsOfTiles*numberOfColumnsOfTiles];		// rather than numberOfFrames, in case that is wrong
		columnForFrame = new int[numberOfRowsOfTiles*numberOfColumnsOfTiles];
		for (int row=0; row<numberOfRowsOfTiles; ++row) {
			int[] framesThisRow = index[row];
			for (int column=0; column<numberOfColumnsOfTiles; ++column) {
				int frame = framesThisRow[column]-1;
				slf4jlogger.trace("computeRowAndColumnForFrame(): row = {}, column = {}, frame = {}",row,column,frame);
				if (frame >= 0 && frame < numberOfFrames) {
					rowForFrame[frame] = row;
					columnForFrame[frame] = column;
				}
				else {
					slf4jlogger.warn("computeRowAndColumnForFrame(): frame {} for tile row {} column {} out of range 0 to numberOfFrames-1 ({}) - frame for that tile mist be absent from input",frame,row,column,numberOfFrames-1);
				}
			}
		}
	}

	/**
	 * <p>Get the row number for the specified frame</p>
	 *
	 * @param	frame	the frame number from 1
	 * @return			row	the number of the tile along the column direction (which row of tiles), numbered from 0
	 * @throws	ArrayIndexOutOfBoundsException	if frame number is beyond limits of tile array
	 */

	public int getRow(int frame) {
		if (rowForFrame == null) {
			computeRowAndColumnForFrame();
		}
		return rowForFrame[frame-1];
	}

	/**
	 * <p>Get the column number for the specified frame</p>
	 *
	 * @param	frame	the frame number from 1
	 * @return			column	the number of the tile along the row direction (which column of tiles), numbered from 0
	 * @throws	ArrayIndexOutOfBoundsException	if frame number is beyond limits of tile array
	 */

	public int getColumn(int frame) {
		if (columnForFrame == null) {
			computeRowAndColumnForFrame();
		}
		return columnForFrame[frame-1];
	}
	
	/**
	 * <p>Index the tiles by row and column position</p>
	 *
	 * @param	list	an AttributeList for a Whole Slide Image
	 * @param	extractPhysicalOffsets	extract the physical as well as logical position
	 * @param	buildInverseIndex	build the inverse index rather than waiting until it is needed
	 * @param	ignorePlanePosition	ignore the PlanePositionSequence and assume frame order is normal raster
	 * @throws	DicomException	if insufficient or inconsistent information
	 */

	public TiledFramesIndex(AttributeList list,boolean extractPhysicalOffsets,boolean buildInverseIndex,boolean ignorePlanePosition) throws DicomException {
		int                    rows = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.Rows,0);
		int                 columns = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.Columns,0);
		int totalPixelMatrixColumns = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.TotalPixelMatrixColumns,0);
		int    totalPixelMatrixRows = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.TotalPixelMatrixRows,0);
		
		slf4jlogger.debug("rows = {}",rows);
		slf4jlogger.debug("columns = {}",columns);
		slf4jlogger.debug("totalPixelMatrixColumns = {}",totalPixelMatrixColumns);
		slf4jlogger.debug("totalPixelMatrixRows = {}",totalPixelMatrixRows);

		numberOfColumnsOfTiles = totalPixelMatrixColumns/columns;
		if (totalPixelMatrixColumns % columns != 0) {
			//throw new DicomException("TotalPixelMatrixColumns is not an exact multiple of Columns");
			slf4jlogger.debug("TotalPixelMatrixColumns {} is not an exact multiple of Columns {}",totalPixelMatrixColumns,columns);
			++numberOfColumnsOfTiles;
		}
		slf4jlogger.debug("numberOfColumnsOfTiles = {}",numberOfColumnsOfTiles);
		
		numberOfRowsOfTiles = totalPixelMatrixRows/rows;
		if (totalPixelMatrixRows % rows != 0) {
			//throw new DicomException("TotalPixelMatrixRows is not an exact multiple of Rows");
			slf4jlogger.debug("TotalPixelMatrixRows {} is not an exact multiple of Rows {}",totalPixelMatrixRows,rows);
			++numberOfRowsOfTiles;
		}
		slf4jlogger.debug("numberOfRowsOfTiles = {}",numberOfRowsOfTiles);
		
		if (extractPhysicalOffsets) {
			totalPixelMatrixXOffsetInSlideCoordinateSystem = Double.parseDouble(SequenceAttribute.getSingleStringValueOfNamedAttributeFromWithinSequenceWithSingleItemOrDefault(list,TagFromName.TotalPixelMatrixOriginSequence,TagFromName.XOffsetInSlideCoordinateSystem,"0"));
			totalPixelMatrixYOffsetInSlideCoordinateSystem = Double.parseDouble(SequenceAttribute.getSingleStringValueOfNamedAttributeFromWithinSequenceWithSingleItemOrDefault(list,TagFromName.TotalPixelMatrixOriginSequence,TagFromName.YOffsetInSlideCoordinateSystem,"0"));
		}

		numberOfFrames = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.NumberOfFrames,1);	// default to 1 if missing (001017)
		slf4jlogger.debug("NumberOfFrames = {}",numberOfFrames);
		
		// create non-sparse index of frame numbers
		// initialized values will be zero which is a "not set" flag since real frame numbers start from 1
		// the indices of the index matrix however, start from 0 (e.g., index[0][0] is the TLHC tile
		index = new int[numberOfRowsOfTiles][];

		if (!ignorePlanePosition) {
			try {
				if (extractPhysicalOffsets) {
					xOffsetInSlideCoordinateSystem = new double[numberOfRowsOfTiles][];
					yOffsetInSlideCoordinateSystem = new double[numberOfRowsOfTiles][];
					zOffsetInSlideCoordinateSystem = new double[numberOfRowsOfTiles][];
				}
				for (int row=0; row<numberOfRowsOfTiles; ++row) {
					index[row] = new int[numberOfColumnsOfTiles];
					if (extractPhysicalOffsets) {
						xOffsetInSlideCoordinateSystem[row] = new double[numberOfColumnsOfTiles];
						yOffsetInSlideCoordinateSystem[row] = new double[numberOfColumnsOfTiles];
						zOffsetInSlideCoordinateSystem[row] = new double[numberOfColumnsOfTiles];
					}
				}
				
				boolean alreadyWarned = false;
				SequenceAttribute perFrameFunctionalGroupsSequence = (SequenceAttribute)list.get(TagFromName.PerFrameFunctionalGroupsSequence);
				if (perFrameFunctionalGroupsSequence != null) {
					int nItems = perFrameFunctionalGroupsSequence.getNumberOfItems();
					if (nItems != numberOfFrames) {
						if (nItems < numberOfFrames) {
							throw new DicomException("Number of Items in PerFrameFunctionalGroupsSequence "+nItems+" does not match (is less than) NumberOfFrames "+numberOfFrames);
						}
						else {
							slf4jlogger.warn("Number of Items in PerFrameFunctionalGroupsSequence {} does not match (is greater than) NumberOfFrames {} - ignoring",nItems,numberOfFrames);
						}
					}
					for (int i=0;i<nItems;++i) {
						int vTileColumn = 0;
						int vTileRow = 0;
						// could use convenience method Attribute.getSingleIntegerValueOrDefault(), but want to distinguish explicit (bad) zero from default of zero if missing
						AttributeList pfList = perFrameFunctionalGroupsSequence.getItem(i).getAttributeList();
						SequenceAttribute planePositionSlideSequence = (SequenceAttribute)pfList.get(TagFromName.PlanePositionSlideSequence);
						if (planePositionSlideSequence == null || planePositionSlideSequence.getNumberOfItems() < 1) {
							throw new DicomException("Missing or empty PlanePositionSlideSequence for frame "+(i+1));
						}
						else {
							AttributeList ppsList = planePositionSlideSequence.getItem(0).getAttributeList();
							{
								Attribute aColumnPositionInTotalImagePixelMatrix = ppsList.get(TagFromName.ColumnPositionInTotalImagePixelMatrix);
								if (aColumnPositionInTotalImagePixelMatrix == null) {
									throw new DicomException("Missing ColumnPositionInTotalImagePixelMatrix for frame "+(i+1));
								}
								int[] v = aColumnPositionInTotalImagePixelMatrix.getIntegerValues();
								if (v.length > 0) {
									int vColumnPositionInTotalImagePixelMatrix = v[0];
									slf4jlogger.trace("Frame {} have vColumnPositionInTotalImagePixelMatrix = {}",(i+1),vColumnPositionInTotalImagePixelMatrix);
									int columnOriginOffset = vColumnPositionInTotalImagePixelMatrix%columns;
									if (columnOriginOffset == 0) {
										if (!alreadyWarned) {
											slf4jlogger.warn("ColumnPositionInTotalImagePixelMatrix is using an origin of zero not one as required");
											alreadyWarned = true;
										}
										vTileColumn = vColumnPositionInTotalImagePixelMatrix/columns;
									}
									else if (columnOriginOffset == 1) {
										vTileColumn = vColumnPositionInTotalImagePixelMatrix/columns;
									}
									else {
										throw new DicomException("Frame "+(i+1)+" has ColumnPositionInTotalImagePixelMatrix "+vColumnPositionInTotalImagePixelMatrix+" that is not a multiple of columns "+columns+" plus 1");
									}
									slf4jlogger.trace("Frame {} have vTileColumn = {}",(i+1),vTileColumn);
									if (!(vTileColumn < numberOfColumnsOfTiles)) {
										throw new DicomException("Frame "+(i+1)+" has ColumnPositionInTotalImagePixelMatrix "+vColumnPositionInTotalImagePixelMatrix+" that is beyond TotalPixelMatrixColumns "+totalPixelMatrixColumns);
									}
								}
							}
							{
								Attribute aRowPositionInTotalImagePixelMatrix = ppsList.get(TagFromName.RowPositionInTotalImagePixelMatrix);
								if (aRowPositionInTotalImagePixelMatrix == null) {
									throw new DicomException("Missing RowPositionInTotalImagePixelMatrix for frame "+(i+1));
								}
								int[] v = aRowPositionInTotalImagePixelMatrix.getIntegerValues();
								if (v.length > 0) {
									int vRowPositionInTotalImagePixelMatrix = v[0];
									slf4jlogger.trace("Frame {} have vRowPositionInTotalImagePixelMatrix = {}",(i+1),vRowPositionInTotalImagePixelMatrix);
									int rowOriginOffset = vRowPositionInTotalImagePixelMatrix%rows;
									if (rowOriginOffset == 0) {
										if (!alreadyWarned) {
											slf4jlogger.warn("RowPositionInTotalImagePixelMatrix is using an origin of zero not one as required");
											alreadyWarned = true;
										}
										vTileRow = vRowPositionInTotalImagePixelMatrix/rows;
									}
									else if (rowOriginOffset == 1) {
										vTileRow = vRowPositionInTotalImagePixelMatrix/rows;
									}
									else {
										throw new DicomException("Frame "+(i+1)+" has RowPositionInTotalImagePixelMatrix "+vRowPositionInTotalImagePixelMatrix+" that is not a multiple of rows "+rows+" plus 1");
									}
									slf4jlogger.trace("Frame {} have vTileRow = {}",(i+1),vTileRow);
									if (!(vTileRow < numberOfRowsOfTiles)) {
										throw new DicomException("Frame "+(i+1)+" has RowPositionInTotalImagePixelMatrix "+vRowPositionInTotalImagePixelMatrix+" that is beyond TotalPixelMatrixRows "+totalPixelMatrixRows);
									}
								}
							}
							// have already checked bounds
							slf4jlogger.trace("Setting index[{}][{}] = {}",vTileRow,vTileColumn,(i+1));
							index[vTileRow][vTileColumn] = (i+1);	// frame numbers start from 1 and are implicitly in the order of per-frame functional group serquence items
							
							if (extractPhysicalOffsets) {
								xOffsetInSlideCoordinateSystem[vTileRow][vTileColumn] = Attribute.getSingleDoubleValueOrDefault(ppsList,TagFromName.XOffsetInSlideCoordinateSystem,0d);
								yOffsetInSlideCoordinateSystem[vTileRow][vTileColumn] = Attribute.getSingleDoubleValueOrDefault(ppsList,TagFromName.YOffsetInSlideCoordinateSystem,0d);
								zOffsetInSlideCoordinateSystem[vTileRow][vTileColumn] = Attribute.getSingleDoubleValueOrDefault(ppsList,TagFromName.ZOffsetInSlideCoordinateSystem,0d);
								
								slf4jlogger.trace("Frame {} XOffsetInSlideCoordinateSystem = {}",(i+1),xOffsetInSlideCoordinateSystem[vTileRow][vTileColumn]);
								slf4jlogger.trace("Frame {} YOffsetInSlideCoordinateSystem = {}",(i+1),yOffsetInSlideCoordinateSystem[vTileRow][vTileColumn]);
								slf4jlogger.trace("Frame {} ZOffsetInSlideCoordinateSystem = {}",(i+1),zOffsetInSlideCoordinateSystem[vTileRow][vTileColumn]);
							}
						}
					}
				}
				else {
					String dimensionOrganizationType = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.DimensionOrganizationType);
					if (dimensionOrganizationType.equals("TILED_FULL")) {
						slf4jlogger.info("DimensionOrganizationType is TILED_FULL so missing PerFrameFunctionalGroupsSequence is OK and assuming raster scan order for tiles");
						ignorePlanePosition = true;
					}
					else {
						throw new DicomException("Missing PerFrameFunctionalGroupsSequence");
					}
				}
			}
			catch (Exception e) {
				slf4jlogger.error("Failed to construct tile index from PerFrameFunctionalGroupsSequence information - assuming raster scan order for tiles: ",e);
				ignorePlanePosition = true;
			}
		}
		else {
			slf4jlogger.debug("Asked to ignore PlanePositionSequence and assume raster scan order for tiles");
		}
		if (ignorePlanePosition) {
			slf4jlogger.debug("Assuming raster scan order for tiles");
			int frame = 0;
			for (int row=0; row<numberOfRowsOfTiles; ++row) {
				index[row] = new int[numberOfColumnsOfTiles];
				for (int column=0; column<numberOfColumnsOfTiles; ++column) {
					index[row][column] = ++frame;	// frame numbers start from 1 and are implicitly in the order of per-frame functional group serquence items
				}
			}
			if (frame != numberOfFrames) {
				slf4jlogger.debug("last frame {} after assuming raster scan order for tiles is != numberOfFrames {}",frame,numberOfFrames);
			}
		}
		
		if (buildInverseIndex) {
			computeRowAndColumnForFrame();
		}
	}

	
	/**
	 * <p>Index the tiles by row and column position</p>
	 *
	 * @param	list	an AttributeList for a Whole Slide Image
	 * @throws	DicomException	if insufficient or inconsistent information
	 */

	public TiledFramesIndex(AttributeList list) throws DicomException {
		this(list,false/*extractPhysicalOffsets*/,false/*buildInverseIndex*/,false/*ignorePlanePosition*/);
	}
	
	protected boolean isSparse;
	protected boolean isSparseHasBeenTested;
	
	/**
	 * <p>Is the encoded matrix of tiles sparse?</p>
	 *
	 * @return			true if any tile position does not have an encoded frame
	 */
	public boolean isSparse() {
		if (!isSparseHasBeenTested) {
			isSparse = false;
			for (int row=0; row<numberOfRowsOfTiles && !isSparse; ++row) {
				int[] framesThisRow = index[row];
				for (int column=0; column<numberOfColumnsOfTiles && !isSparse; ++column) {
					if (framesThisRow[column] == 0) {
						isSparse = true;
					}
				}
			}
			isSparseHasBeenTested = true;
		}
		return isSparse;
	}
	
	protected boolean isEncodedInStandardRasterOrder;
	protected boolean isEncodedInStandardRasterOrderHasBeenTested;
	
	/**
	 * <p>Is the encoded matrix of tiles organized in a standard raster pattern?</p>
	 *
	 * <p>The standard pattern is all the columns of the first row from left to right, then the second row, etc.</p>
	 *
	 * @return			true if in the standard raster order
	 */
	public boolean isEncodedInStandardRasterOrder() {
		if (!isEncodedInStandardRasterOrderHasBeenTested) {
			int expectedFrameNumber = 1;
			isEncodedInStandardRasterOrder = true;
			for (int row=0; row<numberOfRowsOfTiles && isEncodedInStandardRasterOrder; ++row) {
				int[] framesThisRow = index[row];
				for (int column=0; column<numberOfColumnsOfTiles && isEncodedInStandardRasterOrder; ++column) {
					if (framesThisRow[column] != expectedFrameNumber) {
						isEncodedInStandardRasterOrder = false;
					}
					++expectedFrameNumber;
				}
			}
			isEncodedInStandardRasterOrderHasBeenTested = true;
		}
		return isEncodedInStandardRasterOrder;
	}
	
	/**
	 * <p>Dump the contents of the index as a human-readable string.</p>
	 *
	 * @return			the string
	 */
	public String toString() {
		StringBuffer str = new StringBuffer();
		for (int row=0; row<numberOfRowsOfTiles; ++row) {
			int[] framesThisRow = index[row];
			for (int column=0; column<numberOfColumnsOfTiles; ++column) {
				str.append("tile["+row+","+column+"] = frame "+framesThisRow[column]);
				if (xOffsetInSlideCoordinateSystem != null) {
					str.append(" ("+xOffsetInSlideCoordinateSystem[row][column]+","+yOffsetInSlideCoordinateSystem[row][column]+","+zOffsetInSlideCoordinateSystem[row][column]+") mm");
				}
				str.append("\n");
			}
		}
		if (xOffsetInSlideCoordinateSystem != null) {	// signal that was extracted
			str.append("totalPixelMatrix OffsetInSlideCoordinateSystem = ("+totalPixelMatrixXOffsetInSlideCoordinateSystem+","+totalPixelMatrixYOffsetInSlideCoordinateSystem+")\n");
		}
		str.append("isSparse() = "+isSparse()+"\n");
		str.append("isEncodedInStandardRasterOrder() = "+isEncodedInStandardRasterOrder()+"\n");
		return str.toString();
	}

	/**
	 * <p>Read the DICOM input file as a list of attributes, create and index of the tiled frames, and dump it.</p>
	 *
	 * @param	arg	array of one string (the filename to read and dump),
	 */
	public static void main(String arg[]) {
		try {
			AttributeList list = new AttributeList();
long startReadTime = System.currentTimeMillis();
			list.setDecompressPixelData(false);
			list.read(arg[0]);
System.err.println("TiledFramesIndex.main(): read - done in "+(System.currentTimeMillis()-startReadTime)+" ms");
long startIndexTime = System.currentTimeMillis();
			TiledFramesIndex index = new TiledFramesIndex(list,true/*physical*/,false/*buildInverseIndex*/,false/*ignorePlanePosition*/);
System.err.println("TiledFramesIndex.main(): index - done in "+(System.currentTimeMillis()-startIndexTime)+" ms");
			System.err.print(index);
		} catch (Exception e) {
			slf4jlogger.error("",e);	// use SLF4J since may be invoked from script when not testing
		}
	}
}

