/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon.environment;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.HashMap;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.cocoon.environment.Environment;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public abstract class AbstractEnvironment implements Environment {

    /** The current uri in progress */
    protected String uri = null;

    /** The current prefix to strip off from the request uri */
    protected StringBuffer prefix = new StringBuffer();

    /** The View requested */
    protected String view = null;

     /** The Context path */
    protected URL context = null;

    /** The servlet object model */
    protected HashMap objectModel = null;

    /**
     * Constructs the abstract enviornment
     */
    public AbstractEnvironment(String uri, String view, String context) 
    throws MalformedURLException {
        this(uri, view, new File(context));
    }

    /**
     * Constructs the abstract enviornment
     */
    public AbstractEnvironment(String uri, String view, File context) 
    throws MalformedURLException {
        this.uri = uri;
        this.view = view;
        this.context = context.toURL();
        this.objectModel = new HashMap();
    }

    // Sitemap methods

    /**
     * Returns the uri in progress. The prefix is stripped off
     */
    public String getUri() {
        return this.uri;
    }

    /**
     * Adds an prefix to the overall stripped off prefix from the request uri
     */
    public void changeContext(String prefix, String context)
    throws MalformedURLException {
        if (uri.startsWith(prefix)) {
            this.prefix.append(prefix);
            uri = uri.substring(prefix.length());
            File f = new File(context);
            if (f.isFile()) {
                this.context = f.getParentFile().toURL();
            } else {
                this.context = f.toURL();
            }
        } else {
            throw new RuntimeException("The current URI (" 
                + uri + ") doesn't start with given prefix (" 
                + prefix + ")"
            );
        }
    }

    /**
     * Redirect the client to a new URL
     */
    public abstract void redirect(String newURL) throws IOException;
    
    // Request methods

    /**
     * Returns the request view
     */
    public String getView() {
        return this.view;
    }
    
    // Response methods

    /**
     * Set a status code
     */
    public void setStatus(int statusCode) {
    }

    // Object model method

    /**
     * Returns a Map containing environment specific objects
     */
    public Map getObjectModel() {
        return this.objectModel;
    }

    // EntityResolver method

    /**
     * Resolve an entity.
     */
    public InputSource resolveEntity(String publicId, String systemId)
    throws SAXException, IOException {
        if (systemId == null) throw new SAXException("Invalid System ID");

        if (systemId.length() == 0)
            return new InputSource(this.context.toExternalForm());
        if (systemId.indexOf(":/") > 0)
            return new InputSource(systemId);
        if (systemId.charAt(0) == '/')
            return new InputSource(this.context.getProtocol() + ":" + systemId);
        return new InputSource(new URL(this.context,systemId).toExternalForm());
    }
}
