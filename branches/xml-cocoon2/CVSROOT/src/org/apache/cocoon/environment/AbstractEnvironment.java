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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.apache.cocoon.environment.Environment;

import org.apache.avalon.AbstractLoggable;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.apache.log.LogKit;

public abstract class AbstractEnvironment extends AbstractLoggable implements Environment {

    /** The current uri in progress */
    protected ArrayList uris = new ArrayList();

    /** The current prefix to strip off from the request uri */
    protected StringBuffer prefix = new StringBuffer();

    /** The View requested */
    protected String view = null;

    /** The Action requested */
    protected String action = null;

     /** The Context path */
    protected URL context = null;

    /** The servlet object model */
    protected HashMap objectModel = null;

    /**
     * Constructs the abstract enviornment
     */
    public AbstractEnvironment(String uri, String view, String context)
    throws MalformedURLException {
        this(uri, view, new File(context), null);
    }

    /**
     * Constructs the abstract enviornment
     */
    public AbstractEnvironment(String uri, String view, String context, String action)
    throws MalformedURLException {
        this(uri, view, new File(context), action);
    }

    /**
     * Constructs the abstract enviornment
     */
    public AbstractEnvironment(String uri, String view, File context)
    throws MalformedURLException {
        this(uri, view, context, null);
    }

    /**
     * Constructs the abstract enviornment
     */
    public AbstractEnvironment(String uri, String view, File context, String action)
    throws MalformedURLException {
        this.pushURI(uri);
        this.view = view;
        this.context = context.toURL();
        this.action = action;
        this.objectModel = new HashMap();
    }

    // Sitemap methods

    /**
     * Returns the uri in progress. The prefix is stripped off
     */
    public String getURI() {
        return (String)this.uris.get(this.uris.size()-1);
    }

    /**
     * Adds an prefix to the overall stripped off prefix from the request uri
     */
    public void changeContext(String prefix, String context)
    throws MalformedURLException {
        String uri = (String)this.uris.get(this.uris.size()-1);
        LogKit.getLoggerFor("cocoon").debug("Changing Cocoon context(" + context + ") to prefix(" + prefix + ")");
        LogKit.getLoggerFor("cocoon").debug("\tfrom context(" + this.context.toExternalForm() + ") and prefix(" + this.prefix + ")");
        LogKit.getLoggerFor("cocoon").debug("\tat URI " + uri);
        if (uri.startsWith(prefix)) {
            this.prefix.append(prefix);
            uri = uri.substring(prefix.length());

            // if we got a absolute context or one with a protocol resolve it
            if (context.charAt(0) == '/') {
                this.context = new URL("file:" + context);
            }else if (context.indexOf(':') > 1) {
                this.context = new URL(context);
            }else {
                this.context = new URL(this.context, context);
            }
            File f = new File(this.context.getFile());
            if (f.isFile()) {
                this.context = f.getParentFile().toURL();
            } else {
                this.context = f.toURL();
            }
        } else {
            getLogger().error("The current URI ("
                + uri + ") doesn't start with given prefix ("
                + prefix + ")"
            );
            throw new RuntimeException("The current URI ("
                + uri + ") doesn't start with given prefix ("
                + prefix + ")"
            );
        }
        LogKit.getLoggerFor("cocoon").debug("New context is " + this.context.toExternalForm());
        this.uris.set(this.uris.size()-1, uri);
    }

    /**
     * Redirect the client to a new URL
     */
    public abstract void redirect(boolean sessionmode, String newURL) throws IOException;

    // Request methods

    /**
     * Returns the request view
     */
    public String getView() {
        return this.view;
    }

    /**
     * Returns the request action
     */
    public String getAction() {
        return this.action;
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
        if (systemId.indexOf(":") > 1)
            return new InputSource(systemId);
        if (systemId.charAt(0) == '/')
            return new InputSource(this.context.getProtocol() + ":" + systemId);
        return new InputSource(new URL(this.context,systemId).toExternalForm());
    }

    /**
     * Push a new URI for processing
     */
    public void pushURI(String uri) {
        this.uris.add(uri);
    }

    /**
     * Pop last pushed URI
     */
    public String popURI() {
        String uri = (String)this.uris.get(this.uris.size()-1);
        this.uris.remove(this.uris.size()-1);
        return uri;
    }
}
