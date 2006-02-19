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
package org.apache.cocoon.environment.commandline;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;

import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.environment.Context;
import org.apache.cocoon.environment.impl.AbstractContext;

/**
 *
 * Implements the {@link org.apache.cocoon.environment.Context} interface
 * @version $Id$
 */
public class CommandLineContext extends AbstractContext implements Context {

    /** The context directory path*/
    private String contextDir;

    /**
     * Constructs a CommandlineContext object from a ServletContext object
     */
    public CommandLineContext(String contextDir, Logger logger) {
        super(logger);
        String contextDirPath = new File(contextDir).getAbsolutePath();
        // store contextDirPath as is don't remove trailing /.
        this.contextDir = contextDirPath;
    }

    /**
     * @see org.apache.cocoon.environment.Context#getAttribute(java.lang.String)
     */
    public Object getAttribute(String name) {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("CommandlineContext: getAttribute=" + name);
        }
        return super.getAttribute(name);
    }

    /**
     * @see org.apache.cocoon.environment.Context#setAttribute(java.lang.String, java.lang.Object)
     */
    public void setAttribute(String name, Object value) {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("CommandlineContext: setAttribute=" + name);
        }
        super.setAttribute(name, value);
    }

    /**
     * @see org.apache.cocoon.environment.Context#removeAttribute(java.lang.String)
     */
    public void removeAttribute(String name) {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("CommandlineContext: removeAttribute=" + name);
        }
        super.removeAttribute(name);
    }

    /**
     * @see org.apache.cocoon.environment.Context#getAttributeNames()
     */
    public Enumeration getAttributeNames() {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("CommandlineContext: getAttributeNames");
        }
        return super.getAttributeNames();
    }

    /**
     * @see org.apache.cocoon.environment.Context#getResource(java.lang.String)
     */
    public URL getResource(String path) throws MalformedURLException {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("CommandlineContext: getResource=" + path);
        }
        // rely on File to build correct File and URL
        File f = new File( contextDir, path );
        if (!f.exists()) return null;
        return f.toURL();
    }

    /**
     * @see org.apache.cocoon.environment.Context#getRealPath(java.lang.String)
     */
    public String getRealPath(String path) {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("CommandlineContext: getRealPath=" + path);
        }
        // rely on File to build correct File and URL
        File f = new File( this.contextDir, path );
        return f.getAbsolutePath();
    }

    /**
     * @see org.apache.cocoon.environment.Context#getMimeType(java.lang.String)
     */
    public String getMimeType(String file) {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("CommandlineContext: getMimeType=" + file);
        }
        //return servletContext.getMimeType(file);
        return null;
    }

    /**
     * @see org.apache.cocoon.environment.Context#getInitParameter(java.lang.String)
     */
    public String getInitParameter(String name) {
        this.logger.debug("CommandlineContext: getInitParameter=" + name);
        return super.getInitParameter(name);
    }

    /**
     * @see org.apache.cocoon.environment.Context#getResourceAsStream(java.lang.String)
     */
    public InputStream getResourceAsStream(String path){
        this.logger.debug("CommandlineContext: getResourceAsStream "+path);
        return super.getResourceAsStream(path);
    }
}
