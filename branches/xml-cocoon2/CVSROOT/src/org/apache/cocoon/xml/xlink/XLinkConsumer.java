/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon.xml.xlink;

import org.xml.sax.SAXException;
import org.xml.sax.Attributes;

import org.apache.cocoon.xml.AbstractXMLConsumer;

/**
 * This class implements a SAX consumer wrapper that transforms the
 * general SAX semantic into XLink semantics for easier consumption.
 *
 * Classes should extend this class and overwrite the abstract method
 * to consume the XLink events that come in as SAX events.
 *
 * NOTE: this is based on XLink W3C Candidate Recommendation 3 July 2000
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-08-23 22:44:30 $
 */
 
public abstract class XLinkConsumer extends AbstractXMLConsumer implements XLinkHandler {

	public static final String XLINK_NAMESPACE_URI = "http://www.w3.org/1999/xlink";
	public static final String XLINK_TYPE          = "type";
	public static final String XLINK_HREF          = "href";
	public static final String XLINK_ROLE          = "role";
	public static final String XLINK_ARCROLE       = "arcrole";
	public static final String XLINK_TITLE         = "title";
	public static final String XLINK_SHOW          = "show";
	public static final String XLINK_ACTUATE       = "actuate";
	public static final String XLINK_LABEL         = "label";
	public static final String XLINK_FROM          = "from";
	public static final String XLINK_TO            = "to";
	public static final String XLINK_TYPE_SIMPLE   = "simple";
	public static final String XLINK_TYPE_EXTENDED = "extended";
	public static final String XLINK_TYPE_LOCATOR  = "locator";
	public static final String XLINK_TYPE_ARC      = "arc";
	public static final String XLINK_TYPE_RESOURCE = "resource";
	public static final String XLINK_TYPE_TITLE    = "title";

    private String extendedLinkElementName = null;
    private String extendedLinkElementURI = null;
    private String linkLocatorElementName = null;
    private String linkLocatorElementURI = null;
    private String linkArcElementName = null;
    private String linkArcElementURI = null;
    
