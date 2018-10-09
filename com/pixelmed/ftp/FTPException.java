/* Copyright (c) 2001-2018, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.ftp;

/**
 * @author	dclunie
 */
public class FTPException extends Exception {

	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/ftp/FTPException.java,v 1.7 2018/02/09 15:35:26 dclunie Exp $";
	
	/**
	 * @param	msg
	 */
	public FTPException(String msg) {
		super(msg);
	}
	
	/**
	 * @param	e
	 */
	public FTPException(FTPException e) {
		super(e);
	}
}



