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
 * @version CVS $Id: AvalonToCocoonSource.java,v 1.4 2003/12/23 15:28:33 joerg Exp $
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
