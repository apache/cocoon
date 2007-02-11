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

import org.apache.commons.collections.iterators.IteratorEnumeration;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.cocoon.environment.Context;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.HashMap;
import java.io.InputStream;

/**
 *
 * Implements the {@link org.apache.cocoon.environment.Context} interface
 * @author ?
 * @version CVS $Id: CommandLineContext.java,v 1.3 2004/03/05 13:02:54 bdelacretaz Exp $
 */

public class CommandLineContext extends AbstractLogEnabled implements Context {

    /** The context directory path*/
    private String contextDir;

    /** The context attributes */
    private Map attributes;

    /**
     * Constructs a CommandlineContext object from a ServletContext object
     */
    public CommandLineContext (String contextDir) {
        String contextDirPath = new File(contextDir).getAbsolutePath();
        // store contextDirPath as is don't remove trailing /.
        this.contextDir = contextDirPath;
        this.attributes = new HashMap();
    }

    public Object getAttribute(String name) {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("CommandlineContext: getAttribute=" + name);
        }
        return this.attributes.get(name);
    }

    public void setAttribute(String name, Object value) {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("CommandlineContext: setAttribute=" + name);
        }
        this.attributes.put(name, value);
    }

    public void removeAttribute(String name) {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("CommandlineContext: removeAttribute=" + name);
        }
        this.attributes.remove(name);
    }

    public Enumeration getAttributeNames() {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("CommandlineContext: getAttributeNames");
        }
        return new IteratorEnumeration(this.attributes.keySet().iterator());
    }

    public URL getResource(String path) throws MalformedURLException {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("CommandlineContext: getResource=" + path);
        }
        // rely on File to build correct File and URL
        File f = new File( contextDir, path );
        if (!f.exists()) return null;
        return f.toURL();
    }

    public String getRealPath(String path) {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("CommandlineContext: getRealPath=" + path);
        }
        // rely on File to build correct File and URL
        File f = new File( this.contextDir, path );
        return f.getAbsolutePath();
    }

    public String getMimeType(String file) {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("CommandlineContext: getMimeType=" + file);
        }
        //return servletContext.getMimeType(file);
        return null;
    }

    public String getInitParameter(String name) {
        getLogger().debug("CommandlineContext: getInitParameter=" + name);
        return null;
    }

    public InputStream getResourceAsStream(String path){
        getLogger().debug("CommandlineContext: getResourceAsStream "+path);
    return null;
    }
}
