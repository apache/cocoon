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

import org.apache.avalon.excalibur.pool.Recyclable;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceValidity;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

/**
 * A wrapper around a <code>Source</code> that reduces the number of calls to
 * <code>Source.getLastModified()</code> which can be a costly operation.
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: DelayedRefreshSourceWrapper.java,v 1.2 2003/03/16 17:49:13 vgritsenko Exp $
 */
public final class DelayedRefreshSourceWrapper
    implements Source {

    private Source source;

    private long delay;

    private long nextCheckTime = 0;

    private long lastModified = 0;

    /**
     * Creates a wrapper for a <code>Source</code> which ensures that
     * <code>Source.getLastModified()</code> won't be called more than once per
     * <code>delay</code> milliseconds period.
     *
     * @param source the wrapped <code>Source</code>
     * @param delay  the last-modified refresh delay, in milliseconds
     */
    public DelayedRefreshSourceWrapper(Source source, long delay) {
        this.source = source;
        this.delay = delay;
    }

    public final InputStream getInputStream()
    throws SourceException, IOException {
        return this.source.getInputStream();
    }

    public final String getURI() {
        return this.source.getURI();
    }

    /**
     *  Get the Validity object. This can either wrap the last modification
     *  date or the expires information or...
     *  If it is currently not possible to calculate such an information
     *  <code>null</code> is returned.
     */
    public SourceValidity getValidity() {
        return this.source.getValidity();
    }

    /**
     * Return the protocol identifier.
     */
    public String getScheme() {
        return this.source.getScheme();
    }

    /**
     * 
     * @see org.apache.excalibur.source.Source#exists()
     */
    public boolean exists() {
        return this.source.exists();
    }
    
    /**
     * Get the last modification time for the wrapped <code>Source</code>. The
     * age of the returned information is guaranteed to be lower than or equal to
     * the delay specified in the constructor.
     * <p>
     * This method is also thread-safe, even if the underlying Source is not.
     *
     * @return the last modification time.
     */
    public final long getLastModified() {

        // Do we have to refresh the source ?
        if (System.currentTimeMillis() >= nextCheckTime) {
            // Yes
            this.refresh();
        }

        return this.lastModified;
    }

    /**
     * Force the refresh of the wrapped <code>Source</code>, even if the refresh period
     * isn't over, and starts a new period.
     * <p>
     * This method is thread-safe, even if the underlying Source is not.
     */
    public synchronized final void refresh() {

        this.nextCheckTime = System.currentTimeMillis() + this.delay;
        // Refresh modifiable sources
        this.source.refresh();

        // Keep the last modified date
        this.lastModified = source.getLastModified();
    }

    public final long getContentLength() {
        return this.source.getContentLength();
    }

    /**
     * The mime-type of the content described by this object.
     * If the source is not able to determine the mime-type by itself
     * this can be <code>null</code>.
     */
    public String getMimeType() {
        return this.source.getMimeType();
    }

    public final void recycle() {
        if (this.source instanceof Recyclable) {
            ((Recyclable)this.source).recycle();
        }
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
