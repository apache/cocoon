/*****************************************************************************
* Copyright (C) The Apache Software Foundation. All rights reserved.        *
* ------------------------------------------------------------------------- *
* This software is published under the terms of the Apache Software License *
* version 1.1, a copy of which has been included  with this distribution in *
* the LICENSE file.                                                         *
*****************************************************************************/

package org.apache.cocoon.generation;

import java.util.Calendar;
import java.util.Date;
import java.text.DateFormat;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;


/** Generates an XML representation of the current status of Cocoon.
 * Potted DTD:
 *
 * <code>
 * &lt;!ELEMENT statusinfo (group|value)*&gt;
 *
 * &lt;!ATTLIST statusinfo
 *     date CDATA #IMPLIED
 *     host CDATA #IMPLIED
 * &gt;
 *
 * &lt;!ELEMENT group (group|value)*&gt;
 * &lt;!ATTLIST group
 *     name CDATA #IMPLIED
 * &gt;
 *
 * &lt;!ELEMENT value (#PCDATA)&gt;
 * &lt;!ATTLIST value
 *     name CDATA #REQUIRED
 * &gt;
 * </code>
 * 
 * @author <a href="mailto:paul@luminas.co.uk">Paul Russell</a> (Luminas Limited)
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2000-07-22 20:41:48 $
 */
public class StatusGenerator extends ComposerGenerator {

	/** The XML namespace for the output document.
	 */
	protected static final String namespace =
		"http://apache.org/cocoon/status";

	/** The XML namespace for xlink
	 */
	protected static final String xlinkNamespace =
		"http://www.w3.org/1999/xlink";
		
	/** Generate the status information in XML format.
	 * @throws SAXException
	 *	when there is a problem creating the output SAX events.
	 */
	public void generate() throws SAXException {
	
		// Start the document and set the namespace.
		this.contentHandler.startDocument();
		this.contentHandler.startPrefixMapping("", namespace);
		this.contentHandler.startPrefixMapping("xlink", xlinkNamespace);

		genStatus(this.contentHandler);

		// End the document.
		this.contentHandler.endPrefixMapping("xlink");
		this.contentHandler.endPrefixMapping("");
		this.contentHandler.endDocument();
	}

	/** Generate the main status document.
	 */
	private void genStatus(ContentHandler ch) throws SAXException {
		// Root element.

		// The current date and time.
		String dateTime = DateFormat.getDateTimeInstance().format(new Date());

		String localHost;

		// The local host.
		try {
			localHost = InetAddress.getLocalHost().getHostName();
		} catch ( UnknownHostException e ) {
			localHost = "";
		} catch ( SecurityException e ) {
			localHost = "";
		}
		
		AttributesImpl atts = new AttributesImpl();
		atts.addAttribute(namespace, "date", "date", "CDATA", dateTime);
		atts.addAttribute(namespace, "host", "host", "CDATA", localHost);
		ch.startElement(namespace, "statusinfo", "statusinfo", atts);

		genVMStatus(ch);

		// End root element.
		ch.endElement(namespace, "statusinfo", "statusinfo");
	}

	private void genVMStatus(ContentHandler ch) throws SAXException {
		String buf;
		AttributesImpl atts = new AttributesImpl();
	
		startGroup(ch, "vm");
		// BEGIN Memory status
		startGroup(ch, "memory");
		addValue(ch, "total", String.valueOf(Runtime.getRuntime().totalMemory()));
		addValue(ch, "free", String.valueOf(Runtime.getRuntime().freeMemory()));
		endGroup(ch);
		// END Memory status
		
		// BEGIN JRE
		startGroup(ch, "jre");
		addValue(ch, "version", System.getProperty("java.version"));
		atts.clear();
		atts.addAttribute(xlinkNamespace, "type", "type", "CDATA", "simple");
		atts.addAttribute(xlinkNamespace, "href", "href", "CDATA",
			System.getProperty("java.vendor.url") );
		addValue(ch, "java-vendor", System.getProperty("java.vendor"), atts);
		endGroup(ch);
		// END JRE
		
		// BEGIN Operating system
		startGroup(ch, "operating-system");
		addValue(ch, "name", System.getProperty("os.name"));
		addValue(ch, "architecture", System.getProperty("os.arch"));
		addValue(ch, "version", System.getProperty("os.version"));
		endGroup(ch);
		// END operating system
		
		addValue(ch, "classpath", System.getProperty("java.class.path"));

		// BEGIN OS info
		endGroup(ch);
	}

	/** Utility function to begin a <code>group</code> tag pair. */
	private void startGroup(ContentHandler ch, String name) throws SAXException {
		startGroup(ch, name, null);
	}
	
	/** Utility function to begin a <code>group</code> tag pair with added attributes. */
	private void startGroup(ContentHandler ch, String name, Attributes atts) throws SAXException {
		AttributesImpl ai;
		if ( atts == null ) {
			ai = new AttributesImpl();
		} else {
			ai = new AttributesImpl(atts);
		}
		ai.addAttribute(namespace, "name", "name", "CDATA", name);
		ch.startElement(namespace, "group", "group", ai);
	}

	/** Utility function to end a <code>group</code> tag pair. */
	private void endGroup(ContentHandler ch) throws SAXException {
		ch.endElement(namespace, "group", "group");
	}

	/** Utility function to begin and end a <code>value</code> tag pair. */
	private void addValue(ContentHandler ch, String name, String value) throws SAXException {
		addValue(ch, name, value, null);
	}

	/** Utility function to begin and end a <code>value</code> tag pair with added attributes. */
	private void addValue(ContentHandler ch, String name, String value, Attributes atts) throws SAXException {
		AttributesImpl ai;
		if ( atts == null ) {
			ai = new AttributesImpl();
		} else {
			ai = new AttributesImpl(atts);
		}
		ai.addAttribute(namespace, "name", "name", "CDATA", name);
		ch.startElement(namespace, "value", "value", ai);
		if ( value != null ) {
			ch.characters(value.toCharArray(), 0, value.length());
		}
		ch.endElement(namespace, "value", "value");
	}
}
 