	public void startElement(String uri, String name, String raw, Attributes attr) throws SAXException {
		String type = attr.getValue(XLINK_NAMESPACE_URI, XLINK_TYPE);
		if (type != null) {
		    if (type.equals(XLINK_TYPE_SIMPLE)) {
		        if (this.extendedLinkElementName != null) {
		            throw new SAXException("An XLink simple link cannot be included into an 'extended' element");
		        } else if (this.linkLocatorElementName != null) {
		            throw new SAXException("An XLink simple link cannot be included into a 'locator' element");
		        } else if (this.linkArcElementName != null) {
		            throw new SAXException("An XLink simple link cannot be included into an 'arc' element");
		        }
		        String href    = attr.getValue(XLINK_NAMESPACE_URI, XLINK_HREF);
		        String role    = attr.getValue(XLINK_NAMESPACE_URI, XLINK_ROLE);
		        String arcrole = attr.getValue(XLINK_NAMESPACE_URI, XLINK_ARCROLE);
		        String title   = attr.getValue(XLINK_NAMESPACE_URI, XLINK_TITLE);
		        String show    = attr.getValue(XLINK_NAMESPACE_URI, XLINK_SHOW);
		        String actuate = attr.getValue(XLINK_NAMESPACE_URI, XLINK_ACTUATE);
		        this.simpleLink(href, role, arcrole, title, show, actuate);
		    } else if (type.equals(XLINK_TYPE_EXTENDED)) {
		        if (this.extendedLinkElementName != null) {
		            throw new SAXException("An XLink extended link cannot include another 'extended' element");
		        } else if (this.linkLocatorElementName != null) {
		            throw new SAXException("An XLink extended link cannot be included into a 'locator' element");
		        } else if (this.linkArcElementName != null) {
		            throw new SAXException("An XLink extended link cannot be included into an 'arc' element");
		        }
		        String role    = attr.getValue(XLINK_NAMESPACE_URI, XLINK_ROLE);
		        String title   = attr.getValue(XLINK_NAMESPACE_URI, XLINK_TITLE);
		        this.extendedLinkElementName = name;
		        this.extendedLinkElementURI = uri;
		        this.startExtendedLink(role, title);
		    } else if (type.equals(XLINK_TYPE_LOCATOR)) {
                if (this.extendedLinkElementName == null) {
		            throw new SAXException("An XLink locator must be included into an 'extended' element");
                } else if (this.linkLocatorElementName != null) {
		            throw new SAXException("An XLink locator  cannot be included into another 'locator' element");
		        } else if (this.linkArcElementName != null) {
		            throw new SAXException("An XLink locator cannot be included into an 'arc' element");
		        }
		        String href    = attr.getValue(XLINK_NAMESPACE_URI, XLINK_HREF);
		        String role    = attr.getValue(XLINK_NAMESPACE_URI, XLINK_ROLE);
		        String title   = attr.getValue(XLINK_NAMESPACE_URI, XLINK_TITLE);
		        String label   = attr.getValue(XLINK_NAMESPACE_URI, XLINK_LABEL);
		        this.linkLocatorElementName = name;
		        this.linkLocatorElementURI = uri;
		        this.startLocator(href, role, title, label);
		    } else if (type.equals(XLINK_TYPE_ARC)) {
                if (this.extendedLinkElementName == null) {
		            throw new SAXException("An XLink arc must be included into an 'extended' element");
                } else if (this.linkLocatorElementName != null) {
		            throw new SAXException("An XLink arc cannot be included into a 'locator' element");
		        } else if (this.linkArcElementName != null) {
		            throw new SAXException("An XLink arc cannot be included into another 'arc' element");
		        }
		        String arcrole = attr.getValue(XLINK_NAMESPACE_URI, XLINK_ARCROLE);
		        String title   = attr.getValue(XLINK_NAMESPACE_URI, XLINK_TITLE);
		        String show    = attr.getValue(XLINK_NAMESPACE_URI, XLINK_SHOW);
		        String actuate = attr.getValue(XLINK_NAMESPACE_URI, XLINK_ACTUATE);
		        String from    = attr.getValue(XLINK_NAMESPACE_URI, XLINK_FROM);
		        String to      = attr.getValue(XLINK_NAMESPACE_URI, XLINK_TO);
		        this.linkArcElementName = name;
		        this.linkArcElementURI = uri;
		        this.startArc(arcrole, title, show, actuate, from, to);
		    } else if (type.equals(XLINK_TYPE_RESOURCE)) {
                if (this.extendedLinkElementName == null) {
		            throw new SAXException("An XLink resource must be included into an 'extended' element");
		        }
		        String role    = attr.getValue(XLINK_NAMESPACE_URI, XLINK_ROLE);
		        String title   = attr.getValue(XLINK_NAMESPACE_URI, XLINK_TITLE);
		        String label   = attr.getValue(XLINK_NAMESPACE_URI, XLINK_LABEL);
		        this.linkResource(role, title, label);
		    } else if (type.equals(XLINK_TYPE_TITLE)) {
                if ((this.extendedLinkElementName == null) 
                  && (this.linkLocatorElementName == null) 
                  && (this.linkArcElementName == null)) {
		            throw new SAXException("An XLink title must be included into an 'extended', 'locator' or 'arc' element");
		        }
		        this.linkTitle();
		    }
		}
	}

	public void endElement(String uri, String name, String raw) throws SAXException {
	    if ((name.equals(this.extendedLinkElementName)) && (uri.equals(this.extendedLinkElementURI))) {
	        this.extendedLinkElementName = null;
	        this.extendedLinkElementURI = null;
	        this.endExtendedLink();
	    } else if ((name.equals(this.linkLocatorElementName)) && (uri.equals(this.linkLocatorElementURI))) {
	        this.linkLocatorElementName = null;
	        this.linkLocatorElementURI = null;
	        this.endLocator();
	    } else if ((name.equals(this.linkArcElementName)) && (uri.equals(this.linkArcElementURI))) {
	        this.linkArcElementName = null;
	        this.linkArcElementURI = null;
	        this.endArc();
	    }
	}

    // XLinkHandler implementation
    
    public void simpleLink(String href, String role, String arcrole, String title, String show, String actuate) throws SAXException {}
    
    public void startExtendedLink(String role, String title) throws SAXException {}
    
    public void endExtendedLink() throws SAXException {}
    
    public void startLocator(String href, String role, String title, String label) throws SAXException {}

    public void endLocator() throws SAXException {}
    
    public void startArc(String arcrole, String title, String show, String actuate, String from, String to) throws SAXException {}
    
    public void endArc() throws SAXException {}

    public void linkResource(String role, String title, String label) throws SAXException {}
    
    public void linkTitle() throws SAXException {}
}

