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
package org.apache.cocoon.taglib.core;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.taglib.XMLProducerTagSupport;
import org.apache.cocoon.xml.EmbeddedXMLPipe;

import org.apache.excalibur.source.Source;

import org.xml.sax.SAXException;

/**
 * @version $Id$
 */
public class SourceTag extends XMLProducerTagSupport {
    private String src;

    public void setSrc(String src) {
        this.src = src;
    }

    /*
     * @see Tag#doEndTag(String, String, String)
     */
    public int doEndTag(String namespaceURI, String localName, String qName)
    throws SAXException {
        Source source = null;
        try {
            source = resolver.resolveURI(src);
            SourceUtil.toSAX(source, new EmbeddedXMLPipe(this.xmlConsumer));
        } catch (SAXException e) {
            throw e;
        } catch (Exception e) {
            if (e instanceof ProcessingException) {
                ProcessingException pe = (ProcessingException) e;
                Throwable t = pe.getCause();
                if (t != null && t instanceof SAXException)
                    throw (SAXException) t;
            }
            throw new SAXException(e.getMessage(), e);
        } finally {
            if (source != null) {
                resolver.release(source);
            }
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
