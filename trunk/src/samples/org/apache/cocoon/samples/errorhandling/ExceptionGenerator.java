/*
	ExceptionGenerator.java

	Author: Björn Lütkemeier <bluetkemeier@s-und-n.de>
	Date: April 23, 2003

 */
package org.apache.cocoon.samples.errorhandling;

import java.io.IOException;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.generation.AbstractGenerator;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class ExceptionGenerator 
extends AbstractGenerator {
	
	/*
	 * Name of request parameters. 
	 */
	public static final String PAR_EXCEPTION = "exception";
	public static final String PAR_CODE      = "code";

	/**
	 * Overridden from superclass.
	 */
	public void generate()
	throws IOException, SAXException, ProcessingException {
		Request request = ObjectModelHelper.getRequest(this.objectModel);
		String exception = request.getParameter(PAR_EXCEPTION);
		String text = null;

		if (exception == null) {
			text = "No exception occured.";
		} else if (exception.equals("validation")) {
			throw new ProcessingException(new ValidationException());
		} else if (exception.equals("application")) {
			throw new ProcessingException(new ApplicationException(
				Integer.parseInt(request.getParameter(PAR_CODE))));
		} else if (exception.equals("resourceNotFound")) {
			throw new ProcessingException(new ResourceNotFoundException(""));
		} else if (exception.equals("nullPointer")) {
			throw new ProcessingException(new NullPointerException());
		} else {
			text = "Unknown exception requested.";
		}

		Attributes noAttrs = new AttributesImpl();
		this.contentHandler.startDocument();
		this.contentHandler.startElement(null, "html", "html", noAttrs);
		this.contentHandler.startElement(null, "body", "body", noAttrs);
		this.contentHandler.startElement(null, "p", "p", noAttrs);
		this.contentHandler.characters(text.toCharArray(), 0, text.length());
		this.contentHandler.endElement(null, "p", "p");
		this.contentHandler.endElement(null, "body", "body");
		this.contentHandler.endElement(null, "html", "html");
		this.contentHandler.endDocument();
	}

}
