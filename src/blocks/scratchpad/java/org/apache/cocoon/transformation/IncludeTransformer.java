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
import org.apache.cocoon.xml.NamespacesTable;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceValidity;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * <p>A simple transformer including resolvable sources (accessed through Cocoon's
 * {@link SourceResolver} from its input.</p>
 * 
 * <p>Inclusion is triggered by the <code>&lt;include ... /&gt;</code> element
 * defined in the <code>http://apache.org/cocoon/include/1.0</code> namespace.</p>
 * 
 * <p>Example:</p>
 *
 * <p><code>&lt;incl:include xmlns="http://apache.org/cocoon/include/1.0"
 * src="cocoon://path/to/include"/&gt;</code></p>
 * 
 * <p>An interesting feature of this {@link Transformer} is that it implements the
 * {@link CacheableProcessingComponent} interface and provides full support for
 * caching. In other words, if the input given to this transformer has not changed,
 * and all of the included sources are (cacheable) and still valid, this transformer
 * will not force a pipeline re-generation like the {@link CIncludeTransformer}.</p>
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
    private NamespacesTable m_namespaces;

    /**
     * <p>Create a new {@link IncludeTransformer} instance.</p>
     */
    public IncludeTransformer() {
        super();
    }

    /**
     * <p>Setup the {@link ServiceManager} available for this instance.</p>
     *
     * @see Serviceable#service(ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.m_manager = manager;
    }

    /**
     * <p>Setup this component instance in the context of its pipeline and
     * current request.</p>
     *
     * @see Serviceable#service(ServiceManager)
     */
    public void setup(SourceResolver resolver, Map om, String src, Parameters parameters) 
    throws ProcessingException, SAXException, IOException {
        this.m_resolver = resolver;
        this.m_validity = null;
        this.m_namespaces = new NamespacesTable();
    }

    /**
     * <p>Recycle this component instance.</p>
     *
     * @see org.apache.avalon.excalibur.pool.Recyclable#recycle()
     */
    public void recycle() {
        super.recycle();
        this.m_resolver = null;
        this.m_validity = null;
        this.m_namespaces = new NamespacesTable();
    }

    /**
     * <p>Receive notification of the beginning of an XML document.</p>
     *
     * @see org.xml.sax.ContentHandler#startDocument()
     */
    public void startDocument()
    throws SAXException {
        /* Make sure that we have a validity while processing */
        this.getValidity();
        super.startDocument();
    }

    /**
     * <p>Receive notification of the end of an XML document.</p>
     *
     * @see org.xml.sax.ContentHandler#startDocument()
     */
    public void endDocument()
    throws SAXException {
        /* Make sure that the validity is "closed" at the end */
        this.m_validity.close();
        super.endDocument();
    }

    /**
     * <p>Receive notification of the start of a prefix mapping.</p>
     *
     * <p>This transformer will remove all prefix mapping declarations for those
     * prefixes associated with the <code>http://apache.org/cocoon/include/1.0</code>
     * namespace.</p>
     *
     * @see org.xml.sax.ContentHandler#startPrefixMapping(String)
     */
    public void startPrefixMapping(String prefix, String nsuri)
    throws SAXException {
        if (NS_URI.equals(nsuri)) {
            /* Skipping mapping for the current prefix as it's ours */
            this.m_namespaces.addDeclaration(prefix, nsuri);
        } else {
            /* Map the current prefix, as we don't know it */
            super.startPrefixMapping(prefix, nsuri);
        }
    }

    /**
     * <p>Receive notification of the end of a prefix mapping.</p>
     *
     * <p>This transformer will remove all prefix mapping declarations for those
     * prefixes associated with the <code>http://apache.org/cocoon/include/1.0</code>
     * namespace.</p>
     *
     * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
     */
    public void endPrefixMapping(String prefix)
    throws SAXException {
        if (NS_URI.equals(this.m_namespaces.getUri(prefix))) {
            /* Skipping unmapping for the current prefix as it's ours */
            this.m_namespaces.removeDeclaration(prefix);
        } else {
            /* Unmap the current prefix, as we don't know it */
            super.endPrefixMapping(prefix);
        }
    }

    /**
     * <p>Receive notification of the start of an element.</p>
     *
     * @see org.xml.sax.ContentHandler#startElement(String, String, String, org.xml.sax.Attributes)
     */
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
        } else {
            super.startElement(uri, localName, qName, atts);
        }
    }

    /**
     * <p>Receive notification of the end of an element.</p>
     *
     * @see org.xml.sax.ContentHandler#endElement(String, String, String)
     */
    public void endElement(String uri, String localName, String qName)
    throws SAXException {
        if (!NS_URI.equals(uri)) {
            super.endElement(uri, localName, qName);
        }
    }

    /**
     * <p>Return the validity key associated with this transformation.</p>
     *
     * @see CacheableProcessingComponent#getKey()
     */
    public Serializable getKey() {
        // FIXME: In case of including "cocoon://" or other dynamic sources
        // key has to be dynamic.
        return "I";
    }

    /**
     * <p>Generate (or return) the {@link SourceValidity} instance used to
     * possibly validate cached generations.</p>
     *
     * @return a <b>non null</b> {@link SourceValidity}.
     * @see org.apache.cocoon.caching.CacheableProcessingComponent#getValidity()
     */
    public SourceValidity getValidity() {
        if (m_validity == null) {
            m_validity = new MultiSourceValidity(m_resolver, -1);
        }
        return m_validity;
    }
}
