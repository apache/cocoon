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
package org.apache.cocoon.taglib.core;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.taglib.XMLProducerTagSupport;
import org.apache.cocoon.xml.EmbeddedXMLPipe;

import org.apache.excalibur.source.Source;

import org.xml.sax.SAXException;

/**
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * @version CVS $Id: SourceTag.java,v 1.4 2004/03/05 13:02:24 bdelacretaz Exp $
 */
public class SourceTag extends XMLProducerTagSupport {
    private String src;
    private EmbeddedXMLPipe embeddedXMLPipe = new EmbeddedXMLPipe(null);

    public void setSrc(String src) {
        this.src = src;
    }

    /*
     * @see Tag#doEndTag(String, String, String)
     */
    public int doEndTag(String namespaceURI, String localName, String qName) throws SAXException {
        Source source = null;
        try {
            embeddedXMLPipe.setConsumer(this.xmlConsumer);
            source = resolver.resolveURI(src);
            SourceUtil.toSAX(source, this.embeddedXMLPipe);
        } catch (Exception e) {
            if (e instanceof ProcessingException) {
                ProcessingException pe = (ProcessingException) e;
                Throwable t = pe.getCause();
                if (t != null && t instanceof SAXException)
                    throw (SAXException) t;
            }
            throw new SAXException(e.getMessage(), e);
        } finally {
            embeddedXMLPipe.setConsumer(null);
            if (source != null)
                resolver.release(source);
        }
        return EVAL_PAGE;
    }

    /*
     * @see Recyclable#recycle()
     */
    public void recycle() {
        this.src = null;
        super.recycle();
    }
}
