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

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.CascadingIOException;
import org.apache.cocoon.Constants;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.environment.AbstractEnvironment;
import org.apache.cocoon.environment.internal.EnvironmentHelper;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceResolver;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;

/**
 * This environment is used to save the requested file to disk.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Id: AbstractCommandLineEnvironment.java,v 1.10 2004/03/08 14:02:49 cziegeler Exp $
 */

public abstract class AbstractCommandLineEnvironment
extends AbstractEnvironment {

    protected String contentType;
    protected int contentLength;
    protected int statusCode;

    public AbstractCommandLineEnvironment(String uri,
                                          String view,
                                          File context,
                                          OutputStream stream,
                                          Logger log)
    throws MalformedURLException {
        super(uri, view);
        this.enableLogging(log);
        this.outputStream = stream;
        this.statusCode = 0;
    }

    /**
     * Redirect to the given URL
     */
    public void redirect(String newURL, boolean global, boolean permanent) 
    throws IOException {

        // fix all urls created with request.getScheme()+... etc.
        if (newURL.startsWith("cli:/")) {
            int pos = newURL.indexOf('/', 6);
            newURL = newURL.substring(pos+1);
        }

        // fix all relative urls to use to cocoon: protocol
        if (newURL.indexOf(":") == -1) {
            newURL = "cocoon:/" + newURL;
        }

        // FIXME: this is a hack for the links view
        ServiceManager manager = EnvironmentHelper.getSitemapServiceManager();
        SourceResolver resolver = null;
        try {
            resolver = (SourceResolver)manager.lookup(SourceResolver.ROLE);
            if (newURL.startsWith("cocoon:")
                && this.getView() != null
                && this.getView().equals(Constants.LINK_VIEW)) {
            
                // as the internal cocoon protocol is used the last
                // serializer is removed from it! And therefore
                // the LinkSerializer is not used.
                // so we create one without Avalon...
                org.apache.cocoon.serialization.LinkSerializer ls =
                    new org.apache.cocoon.serialization.LinkSerializer();
                ls.setOutputStream(this.outputStream);
    
                Source redirectSource = null;
                try {
                    redirectSource = resolver.resolveURI(newURL);
                    SourceUtil.parse( manager, redirectSource, ls);
                } catch (SourceException se) {
                    throw new CascadingIOException("SourceException: " + se, se);
                } catch (SAXException se) {
                    throw new CascadingIOException("SAXException: " + se, se);
                } catch (ProcessingException pe) {
                    throw new CascadingIOException("ProcessingException: " + pe, pe);
                } finally {
                    resolver.release( redirectSource );
                }
                
            } else {
                Source redirectSource = null;
                try {
                    redirectSource = resolver.resolveURI(newURL);
                    InputStream is = redirectSource.getInputStream();
                    byte[] buffer = new byte[8192];
                    int length = -1;
    
                    while ((length = is.read(buffer)) > -1) {
                        this.outputStream.write(buffer, 0, length);
                    }
                } catch (SourceException se) {
                    throw new CascadingIOException("SourceException: " + se, se);
                } finally {
                    resolver.release( redirectSource);
                }
            }
        } catch (ServiceException se) {
            throw new CascadingIOException("Unable to get source resolver.", se);
        } finally {
            manager.release(resolver);
        }
    }

    /**
     * Set the StatusCode
     */
    public void setStatus(int statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * Get the StatusCode
     */
    public int getStatus() {
        return statusCode;
    }

    /**
     * Set the ContentType
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Set the ContentLength
     */
    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }

    /**
     * Get the ContentType
     */
    public String getContentType() {
        return this.contentType;
    }
    
    /**
     * Always return <code>true</code>.
     */
    public boolean isExternal() {
        return true;
    }

}