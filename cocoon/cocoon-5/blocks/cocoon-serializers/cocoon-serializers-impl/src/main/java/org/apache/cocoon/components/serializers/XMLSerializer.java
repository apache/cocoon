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
 * <p>A fancy XML serializer not relying on the JAXP API.</p>
 *
 * <p>For configuration options of this serializer, please look at the
 * {@link EncodingSerializer}.</p>
 *
 * @version CVS $Id$
 */
public class XMLSerializer
    extends org.apache.cocoon.components.serializers.util.XMLSerializer
    implements Serializer, SitemapModelComponent, Recyclable, Configurable  {

    /**
     * @see org.apache.cocoon.sitemap.SitemapModelComponent#setup(org.apache.cocoon.environment.SourceResolver, java.util.Map, java.lang.String, org.apache.avalon.framework.parameters.Parameters)
     */
    public void setup(SourceResolver resolver,
                      Map            objectModel,
                      String         src,
                      Parameters     par)
    throws ProcessingException, SAXException, IOException {
        this.setup((HttpServletRequest) objectModel.get(HttpEnvironment.HTTP_REQUEST_OBJECT));
    }


    /**
     * Test if the component wants to set the content length.
     */
    public boolean shouldSetContentLength() {
        return false;
    }

    /**
     * Set the configurations for this serializer.
     */
    public void configure(Configuration conf)
    throws ConfigurationException {
        String encoding = conf.getChild("encoding").getValue(null);
        try {
            this.setEncoding(encoding);
        } catch (UnsupportedEncodingException exception) {
            throw new ConfigurationException("Encoding not supported: "
                                             + encoding, exception);
        }

        this.setIndentPerLevel(conf.getChild("indent").getValueAsInteger(0));
    }

}
