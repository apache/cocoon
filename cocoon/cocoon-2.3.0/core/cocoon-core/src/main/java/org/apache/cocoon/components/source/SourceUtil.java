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
package org.apache.cocoon.components.source;

import java.io.IOException;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.internal.EnvironmentHelper;
import org.apache.cocoon.xml.IncludeXMLConsumer;
import org.apache.cocoon.xml.dom.DOMBuilder;
import org.apache.excalibur.source.Source;
import org.w3c.dom.Document;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * This class contains some utility methods for the source resolving.
 *
 * @version $Id$
 */
public abstract class SourceUtil extends org.apache.cocoon.components.source.util.SourceUtil{

    /**
     * Get the current sitemap component manager.
     * This method returns the current sitemap component manager. This
     * is the manager that holds all the components of the currently
     * processed (sub)sitemap.
     * @deprecated This method will be removed.
     */
    static private ServiceManager getSitemapServiceManager() {
        return EnvironmentHelper.getSitemapServiceManager(); 
    }

    /**
     * Generates SAX events from the given source.
     *
     * <p><b>NOTE</b>: If the implementation can produce lexical events,
     * care should be taken that <code>handler</code> can actually
     * directly implement the LexicalHandler interface!</p>
     *
     * @param  source    the data
     * @throws ProcessingException if no suitable converter is found
     */
    static public void toSAX(Source         source,
                             ContentHandler handler)
    throws SAXException, IOException, ProcessingException {
        toSAX(getSitemapServiceManager(),
              source, null, handler);
    }

    /**
     * Generates SAX events from the given source by using XMLizer.
     * Current sitemap manager will be used to lookup XMLizer.
     *
     * <p><b>NOTE</b>: If the implementation can produce lexical events,
     * care should be taken that <code>handler</code> can actually
     * directly implement the LexicalHandler interface!</p>
     *
     * @param  source    the data
     * @throws ProcessingException if no suitable converter is found
     */
    static public void toSAX(Source         source,
                             String         mimeTypeHint,
                             ContentHandler handler)
    throws SAXException, IOException, ProcessingException {
        toSAX(getSitemapServiceManager(),
              source, mimeTypeHint, handler);
    }

    /**
     * Generates SAX events from the given source with possible URL rewriting.
     *
     * <p><b>NOTE</b>: If the implementation can produce lexical events,
     * care should be taken that <code>handler</code> can actually
     * directly implement the LexicalHandler interface!</p>
     *
     * @param  source    the data
     * @throws ProcessingException if no suitable converter is found
     */
    static public void toSAX(Source source,
                             ContentHandler handler,
                             Parameters typeParameters,
                             boolean filterDocumentEvent)
    throws SAXException, IOException, ProcessingException {
        // Test for url rewriting
        if (typeParameters != null
                && typeParameters.getParameter(URLRewriter.PARAMETER_MODE, null) != null) {
            handler = new URLRewriter(typeParameters, handler);
        }

        String mimeTypeHint = null;
        if (typeParameters != null) {
            mimeTypeHint = typeParameters.getParameter("mime-type", mimeTypeHint);
        }
        if (filterDocumentEvent) {
            IncludeXMLConsumer filter = new IncludeXMLConsumer(handler);
            toSAX(source, mimeTypeHint, filter);
        } else {
            toSAX(source, mimeTypeHint, handler);
        }
    }

    /**
     * Generates a DOM from the given source
     * @param source The data
     *
     * @return Created DOM document.
     *
     * @throws IOException If a io exception occurs.
     * @throws ProcessingException if no suitable converter is found
     * @throws SAXException If a SAX exception occurs.
     */
    static public Document toDOM(Source source)
    throws SAXException, IOException, ProcessingException {
        DOMBuilder builder = new DOMBuilder();

        toSAX(source, builder);

        Document document = builder.getDocument();
        if (document == null) {
            throw new ProcessingException("Could not build DOM for '" +
                                          source.getURI() + "'");
        }

        return document;
    }
}
