/* Copyright (c) 2001-2018, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.network;

/**
 * @author	dclunie
 */
public class DicomNetworkException extends Exception {

	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/network/DicomNetworkException.java,v 1.8 2018/02/09 15:35:28 dclunie Exp $";

	/**
	 * @param	msg
	 */
	public DicomNetworkException(String msg) {
		super(msg);
	}
}



