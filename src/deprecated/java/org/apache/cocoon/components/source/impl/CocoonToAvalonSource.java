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
package org.apache.cocoon.components.source.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.apache.avalon.excalibur.pool.Recyclable;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.environment.ModifiableSource;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceNotFoundException;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.TimeStampValidity;
import org.apache.excalibur.xml.sax.XMLizable;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * This source objects wraps an obsolete Cocoon Source object
 * to avoid recoding existing source objects.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: CocoonToAvalonSource.java,v 1.3 2004/03/01 03:50:59 antonio Exp $
 */
public final class CocoonToAvalonSource
    implements Source, XMLizable, Recyclable {

    /** The real source */
    protected org.apache.cocoon.environment.Source source;

    /** The protocol */
    protected String protocol;

    /**
     * Constructor
     */
    public CocoonToAvalonSource(
        String location,
        org.apache.cocoon.environment.Source source) {
        this.source = source;
        int pos = location.indexOf(':');
        this.protocol = location.substring(0, pos);
    }

    /**
     * Return the protocol identifier.
     */
    public String getScheme() {
        return this.protocol;
    }

    /**
     * @see org.apache.excalibur.source.Source#exists()
     */
    public boolean exists() {
        try {
            this.getInputStream();
            return true;
        } catch (Exception local) {
            return false;
        }
    }

    /**
     * Return an <code>InputStream</code> object to read from the source.
     */
    public InputStream getInputStream() throws IOException, SourceException {
        try {
            return this.source.getInputStream();
        } catch (ResourceNotFoundException rnfe) {
            throw new SourceNotFoundException("Source not found.", rnfe);
        } catch (ProcessingException pe) {
            throw new SourceException("ProcessingException", pe);
        }
    }

    /**
     * Return the unique identifer for this source
     */
    public String getURI() {
        return this.source.getSystemId();
    }

    /**
     *  Get the Validity object. This can either wrap the last modification
     *  date or the expires information or...
     *  If it is currently not possible to calculate such an information
     *  <code>null</code> is returned.
     */
    public SourceValidity getValidity() {
        if (this.source.getLastModified() > 0) {
            return new TimeStampValidity(this.source.getLastModified());
        }
        return null;
    }

    /**
     * Refresh this object and update the last modified date
     * and content length.
     */
    public void refresh() {
        if (this.source instanceof ModifiableSource) {
            ((ModifiableSource) this.source).refresh();
        }
    }

    /**
     * The mime-type of the content described by this object.
     * If the source is not able to determine the mime-type by itself
     * this can be null.
     */
    public String getMimeType() {
        return null;
    }

    /**
     * Stream content to the content handler
     */
    public void toSAX(ContentHandler contentHandler) throws SAXException {
        this.source.toSAX(contentHandler);
    }

    /**
     * Recyclable
     */
    public void recycle() {
        this.source.recycle();
    }

    /**
     * Return the content length of the content or -1 if the length is
     * unknown
     */
    public long getContentLength() {
        return this.source.getContentLength();
    }

    /**
     * Get the last modification date of the source or 0 if it
     * is not possible to determine the date.
     */
    public long getLastModified() {
        return this.source.getLastModified();
    }

    /**
     * Get the value of a parameter.
     * Using this it is possible to get custom information provided by the
     * source implementation, like an expires date, HTTP headers etc.
     */
    public String getParameter(String name) {
        return null;
    }

    /**
     * Get the value of a parameter.
     * Using this it is possible to get custom information provided by the
     * source implementation, like an expires date, HTTP headers etc.
     */
    public long getParameterAsLong(String name) {
        return 0;
    }

    /**
     * Get parameter names
     * Using this it is possible to get custom information provided by the
     * source implementation, like an expires date, HTTP headers etc.
     */
    public Iterator getParameterNames() {
        return java.util.Collections.EMPTY_LIST.iterator();
    }

}
