/*
	ApplicationException.java

	Author: Björn Lütkemeier <bluetkemeier@s-und-n.de>
	Date: April 23, 2003

 */
package org.apache.cocoon.samples.errorhandling;

public class ApplicationException 
extends Exception {
	private int errorCode;
	
	public ApplicationException(int errorCode) {
		this.errorCode = errorCode;
	}
	
	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}
	
	public int getErrorCode() {
		return this.errorCode;
	}
}
