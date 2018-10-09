/* Copyright (c) 2001-2018, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.display.event;

import com.pixelmed.event.Event;

/**
 * @author	dclunie
 */
public class StatusChangeEvent extends Event {

	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/display/event/StatusChangeEvent.java,v 1.9 2018/02/09 15:35:25 dclunie Exp $";

	private String statusMessage;

	/**
	 * @param	statusMessage
	 */
	public StatusChangeEvent(String statusMessage) {
		super();
		this.statusMessage=statusMessage;
	}

	/***/
	public String getStatusMessage() { return statusMessage; }
}

