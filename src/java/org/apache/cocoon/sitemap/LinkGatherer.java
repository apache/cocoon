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
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.Constants;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.transformation.Transformer;
import org.apache.cocoon.xml.xlink.ExtendedXLinkPipe;

import org.apache.excalibur.source.SourceValidity;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:uv@upaya.co.uk">Upayavira</a>
 * @version CVS $Id: LinkGatherer.java,v 1.6 2004/03/05 13:02:58 bdelacretaz Exp $
 */
public class LinkGatherer extends ExtendedXLinkPipe implements Transformer, CacheableProcessingComponent {
    private List links;


    /**
     * Set the <code>SourceResolver</code>, objectModel <code>Map</code>,
     * the source and sitemap <code>Parameters</code> used to process the request.
     */
    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par) throws ProcessingException,
        SAXException, IOException {
            this.links = (List)objectModel.get(Constants.LINK_COLLECTION_OBJECT);
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
//      Whilst the cache does not store gathered links, this component must be non-cacheable
//      return NOPValidity.SHARED_INSTANCE;
        return null;
    }

    public void simpleLink(String href, String role, String arcrole, String title, String show, String actuate, String uri,
        String name, String raw, Attributes attr) throws SAXException {
            if (!this.links.contains(href)){
                this.addLink(href);
            }
            super.simpleLink(href, role, arcrole, title, show, actuate, uri, name, raw, attr);
    }

    public void startLocator(String href, String role, String title, String label, String uri, String name, String raw,
        Attributes attr) throws SAXException {
            if (!this.links.contains(href)){
                this.addLink(href);
            }
            super.startLocator(href, role, title, label, uri, name, raw, attr);
    }
    private void addLink(String href) {
        if (href.length() == 0) return;
        if (href.charAt(0) == '#') return;
        if (href.indexOf("://") != -1) return;
        if (href.startsWith("mailto:")) return;
        if (href.startsWith("news:")) return;
        if (href.startsWith("javascript:")) return;

        int anchorPos = href.indexOf('#');
        if (anchorPos == -1) {
            this.links.add(href);
        } else {
            this.links.add(href.substring(0, anchorPos));
        }
    }
}
