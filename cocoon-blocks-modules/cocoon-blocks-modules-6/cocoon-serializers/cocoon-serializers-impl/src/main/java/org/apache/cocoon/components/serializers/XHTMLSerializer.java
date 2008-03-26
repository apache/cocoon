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
package org.apache.cocoon.components.serializers;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.avalon.excalibur.pool.Recyclable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.environment.http.HttpEnvironment;
import org.apache.cocoon.serialization.Serializer;
import org.apache.cocoon.sitemap.SitemapModelComponent;
import org.xml.sax.SAXException;

/**
 * <p>A pedantinc XHTML serializer encoding all recognized entities with their
 * proper HTML names.</p>
 *
 * <p>For configuration options of this serializer, please look at the
 * {@link org.apache.cocoon.components.serializers.util.EncodingSerializer},
 * in addition to those, this serializer also support the specification of a
 * default doctype. This default will be used if no document type is received
 * in the SAX events, and can be configured in the following way:</p>
 *
 * <pre>
 * &lt;serializer class="org.apache.cocoon.components.serializers..." ... &gt;
 *   &lt;doctype-default&gt;mytype&lt;/doctype-default&gt;
 * &lt;/serializer&gt;
 * </pre>
 *
 * <p>The value <i>mytype</i> can be one of:</p>
 *
 * <dl>
 *   <dt>"<code>none</code>"</dt>
 *   <dd>Not to emit any dococument type declaration.</dd>
 *   <dt>"<code>strict</code>"</dt>
 *   <dd>The XHTML 1.0 Strict document type.</dd>
 *   <dt>"<code>loose</code>"</dt>
 *   <dd>The XHTML 1.0 Transitional document type.</dd>
 *   <dt>"<code>frameset</code>"</dt>
 *   <dd>The XHTML 1.0 Frameset document type.</dd>
 * </dl>
 *
 * @version $Id$
 */
public class XHTMLSerializer extends org.apache.cocoon.components.serializers.util.XHTMLSerializer
                             implements Serializer, SitemapModelComponent, Recyclable, Configurable  {

    /**
     * Set the configurations for this serializer.
     */
    public void configure(Configuration conf)
    throws ConfigurationException {
        String encoding = conf.getChild("encoding").getValue(null);
        try {
            setEncoding(encoding);
        } catch (UnsupportedEncodingException exception) {
            throw new ConfigurationException("Encoding not supported: "
                                             + encoding, exception);
        }

        setIndentPerLevel(conf.getChild("indent").getValueAsInteger(0));
        setOmitXmlDeclaration(conf.getChild("omit-xml-declaration").getValue(null));
        setDoctypeDefault(conf.getChild("doctype-default").getValue(null));
    }

    /**
     * @see SitemapModelComponent#setup(SourceResolver, Map, String, Parameters)
     */
    public void setup(SourceResolver resolver,
                      Map            objectModel,
                      String         src,
                      Parameters     par)
    throws ProcessingException, SAXException, IOException {
        setup((HttpServletRequest) objectModel.get(HttpEnvironment.HTTP_REQUEST_OBJECT));
    }

    /**
     * Test if the component wants to set the content length.
     */
    public boolean shouldSetContentLength() {
        return false;
    }
}
