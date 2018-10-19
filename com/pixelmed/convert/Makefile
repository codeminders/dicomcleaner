OBJS = \
	AnalyzeToDicom.class \
	AnalyzeHeader.class \
	AnalyzeException.class \
	CommonConvertedAttributeGeneration.class \
	ConvertAmicasJPEG2000FilesetToDicom.class \
	NIfTI1ToDicom.class \
	NIfTI1Header.class \
	NIfTI1Exception.class \
	NRRDToDicom.class \
	NRRDHeader.class \
	NRRDException.class \
	RawImageDescription.class \
	RawRGBInformation.class \
	RawRGBToDicomMultiFrame.class \
	RawToDicomMultiFrame.class \
	UnencapsulateCompressedPixelData.class \
	EncapsulateCompressedPixelData.class \
	IndexedLabelMapToSegmentation.class

all:	${OBJS}

PATHTOROOT = ../../..

include ${PATHTOROOT}/Makefile.common.mk

# override javac command in Makefile.common.mk to add javax.json-api-1.0.jar to classpath for RawImageDescription
.java.class:
	export JAVAVERSIONTARGETJARFILE=`/usr/libexec/java_home -v ${JAVAVERSIONTARGET} | tail -1`/jre/lib/rt.jar; javac ${JAVACOPTIONS} \
		-classpath ${PATHTOROOT}:${SLF4JCOMPILEADDITIONALJAR}:${DICOMADDITIONALJARS}:${VIEWERADDITIONALJARS}:${FTPADDITIONALJARS}:${JUNITJAR}:${PATHTOADDITIONAL}/javax.json-api-1.0.jar \
		-sourcepath ${PATHTOROOT} $<

testamicas:	ConvertAmicasJPEG2000FilesetToDicom.class
	mkdir -p /tmp/todicomMR1
	java -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar com.pixelmed.convert.ConvertAmicasJPEG2000FilesetToDicom "${PATHTOHOME}/Pictures/Medical/AmicasJ2K/ELMR20030206" /tmp/todicomMR1
	mkdir -p /tmp/todicomCT1
	java -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar com.pixelmed.convert.ConvertAmicasJPEG2000FilesetToDicom "${PATHTOHOME}/Pictures/Medical/AmicasJ2K/CD0105153529" /tmp/todicomCT1
	mkdir -p /tmp/todicomCT2
	java -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar com.pixelmed.convert.ConvertAmicasJPEG2000FilesetToDicom "${PATHTOHOME}/Pictures/Medical/AmicasJ2K/CD0114154520" /tmp/todicomCT2

testamicasdialog:	ConvertAmicasJPEG2000FilesetToDicom.class
	java -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar com.pixelmed.convert.ConvertAmicasJPEG2000FilesetToDicom


testanalyzeheader:	AnalyzeHeader.class AnalyzeException.class
	java -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.AnalyzeHeader \
		"/Volumes/Toshiba5TEnc/Pictures/Medical/Analyze/avg152T1_LR-marked/avg152T1_LR-marked.hdr"
		#"$${HOME}/Documents/Medical/stuff/medical.nema.org/MEDICAL/Dicom/DataSets/WG30/Mayo/MR/KlineErickson_MTexample_05192015/Analyze/Analyze75/MT_H13885_4.hdr"

testanalyzeconvert:	AnalyzeToDicom.class AnalyzeHeader.class AnalyzeException.class
	rm -f /tmp/avg152T1_LR-marked.dcm
	java -Xmx512m -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.AnalyzeToDicom \
		"/Volumes/Toshiba5TEnc/Pictures/Medical/Analyze/avg152T1_LR-marked/avg152T1_LR-marked.hdr" \
		"/tmp/avg152T1_LR-marked.dcm" \
		"H13885" H13885_9M "" 40001 1 MR
	if [ -f "/Volumes/Toshiba5TEnc/Pictures/Medical/Analyze/avg152T1_LR-marked/avg152T1_LR-marked.dcm" ]; \
	then \
		echo "Comparing header ..."; \
		dcdiff "/Volumes/Toshiba5TEnc/Pictures/Medical/Analyze/avg152T1_LR-marked/avg152T1_LR-marked.dcm" "/tmp/avg152T1_LR-marked.dcm"; \
		echo "Comparing pixel data ..."; \
		dccmp "/Volumes/Toshiba5TEnc/Pictures/Medical/Analyze/avg152T1_LR-marked/avg152T1_LR-marked.dcm" "/tmp/avg152T1_LR-marked.dcm"; \
		echo "Finished comparing"; \
	fi

testanalyzemtmouse:	AnalyzeToDicom.class AnalyzeHeader.class AnalyzeException.class
	rm -f /tmp/MT_H13885_4.dcm
	java -Xmx512m -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.AnalyzeToDicom \
		"$${HOME}/Documents/Medical/stuff/medical.nema.org/MEDICAL/Dicom/DataSets/WG30/Mayo/MR/KlineErickson_MTexample_05192015/Analyze/Analyze75/MT_H13885_4.hdr" \
		"/tmp/MT_H13885_4.dcm" \
		"H13885" H13885_9M "" 40001 1 MR
	if [ -f "$${HOME}/Documents/Medical/stuff/medical.nema.org/MEDICAL/Dicom/DataSets/WG30/Mayo/MR/KlineErickson_MTexample_05192015/Bruker/DICOM/EnhancedMR/H13885_9M_H13885_PKD__E4_P1/EnIm1.dcm" ]; \
	then \
		echo "Comparing header ..."; \
		dcdiff "$${HOME}/Documents/Medical/stuff/medical.nema.org/MEDICAL/Dicom/DataSets/WG30/Mayo/MR/KlineErickson_MTexample_05192015/Bruker/DICOM/EnhancedMR/H13885_9M_H13885_PKD__E4_P1/EnIm1.dcm" "/tmp/MT_H13885_4.dcm"; \
		echo "Comparing pixel data ..."; \
		dccmp "$${HOME}/Documents/Medical/stuff/medical.nema.org/MEDICAL/Dicom/DataSets/WG30/Mayo/MR/KlineErickson_MTexample_05192015/Bruker/DICOM/EnhancedMR/H13885_9M_H13885_PKD__E4_P1/EnIm1.dcm" "/tmp/MT_H13885_4.dcm"; \
		echo "Finished comparing"; \
	fi

testanalyzecompare:	testanalyzeconvert

testanalyzecreate:
	rm -f "/Volumes/Toshiba5TEnc/Pictures/Medical/Analyze/avg152T1_LR-marked/avg152T1_LR-marked.dcm"
	make testanalyzecompare
	cp /tmp/avg152T1_LR-marked.dcm "/Volumes/Toshiba5TEnc/Pictures/Medical/Analyze/avg152T1_LR-marked/avg152T1_LR-marked.dcm"


testnifti1header:	NIfTI1Header.class NIfTI1Exception.class
	java -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.NIfTI1Header \
		"$${HOME}/Pictures/Medical/ParametricMaps/DICOM_FP/GE_Normalized_Boxerman_rBFMap_LeakageCorrected.nii"
		#"$${HOME}/Pictures/Medical/ParametricMaps/DICOM_FP/dti_adc.nii"

