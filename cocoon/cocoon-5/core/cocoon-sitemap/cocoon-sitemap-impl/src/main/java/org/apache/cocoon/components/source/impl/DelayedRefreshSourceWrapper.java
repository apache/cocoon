/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

/**
 * A wrapper around a <code>Source</code> that reduces the number of calls to
 * <code>Source.getLastModified()</code> which can be a costly operation.
 *
 * @version $Id$
 */
public final class DelayedRefreshSourceWrapper
    implements Source, Recyclable {

    private Source source;

    private long delay;

    private long nextCheckTime;

    private long lastModified;

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

    /**
     * @see org.apache.excalibur.source.Source#getInputStream()
     */
    public final InputStream getInputStream()
    throws SourceException, IOException {
        return this.source.getInputStream();
    }

    /**
     * @see org.apache.excalibur.source.Source#getURI()
     */
    public final String getURI() {
        return this.source.getURI();
    }

    /**
     * @see org.apache.excalibur.source.Source#getValidity()
     */
    public SourceValidity getValidity() {
        return this.source.getValidity();
    }

    /**
     * @see org.apache.excalibur.source.Source#getScheme()
     */
    public String getScheme() {
        return this.source.getScheme();
    }

    /**
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
     * @see org.apache.excalibur.source.Source#getLastModified()
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
     * @see org.apache.excalibur.source.Source#refresh()
     */
    public synchronized final void refresh() {

        this.nextCheckTime = System.currentTimeMillis() + this.delay;
        // Refresh modifiable sources
        this.source.refresh();

        // Keep the last modified date
        this.lastModified = source.getLastModified();
    }

    /**
     * @see org.apache.excalibur.source.Source#getContentLength()
     */
    public final long getContentLength() {
        return this.source.getContentLength();
    }

    /**
     * @see org.apache.excalibur.source.Source#getMimeType()
     */
    public String getMimeType() {
        return this.source.getMimeType();
    }

    /**
     * @see org.apache.avalon.excalibur.pool.Recyclable#recycle()
     */
    public final void recycle() {
        if (this.source instanceof Recyclable) {
            ((Recyclable)this.source).recycle();
        }
    }
}
