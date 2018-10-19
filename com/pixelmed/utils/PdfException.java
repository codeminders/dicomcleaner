/* Copyright (c) 2001-2018, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.utils;

/**
 * @author	dclunie
 */
public class PdfException extends Exception {

	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/utils/PdfException.java,v 1.5 2018/02/09 15:35:34 dclunie Exp $";

	/**
	 * @param	msg
	 */
	public PdfException(String msg) {
		super(msg);
	}
}


