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
package org.apache.cocoon.portal.coplets.basket;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.generation.AbstractCopletGenerator;
import org.apache.cocoon.xml.SaxBuffer;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.xml.sax.SAXParser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This generator displays the content of one item.
 *
 * @version CVS $Id$
 */
public class BasketContentGenerator
extends AbstractCopletGenerator {
    
    /** This is the attribute name containing the content */
    protected String attributeName;
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.sitemap.SitemapModelComponent#setup(org.apache.cocoon.environment.SourceResolver, java.util.Map, java.lang.String, org.apache.avalon.framework.parameters.Parameters)
     */
    public void setup(SourceResolver resolver,
                      Map objectModel,
                      String src,
                      Parameters par)
    throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, src, par);

        this.attributeName = par.getParameter("attribute-name", null);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.generation.Generator#generate()
     */
    public void generate()
    throws IOException, SAXException, ProcessingException {
        boolean streamed = false;
        SAXParser parser = null;
        try {
            parser = (SAXParser) this.manager.lookup(SAXParser.ROLE);
            if ( this.attributeName != null ) {
                CopletInstanceData cid = this.getCopletInstanceData();
                byte[] content = (byte[])cid.getAttribute(this.attributeName);
                if ( content == null ) {
                    this.xmlConsumer.startDocument();
                    XMLUtils.createElement(this.xmlConsumer, "p");
                    this.xmlConsumer.endDocument();
                    return;
                }
                try {
                    InputSource is = new InputSource(new ByteArrayInputStream(content));
                    SaxBuffer buffer = new SaxBuffer();
                    parser.parse(is, buffer);
                    streamed = true;
                    buffer.toSAX(this.xmlConsumer);
                } catch (Exception ignore) {
                    // ignore
                }
            }
            if ( !streamed ) {
                Source source = null;
                try {
                    source = this.resolver.resolveURI(this.source);
                    parser.parse(SourceUtil.getInputSource(source), this.xmlConsumer);
                } finally {
                    this.resolver.release(source);
                }
            }
        } catch (ServiceException se) {
            throw new ProcessingException("Unable to lookup parser.", se);
        } finally {
            this.manager.release(parser);
        }
    }
    
}
