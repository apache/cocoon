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
 * @version CVS $Id: DelayedRefreshSourceWrapper.java,v 1.4 2004/03/08 14:01:55 cziegeler Exp $
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
    
    /**
     * Get the real source
     */
    public Source getSource() {
        return this.source;
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
