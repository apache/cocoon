/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

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
 * @version CVS $Id: AbstractCommandLineEnvironment.java,v 1.9 2004/01/10 14:38:19 cziegeler Exp $
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