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
 * @version CVS $Id: URLFactorySourceResolver.java,v 1.1 2003/03/09 00:07:13 pier Exp $
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
