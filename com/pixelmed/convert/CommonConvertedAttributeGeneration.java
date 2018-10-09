/* Copyright (c) 2001-2018, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.convert;

import com.pixelmed.dicom.*;

import com.pixelmed.slf4j.Logger;
import com.pixelmed.slf4j.LoggerFactory;

/**
 * <p>A class to support converting proprietary image input format files into images of a specified or appropriate SOP Class.</p>
 *
 * @author	dclunie
 */

public class CommonConvertedAttributeGeneration {
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/convert/CommonConvertedAttributeGeneration.java,v 1.11 2018/08/06 16:52:08 dclunie Exp $";

	private static final Logger slf4jlogger = LoggerFactory.getLogger(CommonConvertedAttributeGeneration.class);

	private static void addParametricMapFrameTypeSharedFunctionalGroup(AttributeList list) throws DicomException {
		Attribute aFrameType = new CodeStringAttribute(TagFromName.FrameType);
		aFrameType.addValue("DERIVED");
		aFrameType.addValue("PRIMARY");
		aFrameType.addValue("");		// do not have a value for Image Flavor, but value is required to be present
		aFrameType.addValue("");		// do not have a value for Derived Pixel Contrast, but value is required to be present
		FunctionalGroupUtilities.generateFrameTypeSharedFunctionalGroup(list,TagFromName.ParametricMapFrameTypeSequence,aFrameType);
	}
	
	private static void addModalitySpecificFrameTypeSharedFunctionalGroup(AttributeList list,String modality) throws DicomException {
		AttributeTag frameTypeSequenceTag = null;
		if (modality != null) {
			switch (modality) {
				case "CT":	frameTypeSequenceTag = TagFromName.CTImageFrameTypeSequence; break;
				case "MR":	frameTypeSequenceTag = TagFromName.MRImageFrameTypeSequence; break;
				case "PT":	frameTypeSequenceTag = TagFromName.PETFrameTypeSequence; break;
				case "DX":	frameTypeSequenceTag = TagFromName.XRay3DFrameTypeSequence; break;
				case "MG":	frameTypeSequenceTag = TagFromName.XRay3DFrameTypeSequence; break;
				case "XA":	frameTypeSequenceTag = TagFromName.XRay3DFrameTypeSequence; break;
			}
		}
		if (frameTypeSequenceTag != null) {
			Attribute aFrameType = new CodeStringAttribute(TagFromName.FrameType);
			aFrameType.addValue("DERIVED");
			aFrameType.addValue("PRIMARY");
			aFrameType.addValue("");		// do not have a value for Image Flavor, but value is required to be present
			aFrameType.addValue("");		// do not have a value for Derived Pixel Contrast, but value is required to be present
			FunctionalGroupUtilities.generateFrameTypeSharedFunctionalGroup(list,frameTypeSequenceTag,aFrameType);
			{
				AttributeList macroList = ((SequenceAttribute)SequenceAttribute.getNamedAttributeFromWithinSelectedItemWithinSequence(list,TagFromName.SharedFunctionalGroupsSequence,0,frameTypeSequenceTag)).getItem(0).getAttributeList();
				{ Attribute a = new CodeStringAttribute(TagFromName.PixelPresentation);               a.addValue("MONOCHROME"); macroList.put(a); }		// should we bother checking PhotometricInterpretation ?
				{ Attribute a = new CodeStringAttribute(TagFromName.VolumetricProperties);            a.addValue("VOLUME");     macroList.put(a); }		// no way to check that we are something funky like a MIP
				{ Attribute a = new CodeStringAttribute(TagFromName.VolumeBasedCalculationTechnique); a.addValue("NONE");       macroList.put(a); }		// no information
			}
		}
	}
	
