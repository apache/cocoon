/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.filters;

import java.net.URL;
import java.net.MalformedURLException;
import java.io.IOException;
import java.io.Reader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Stack;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.apache.cocoon.Request;
import org.apache.cocoon.Response;
import org.apache.cocoon.Parameters;
import org.apache.cocoon.ProcessingException;

/**
 * My first pass at an XInclude filter. Currently it should set the base URI 
 * from the SAX Locator's system id but allow it to be overridden by xml:base
 * elements as the XInclude spec mandates. It's also got some untested code
 * that should handle inclusion of text includes, but that method isn't called
 * by the SAX event FSM yet.
 *
 * @author <a href="mailto:balld@webslingerZ.com">Donald Ball</a>
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2000-04-03 03:36:31 $ $Author: balld $
 */
public class XIncludeFilter extends AbstractFilter {

	protected boolean debug = true;

	public static final String XMLBASE_NAMESPACE_URI = "http://www.w3.org/XML/1998/namespace";
	public static final String XMLBASE_ATTRIBUTE = "base";

	public static final String XINCLUDE_NAMESPACE_URI = "http://www.w3.org/1999/XML/xinclude";
	public static final String XINCLUDE_INCLUDE_ELEMENT = "include";
	public static final String XINCLUDE_INCLUDE_ELEMENT_HREF_ATTRIBUTE = "href";
	public static final String XINCLUDE_INCLUDE_ELEMENT_PARSE_ATTRIBUTE = "parse";

	protected URL base_xmlbase_uri = null;
	
	/** The current XMLBase URI. We start with an empty "dummy" URL. **/
	protected URL current_xmlbase_uri = null;

	/** This is a stack of xml:base attributes which belong to our ancestors **/
	protected Stack xmlbase_stack = new Stack();

	/** namespace uri of the last element which had an xml:base attribute **/
	protected String last_xmlbase_element_uri = "";

	protected Stack xmlbase_element_uri_stack = new Stack();

	/** name of the last element which had an xml:base attribute **/
	protected String last_xmlbase_element_name = "";

	protected Stack xmlbase_element_name_stack = new Stack();

    public void setup(Request request, Response response, 
                      String source, Parameters parameters) 
            throws ProcessingException, SAXException, IOException {}
	/*
		try {
			System.err.println("SOURCE: "+source);
			base_xmlbase_uri = new URL(source);
			System.err.println("SOURCE URI: "+base_xmlbase_uri.toString());
		} catch (MalformedURLException e) {
			throw new ProcessingException(e.getMessage());
		}
	}
	*/

	public void startElement(String uri, String name, String raw, Attributes attr) throws SAXException {
		String value;
		if ((value = attr.getValue(XMLBASE_NAMESPACE_URI,XMLBASE_ATTRIBUTE)) != null) {
			try {
				startXMLBaseAttribute(uri,name,value);
			} catch (MalformedURLException e) {
				throw new SAXException(e);
			}
		}
		if (uri.equals(XINCLUDE_NAMESPACE_URI) && name.equals(XINCLUDE_INCLUDE_ELEMENT)) {
			String href = attr.getValue("",XINCLUDE_INCLUDE_ELEMENT_HREF_ATTRIBUTE);
			String parse = attr.getValue("",XINCLUDE_INCLUDE_ELEMENT_PARSE_ATTRIBUTE);
			try {
				processXIncludeElement(href, parse);
			} catch (MalformedURLException e) {
				throw new SAXException(e);
			} catch (IOException e) {
				throw new SAXException(e);
			}
			return;
		}
		if (super.contentHandler!= null) {
			super.contentHandler.startElement(uri,name,raw,attr);
		}
	}

	public void endElement(String uri, String name, String raw) throws SAXException {
		if (last_xmlbase_element_uri.equals(uri) && last_xmlbase_element_name.equals(name)) {
			endXMLBaseAttribute();
		}
		if (uri.equals(XINCLUDE_NAMESPACE_URI) && name.equals(XINCLUDE_INCLUDE_ELEMENT)) {
			return;
		}
		if (super.contentHandler!= null) {
			super.contentHandler.endElement(uri,name,raw);
		}
	}

    public void setDocumentLocator(Locator locator) {
		try {
			base_xmlbase_uri = new URL(locator.getSystemId());
			if (current_xmlbase_uri == null) {
				current_xmlbase_uri = base_xmlbase_uri;
			}
		} catch (MalformedURLException e) {}
        if (super.contentHandler!=null) {
            super.contentHandler.setDocumentLocator(locator);
		}
    }

	protected void startXMLBaseAttribute(String uri, String name, String value) throws MalformedURLException {
		if (current_xmlbase_uri != null) {
			xmlbase_stack.push(current_xmlbase_uri);
		}
		current_xmlbase_uri = new URL(value);

		xmlbase_element_uri_stack.push(last_xmlbase_element_uri);
		last_xmlbase_element_uri = uri;

		xmlbase_element_name_stack.push(last_xmlbase_element_name);
		last_xmlbase_element_name = name;
	}

	protected void endXMLBaseAttribute() {
		if (xmlbase_stack.size() > 0) {
			current_xmlbase_uri = (URL)xmlbase_stack.pop();
		} else {
			current_xmlbase_uri = base_xmlbase_uri;
		}
		last_xmlbase_element_uri = (String)xmlbase_element_uri_stack.pop();
		last_xmlbase_element_name = (String)xmlbase_element_name_stack.pop();
	}

	protected void processXIncludeElement(String href, String parse) throws SAXException,MalformedURLException,IOException {
		if (debug) { System.err.println("Processing XInclude element: href="+href+", parse="+parse); }
		URL url = new URL(current_xmlbase_uri,href);
		if (debug) { System.err.println("URL: "+url); }
		Object object = url.getContent();
		if (debug) { System.err.println("Object: "+object); }
		if (parse.equals("text")) {
			if (object instanceof Reader) {
				Reader reader = (Reader)object;
				int read;
				char ary[] = new char[1024];
				if (reader != null) {
					while ((read = reader.read(ary)) != -1) {
						if (super.contentHandler != null) {
							super.contentHandler.characters(ary,0,read);
						}
					}
					reader.close();
				}
			} else if (object instanceof InputStream) {
				InputStream input = (InputStream)object;
				InputStreamReader reader = new InputStreamReader(input);
				int read;
				char ary[] = new char[1024];
				if (reader != null) {
					while ((read = reader.read(ary)) != -1) {
						if (super.contentHandler != null) {
							super.contentHandler.characters(ary,0,read);
						}
					}
					reader.close();
				}
			}
		}
	}

}
