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
package org.apache.cocoon.sitemap;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.Constants;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.transformation.Transformer;
import org.apache.cocoon.xml.xlink.ExtendedXLinkPipe;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.NOPValidity;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Map;

/**
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Id: LinkTranslator.java,v 1.7 2004/03/08 14:03:30 cziegeler Exp $
 * 
 * @avalon.component
 * @avalon.service type=Transformer
 * @x-avalon.lifestyle type=pooled
 * @x-avalon.info name=link-translator
 */
public class LinkTranslator extends ExtendedXLinkPipe implements Transformer, CacheableProcessingComponent {
    
    private Map links;

    /**
     * Set the <code>SourceResolver</code>, objectModel <code>Map</code>,
     * the source and sitemap <code>Parameters</code> used to process the request.
     */
    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par) 
    throws ProcessingException, SAXException, IOException {
        this.links = (Map)objectModel.get(Constants.LINK_OBJECT);
    }

    /**
     * Generate the unique key.
     * This key must be unique inside the space of this component.
     *
     * @return The generated key hashes the src
     */
    public java.io.Serializable getKey() {
        return "1";
    }

    /**
     * Generate the validity object.
     *
     * @return The generated validity object or <code>null</code> if the
     *         component is currently not cacheable.
     */
    public SourceValidity getValidity() {
        return NOPValidity.SHARED_INSTANCE;
    }

    public void simpleLink(String href, String role, String arcrole, 
                           String title, String show, String actuate, String uri,
                           String name, String raw, Attributes attr) 
    throws SAXException {
        final String newHref = (String)this.links.get(href);
        super.simpleLink((newHref != null) ? newHref : href, role, arcrole, title, show, actuate, uri, name, raw, attr);
    }

    public void startLocator(String href, String role, String title, 
                             String label, String uri, String name, String raw,
                             Attributes attr) 
    throws SAXException {
        final String newHref = (String)this.links.get(href);
        super.startLocator((newHref != null) ? newHref : href, role, title, label, uri, name, raw, attr);
    }
}