	/**
	 * <p>Generate common attributes for converted images.</p>
	 *
	 * <p>Does NOT add ManufacturerModelName ... that should be added by caller.</p>
	 *
	 * <p>Does NOT call CodingSchemeIdentification.replaceCodingSchemeIdentificationSequenceWithCodingSchemesUsedInAttributeList ... that should be done by caller.</p>
	 *
	 * @param	list
	 * @param	patientName
	 * @param	patientID
	 * @param	studyID
	 * @param	seriesNumber
	 * @param	instanceNumber
	 * @param	modality	may be null
	 * @param	sopClass	may be null
	 * @param	generateUnassignedConverted	whether or not to generate empty Unassigned Converted Attributes Sequences (populates Shared and Per-Frame Functional Groups)
	 * @throws			DicomException
	 * @throws			NumberFormatException
	 */
	public static void generateCommonAttributes(AttributeList list,String patientName,String patientID,String studyID,String seriesNumber,String instanceNumber,String modality,String sopClass,boolean generateUnassignedConverted) throws  DicomException {
		UIDGenerator u = new UIDGenerator();	

		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID); a.addValue(u.getAnotherNewUID()); list.put(a); }
		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SeriesInstanceUID); a.addValue(u.getAnotherNewUID()); list.put(a); }
		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.StudyInstanceUID); a.addValue(u.getAnotherNewUID()); list.put(a); }
		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.FrameOfReferenceUID); a.addValue(u.getAnotherNewUID()); list.put(a); }

		{ Attribute a = new PersonNameAttribute(TagFromName.PatientName); a.addValue(patientName); list.put(a); }
		{ Attribute a = new LongStringAttribute(TagFromName.PatientID); a.addValue(patientID); list.put(a); }
		{ Attribute a = new DateAttribute(TagFromName.PatientBirthDate); list.put(a); }
		{ Attribute a = new CodeStringAttribute(TagFromName.PatientSex); list.put(a); }
		{ Attribute a = new ShortStringAttribute(TagFromName.StudyID); a.addValue(studyID); list.put(a); }
		{ Attribute a = new PersonNameAttribute(TagFromName.ReferringPhysicianName); a.addValue("^^^^"); list.put(a); }
		{ Attribute a = new ShortStringAttribute(TagFromName.AccessionNumber); list.put(a); }
		{ Attribute a = new IntegerStringAttribute(TagFromName.SeriesNumber); a.addValue(seriesNumber); list.put(a); }
		{ Attribute a = new IntegerStringAttribute(TagFromName.InstanceNumber); a.addValue(instanceNumber); list.put(a); }
		
		{ Attribute a = new LongStringAttribute(TagFromName.Manufacturer); a.addValue("PixelMed"); list.put(a); }
		{ Attribute a = new LongStringAttribute(TagFromName.DeviceSerialNumber); a.addValue(new java.rmi.dgc.VMID().toString()); list.put(a); }
		{ Attribute a = new LongStringAttribute(TagFromName.SoftwareVersions); a.addValue(VersionAndConstants.getBuildDate()); list.put(a); }
		
		{ Attribute a = new CodeStringAttribute(TagFromName.PatientOrientation); list.put(a); }
		
		{ Attribute a = new CodeStringAttribute(TagFromName.BurnedInAnnotation); a.addValue("NO"); list.put(a); }
		{ Attribute a = new CodeStringAttribute(TagFromName.RecognizableVisualFeatures); a.addValue("NO"); list.put(a); }
		{ Attribute a = new CodeStringAttribute(TagFromName.ContentQualification); a.addValue("RESEARCH"); list.put(a); }

		{ Attribute a = new CodeStringAttribute(TagFromName.LossyImageCompression); a.addValue("00"); list.put(a); }

		{ Attribute a = new LongStringAttribute(TagFromName.PositionReferenceIndicator); list.put(a); }
		
		{
			java.util.Date currentDateTime = new java.util.Date();
			String currentDate = new java.text.SimpleDateFormat("yyyyMMdd").format(currentDateTime);
			String currentTime = new java.text.SimpleDateFormat("HHmmss.SSS").format(currentDateTime);
			{ Attribute a = new DateAttribute(TagFromName.StudyDate);						a.addValue(currentDate); list.put(a); }
			{ Attribute a = new TimeAttribute(TagFromName.StudyTime);						a.addValue(currentTime); list.put(a); }
			{ Attribute a = new DateAttribute(TagFromName.SeriesDate);						a.addValue(currentDate); list.put(a); }
			{ Attribute a = new TimeAttribute(TagFromName.SeriesTime);						a.addValue(currentTime); list.put(a); }
			{ Attribute a = new DateAttribute(TagFromName.ContentDate);						a.addValue(currentDate); list.put(a); }
			{ Attribute a = new TimeAttribute(TagFromName.ContentTime);						a.addValue(currentTime); list.put(a); }
			{ Attribute a = new DateAttribute(TagFromName.InstanceCreationDate);			a.addValue(currentDate); list.put(a); }
			{ Attribute a = new TimeAttribute(TagFromName.InstanceCreationTime);			a.addValue(currentTime); list.put(a); }
			{ Attribute a = new ShortStringAttribute(TagFromName.TimezoneOffsetFromUTC);	a.addValue(DateTimeAttribute.getTimeZone(java.util.TimeZone.getDefault(),currentDateTime)); list.put(a); }
		}
		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.InstanceCreatorUID); a.addValue(VersionAndConstants.instanceCreatorUID); list.put(a); }
		
		int numberOfFrames = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.NumberOfFrames,1);
		int samplesPerPixel = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.SamplesPerPixel,1);

		if (sopClass == null) {
			// if modality were not null, could actually attempt to guess SOP Class based on modality here :(
			sopClass = SOPClass.SecondaryCaptureImageStorage;
			if (numberOfFrames > 1) {
				if (samplesPerPixel == 1) {
					int bitsAllocated = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.BitsAllocated,1);
					if (bitsAllocated == 8) {
						sopClass = SOPClass.MultiframeGrayscaleByteSecondaryCaptureImageStorage;
					}
					else if (bitsAllocated == 16) {
						sopClass = SOPClass.MultiframeGrayscaleWordSecondaryCaptureImageStorage;
					}
					else {
						Attribute aPixelData = list.getPixelData();
						if (aPixelData instanceof OtherFloatAttribute || aPixelData instanceof OtherDoubleAttribute) {
							sopClass = SOPClass.ParametricMapStorage;
						}
					}
				}
				else if (samplesPerPixel == 3) {
					sopClass = SOPClass.MultiframeTrueColorSecondaryCaptureImageStorage;
				}
			}
		}

		if (sopClass.equals(SOPClass.ParametricMapStorage)) {
			addParametricMapFrameTypeSharedFunctionalGroup(list);
			{ Attribute a = new CodeStringAttribute(TagFromName.ContentLabel); a.addValue("LABEL1"); list.put(a); }
			{ Attribute a = new LongStringAttribute(TagFromName.ContentDescription); list.put(a); }
			{ Attribute a = new PersonNameAttribute(TagFromName.ContentCreatorName); list.put(a); }
		}

		if (SOPClass.isEnhancedMultiframeImageStorage(sopClass)) {
			if (samplesPerPixel == 1) {
				double windowWidth = Attribute.getSingleDoubleValueOrDefault(list,TagFromName.WindowWidth,0);
				if (windowWidth > 0) {
					double windowCenter = Attribute.getSingleDoubleValueOrDefault(list,TagFromName.WindowCenter,0);
					String voiLUTFunction = Attribute.getSingleStringValueOrDefault(list,TagFromName.VOILUTFunction,"LINEAR");
					FunctionalGroupUtilities.generateVOILUTFunctionalGroup(list,numberOfFrames,windowWidth,windowCenter,voiLUTFunction);
					list.remove(TagFromName.WindowCenter);
					list.remove(TagFromName.WindowWidth);
					list.remove(TagFromName.VOILUTFunction);
				}
			}
			
			double rescaleSlope = Attribute.getSingleDoubleValueOrDefault(list,TagFromName.RescaleSlope,0);
			if (rescaleSlope > 0) {
				double rescaleIntercept = Attribute.getSingleDoubleValueOrDefault(list,TagFromName.RescaleIntercept,0);
				String rescaleType = Attribute.getSingleStringValueOrDefault(list,TagFromName.RescaleType,"US");
				FunctionalGroupUtilities.generatePixelValueTransformationFunctionalGroup(list,numberOfFrames,rescaleSlope,rescaleIntercept,rescaleType);
				list.remove(TagFromName.RescaleSlope);
				list.remove(TagFromName.RescaleIntercept);
				list.remove(TagFromName.RescaleType);
			}
			
			// create unassigned groups, even if empty and even if not used by SOP Class (e.g., parametric map) in case later converted ...
			if (generateUnassignedConverted) {
				FunctionalGroupUtilities.generateUnassignedConvertedAttributesSequenceFunctionalGroups(list,numberOfFrames);
			}
			
			// four values required
			{ Attribute a = new CodeStringAttribute(TagFromName.ImageType); a.addValue("DERIVED"); a.addValue("PRIMARY"); a.addValue("");  a.addValue(""); list.put(a); }
			{ Attribute a = new CodeStringAttribute(TagFromName.PixelPresentation);               a.addValue("MONOCHROME"); list.put(a); }		// should we bother checking PhotometricInterpretation ?
			{ Attribute a = new CodeStringAttribute(TagFromName.VolumetricProperties);            a.addValue("VOLUME");     list.put(a); }		// no way to check that we are something funky like a MIP
			{ Attribute a = new CodeStringAttribute(TagFromName.VolumeBasedCalculationTechnique); a.addValue("NONE");       list.put(a); }		// no information

			addModalitySpecificFrameTypeSharedFunctionalGroup(list,modality);

		}
		else {
			// two values will usually do
			{ Attribute a = new CodeStringAttribute(TagFromName.ImageType); a.addValue("DERIVED"); a.addValue("PRIMARY"); list.put(a); }
		}
		
		if (SOPClass.isMultiframeSecondaryCaptureImageStorage(sopClass)) {
			if (list.get(TagFromName.PerFrameFunctionalGroupsSequence) != null) {
				{ AttributeTagAttribute a = new AttributeTagAttribute(TagFromName.FrameIncrementPointer); a.addValue(TagFromName.PerFrameFunctionalGroupsSequence); list.put(a); }
			}
			else {
				if (numberOfFrames > 1) {
					{ AttributeTagAttribute a = new AttributeTagAttribute(TagFromName.FrameIncrementPointer); a.addValue(TagFromName.PageNumberVector); list.put(a); }
					{
						Attribute a = new IntegerStringAttribute(TagFromName.PageNumberVector);
						for (int page=1; page <= numberOfFrames; ++page) {
							a.addValue(page);
						}
						list.put(a);
					}
				}
			}
		}

		slf4jlogger.debug("CommonConvertedAttributeGeneration.generateCommonAttributes(): SOP Class = {}",sopClass);
		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPClassUID); a.addValue(sopClass); list.put(a); }
		
		if (SOPClass.isSecondaryCaptureImageStorage(sopClass)) {
			{ Attribute a = new CodeStringAttribute(TagFromName.ConversionType); a.addValue("WSD"); list.put(a); }
		}
		else {
			{ Attribute a = new SequenceAttribute(TagFromName.AcquisitionContextSequence); list.put(a); }	// not in SC IODs (001080)
		}
		
		if (modality == null) {
			// could actually attempt to guess modality based on SOP Class here :(
			modality = "OT";
		}
		{ Attribute a = new CodeStringAttribute(TagFromName.Modality); a.addValue(modality); list.put(a); }

	}


	/**
	 * <p>Generate common attributes for converted images.</p>
	 *
	 * <p>Does NOT add ManufacturerModelName ... that should be added by caller.</p>
	 *
	 * <p>Does NOT call CodingSchemeIdentification.replaceCodingSchemeIdentificationSequenceWithCodingSchemesUsedInAttributeList ... that should be done by caller.</p>
	 *
	 * <p>Does generate empty Unassigned Converted Attributes Sequences (populates Shared and Per-Frame Functional Groups).</p>
	 *
	 * @param	list
	 * @param	patientName
	 * @param	patientID
	 * @param	studyID
	 * @param	seriesNumber
	 * @param	instanceNumber
	 * @param	modality	may be null
	 * @param	sopClass	may be null
	 * @throws			DicomException
	 * @throws			NumberFormatException
	 * @deprecated
	 */
	public static void generateCommonAttributes(AttributeList list,String patientName,String patientID,String studyID,String seriesNumber,String instanceNumber,String modality,String sopClass) throws  DicomException {
		generateCommonAttributes(list,patientName,patientID,studyID,seriesNumber,instanceNumber,modality,sopClass,true/*generateUnassignedConverted*/);
	}
}


