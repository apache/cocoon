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
import java.net.MalformedURLException; 
import java.net.URL; 

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
    private HttpRequest req = null;


    /** The HttpServletResponse */ 
    private HttpResponse res = null; 
 
    /** The ServletContext */ 
    private ServletContext servletContext  = null; 
 
    /** The Context path */ 
    private URL context = null; 

    /**
     * Constructs a HttpEnvironment object from a HttpServletRequest 
     * and HttpServletResponse objects
     */
    public HttpEnvironment (String uri, HttpServletRequest req, 
                            HttpServletResponse res, 
                            ServletContext servletContext) 
    throws MalformedURLException {
        this.uri = uri;
        this.view = req.getHeader("cocoon-view");
        this.req = new HttpRequest (req, this);
        this.res = new HttpResponse (res);
        this.servletContext = servletContext; 
        System.out.println ("HttpEnvironment: Context path is \""+req.getContextPath()+"\"");
        System.out.println ("HttpEnvironment: Servlet path is \""+req.getServletPath()+"\"");
        System.out.println ("HttpEnvironment: Translated path is \""+req.getPathTranslated()+"\"");
        System.out.println ("HttpEnvironment: Real path is \""+servletContext.getRealPath("/")+"\"");
        this.context = new URL("file://"+servletContext.getRealPath("/"));
        System.out.println ("HttpEnvironment: Context path is \""+servletContext.getRealPath("/")+"\"");
    }
    /**
     * Adds an prefix to the overall stripped off prefix from the request uri
     */
    public void changeContext(String prefix, String context) 
    throws MalformedURLException { 
        System.out.println ("Environment: addUriPrefix(\""+prefix+"\",\""+context+"\")");
        if (uri.startsWith (prefix)) {
            this.prefix.append (prefix);
            uri = uri.substring(prefix.length());
            File f = new File(context);
            if (f.isFile()) 
                this.context=f.getParentFile().toURL(); 
            else
                this.context = f.toURL();
            System.out.println ("HttpEnvironment: New context path set to \""+this.context+"\"");
        } else {
            //FIXME: should we throw an error here ?
        }
        System.out.println ("Environment: addUriPrefix: new uri=\""+this.uri+"\", new context=\""+this.context+"\")");
    }

    /**
     * Returns the request view
     */
    public String getView () {
        return this.view;
    }

    /**
     * Returns the uri in progress. The prefix is stripped off
     */
    public String getUri () {
        System.out.println ("Environment: getUri = \""+this.uri+"\"");
        return this.uri;
    }

    /**
     * Returns a wrapped HttpRequest object of the real HttpRequest in progress
     */
    public HttpRequest getRequest () {
        return this.req;
    }
 
    /** 
     * Returns a wrapped HttpResponse object of the real HttpResponse in progress 
     */ 
    public HttpResponse getResponse () { 
        return this.res; 
    } 
  
    /** 
     * Resolve an entity. 
     */ 
    public InputSource resolveEntity(String systemId) 
    throws SAXException, IOException { 
        return(this.resolveEntity(null,systemId)); 
    } 
 
    /** 
     * Resolve an entity. 
     */ 
    public InputSource resolveEntity(String publicId, String systemId) 
    throws SAXException, IOException { 
        System.out.println ("Environment: resolveEntity(\""+publicId+"\",\""+systemId+"\")");
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
