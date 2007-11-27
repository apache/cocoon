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
package org.apache.cocoon.portal.coplet.adapter.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.transform.sax.SAXResult;

import org.apache.cocoon.core.xml.SAXParser;
import org.apache.cocoon.portal.om.CopletInstance;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This is the adapter to use uris through the java.net.* classes as coplets.
 *
 * TODO - We should provide a configuration option to directly stream the output
 *        into the resulting output stream like the portlet adapter does.
 * @version $Id$
 */
public class URICopletAdapter
    extends AbstractCopletAdapter {

    /** The parser for parsing the output and streaming it into the pipeline. */
    protected SAXParser parser;

    public void setParser(final SAXParser p) {
        this.parser = p;
    }

    /**
     * @see org.apache.cocoon.portal.coplet.adapter.impl.AbstractCopletAdapter#streamContent(org.apache.cocoon.portal.om.CopletInstance, org.xml.sax.ContentHandler)
     */
    protected void streamContent(CopletInstance coplet, ContentHandler contentHandler)
    throws SAXException {
        final String uri = (String)coplet.getCopletDefinition().getAttribute("uri");
        if ( uri == null ) {
            throw new SAXException("No URI for coplet definition " + coplet.getCopletDefinition().getId()+" found.");
        }
        this.streamContent( coplet, uri, contentHandler);
    }

    protected void streamContent(final CopletInstance coplet,
                                 final String uri,
                                 final ContentHandler contentHandler)
    throws SAXException {
        if ( this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("Streaming coplet instance + " + coplet + " using uri: " + uri);
        }
		try {
	        final URL url = new URL(uri);
	        final Object content = url.getContent(new Class[] {SAXResult.class});
	        if ( content instanceof SAXResult ) {
	            ((SAXResult)content).setHandler(contentHandler);
	        } else {
	            final InputSource source = new InputSource((InputStream)content);
	            source.setSystemId(url.toExternalForm());
	            this.parser.parse(source, contentHandler);
	        }
		} catch (IOException ioe) {
			throw new SAXException("Unable to stream coplet instance " + coplet + " from uri " + uri, ioe);
		}
    }

    /**
     * Render the error content for a coplet
     * @param coplet  The coplet instance data
     * @param handler The content handler
     * @param error   The exception that occured
     * @return True if the error content has been rendered, otherwise false
     * @throws SAXException
     */
    protected boolean renderErrorContent(CopletInstance coplet,
                                         ContentHandler     handler,
                                         Exception          error)
    throws SAXException {
        final String uri = (String) this.getConfiguration(coplet, "error-uri");
        if ( uri != null ) {
            // TODO - if an error occured for this coplet, remember this
            //         and use directly the error-uri from now on

            this.streamContent( coplet, uri, handler);

            return true;
        }
        return false;
    }
}
