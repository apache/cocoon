/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.environment.http;

import java.io.File; 
import java.io.IOException; 
import java.io.OutputStream; 
import java.net.MalformedURLException; 
import java.net.URL; 
import java.util.Dictionary; 
import java.util.Hashtable; 

import javax.servlet.ServletContext; 
import javax.servlet.http.HttpServletRequest; 
import javax.servlet.http.HttpServletResponse;

import org.apache.cocoon.environment.Environment; 
 
import org.xml.sax.InputSource; 
import org.xml.sax.SAXException; 

public class HttpEnvironment implements Environment {

    /** The current uri in progress */
    private String uri = null;

    /** The current prefix to strip off from the request uri */
    private StringBuffer prefix = new StringBuffer();

    /** The View requested */
    private String view = "";

    /** The HttpServletRequest */
    private HttpRequest request = null;
    private HttpServletRequest servletRequest = null;

    /** The HttpServletResponse */ 
    private HttpResponse response = null; 
    private HttpServletResponse servletResponse = null; 
 
    /** The ServletContext */ 
    private ServletContext servletContext  = null; 
 
    /** The OutputStream */ 
    private OutputStream outputStream = null; 
 
    /** The Context path */ 
    private URL context = null; 
 
    /** The servlet object model */ 
    private Hashtable objectModel = null; 

    /**
     * Constructs a HttpEnvironment object from a HttpServletRequest 
     * and HttpServletResponse objects
     */
    public HttpEnvironment (String uri, HttpServletRequest request, 
                            HttpServletResponse response, 
                            ServletContext servletContext) 
    throws MalformedURLException, IOException {
        this.uri = uri;
        this.view = request.getHeader("cocoon-view");
        this.request = new HttpRequest (request, this);
        this.servletRequest = request;
        this.response = new HttpResponse (response);
        this.servletResponse = response;
        this.servletContext = servletContext; 
        this.context = new URL("file://"+servletContext.getRealPath("/"));
        this.outputStream = response.getOutputStream();
        this.objectModel = new Hashtable();
        this.objectModel.put("request", this.request);
        this.objectModel.put("response", this.response);
        this.objectModel.put("context", this.servletContext);
    }

    // Sitemap methods

    /**
     * Returns the uri in progress. The prefix is stripped off
     */
    public String getUri () {
        return this.uri;
    }

    /**
     * Adds an prefix to the overall stripped off prefix from the request uri
     */
    public void changeContext(String prefix, String context) 
    throws MalformedURLException { 
        if (uri.startsWith (prefix)) {
            this.prefix.append (prefix);
            uri = uri.substring(prefix.length());
            File f = new File(context);
            if (f.isFile()) 
                this.context=f.getParentFile().toURL(); 
            else
                this.context = f.toURL();
        } else {
            //FIXME: should we throw an error here ?
        }
    }

    // Request methods

    /**
     * Returns the request view
     */
    public String getView () {
        return this.view;
    }

    // Response methods
 
    /** 
     * Set the ContentType 
     */ 
    public void setContentType (String contentType) { 
        this.response.setContentType (contentType); 
    } 
 
    /** 
     * Get the OutputStream 
     */ 
    public OutputStream getOutputStream() throws IOException {
        return this.outputStream;
    }

    // Object model method

    /**
     * Returns a Dictionary containing environment specific objects
     */
    public Dictionary getObjectModel () {
        return this.objectModel;
    }

    // EntityResolver method
 
    /** 
     * Resolve an entity. 
     */ 
    public InputSource resolveEntity(String publicId, String systemId) 
    throws SAXException, IOException { 
        if (systemId==null) throw new SAXException("Invalid System ID"); 
 
        if (systemId.length()==0) 
            return new InputSource(this.context.toExternalForm()); 
        if (systemId.indexOf(":/")>0) 
            return new InputSource(systemId); 
        if (systemId.charAt(0)=='/') 
            return new InputSource(this.context.getProtocol()+":"+systemId); 
        return(new InputSource(new URL(this.context,systemId).toExternalForm())); 
    } 
}
