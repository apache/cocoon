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

import org.apache.avalon.framework.component.ComponentManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.ModifiableSource;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceResolver;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;

/**
 * This source objects wraps an Avalon Excalibur Source to get
 * an obsolete Cocoon Source object for the use of the deprecated
 * {@link org.apache.cocoon.environment.SourceResolver#resolve(String)}
 * method.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: AvalonToCocoonSource.java,v 1.5 2004/03/05 13:02:41 bdelacretaz Exp $
 */
public final class AvalonToCocoonSource
    implements ModifiableSource {

    /** The real source */
    protected Source source;

    /** The source resolver */
    protected SourceResolver resolver;

    /** The environment */
    protected Environment environment;

    /** The manager */
    protected ComponentManager manager;
    
    /**
     * Constructor
     */
    public AvalonToCocoonSource(Source source,
                                SourceResolver resolver,
                                Environment environment,
                                ComponentManager manager) {
        this.source = source;
        this.resolver = resolver;
        this.environment = environment;
        this.manager = manager;
    }

    /**
     * Get the last modification date of the source or 0 if it
     * is not possible to determine the date.
     */
    public long getLastModified() {
        return this.source.getLastModified();
    }

    /**
     * Get the content length of the source or -1 if it
     * is not possible to determine the length.
     */
    public long getContentLength() {
        return this.source.getContentLength();
    }

    /**
     * Return an <code>InputStream</code> object to read from the source.
     */
    public InputStream getInputStream()
    throws ProcessingException, IOException {
        try {
            return this.source.getInputStream();
        } catch (SourceException e) {
            throw SourceUtil.handle(e);
        }
    }

    /**
     * Return an <code>InputSource</code> object to read the XML
     * content.
     *
     * @return an <code>InputSource</code> value
     * @exception ProcessingException if an error occurs
     * @exception IOException if an error occurs
     */
    public InputSource getInputSource()
    throws ProcessingException, IOException {
        try {
            InputSource newObject = new InputSource(this.source.getInputStream());
            newObject.setSystemId(this.getSystemId());
            return newObject;
        } catch (SourceException se) {
            throw SourceUtil.handle(se);
        }
    }

    /**
     * Return the unique identifer for this source
     */
    public String getSystemId() {
        return this.source.getURI();
    }

    public void recycle() {
        this.resolver.release(this.source);
        this.source = null;
        this.environment = null;
    }

    public void refresh() {
        this.source.refresh();
    }

    /**
     * Stream content to a content handler or to an XMLConsumer.
     *
     * @throws SAXException if failed to parse source document.
     */
    public void toSAX(ContentHandler handler)
    throws SAXException {
        try {
            SourceUtil.parse(this.manager, this.source, handler);
        } catch (ProcessingException pe) {
            throw new SAXException("ProcessingException during streaming.", pe);
        } catch (IOException ioe) {
            throw new SAXException("IOException during streaming.", ioe);
        }
    }

}
