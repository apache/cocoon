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
package org.apache.cocoon.components.modules.input;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;
import java.util.Vector;

/**
 * Loacte a resource in a resource tree. Any attribute name is interpreted as a
 * URI with the last part being the resource name unless it ends with a slash. 
 * The URI is checked if the resource exists and the URI is returned. If the
 * resource does not exist, the URI is shortened until the resource name is found
 * and the new URI is returned. If no resource with the given name exists, null
 * is returned. 
 * 
 * <p>A use case is to locate the closest menu file or when moving a site from
 * a filesystem path == URL system from a httpd to Cocoon and provide similar
 * functions to .htaccess files.</p>
 * 
 * <p>Example: for context:/some/path/to/a/file.xml the following URIs
 * are tested: context:/some/path/to/a/file.xml, context:/some/path/to/file.xml,
 * context:/some/path/file.xml, context:/some/file.xml, and context:/file.xml.
 * For the attribute name context:/some/path/foo/ tests context:/some/path/foo/,
 * context:/some/path/, context:/some/, and context:/ are tested.</p> 
 * 
 * <p>The getAttribute() method will return the URI for the first match while 
 * getAttributeValues() will return an array of all existing paths. 
 * getAttributeNames() will return an Iterator to an empty collection.</p>
 * 
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @version CVS $Id: LocateResource.java,v 1.4 2004/03/08 13:58:30 cziegeler Exp $
 */
public class LocateResource extends AbstractInputModule implements Serviceable, ThreadSafe {

    protected static Collection col = null;
    static {
        col = new TreeSet();
    }

    protected ServiceManager manager = null;

    /**
     * Calculate the minimal length of the URL, that is the position
     * of the first ":" if a protocol is provided or otherwise 0.
     * @param name
     * @return minimal length
     */
    protected int calculateMinLen(String name) {

        int minLen = name.indexOf(':');
        minLen = (minLen == -1 ? 0 : minLen);

        return minLen;
    }

    /**
     * Remove one path element from the URL unless minimum length has
     * been reached.
     * 
     * @param urlstring
     * @param minLen
     * @return shortened URI
     */
    protected String shortenURI(String urlstring, int minLen) {

        int idx = urlstring.lastIndexOf('/');
        idx = (idx <= minLen + 1) ? minLen : idx;
        urlstring = urlstring.substring(0, idx);

        return urlstring;
    }

    /** 
     * if the url does not end with a "/", keep the last part in
     * order to add it later again after traversing up
     */
    protected String extractFilename(String urlstring) {

        String filename = "";
        if (!urlstring.endsWith("/")) {
            int idx = urlstring.lastIndexOf('/');
            filename = urlstring.substring(idx);
        }

        return filename;
    }

    /**
     * Locate a resource with the given URL consisting of urlstring + filename.
     * The filename is appended each time the path is shortened. Returns the first
     * existing occurance.
     * 
     * @param urlstring
     * @param filename
     * @param minLen
     * @return urlstring if resource was found, <code>null</code> otherwise
     */
    protected String locateResource(String urlstring, String filename, int minLen) {
        String sourcename = null;
        Source src = null;
        SourceResolver resolver = null;
        boolean found = false;
        try {
            resolver =
                (SourceResolver) this.manager.lookup(
                    org.apache.excalibur.source.SourceResolver.ROLE);
            while (!found && urlstring.length() > minLen) {
                sourcename = urlstring + filename;
                try {
                    src = resolver.resolveURI(sourcename);
                    if (src.exists())
                        found = true;
                } catch (Exception e) {
                    if (this.getLogger().isWarnEnabled())
                        this.getLogger().warn("Exception resolving URL " + sourcename, e);
                } finally {
                    resolver.release(src);
                }
                if (!found) {
                    urlstring = shortenURI(urlstring, minLen);
                }
            }
        } catch (ServiceException e1) {
            if (this.getLogger().isErrorEnabled())
                this.getLogger().error("Exception obtaining source resolver ", e1);
        } finally {
            if (resolver != null) {
                this.manager.release(resolver);
            }
        }
        return (found ? urlstring : null);
    }

    /**
     * Set the current <code>ServiceManager</code> instance used by this
     * <code>Serviceable</code>.
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.modules.input.InputModule#getAttribute(java.lang.String, org.apache.avalon.framework.configuration.Configuration, java.util.Map)
     */
    public Object getAttribute(String name, Configuration modeConf, Map objectModel)
        throws ConfigurationException {

        String urlstring = name;
        String filename = extractFilename(urlstring);
        int minLen = calculateMinLen(name);
        if (filename != "") {
            urlstring = shortenURI(urlstring, minLen);
        }

        String result = locateResource(urlstring, filename, minLen);
        result = (result == null? result : result + filename);
        if (this.getLogger().isDebugEnabled())
            this.getLogger().debug(
                "located " + name + " @ " + result);
        return result;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.modules.input.InputModule#getAttributeNames(org.apache.avalon.framework.configuration.Configuration, java.util.Map)
     */
    public Iterator getAttributeNames(Configuration modeConf, Map objectModel)
        throws ConfigurationException {
        // return an iterator to an empty collection
        return LocateResource.col.iterator();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.modules.input.InputModule#getAttributeValues(java.lang.String, org.apache.avalon.framework.configuration.Configuration, java.util.Map)
     */
    public Object[] getAttributeValues(String name, Configuration modeConf, Map objectModel)
        throws ConfigurationException {

        Vector uris = null;
        String urlstring = name;
        String filename = extractFilename(urlstring);
        int minLen = calculateMinLen(name);
        if (filename != "") {
            urlstring = shortenURI(urlstring, minLen);
        }

        while (urlstring != null && urlstring.length() > minLen) {
            urlstring = this.locateResource(urlstring, filename, minLen);
            if (urlstring != null) {
                if (uris == null)
                    uris = new Vector();
                if (this.getLogger().isDebugEnabled())
                    this.getLogger().debug("-> located " + name + " @ " + urlstring + filename);
                uris.add(urlstring + filename);
                urlstring = shortenURI(urlstring, minLen);
            }
        }
        return (uris == null ? null : uris.toArray());
    }

}
