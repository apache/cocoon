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
package org.apache.cocoon.environment;

import org.apache.excalibur.source.SourceException;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.source.URLSource;
import org.apache.cocoon.components.url.URLFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * A <code>SourceResolver</code> based on a <code>URLFactory</code>.
 * @deprecated by the new source resolving
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: URLFactorySourceResolver.java,v 1.2 2004/03/05 13:02:41 bdelacretaz Exp $
 */

public class URLFactorySourceResolver implements SourceResolver {

    /** The component manager */
    protected ComponentManager manager;

    /** The URL factory */
    protected URLFactory urlFactory;

    /**
     * Creates an <code>URLFactorySourceResolver</code> with a component manager.
     * The <code>URLFactory</code> is looked up in the component manager.
     */
    public URLFactorySourceResolver(ComponentManager manager)
      throws ComponentException {
        this.manager = manager;
        this.urlFactory = (URLFactory)manager.lookup(URLFactory.ROLE);
    }

    /**
     * Creates an <code>URLFactorySourceResolver</code> with a component manager and
     * a <code>URLFactory</code> that will be used to resolve URLs.
     */
    public URLFactorySourceResolver(URLFactory factory, ComponentManager manager) {
        this.urlFactory = factory;
        this.manager = manager;
    }

    /**
     * Resolve the source.
     *
     * @param systemID This is either a system identifier
     *        (<code>java.net.URL</code>) or a local file.
     */
    public Source resolve(String systemID)
      throws ProcessingException, SAXException, IOException {

        URL url = this.urlFactory.getURL(systemID);
        return new URLSource(url, this.manager);
    }

    /**
     * Get a <code>Source</code> object.
     */
    public org.apache.excalibur.source.Source resolveURI(final String location)
    throws MalformedURLException, IOException, SourceException
    {
        return this.resolveURI(location, null, null);
    }

    /**
     * Get a <code>Source</code> object.
     */
    public org.apache.excalibur.source.Source resolveURI(final String location,
                                                                String baseURI,
                                                                final Map    parameters)
    throws MalformedURLException, IOException, SourceException {
        throw new RuntimeException("URLFactorySourceResolver.resolveURI() is not implemented yet.");
    }

    /**
     * Releases a resolved resource
     */
    public void release( final org.apache.excalibur.source.Source source ) {
        throw new RuntimeException("URLFactorySourceResolver.release() is not implemented yet.");
    }

    /**
     * Generates SAX events from the given source
     * <b>NOTE</b> : if the implementation can produce lexical events, care should be taken
     * that <code>handler</code> can actually
     * directly implement the LexicalHandler interface!
     * @param  source    the data
     * @throws ProcessingException if no suitable converter is found
     */
    public void toSAX( org.apache.excalibur.source.Source source,
                ContentHandler handler )
    throws SAXException, IOException, ProcessingException {
        throw new RuntimeException("ProcessingException.toSAX() is not implemented yet.");
    }

    /**
     * Generates SAX events from the given source
     * <b>NOTE</b> : if the implementation can produce lexical events, care should be taken
     * that <code>handler</code> can actually
     * directly implement the LexicalHandler interface!
     * @param  source    the data
     * @throws ProcessingException if no suitable converter is found
     */
    public void toSAX( org.apache.excalibur.source.Source source,
                String mimeTypeHint,
                ContentHandler handler )
    throws SAXException, IOException, ProcessingException {
        throw new RuntimeException("ProcessingException.toSAX() is not implemented yet.");
    }
}
