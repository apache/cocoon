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

package org.apache.cocoon.components.source;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.source.impl.AvalonToCocoonSource;
import org.apache.cocoon.environment.Source;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.excalibur.xml.sax.SAXParser;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @version CVS $Id: SourceResolverAdapter.java,v 1.6 2003/05/17 11:51:42 vgritsenko Exp $
 */
public class SourceResolverAdapter implements SourceResolver
{
    private org.apache.excalibur.source.SourceResolver resolver;
    private ComponentManager manager;

    public SourceResolverAdapter(org.apache.excalibur.source.SourceResolver resolver, ComponentManager manager) {
        this.resolver = resolver;
        this.manager = manager;
    }

    /**
     * Get a <code>Source</code> object.
     * This is a shortcut for <code>resolve(location, null, null)</code>
     * @throws org.apache.excalibur.source.SourceException if the source cannot be resolved
     */
    public org.apache.excalibur.source.Source resolveURI( String location )
        throws MalformedURLException, IOException, org.apache.excalibur.source.SourceException {
  
        return this.resolver.resolveURI(location);
    }

    /**
     * Get a <code>Source</code> object.
     * @param location - the URI to resolve. If this is relative it is either
     *                   resolved relative to the base parameter (if not null)
     *                   or relative to a base setting of the source resolver
     *                   itself.
     * @param base - a base URI for resolving relative locations. This
     *               is optional and can be <code>null</code>.
     * @param parameters - Additional parameters for the URI. The parameters
     *                     are specific to the used protocol.
     * @throws org.apache.excalibur.source.SourceException if the source cannot be resolved
     */
    public org.apache.excalibur.source.Source resolveURI( String location,
                                                          String base,
                                                          Map parameters )
        throws MalformedURLException, IOException, org.apache.excalibur.source.SourceException {

        return this.resolver.resolveURI(location, base, parameters);
    }

    /**
     * Releases a resolved resource
     */
    public void release( org.apache.excalibur.source.Source source ) {
        this.resolver.release(source);
    }

    /**
     * Resolve the source.
     * @param systemID This is either a system identifier
     * (<code>java.net.URL</code> or a local file.
     * @deprecated Use the resolveURI methods instead
     */
    public Source resolve(String systemID)
        throws ProcessingException, SAXException, IOException {

        try {
            return new AvalonToCocoonSource(this.resolver.resolveURI(systemID), this.resolver, null, manager);
        } catch (org.apache.excalibur.source.SourceException se) {
            throw new ProcessingException(se.toString());
        }
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

        SAXParser parser = null;
        try {
            parser = (SAXParser) this.manager.lookup(SAXParser.ROLE);

            parser.parse(new InputSource(source.getInputStream()), handler);
        } catch (ComponentException ce) {
            throw new ProcessingException("Couldn't xmlize source", ce);
        } catch (org.apache.excalibur.source.SourceException se) {
            throw new ProcessingException("Couldn't xmlize source", se);
        } finally {
            this.manager.release((Component) parser);
        } 
    }

    /**
     * Generates SAX events from the given source
     * <b>NOTE</b> : if the implementation can produce lexical events, care should be taken
     * that <code>handler</code> can actually
     * directly implement the LexicalHandler interface!
     * @param  source    the data
     * @throws ProcessingException if no suitable converter is found
     */
    public void toSAX( org.apache.excalibur.source.Source source, String mimeType,
                ContentHandler handler )
        throws SAXException, IOException, ProcessingException {

        this.toSAX( source, handler );
    }
}