testnifti1convert:	NIfTI1ToDicom.class NIfTI1Header.class NIfTI1Exception.class
	rm -f /tmp/testnifti1fp.dcm
	rm -f /tmp/testnifti1fp_scaledtoint.dcm
	rm -f /tmp/testnifti1fp_scaledtointthenmadefloatagain.dcm
	java -Xmx512m -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.NIfTI1ToDicom \
		"$${HOME}/Pictures/Medical/ParametricMaps/DICOM_FP/DICOM_FP/GE_Normalized_Boxerman_rBFMap_LeakageCorrected.nii" \
		/tmp/testnifti1fp.dcm \
		"Doe^Jane" 1234 5678 100 1
	java -Xmx512m -cp .:${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.apps.IntegerScalingOfFloatingPointPixelData \
		/tmp/testnifti1fp.dcm /tmp/testnifti1fp_scaledtoint.dcm
	java -Xmx512m -cp .:${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.apps.IntegerScalingOfFloatingPointPixelData \
		toFLOAT /tmp/testnifti1fp_scaledtoint.dcm /tmp/testnifti1fp_scaledtointthenmadefloatagain.dcm
	andump /tmp/testnifti1fp.dcm
	#dciodvfy /tmp/testnifti1fp.dcm
	andump /tmp/testnifti1fp_scaledtoint.dcm
	dciodvfy /tmp/testnifti1fp_scaledtoint.dcm
	#dchist -h /tmp/testnifti1fp_scaledtoint.dcm
	#andiff /tmp/testnifti1fp.dcm /tmp/testnifti1fp_scaledtoint.dcm
	@echo "Checking round trip to int and back to float ..."
	#dccmp /tmp/testnifti1fp.dcm /tmp/testnifti1fp_scaledtointthenmadefloatagain.dcm
	andiff /tmp/testnifti1fp.dcm /tmp/testnifti1fp_scaledtointthenmadefloatagain.dcm

testnifti1talairach:	NIfTI1ToDicom.class NIfTI1Header.class NIfTI1Exception.class
	rm -f "$${HOME}/Documents/Medical/stuff/Atlases/Talairach/talairach.dcm"
	java -Xmx512m -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.NIfTI1ToDicom \
		"$${HOME}/Documents/Medical/stuff/Atlases/Talairach/talairach.nii" \
		"$${HOME}/Documents/Medical/stuff/Atlases/Talairach/talairach.dcm" \
		"Talairach" 1234 5678 100 1 MR

testnifti1dti:	NIfTI1ToDicom.class NIfTI1Header.class NIfTI1Exception.class
	rm -f /tmp/dti20.dcm
	java -Xmx512m -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.NIfTI1ToDicom \
		"/Volumes/Toshiba5TEnc/Pictures/Medical/dcm2nii_TestDataSets/dti20/dti20.nii" \
		"/tmp/dti20.dcm" \
		"DTI" 1234 5678 100 1 MR "1.2.840.10008.5.1.4.1.1.4.4"
	if [ -f "/Volumes/Toshiba5TEnc/Pictures/Medical/dcm2nii_TestDataSets/dti20.dcm" ]; \
	then \
		echo "Comparing header ..."; \
		dcdiff "/Volumes/Toshiba5TEnc/Pictures/Medical/dcm2nii_TestDataSets/dti20.dcm" "/tmp/dti20.dcm"; \
		echo "Comparing pixel data ..."; \
		dccmp "/Volumes/Toshiba5TEnc/Pictures/Medical/dcm2nii_TestDataSets/dti20.dcm" "/tmp/dti20.dcm"; \
		echo "Finished comparing"; \
	fi

testnifti1test4d1:	NIfTI1ToDicom.class NIfTI1Header.class NIfTI1Exception.class
	rm -f "/tmp/filtered_func_data.dcm"
	java -Xmx512m -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.NIfTI1ToDicom \
		"/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/filtered_func_data.nii" \
		"/tmp/filtered_func_data.dcm" \
		"4D" 1234 5678 100 1 MR "1.2.840.10008.5.1.4.1.1.4.4"
	if [ -f "/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/filtered_func_data.dcm" ]; \
	then \
		echo "Comparing header ..."; \
		dcdiff "/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/filtered_func_data.dcm" "/tmp/filtered_func_data.dcm"; \
		echo "Comparing pixel data ..."; \
		dccmp "/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/filtered_func_data.dcm" "/tmp/filtered_func_data.dcm"; \
		echo "Finished comparing"; \
	fi

testnifti1testLR:	NIfTI1ToDicom.class NIfTI1Header.class NIfTI1Exception.class
	rm -f "/tmp/avg152T1_LR_nifti.dcm"
	java -Xmx512m -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.NIfTI1ToDicom \
		"/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/avg152T1_LR_nifti.nii" \
		"/tmp/avg152T1_LR_nifti.dcm" \
		"LR" 1234 5678 100 1 MR "1.2.840.10008.5.1.4.1.1.4.4"
	if [ -f "/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/avg152T1_LR_nifti.dcm" ]; \
	then \
		echo "Comparing header ..."; \
		dcdiff "/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/avg152T1_LR_nifti.dcm" "/tmp/avg152T1_LR_nifti.dcm"; \
		echo "Comparing pixel data ..."; \
		dccmp "/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/avg152T1_LR_nifti.dcm" "/tmp/avg152T1_LR_nifti.dcm"; \
		echo "Finished comparing"; \
	fi

testnifti1testLR_hdr:	NIfTI1ToDicom.class NIfTI1Header.class NIfTI1Exception.class
	rm -f "/tmp/avg152T1_LR_nifti.dcm"
	java -Xmx512m -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.NIfTI1ToDicom \
		"/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/avg152T1_LR_nifti.hdr" \
		"/tmp/avg152T1_LR_nifti.dcm" \
		"LR" 1234 5678 100 1 MR "1.2.840.10008.5.1.4.1.1.4.4"
	if [ -f "/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/avg152T1_LR_nifti.dcm" ]; \
	then \
		echo "Comparing header ..."; \
		dcdiff "/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/avg152T1_LR_nifti.dcm" "/tmp/avg152T1_LR_nifti.dcm"; \
		echo "Comparing pixel data ..."; \
		dccmp "/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/avg152T1_LR_nifti.dcm" "/tmp/avg152T1_LR_nifti.dcm"; \
		echo "Finished comparing"; \
	fi

testnifti1testRL:	NIfTI1ToDicom.class NIfTI1Header.class NIfTI1Exception.class
	rm -f "/tmp/avg152T1_RL_nifti.dcm"
	java -Xmx512m -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.NIfTI1ToDicom \
		"/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/avg152T1_RL_nifti.nii" \
		"/tmp/avg152T1_RL_nifti.dcm" \
		"RL" 1234 5678 100 1 MR "1.2.840.10008.5.1.4.1.1.4.4"
	if [ -f "/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/avg152T1_RL_nifti.dcm" ]; \
	then \
		echo "Comparing header ..."; \
		dcdiff "/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/avg152T1_RL_nifti.dcm" "/tmp/avg152T1_RL_nifti.dcm"; \
		echo "Comparing pixel data ..."; \
		dccmp "/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/avg152T1_RL_nifti.dcm" "/tmp/avg152T1_RL_nifti.dcm"; \
		echo "Finished comparing"; \
	fi

testnifti1testZ:	NIfTI1ToDicom.class NIfTI1Header.class NIfTI1Exception.class
	rm -f "/tmp/zstat1.dcm"
	java -Xmx512m -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.NIfTI1ToDicom \
		"/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/zstat1.nii" \
		"/tmp/zstat1.dcm" \
		"RL" 1234 5678 100 1 MR "1.2.840.10008.5.1.4.1.1.4.4"
	if [ -f "/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/zstat1.dcm" ]; \
	then \
		echo "Comparing header ..."; \
		dcdiff "/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/zstat1.dcm" "/tmp/zstat1.dcm"; \
		echo "Comparing pixel data ..."; \
		dccmp "/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/zstat1.dcm" "/tmp/zstat1.dcm"; \
		echo "Finished comparing"; \
	fi

testnifti1testfunc:	NIfTI1ToDicom.class NIfTI1Header.class NIfTI1Exception.class
	rm -f "/tmp/newsirp_final_XML.dcm"
	java -Xmx512m -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.NIfTI1ToDicom \
		"/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/sirp_fmri_study_ver4/newsirp_final_XML.nii" \
		"/tmp/newsirp_final_XML.dcm" \
		"RL" 1234 5678 100 1 MR "1.2.840.10008.5.1.4.1.1.4.4"
	if [ -f "/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/sirp_fmri_study_ver4/newsirp_final_XML.dcm" ]; \
	then \
		echo "Comparing header ..."; \
		dcdiff "/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/sirp_fmri_study_ver4/newsirp_final_XML.dcm" "/tmp/newsirp_final_XML.dcm"; \
		echo "Comparing pixel data ..."; \
		dccmp "/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/sirp_fmri_study_ver4/newsirp_final_XML.dcm" "/tmp/newsirp_final_XML.dcm"; \
		echo "Finished comparing"; \
	fi

testnifti1brainlab:	NIfTI1ToDicom.class NIfTI1Header.class NIfTI1Exception.class
	rm -f "/tmp/1010_brain_mr_06_pixelmed.dcm"
	java -Xmx512m -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.NIfTI1ToDicom \
		"/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/BrainLabQuaternionTest/1010_brain_mr_06.nii" \
		"/tmp/1010_brain_mr_06_pixelmed.dcm" \
		"BrainLab^Quaternion" 1234 5678 100 1 MR "1.2.840.10008.5.1.4.1.1.4.4"
	if [ -f "/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/BrainLabQuaternionTest/1010_brain_mr_06_pixelmed.dcm" ]; \
	then \
		echo "Comparing header ..."; \
		dcdiff "/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/BrainLabQuaternionTest/1010_brain_mr_06_pixelmed.dcm" "/tmp/1010_brain_mr_06_pixelmed.dcm"; \
		echo "Comparing pixel data ..."; \
		dccmp "/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/BrainLabQuaternionTest/1010_brain_mr_06_pixelmed.dcm" "/tmp/1010_brain_mr_06_pixelmed.dcm"; \
		echo "Finished comparing"; \
	fi

testnifti1compare:	testnifti1dti testnifti1test4d1 testnifti1testLR testnifti1testRL testnifti1testZ testnifti1testfunc testnifti1brainlab

testnifti1create:
	rm -f "/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/sirp_fmri_study_ver4/newsirp_final_XML.dcm"
	rm -f "/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/zstat1.dcm"
	rm -f "/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/avg152T1_RL_nifti.dcm"
	rm -f "/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/avg152T1_LR_nifti.dcm"
	rm -f "/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/filtered_func_data.dcm"
	rm -f "/Volumes/Toshiba5TEnc/Pictures/Medical/dcm2nii_TestDataSets/dti20.dcm"
	rm -f "/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/BrainLabQuaternionTest/1010_brain_mr_06_pixelmed.dcm"
	make testnifti1compare
	cp /tmp/newsirp_final_XML.dcm "/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/sirp_fmri_study_ver4/newsirp_final_XML.dcm"
	cp /tmp/zstat1.dcm "/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/zstat1.dcm"
	cp /tmp/avg152T1_RL_nifti.dcm "/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/avg152T1_RL_nifti.dcm"
	cp /tmp/avg152T1_LR_nifti.dcm "/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/avg152T1_LR_nifti.dcm"
	cp /tmp/filtered_func_data.dcm "/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/filtered_func_data.dcm"
	cp /tmp/dti20.dcm "/Volumes/Toshiba5TEnc/Pictures/Medical/dcm2nii_TestDataSets/dti20.dcm"
	cp /tmp/1010_brain_mr_06_pixelmed.dcm "/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/BrainLabQuaternionTest/1010_brain_mr_06_pixelmed.dcm"

testnrrdheaderseparate:	NRRDHeader.class NRRDException.class
	java -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.NRRDHeader \
		"$${HOME}/Pictures/Medical/ParametricMaps/PkModeling_verification/PCAMPMRI-0241/SlicerScene/MRMLMultiVolume.nhdr"

testnrrdheadercombined:	NRRDHeader.class NRRDException.class
	java -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.NRRDHeader \
		"$${HOME}/Pictures/Medical/ParametricMaps/PkModeling_verification/PCAMPMRI-0241/SlicerScene/MRMLMultiVolume frame 21.nrrd"

testnrrdconvertseparate:	NRRDToDicom.class NRRDHeader.class NRRDException.class
	rm -f /tmp/testnrrdfp.dcm
	rm -f /tmp/testnrrdfp_scaledtoint.dcm
	rm -f /tmp/testnrrdfp_scaledtointthenmadefloatagain.dcm
	java -Xmx2g -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.NRRDToDicom \
		"$${HOME}/Pictures/Medical/ParametricMaps/PkModeling_verification/PCAMPMRI-0241/SlicerScene/MRMLMultiVolume.nhdr" \
		/tmp/testnrrdfp.dcm \
		"Doe^Jane" 1234 5678 100 1
	java -Xmx1g -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.apps.IntegerScalingOfFloatingPointPixelData \
		/tmp/testnrrdfp.dcm /tmp/testnrrdfp_scaledtoint.dcm
	java -Xmx1g -cp .:${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.apps.IntegerScalingOfFloatingPointPixelData \
		toDOUBLE /tmp/testnrrdfp_scaledtoint.dcm /tmp/testnrrdfp_scaledtointthenmadefloatagain.dcm
	#andump /tmp/testnrrdfp.dcm
	#dciodvfy /tmp/testnrrdfp.dcm
	andump /tmp/testnrrdfp_scaledtoint.dcm
	dciodvfy /tmp/testnrrdfp_scaledtoint.dcm
	#dchist -h /tmp/testnrrdfp_scaledtoint.dcm
	#andiff /tmp/testnrrdfp.dcm /tmp/testnrrdfp_scaledtoint.dcm
	#@echo "Checking round trip to int and back to float ..."
	#dccmp /tmp/testnrrdfp.dcm /tmp/testnrrdfp_scaledtointthenmadefloatagain.dcm		#dccmp fails (hangs for ever during dctoraw ... ? why :()
	andiff /tmp/testnrrdfp.dcm /tmp/testnrrdfp_scaledtointthenmadefloatagain.dcm

testnrrdconvertcombined:	NRRDToDicom.class NRRDHeader.class NRRDException.class
	rm -f /tmp/testnrrdfp.dcm
	rm -rf /tmp/testnrrdfp_withrwvm
	mkdir -p /tmp/testnrrdfp_withrwvm
	rm -f /tmp/testnrrdfp_scaledtoint.dcm
	rm -f /tmp/testnrrdfp_scaledtointthenmadefloatagain.dcm
	java -Xmx1g -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.NRRDToDicom \
		"$${HOME}/Pictures/Medical/ParametricMaps/PkModeling_verification/PCAMPMRI-0241/SlicerScene/OncoQuant-AutoAIF-Results/PCAMPMRI-0241_GE-Ktrans.nrrd" \
		/tmp/testnrrdfp_norwvm.dcm \
		"Doe^Jane" 1234 5678 100 1 \
		MR
	#"$${HOME}/Pictures/Medical/ParametricMaps/PkModeling_verification/PCAMPMRI-0241/SlicerScene/MRMLMultiVolume frame 21.nrrd"
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} \
		com.pixelmed.apps.InsertRealWorldValueMap \
			0 5 0 1 "Ktrans" "Ktrans" \
			"/min" "UCUM" "" "/min" \
			CODE "(G-C1C6, SRT, Quantity)" "(126312, DCM, Ktrans)" \
			CODE '(G-C036, SRT, "Measurement Method")' '(126340, DCM, "Standard Tofts Model")' \
			/tmp/testnrrdfp_norwvm.dcm \
			/tmp/testnrrdfp_withrwvm
	@echo "Comparing before and after adding RWVM ..."
	andiff /tmp/testnrrdfp_withrwvm/* /tmp/testnrrdfp_norwvm.dcm
	java -Xmx1g -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.apps.IntegerScalingOfFloatingPointPixelData \
		/tmp/testnrrdfp_withrwvm/* /tmp/testnrrdfp_scaledtoint.dcm
	java -Xmx1g -cp .:${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.apps.IntegerScalingOfFloatingPointPixelData \
		toFLOAT /tmp/testnrrdfp_scaledtoint.dcm /tmp/testnrrdfp_scaledtointthenmadefloatagain.dcm
	#andump /tmp/testnrrdfp_withrwvm/*
	#dciodvfy /tmp/testnrrdfp_withrwvm/*
	andump /tmp/testnrrdfp_scaledtoint.dcm
	dciodvfy /tmp/testnrrdfp_scaledtoint.dcm
	#dchist -h /tmp/testnrrdfp_scaledtoint.dcm
	#andiff /tmp/testnrrdfp.dcm /tmp/testnrrdfp_scaledtoint.dcm
	@echo "Checking round trip to int and back to float ..."
	#dccmp /tmp/testnrrdfp_withrwvm/* /tmp/testnrrdfp_scaledtointthenmadefloatagain.dcm
	andiff /tmp/testnrrdfp_withrwvm/* /tmp/testnrrdfp_scaledtointthenmadefloatagain.dcm
	echo  >/tmp/testsetcharacteristics.json "{"
	echo >>/tmp/testsetcharacteristics.json "	\"top\" : {"
	echo >>/tmp/testsetcharacteristics.json "		\"ImageType\" : [ \"DERIVED\", \"PRIMARY\", \"PERFUSION\", \"KTRANS\" ]"
	echo >>/tmp/testsetcharacteristics.json "	},"
	echo >>/tmp/testsetcharacteristics.json "	\"FrameAnatomySequence\" : {"
	echo >>/tmp/testsetcharacteristics.json "		\"AnatomicRegionSequence\" : { \"cv\" : \"T-9200B\", \"csd\" : \"SRT\", \"cm\" : \"Prostate\" },"
	echo >>/tmp/testsetcharacteristics.json "		\"FrameLaterality\" : \"U\""
	echo >>/tmp/testsetcharacteristics.json "	},"
	echo >>/tmp/testsetcharacteristics.json "	\"ParametricMapFrameTypeSequence\" : {"
	echo >>/tmp/testsetcharacteristics.json "		\"FrameType\" : [ \"DERIVED\", \"PRIMARY\", \"PERFUSION\", \"KTRANS\" ]"
	echo >>/tmp/testsetcharacteristics.json "	}"
	echo >>/tmp/testsetcharacteristics.json "}"
	cat -n /tmp/testsetcharacteristics.json
	rm -rf /tmp/testnrrdfp_withrwvmandcharacteristics
	mkdir /tmp/testnrrdfp_withrwvmandcharacteristics
	rm -f /tmp/testsetcharacteristics.log
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS}:${PATHTOADDITIONAL}/javax.json-1.0.4.jar \
		com.pixelmed.apps.SetCharacteristicsFromSummary \
			/tmp/testsetcharacteristics.json \
			/tmp/testnrrdfp_withrwvm/* \
			/tmp/testnrrdfp_withrwvmandcharacteristics \
		2>/tmp/testsetcharacteristics.log
	cat /tmp/testsetcharacteristics.log
	rm -f /tmp/filetouse.dat
	grep </tmp/testsetcharacteristics.log 'Writing with characteristics set file' | tail -1 | sed -e 's/^.*\/tmp/\/tmp/' >/tmp/filetouse.dat
	@echo "check result with dciodvfy ..."
	dciodvfy "`cat /tmp/filetouse.dat`"
	@echo "test that dciodvfy does not require PatientOrientation ..."
	rm -f /tmp/testnrrdfpwithoutpatientorientation.dcm
	dccp -nodisclaimer -noadddicom -d PatientOrientation "`cat /tmp/filetouse.dat`" /tmp/testnrrdfpwithoutpatientorientation.dcm
	dciodvfy /tmp/testnrrdfpwithoutpatientorientation.dcm
	rm /tmp/testnrrdfpwithoutpatientorientation.dcm

testnrrdheaderseparate:	NRRDHeader.class NRRDException.class
	java -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.NRRDHeader \
		"$${HOME}/Pictures/Medical/ParametricMaps/PkModeling_verification/PCAMPMRI-0241/SlicerScene/MRMLMultiVolume.nhdr"

testrawrgbheader:	RawRGBInformation.class
	if [ ! -f "/tmp/vhm/testfiles/README" ]; \
	then \
		mkdir -p "/tmp/vhm/testfiles"; \
		cp "$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/70mm/README" "/tmp/vhm/testfiles/README"; \
	fi
	java -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.RawRGBInformation \
		"/tmp/vhm/testfiles/README"

testrawrgbvhmuncompressed:	RawRGBToDicomMultiFrame.class RawRGBInformation.class
	rm -rf "/tmp/vhm/testfiles"
	mkdir -p "/tmp/vhm/testfiles/srcimages/uncompressed"
	mkdir -p "/tmp/vhm/testfiles/dstimages/testrawrgbvhm"
	cp "$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/70mm/README" "/tmp/vhm/testfiles/"
	gzcat <"$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/70mm/fullbody/2000.rgb.gz" >"/tmp/vhm/testfiles/srcimages/uncompressed/2000.rgb"
	gzcat <"$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/70mm/fullbody/2001.rgb.gz" >"/tmp/vhm/testfiles/srcimages/uncompressed/2001.rgb"
	gzcat <"$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/70mm/fullbody/2002.rgb.gz" >"/tmp/vhm/testfiles/srcimages/uncompressed/2002.rgb"
	java -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.RawRGBToDicomMultiFrame \
		"/tmp/vhm/testfiles/README" \
		"/tmp/vhm/testfiles/srcimages/uncompressed" \
		".rgb" \
		"/tmp/vhm/testfiles/dstimages/testrawrgbvhm" \
		"Doe^John" 1234 5678 100

testrawrgbvhmcompressed:	RawRGBToDicomMultiFrame.class RawRGBInformation.class
	rm -rf "/tmp/vhm/testfiles"
	mkdir -p "/tmp/vhm/testfiles/srcimages/compressed"
	mkdir -p "/tmp/vhm/testfiles/dstimages/testrawrgbvhm"
	cp "$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/70mm/README" "/tmp/vhm/testfiles/"
	cp "$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/70mm/fullbody/2000.rgb.gz" "/tmp/vhm/testfiles/srcimages/compressed/"
	cp "$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/70mm/fullbody/2001.rgb.gz" "/tmp/vhm/testfiles/srcimages/compressed/"
	cp "$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/70mm/fullbody/2002.rgb.gz" "/tmp/vhm/testfiles/srcimages/compressed/"
	java -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.RawRGBToDicomMultiFrame \
		"/tmp/vhm/testfiles/README" \
		"/tmp/vhm/testfiles/srcimages/compressed" \
		".rgb.gz" \
		"/tmp/vhm/testfiles/dstimages/testrawrgbvhm" \
		"Doe^John" 1234 5678 100

testrawrgbvhmuncompressedsmall:	RawRGBToDicomMultiFrame.class RawRGBInformation.class
	rm -rf "/tmp/vhm/testfiles"
	mkdir -p "/tmp/vhm/testfiles/srcimages/uncompressed"
	mkdir -p "/tmp/vhm/testfiles/dstimages/testrawrgbvhm"
	cp "$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/Fullcolor/README" "/tmp/vhm/testfiles/"
	uncompress <"$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/Fullcolor/abdomen/a_vm1455.raw.Z" >"/tmp/vhm/testfiles/srcimages/uncompressed/a_vm1455.raw"
	uncompress <"$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/Fullcolor/abdomen/a_vm1456.raw.Z" >"/tmp/vhm/testfiles/srcimages/uncompressed/a_vm1456.raw"
	uncompress <"$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/Fullcolor/abdomen/a_vm1457.raw.Z" >"/tmp/vhm/testfiles/srcimages/uncompressed/a_vm1457.raw"
	java -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.RawRGBToDicomMultiFrame \
		"/tmp/vhm/testfiles/README" \
		"/tmp/vhm/testfiles/srcimages/uncompressed" \
		".raw" \
		"/tmp/vhm/testfiles/dstimages/testrawrgbvhm" \
		"Doe^John" 1234 5678 100

testrawrgbvhmunixcompressed:	RawRGBToDicomMultiFrame.class RawRGBInformation.class
	rm -rf "/tmp/vhm/testfiles"
	mkdir -p "/tmp/vhm/testfiles/srcimages/compressed"
	mkdir -p "/tmp/vhm/testfiles/dstimages/testrawrgbvhm"
	cp "$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/Fullcolor/README" "/tmp/vhm/testfiles/"
	cp "$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/Fullcolor/abdomen/a_vm1455.raw.Z" "/tmp/vhm/testfiles/srcimages/compressed/"
	cp "$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/Fullcolor/abdomen/a_vm1456.raw.Z" "/tmp/vhm/testfiles/srcimages/compressed/"
	cp "$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/Fullcolor/abdomen/a_vm1457.raw.Z" "/tmp/vhm/testfiles/srcimages/compressed/"
	# BZIP2ADDITIONALJAR is now commons-compress, which contains Unix Z decompressor ...
	java -cp ${PATHTOROOT}:${BZIP2ADDITIONALJAR} -Djava.awt.headless=true \
		com.pixelmed.convert.RawRGBToDicomMultiFrame \
		"/tmp/vhm/testfiles/README" \
		"/tmp/vhm/testfiles/srcimages/compressed" \
		".raw.Z" \
		"/tmp/vhm/testfiles/dstimages/testrawrgbvhm" \
		"Doe^John" 1234 5678 100

testrawrgbvhmbzip2:	RawRGBToDicomMultiFrame.class RawRGBInformation.class
	rm -rf "/tmp/vhm/testfiles"
	mkdir -p "/tmp/vhm/testfiles/srcimages/uncompressed"
	mkdir -p "/tmp/vhm/testfiles/dstimages/testrawrgbvhm"
	cp "$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/Fullcolor/README" "/tmp/vhm/testfiles/"
	uncompress <"$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/Fullcolor/abdomen/a_vm1455.raw.Z" >"/tmp/vhm/testfiles/srcimages/uncompressed/a_vm1455.raw"
	bzip2 "/tmp/vhm/testfiles/srcimages/uncompressed/a_vm1455.raw"
	uncompress <"$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/Fullcolor/abdomen/a_vm1456.raw.Z" >"/tmp/vhm/testfiles/srcimages/uncompressed/a_vm1456.raw"
	bzip2 "/tmp/vhm/testfiles/srcimages/uncompressed/a_vm1456.raw"
	uncompress <"$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/Fullcolor/abdomen/a_vm1457.raw.Z" >"/tmp/vhm/testfiles/srcimages/uncompressed/a_vm1457.raw"
	bzip2 "/tmp/vhm/testfiles/srcimages/uncompressed/a_vm1457.raw"
	java -cp ${PATHTOROOT}:${BZIP2ADDITIONALJAR} -Djava.awt.headless=true \
		com.pixelmed.convert.RawRGBToDicomMultiFrame \
		"/tmp/vhm/testfiles/README" \
		"/tmp/vhm/testfiles/srcimages/uncompressed" \
		".raw.bz2" \
		"/tmp/vhm/testfiles/dstimages/testrawrgbvhm" \
		"Doe^John" 1234 5678 100

testrawrgbvhmentiremaleabdomensmall:	RawRGBToDicomMultiFrame.class RawRGBInformation.class
	rm -rf "/tmp/vhm/testfiles"
	mkdir -p "/tmp/vhm/testfiles/dstimages/testrawrgbvhm"
	java -cp ${PATHTOROOT}:${BZIP2ADDITIONALJAR} -Djava.awt.headless=true \
		com.pixelmed.convert.RawRGBToDicomMultiFrame \
		"$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/Fullcolor/README" \
		"$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/Fullcolor/abdomen" \
		".raw.Z" \
		"/tmp/vhm/testfiles/dstimages/testrawrgbvhm" \
		"Doe^John" 1234 5678 100

testrawrgbvhmsubsetmaleabdomensmall:	RawRGBToDicomMultiFrame.class RawRGBInformation.class
	rm -rf "/tmp/vhm/testfiles"
	mkdir -p "/tmp/vhm/testfiles/srcimages/compressed"
	mkdir -p "/tmp/vhm/testfiles/dstimages/testrawrgbvhm"
	for i in \
		$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/Fullcolor/abdomen/a_vm14[0-9][0-9].raw.Z \
		$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/Fullcolor/abdomen/a_vm15[0-9][0-9].raw.Z \
		$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/Fullcolor/abdomen/a_vm16[0-9][0-9].raw.Z \
		; do cp $$i /tmp/vhm/testfiles/srcimages/compressed/; done
	java -cp ${PATHTOROOT}:${BZIP2ADDITIONALJAR} -Djava.awt.headless=true \
		com.pixelmed.convert.RawRGBToDicomMultiFrame \
		"$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/Fullcolor/README" \
		"/tmp/vhm/testfiles/srcimages/compressed" \
		".raw.Z" \
		"/tmp/vhm/testfiles/dstimages/testrawrgbvhm" \
		"Doe^John" 1234 5678 100


testunencap:	UnencapsulateCompressedPixelData.class
	mkdir -p /tmp/crapunencap
	rm -f /tmp/crapunencap/*
	java -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.UnencapsulateCompressedPixelData \
			"$${HOME}/Pictures/Medical/MultiframeEncapsulated/JPEGBaseline/Acuson/ACUSON-24-YBR_FULL_422-Jpeg_Baseline_1.dcm" \
			/tmp/crapunencap/frame_

testreencap:	UnencapsulateCompressedPixelData.class EncapsulateCompressedPixelData.class
	cp "$${HOME}/Pictures/Medical/MultiframeEncapsulated/JPEGBaseline/Acuson/ACUSON-24-YBR_FULL_422-Jpeg_Baseline_1.dcm" /tmp/srcfile.dcm
	mkdir -p /tmp/crapunencap
	rm -f /tmp/crapunencap/*
	java -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.UnencapsulateCompressedPixelData \
			/tmp/srcfile.dcm \
			/tmp/crapunencap/frame_
	rm -f /tmp/crapreencap.dcm
	java -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.EncapsulateCompressedPixelData \
			/tmp/crapreencap.dcm \
			/tmp/srcfile.dcm \
			/tmp/crapunencap/frame_*
	#rm -f /tmp/srcfile.dcm

/tmp/grayscale2008.dcm:
	java -Xmx1g -cp ${PATHTOROOT} -Djava.awt.headless=true com.pixelmed.convert.NRRDToDicom \
		"${HOME}/Documents/Work/NCI/Slicer/BrainAtlas/2008/SPL_PNL_Brain_Atlas2008/01-MRI/grayscale.nrrd" \
		/tmp/grayscale2008.dcm \
		"Brain^Atlas SPL-PNL 2008" 2008 5678 100 1 \
		MR

/tmp/labelmap2008.dcm:
	java -Xmx1g -cp ${PATHTOROOT} -Djava.awt.headless=true com.pixelmed.convert.NRRDToDicom \
		"${HOME}/Documents/Work/NCI/Slicer/BrainAtlas/2008/SPL_PNL_Brain_Atlas2008/02-labels/seg.nrrd" \
		/tmp/labelmap2008.dcm \
		"Brain^Atlas SPL-PNL 2008" 2008 5678 101 1

# use 2007 label map since none for 2008 ... some values are missing (and some in map are unused, mostly duplicates, but also pituitary)
testlabelmapbrainatlas2008:	IndexedLabelMapToSegmentation.class /tmp/labelmap2008.dcm /tmp/grayscale2008.dcm
	java -Xms6g -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.IndexedLabelMapToSegmentation \
		/tmp/labelmap2008.dcm \
		"${HOME}/Documents/Work/NCI/Slicer/BrainAtlas/2007/SPL-BrainAtlas-label_list_fields_RGB.csv" \
		/tmp/seg2008.dcm \
		/tmp/grayscale2008.dcm \
		102 "Segmented Brain" BRAIN_ATLAS_2008 "SPL-PNL Brain Atlas 2008" "SPL-PNL^Talos et al"
	dcentvfy /tmp/seg2008.dcm /tmp/grayscale2008.dcm



/tmp/grayscale2013_10.dcm:
	java -Xmx1g -cp ${PATHTOROOT} -Djava.awt.headless=true com.pixelmed.convert.NRRDToDicom \
		"${HOME}/Documents/Work/NCI/Slicer/BrainAtlas/2013/nac-brain-atlas-1.0/volumes/A1_grayT1.nrrd" \
		/tmp/grayscale2013_10.dcm \
		"Brain^Atlas NAC 2013" 2013 5678 100 1 \
		MR

/tmp/labelmap2013_10.dcm:
	java -Xmx1g -cp ${PATHTOROOT} -Djava.awt.headless=true com.pixelmed.convert.NRRDToDicom \
		"${HOME}/Documents/Work/NCI/Slicer/BrainAtlas/2013/nac-brain-atlas-1.0/volumes/A1_labels.nrrd" \
		/tmp/labelmap2013_10.dcm \
		"Brain^Atlas NAC 2013" 2013 5678 101 1

testlabelmapbrainatlas2013_10:	IndexedLabelMapToSegmentation.class /tmp/labelmap2013_10.dcm /tmp/grayscale2013_10.dcm
	java -Xms6g -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.IndexedLabelMapToSegmentation \
		/tmp/labelmap2013_10.dcm \
		"${HOME}/Documents/Work/NCI/Slicer/BrainAtlas/2013/BrainAtlas-label_list_fields_RGB.csv" \
		/tmp/seg2013_10.dcm \
		/tmp/grayscale2013_10.dcm \
		102 "Segmented Brain" BRAIN_ATLAS_2013 "NAC Brain Atlas 2013" "NAC^Halle et al"
	dcentvfy /tmp/seg2013_10.dcm /tmp/grayscale2013_10.dcm



/tmp/grayscale2013_nac_hncma.dcm:
	java -Xmx1g -cp ${PATHTOROOT} -Djava.awt.headless=true com.pixelmed.convert.NRRDToDicom \
		"${HOME}/Documents/Work/NCI/Slicer/BrainAtlas/2013/nac-hncma-atlas2013-Slicer4Version/Data/A1_grayT1.nrrd" \
		/tmp/grayscale2013_nac_hncma_fp.dcm \
		"Brain^Atlas NAC HNCMA 2013" 2013 5678 100 1 \
		MR
	echo  >/tmp/testsetcharacteristics.json "{"
	echo >>/tmp/testsetcharacteristics.json "	\"top\" : {"
	echo >>/tmp/testsetcharacteristics.json "		\"ImageType\" : [ \"DERIVED\", \"PRIMARY\", \"PRE_CONTRAST\", \"NONE\" ]"
	echo >>/tmp/testsetcharacteristics.json "	},"
	echo >>/tmp/testsetcharacteristics.json "	\"FrameAnatomySequence\" : {"
	echo >>/tmp/testsetcharacteristics.json "		\"AnatomicRegionSequence\" : { \"cv\" : \"T-A0100\", \"csd\" : \"SRT\", \"cm\" : \"Brain\" },"
	echo >>/tmp/testsetcharacteristics.json "		\"FrameLaterality\" : \"B\""
	echo >>/tmp/testsetcharacteristics.json "	},"
	echo >>/tmp/testsetcharacteristics.json "	\"RealWorldValueMappingSequence\" : {"
	echo >>/tmp/testsetcharacteristics.json "		\"DoubleFloatRealWorldValueFirstValueMapped\" : \"0\","
	echo >>/tmp/testsetcharacteristics.json "		\"DoubleFloatRealWorldValueLastValueMapped\" : \"32767\","
	echo >>/tmp/testsetcharacteristics.json "		\"RealWorldValueIntercept\" : \"0\","
	echo >>/tmp/testsetcharacteristics.json "		\"RealWorldValueSlope\" : \"1\","
	echo >>/tmp/testsetcharacteristics.json "		\"LUTExplanation\" : \"Unitless\","
	echo >>/tmp/testsetcharacteristics.json "		\"LUTLabel\" : \"US\","
	echo >>/tmp/testsetcharacteristics.json "		\"MeasurementUnitsCodeSequence\" : { \"cv\" : \"1\", \"csd\" : \"UCUM\", \"cm\" : \"1\" }"
	echo >>/tmp/testsetcharacteristics.json "	},"
	echo >>/tmp/testsetcharacteristics.json "	\"ParametricMapFrameTypeSequence\" : {"
	echo >>/tmp/testsetcharacteristics.json "		\"FrameType\" : [ \"DERIVED\", \"PRIMARY\", \"PRE_CONTRAST\", \"NONE\" ]"
	echo >>/tmp/testsetcharacteristics.json "	}"
	echo >>/tmp/testsetcharacteristics.json "}"
	cat /tmp/testsetcharacteristics.json
	mkdir -p /tmp/withcharacteristics
	rm -rf /tmp/withcharacteristics/*
	java -Xmx512m -cp .:${PATHTOROOT}:${DICOMADDITIONALJARS}:${PATHTOADDITIONAL}/javax.json-1.0.4.jar \
		com.pixelmed.apps.SetCharacteristicsFromSummary \
			/tmp/testsetcharacteristics.json \
			/tmp/grayscale2013_nac_hncma_fp.dcm \
			/tmp/withcharacteristics \
		2>/tmp/setcharacteristics.log
	rm -f /tmp/filetouse.dat
	grep </tmp/setcharacteristics.log 'Writing with characteristics set file' | tail -1 | sed -e 's/^.*\/tmp/\/tmp/' >/tmp/filetouse.dat
	#@echo "Comparing header before and after characteristics added ..."
	#dcdiff /tmp/grayscale2013_nac_hncma_fp.dcm "`cat /tmp/filetouse.dat`"
	rm -f /tmp/grayscale2013_nac_hncma_fp.dcm
	mv "`cat /tmp/filetouse.dat`" /tmp/grayscale2013_nac_hncma_fp.dcm
	rm -rf /tmp/withcharacteristics
	rm /tmp/testsetcharacteristics.json
	rm /tmp/filetouse.dat
	rm /tmp/setcharacteristics.log
	# toIntTruncate produces spotty result :(
	java -Xmx512m -cp .:${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.apps.IntegerScalingOfFloatingPointPixelData \
		toInt \
		/tmp/grayscale2013_nac_hncma_fp.dcm /tmp/grayscale2013_nac_hncma.dcm
	echo  >/tmp/testsetcharacteristics.json "{"
	echo >>/tmp/testsetcharacteristics.json "	\"remove\" : {"
	echo >>/tmp/testsetcharacteristics.json "		\"PatientOrientation\" : null,"
	echo >>/tmp/testsetcharacteristics.json "		\"ContentLabel\" : null,"
	echo >>/tmp/testsetcharacteristics.json "		\"ContentDescription\" : null,"
	echo >>/tmp/testsetcharacteristics.json "		\"ContentCreatorName\" : null"
	echo >>/tmp/testsetcharacteristics.json "	},"
	echo >>/tmp/testsetcharacteristics.json "	\"removeall\" : {"
	echo >>/tmp/testsetcharacteristics.json "		\"ParametricMapFrameTypeSequence\" : null"
	echo >>/tmp/testsetcharacteristics.json "	},"
	echo >>/tmp/testsetcharacteristics.json "	\"top\" : {"
	echo >>/tmp/testsetcharacteristics.json "		\"PixelPresentation\" : \"MONOCHROME\","
	echo >>/tmp/testsetcharacteristics.json "		\"VolumetricProperties\" : \"VOLUME\","
	echo >>/tmp/testsetcharacteristics.json "		\"VolumeBasedCalculationTechnique\" : \"NONE\","
	echo >>/tmp/testsetcharacteristics.json "		\"SOPClassUID\" : \"1.2.840.10008.5.1.4.1.1.4.4\""
	echo >>/tmp/testsetcharacteristics.json "	},"
	echo >>/tmp/testsetcharacteristics.json "	\"RealWorldValueMappingSequence\" : {"
	echo >>/tmp/testsetcharacteristics.json "		\"RealWorldValueFirstValueMapped\" : \"0\","
	echo >>/tmp/testsetcharacteristics.json "		\"RealWorldValueLastValueMapped\" : \"65535\","
	echo >>/tmp/testsetcharacteristics.json "		\"RealWorldValueIntercept\" : \"0\","
	echo >>/tmp/testsetcharacteristics.json "		\"RealWorldValueSlope\" : \"1\","
	echo >>/tmp/testsetcharacteristics.json "		\"LUTExplanation\" : \"Unitless\","
	echo >>/tmp/testsetcharacteristics.json "		\"LUTLabel\" : \"US\","
	echo >>/tmp/testsetcharacteristics.json "		\"MeasurementUnitsCodeSequence\" : { \"cv\" : \"1\", \"csd\" : \"UCUM\", \"cm\" : \"1\" }"
	echo >>/tmp/testsetcharacteristics.json "	},"
	echo >>/tmp/testsetcharacteristics.json "	\"MRImageFrameTypeSequence\" : {"
	echo >>/tmp/testsetcharacteristics.json "		\"FrameType\" : [ \"DERIVED\", \"PRIMARY\", \"PRE_CONTRAST\", \"NONE\" ],"
	echo >>/tmp/testsetcharacteristics.json "		\"PixelPresentation\" : \"MONOCHROME\","
	echo >>/tmp/testsetcharacteristics.json "		\"VolumetricProperties\" : \"VOLUME\","
	echo >>/tmp/testsetcharacteristics.json "		\"VolumeBasedCalculationTechnique\" : \"NONE\""
	echo >>/tmp/testsetcharacteristics.json "	}"
	echo >>/tmp/testsetcharacteristics.json "}"
	cat /tmp/testsetcharacteristics.json
	mkdir -p /tmp/withcharacteristics
	rm -rf /tmp/withcharacteristics/*
	java -Xmx512m -cp .:${PATHTOROOT}:${DICOMADDITIONALJARS}:${PATHTOADDITIONAL}/javax.json-1.0.4.jar \
		com.pixelmed.apps.SetCharacteristicsFromSummary \
			/tmp/testsetcharacteristics.json \
			/tmp/grayscale2013_nac_hncma.dcm \
			/tmp/withcharacteristics \
		2>/tmp/setcharacteristics.log
	rm -f /tmp/filetouse.dat
	grep </tmp/setcharacteristics.log 'Writing with characteristics set file' | tail -1 | sed -e 's/^.*\/tmp/\/tmp/' >/tmp/filetouse.dat
	#@echo "Comparing header before and after characteristics added ..."
	#dcdiff /tmp/grayscale2013_nac_hncma.dcm "`cat /tmp/filetouse.dat`"
	rm -f /tmp/grayscale2013_nac_hncma.dcm
	mv "`cat /tmp/filetouse.dat`" /tmp/grayscale2013_nac_hncma.dcm
	rm -rf /tmp/withcharacteristics
	rm /tmp/testsetcharacteristics.json
	rm /tmp/filetouse.dat
	# need to give the integer version a different SOP Instance UID ...
	mv /tmp/grayscale2013_nac_hncma.dcm /tmp/grayscale2013_nac_hncma.dcm.bak
	dccp -d SOPInstanceUID -nodisclaimer /tmp/grayscale2013_nac_hncma.dcm.bak /tmp/grayscale2013_nac_hncma.dcm
	@echo "Comparing header before and after replacing SOP Instance UID added ..."
	dcdiff /tmp/grayscale2013_nac_hncma.dcm.bak /tmp/grayscale2013_nac_hncma.dcm
	rm /tmp/grayscale2013_nac_hncma.dcm.bak
	# now clean up floating point now that it has been used to make legacy converted MR ...
	echo  >/tmp/testsetcharacteristics.json "{"
	echo >>/tmp/testsetcharacteristics.json "	\"removeall\" : {"
	echo >>/tmp/testsetcharacteristics.json "		\"UnassignedSharedConvertedAttributesSequence\" : null,"
	echo >>/tmp/testsetcharacteristics.json "		\"UnassignedPerFrameConvertedAttributesSequence\" : null"
	echo >>/tmp/testsetcharacteristics.json "	}"
	echo >>/tmp/testsetcharacteristics.json "}"
	cat /tmp/testsetcharacteristics.json
	mkdir -p /tmp/withcharacteristics
	rm -rf /tmp/withcharacteristics/*
	java -Xmx512m -cp .:${PATHTOROOT}:${DICOMADDITIONALJARS}:${PATHTOADDITIONAL}/javax.json-1.0.4.jar \
		com.pixelmed.apps.SetCharacteristicsFromSummary \
			/tmp/testsetcharacteristics.json \
			/tmp/grayscale2013_nac_hncma_fp.dcm \
			/tmp/withcharacteristics \
		2>/tmp/setcharacteristics.log
	rm -f /tmp/filetouse.dat
	grep </tmp/setcharacteristics.log 'Writing with characteristics set file' | tail -1 | sed -e 's/^.*\/tmp/\/tmp/' >/tmp/filetouse.dat
	#@echo "Comparing header before and after characteristics added ..."
	#dcdiff /tmp/grayscale2013_nac_hncma_fp.dcm "`cat /tmp/filetouse.dat`"
	rm -f /tmp/grayscale2013_nac_hncma_fp.dcm
	mv "`cat /tmp/filetouse.dat`" /tmp/grayscale2013_nac_hncma_fp.dcm
	rm -rf /tmp/withcharacteristics
	rm /tmp/testsetcharacteristics.json
	rm /tmp/filetouse.dat
	rm /tmp/setcharacteristics.log


/tmp/labelmap2013_nac_hncma.dcm:
	java -Xmx1g -cp ${PATHTOROOT} -Djava.awt.headless=true com.pixelmed.convert.NRRDToDicom \
		"${HOME}/Documents/Work/NCI/Slicer/BrainAtlas/2013/nac-hncma-atlas2013-Slicer4Version/Data/hncma-atlas.nrrd" \
		/tmp/labelmap2013_nac_hncma.dcm \
		"Brain^Atlas NAC HNCMA 2013" 2013 5678 101 1

/tmp/seg2013_nac_hncma.dcm:	IndexedLabelMapToSegmentation.class /tmp/labelmap2013_nac_hncma.dcm /tmp/grayscale2013_nac_hncma.dcm
	java -Xms6g -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.IndexedLabelMapToSegmentation \
		/tmp/labelmap2013_nac_hncma.dcm \
		"${HOME}/Documents/Work/NCI/Slicer/BrainAtlas/2013/BrainAtlas-label_list_fields_RGB.csv" \
		/tmp/seg2013_nac_hncma.dcm \
		/tmp/grayscale2013_nac_hncma.dcm \
		102 "Segmented Brain Head Neck Muscles" BRAIN_ATLAS_2013 "NAC HNCMA Brain Atlas 2013" "NAC^Halle et al"

testlabelmapbrainatlas2013_nac_hncma:	/tmp/seg2013_nac_hncma.dcm
	#dcentvfy /tmp/seg2013_nac_hncma.dcm /tmp/grayscale2013_nac_hncma.dcm

/tmp/labelmap2013_nac_hncma_skin.dcm:
	java -Xmx1g -cp ${PATHTOROOT} -Djava.awt.headless=true com.pixelmed.convert.NRRDToDicom \
		"${HOME}/Documents/Work/NCI/Slicer/BrainAtlas/2013/nac-hncma-atlas2013-Slicer4Version/Data/skin.nrrd" \
		/tmp/labelmap2013_nac_hncma_skin.dcm \
		"Brain^Atlas NAC HNCMA 2013" 2013 5678 101 1

/tmp/seg2013_nac_hncma_skin.dcm:	IndexedLabelMapToSegmentation.class /tmp/labelmap2013_nac_hncma_skin.dcm /tmp/grayscale2013_nac_hncma.dcm
	java -Xms6g -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.IndexedLabelMapToSegmentation \
		/tmp/labelmap2013_nac_hncma_skin.dcm \
		"${HOME}/Documents/Work/NCI/Slicer/BrainAtlas/2013/BrainAtlas-label_list_fields_RGB.csv" \
		/tmp/seg2013_nac_hncma_skin.dcm \
		/tmp/grayscale2013_nac_hncma.dcm \
		102 "Segmented Brain Head Neck Muscles" BRAIN_ATLAS_2013 "NAC HNCMA Brain Atlas 2013" "NAC^Halle et al"

testlabelmapbrainatlas2013_nac_hncma_skin:	/tmp/labelmap2013_nac_hncma_skin.dcm
	#dcentvfy /tmp/seg2013_nac_hncma_skin.dcm /tmp/grayscale2013_nac_hncma_skin.dcm

cleanlabelmapbrainatlas2013_nac:
	rm -f \
		/tmp/grayscale2013_nac_hncma.dcm \
		/tmp/grayscale2013_nac_hncma_fp.dcm \
		/tmp/labelmap2013_nac_hncma.dcm \
		/tmp/labelmap2013_nac_hncma_skin.dcm \
		/tmp/seg2013_nac_hncma.dcm \
		/tmp/seg2013_nac_hncma_skin.dcm

testlabelmapbrainatlas2013_nac:	/tmp/grayscale2013_nac_hncma.dcm /tmp/seg2013_nac_hncma.dcm /tmp/seg2013_nac_hncma_skin.dcm
	@echo "Performing dcentvfy"
	dcentvfy \
		/tmp/grayscale2013_nac_hncma.dcm \
		/tmp/grayscale2013_nac_hncma_fp.dcm \
		/tmp/labelmap2013_nac_hncma.dcm \
		/tmp/labelmap2013_nac_hncma_skin.dcm \
		/tmp/seg2013_nac_hncma.dcm \
		/tmp/seg2013_nac_hncma_skin.dcm
	@echo "Performing dciodvfy"
	dciodvfy -filename /tmp/grayscale2013_nac_hncma.dcm
	#dciodvfy -filename /tmp/grayscale2013_nac_hncma_fp.dcm
	#dciodvfy -filename /tmp/labelmap2013_nac_hncma.dcm
	#dciodvfy -filename /tmp/labelmap2013_nac_hncma_skin.dcm
	dciodvfy -filename /tmp/seg2013_nac_hncma.dcm
	dciodvfy -filename /tmp/seg2013_nac_hncma_skin.dcm

archivelabelmapbrainatlas2013_nac:
	rm -f /tmp/seg2013_nac_hncma.tar /tmp/seg2013_nac_hncma.tar.bz2
	(cd /tmp; tar -cvf seg2013_nac_hncma.tar \
		grayscale2013_nac_hncma.dcm \
		seg2013_nac_hncma.dcm \
		seg2013_nac_hncma_skin.dcm \
	)
	bzip2 /tmp/seg2013_nac_hncma.tar

/tmp/grayscaleprostate.dcm:
	mkdir -p /tmp/grayscaleprostate
	rm -rf /tmp/grayscaleprostate/*
	java -Xms3g -Xmx3g -cp ${PATHTOROOT}:${COMMONSCODECADDITIONALJAR} com.pixelmed.dicom.MultiFrameImageFactory \
		"$${HOME}/Pictures/Medical/NCI-ISBI_2013_ProstateChallenge/TrainingFromTCIA/PROSTATE-DIAGNOSIS/ProstateDx-01-0006/1.3.6.1.4.1.14519.5.2.1.4792.2002.208539110710641195855467414500/000000" \
		/tmp/grayscaleprostate
	mv /tmp/grayscaleprostate/* /tmp/grayscaleprostate.dcm

/tmp/labelmapprostate.dcm:
	java -Xmx1g -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.NRRDToDicom \
		"$${HOME}/Pictures/Medical/NCI-ISBI_2013_ProstateChallenge/ProstateDx-NRRD-T2W_TSE_AX-05-07-2012/ProstateDx-01-0006_T2W_TSE_AX.nrrd" \
		/tmp/labelmapprostate.dcm \
		ProstateDx-01-0006 ProstateDx-01-0006 "" 100 1

testllabelmapprostate:	IndexedLabelMapToSegmentation.class /tmp/labelmapprostate.dcm /tmp/grayscaleprostate.dcm
	java -Xms6g -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.IndexedLabelMapToSegmentation \
		/tmp/labelmapprostate.dcm \
		"${HOME}/Pictures/Medical/NCI-ISBI_2013_ProstateChallenge/labelmapfortruth_diagnosis.csv" \
		/tmp/segprostate.dcm \
		/tmp/grayscaleprostate.dcm \
		102 "Segmented Prostate" PROSTATE_DIAGNOSIS "Prostate Diagnosis Collection Segmentations" "ProstateDx-01-0006"
	#dcentvfy /tmp/segprostate.dcm /tmp/grayscaleprostate.dcm

testrawformatdump:	RawImageDescription.class
	java -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/javax.json-1.0.4.jar \
		com.pixelmed.convert.RawImageDescription \
		/Volumes/Toshiba5TEnc/Pictures/Medical/GESpeedyCTImageDeliveryChallenge/Apple_32bit_raw_09.json

testrawfloattodicom:	RawImageDescription.class RawToDicomMultiFrame.class
	rm -f /tmp/testrawfloattodicom.raw /tmp/testrawfloattodicom.dcm
	cat /Volumes/Toshiba5TEnc/Pictures/Medical/GESpeedyCTImageDeliveryChallenge/Apple_32bit_raw_09/*.raw >/tmp/testrawfloattodicom.raw
	java -Xms4g -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/javax.json-1.0.4.jar \
		com.pixelmed.convert.RawToDicomMultiFrame \
		/Volumes/Toshiba5TEnc/Pictures/Medical/GESpeedyCTImageDeliveryChallenge/Apple_32bit_raw_09.json \
		/tmp/testrawfloattodicom.raw \
		/tmp/testrawfloattodicom.dcm \
		"Apple_09" 1234 5678 100 1
	rm -f /tmp/testrawfloattodicom.raw



