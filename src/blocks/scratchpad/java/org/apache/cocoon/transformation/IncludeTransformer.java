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
package org.apache.cocoon.transformation;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.components.source.impl.MultiSourceValidity;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.xml.IncludeXMLConsumer;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceValidity;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Simple Source include transformer.
 * 
 * <p>
 *  Triggers for the element <code>include</code> in the
 *  namespace <code>http://apache.org/cocoon/include/1.0</code>.
 *  Use <code>&lt;include src="scheme://path"/&gt;</code>
 * </p>
 * 
 * @cocoon.sitemap.component.name   include
 * @cocoon.sitemap.component.logger sitemap.transformer.include
 *
 * @cocoon.sitemap.component.pooling.min   2
 * @cocoon.sitemap.component.pooling.max  16
 * @cocoon.sitemap.component.pooling.grow  2
 */
public class IncludeTransformer extends AbstractTransformer 
implements Serviceable, Transformer, CacheableProcessingComponent {

    private static final String NS_URI = "http://apache.org/cocoon/include/1.0";
    private static final String INCLUDE_ELEMENT = "include";
    private static final String SRC_ATTRIBUTE = "src";

    private ServiceManager m_manager;
    private SourceResolver m_resolver;
    private MultiSourceValidity m_validity;

    public IncludeTransformer() {
        super();
    }

    public void service(ServiceManager manager) throws ServiceException {
        m_manager = manager;
    }

    public void setup(SourceResolver resolver, Map om, String src, Parameters parameters) 
    throws ProcessingException, SAXException, IOException {
        m_resolver = resolver;
        m_validity = null;
    }

    public void recycle() {
        super.recycle();
        m_resolver = null;
        m_validity = null;
    }

    public void startElement(String uri, String localName, String qName, Attributes atts) 
    throws SAXException {
        if (NS_URI.equals(uri)) {
            if (INCLUDE_ELEMENT.equals(localName)) {
                String src = atts.getValue(SRC_ATTRIBUTE);
                Source source = null;
                try {
                    source = m_resolver.resolveURI(src);
                    if (m_validity != null) {
                        m_validity.addSource(source);
                    }
                    SourceUtil.toSAX(m_manager, source, "text/xml", 
                            new IncludeXMLConsumer(super.contentHandler));
                }
                catch (IOException e) {
                    throw new SAXException(e);
                }
                catch (ProcessingException e) {
                    throw new SAXException(e);
                }
                finally {
                    if (source != null) {
                        m_resolver.release(source);
                    }
                }
            }
        }
        else {
            super.startElement(uri, localName, qName, atts);
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (!NS_URI.equals(uri)) {
            super.endElement(uri, localName, qName);
        }
    }

    public Serializable getKey() {
        return "IncludeTransformer";
    }

    public SourceValidity getValidity() {
        if (m_validity == null) {
            m_validity = new MultiSourceValidity(m_resolver, -1);
        }
        return m_validity;
    }

}
