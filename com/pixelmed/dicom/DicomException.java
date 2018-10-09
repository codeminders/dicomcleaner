/* Copyright (c) 2001-2018, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.dicom;

/**
 * @author	dclunie
 */
public class DicomException extends Exception {

	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/dicom/DicomException.java,v 1.9 2018/02/09 15:35:19 dclunie Exp $";

	/**
	 * <p>Constructs a new exception with the specified detail message.</p>
	 *
	 * @param	msg	the detail message
	 */
	public DicomException(String msg) {
		super(msg);
	}
}


