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
package org.apache.cocoon.portal.services.aspects.impl;

import java.io.IOException;
import java.util.Properties;

import org.apache.cocoon.portal.services.aspects.ResponseProcessorAspect;
import org.apache.cocoon.portal.services.aspects.ResponseProcessorAspectContext;
import org.apache.cocoon.portal.util.AbstractContentHandler;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.xml.sax.XMLizable;
import org.apache.excalibur.xmlizer.XMLizer;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 *
 * @version $Id$
 */
public class IncludeContentResponseProcessorAspect implements ResponseProcessorAspect {

    public static final String INCLUDE_NAMESPACE_URI = "http://cocoon.apache.org/portal/include/1.0";
    public static final String INCLUDE_ELEMENT = "include";
    public static final String INCLUDE_ELEMENT_SRC_ATTRIBUTE = "src";

    /**
     * @see org.apache.cocoon.portal.services.aspects.ResponseProcessorAspect#render(org.apache.cocoon.portal.services.aspects.ResponseProcessorAspectContext, org.xml.sax.ContentHandler, java.util.Properties)
     */
    public void render(ResponseProcessorAspectContext context,
                       ContentHandler ch,
                       Properties properties)
    throws SAXException {
    }

    protected static final class IncludeContentHandler extends AbstractContentHandler {

        protected SourceResolver resolver;

        protected XMLizer xmlizer;

        protected int ignoreDocumentCount = 0;
        public IncludeContentHandler(ContentHandler ch) {
            super(ch);
            this.namespaceUri = INCLUDE_NAMESPACE_URI;
            this.removeOurNamespacePrefixes = true;
        }


        /**
         * @see org.apache.cocoon.portal.util.AbstractContentHandler#endDocument()
         */
        public void endDocument() throws SAXException {
            if ( this.ignoreDocumentCount == 0 ) {
                super.endDocument();
            }
        }

        /**
         * @see org.apache.cocoon.portal.util.AbstractContentHandler#startDocument()
         */
        public void startDocument() throws SAXException {
            if ( this.ignoreDocumentCount == 0 ) {
                super.startDocument();
            }
        }

        /**
         * @see org.apache.cocoon.portal.util.AbstractContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
         */
        public void endElement(String uri, String loc, String raw) throws SAXException {
            if ( !INCLUDE_NAMESPACE_URI.equals(uri) ) {
                super.endElement(uri, loc, raw);
            }
        }

        /**
         * @see org.apache.cocoon.portal.util.AbstractContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
         */
        public void startElement(String uri, String loc, String raw, Attributes a)
        throws SAXException {
            if ( !INCLUDE_NAMESPACE_URI.equals(uri) ) {
                super.startElement(uri, loc, raw, a);
            }
        }

        protected String processIncludeElement(String src)
        throws SAXException, IOException {
            if (src == null) {
                throw new SAXException("Missing 'src' attribute on cinclude:include element");
            }

            Source source = null;
            try {
                this.ignoreDocumentCount++;
                source = this.resolver.resolveURI(src);

                if (source instanceof XMLizable) {
                    ((XMLizable)source).toSAX(this);
                } else {
                    xmlizer.toSAX(source.getInputStream(),
                                  source.getMimeType(),
                                  source.getURI(),
                                  this);
                }

            } catch (SourceException se) {
                throw new SAXException("Exception in CIncludeTransformer",se);
            } catch (IOException e) {
                throw new SAXException("CIncludeTransformer could not read resource", e);
            } finally {
                this.ignoreDocumentCount--;
                this.resolver.release(source);
            }

            return src;
        }

    }
}
