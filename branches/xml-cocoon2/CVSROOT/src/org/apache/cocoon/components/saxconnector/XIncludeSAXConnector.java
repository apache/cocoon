/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.components.saxconnector;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.Locator;

import org.apache.cocoon.Roles;
import org.apache.avalon.component.Component;
import org.apache.avalon.parameters.Parameters;
import org.apache.avalon.Disposable;
import org.apache.avalon.component.ComponentManager;
import org.apache.avalon.component.ComponentException;
import org.apache.avalon.component.Composable;
import org.apache.excalibur.pool.Poolable;

import org.apache.cocoon.components.url.URLFactory;
import org.apache.cocoon.xml.AbstractXMLPipe;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.sitemap.Sitemap;

import org.xml.sax.SAXException;
import org.xml.sax.EntityResolver;

import java.util.Map;
import java.io.IOException;

/**
 * Copy of code from XIncludeTransformer as a starting point for XIncludeSAXConnector.
 * @author <a href="dims@yahoo.com">Davanum Srinivas</a>
 * @version CVS $Revision: 1.1.2.3 $ $Date: 2001-04-24 20:18:26 $
 */
public class XIncludeSAXConnector extends AbstractXMLPipe implements Composable, Poolable, SAXConnector, Disposable {

    protected URLFactory urlFactory;

    protected ComponentManager manager = null;

    public static final String XMLBASE_NAMESPACE_URI = "http://www.w3.org/XML/1998/namespace";
    public static final String XMLBASE_ATTRIBUTE = "base";

    public static final String XINCLUDE_NAMESPACE_URI = "http://www.w3.org/1999/XML/xinclude";
    public static final String XINCLUDE_INCLUDE_ELEMENT = "include";
    public static final String XINCLUDE_INCLUDE_ELEMENT_HREF_ATTRIBUTE = "href";
    public static final String XINCLUDE_INCLUDE_ELEMENT_PARSE_ATTRIBUTE = "parse";


    /** the current sitemap */
    protected Sitemap sitemap;

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

    public void setup(EntityResolver resolver, Map objectModel,
                      String source, Parameters parameters)
            throws ProcessingException, SAXException, IOException {}

    public void compose(ComponentManager manager) {
        this.manager = manager;
        try {
            this.urlFactory = (URLFactory)this.manager.lookup(Roles.URL_FACTORY);
        } catch (Exception e) {
            getLogger().error("cannot obtain URLFactory", e);
        }
    }

    public void startElement(String uri, String name, String raw, Attributes attr) throws SAXException {
        String value;
        if ((value = attr.getValue(XMLBASE_NAMESPACE_URI,XMLBASE_ATTRIBUTE)) != null) {
            try {
                startXMLBaseAttribute(uri,name,value);
            } catch (MalformedURLException e) {
                getLogger().debug("XIncludeSAXConnector", e);
                throw new SAXException(e);
            }
        }
        if (uri != null && name != null && uri.equals(XINCLUDE_NAMESPACE_URI) && name.equals(XINCLUDE_INCLUDE_ELEMENT)) {
            String href = attr.getValue("",XINCLUDE_INCLUDE_ELEMENT_HREF_ATTRIBUTE);
            String parse = attr.getValue("",XINCLUDE_INCLUDE_ELEMENT_PARSE_ATTRIBUTE);
            try {
                processXIncludeElement(href, parse);
            } catch (MalformedURLException e) {
                getLogger().debug("XIncludeSAXConnector", e);
                throw new SAXException(e);
            } catch (IOException e) {
                getLogger().debug("XIncludeSAXConnector", e);
                throw new SAXException(e);
            }
            return;
        }
        super.startElement(uri,name,raw,attr);
    }

    public void endElement(String uri, String name, String raw) throws SAXException {
        if (last_xmlbase_element_uri.equals(uri) && last_xmlbase_element_name.equals(name)) {
            endXMLBaseAttribute();
        }
        if (uri != null && name != null && uri.equals(XINCLUDE_NAMESPACE_URI) && name.equals(XINCLUDE_INCLUDE_ELEMENT)) {
            return;
        }
        super.endElement(uri,name,raw);
    }

    public void setDocumentLocator(Locator locator) {
        try {
            base_xmlbase_uri = urlFactory.getURL(locator.getSystemId());
            if (current_xmlbase_uri == null) {
                current_xmlbase_uri = base_xmlbase_uri;
            }
        } catch (MalformedURLException e) {getLogger().debug("XIncludeSAXConnector", e);}
        super.setDocumentLocator(locator);
    }

    protected void startXMLBaseAttribute(String uri, String name, String value) throws MalformedURLException {
        if (current_xmlbase_uri != null) {
            xmlbase_stack.push(current_xmlbase_uri);
        }
        current_xmlbase_uri = urlFactory.getURL(value);

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
        getLogger().debug("Processing XInclude element: href="+href+", parse="+parse+", sitemap="+sitemap);
        //System.out.println("Processing XInclude element: href="+href+", parse="+parse+", sitemap="+sitemap);
    }

    public void dispose()
    {
        if(this.urlFactory != null)
            this.manager.release((Component)this.urlFactory);
    }
}
