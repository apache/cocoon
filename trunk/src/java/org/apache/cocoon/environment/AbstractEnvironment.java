/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.environment;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.cocoon.Constants;
import org.apache.cocoon.util.BufferedOutputStream;
import org.apache.commons.collections.iterators.IteratorEnumeration;

/**
 * Base class for any environment
 *
 * @author <a href="mailto:bluetkemeier@s-und-n.de">Bj&ouml;rn L&uuml;tkemeier</a>
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: AbstractEnvironment.java,v 1.25 2004/03/08 14:02:42 cziegeler Exp $
 */
public abstract class AbstractEnvironment 
    extends AbstractLogEnabled 
    implements Environment {

    /** The current uri in progress */
    protected String uri;

    /** The prefix */
    protected String prefix;
    
    /** The View requested */
    protected String view;

    /** The Action requested */
    protected String action;

    /** The object model */
    protected HashMap objectModel;

    /** The attributes */
    private Map attributes = new HashMap();

    /** The secure Output Stream */
    protected BufferedOutputStream secureOutputStream;

    /** The real output stream */
    protected OutputStream outputStream;

    /**
     * Constructs the abstract environment
     */
    public AbstractEnvironment(String uri, String view) {
        this(uri, view, null);
    }

    /**
     * Constructs the abstract environment
     */
    public AbstractEnvironment(String uri, String view, String action) {
        this.uri = uri;
        this.view = view;
        this.action = action;
        this.objectModel = new HashMap();
    }

    /**
     * Allow implementations to set view later than in super() constructor.
     * View can be set only once, and should be set in implementation's constructor.
     */
    protected void setView(String view) {
        if (this.view != null) {
            throw new IllegalStateException("View was already set on this environment");
        }
        this.view = view;
    }

    /**
     * Allow implementations to set action later than in super() constructor
     * Action can be set only once, and should be set in implementation's constructor.
     */
    protected void setAction(String action) {
        if (this.action != null) {
            throw new IllegalStateException("Action was already set on this environment");
        }
        this.action = action;
    }

    /**
     * Helper method to extract the view name from the request.
     */
    protected static String extractView(Request request) {
        return request.getParameter(Constants.VIEW_PARAM);
    }

    /**
     * Helper method to extract the action name from the request.
     */
    protected static String extractAction(Request req) {
        String action = req.getParameter(Constants.ACTION_PARAM);
        if (action != null) {
            /* TC: still support the deprecated syntax */
            return action;
        } else {
            for(Enumeration e = req.getParameterNames(); e.hasMoreElements(); ) {
                String name = (String)e.nextElement();
                if (name.startsWith(Constants.ACTION_PARAM_PREFIX)) {
                    if (name.endsWith(".x") || name.endsWith(".y")) {
                        return name.substring(Constants.ACTION_PARAM_PREFIX.length(),name.length()-2);
                    } else {
                        return name.substring(Constants.ACTION_PARAM_PREFIX.length());
                    }
                }
            }
            return null;
        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Environment#getURI()
     */
    public String getURI() {
        return this.uri;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Environment#getURIPrefix()
     */
    public String getURIPrefix() {
        return this.prefix;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Environment#setURI(java.lang.String)
     */
    public void setURI(String prefix, String value) {
        this.prefix = prefix;
        this.uri = value;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Environment#getView()
     */
    public String getView() {
        return this.view;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Environment#getAction()
     */
    public String getAction() {
        return this.action;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Environment#setStatus(int)
     */
    public void setStatus(int statusCode) {
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Environment#getObjectModel()
     */
    public Map getObjectModel() {
        return this.objectModel;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Environment#isResponseModified(long)
     */
    public boolean isResponseModified(long lastModified) {
        return true; // always modified
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Environment#setResponseIsNotModified()
     */
    public void setResponseIsNotModified() {
        // does nothing
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Environment#getAttribute(java.lang.String)
     */
    public Object getAttribute(String name) {
        return this.attributes.get(name);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Environment#setAttribute(java.lang.String, java.lang.Object)
     */
    public void setAttribute(String name, Object value) {
        this.attributes.put(name, value);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Environment#removeAttribute(java.lang.String)
     */
    public void removeAttribute(String name) {
        this.attributes.remove(name);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Environment#getAttributeNames()
     */
    public Enumeration getAttributeNames() {
        return new IteratorEnumeration(this.attributes.keySet().iterator());
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Environment#getOutputStream(int)
     */
    public OutputStream getOutputStream(int bufferSize)
    throws IOException {
        if (bufferSize == -1) {
            if (this.secureOutputStream == null) {
                this.secureOutputStream = new BufferedOutputStream(this.outputStream);
            }
            return this.secureOutputStream;
        } else if (bufferSize == 0) {
            return this.outputStream;
        } else {
            this.outputStream = new java.io.BufferedOutputStream(this.outputStream, bufferSize);
            return this.outputStream;
        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Environment#tryResetResponse()
     */
    public boolean tryResetResponse()
    throws IOException {
        if (this.secureOutputStream != null) {
            this.secureOutputStream.clearBuffer();
            return true;
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Environment#commitResponse()
     */
    public void commitResponse()
    throws IOException {
        if (this.secureOutputStream != null) {
            this.secureOutputStream.realFlush();
        } else if ( this.outputStream != null ){
            this.outputStream.flush();
        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Environment#startingProcessing()
     */
    public void startingProcessing() {
        // do nothing here
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Environment#finishingProcessing()
     */
    public void finishingProcessing() {
        // do nothing here
    }
}
