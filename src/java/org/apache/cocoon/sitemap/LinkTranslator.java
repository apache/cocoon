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
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.transformation.Transformer;
import org.apache.cocoon.xml.xlink.ExtendedXLinkPipe;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Map;

/**
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Id: LinkTranslator.java,v 1.6 2004/03/05 13:02:58 bdelacretaz Exp $
 */
public class LinkTranslator extends ExtendedXLinkPipe implements Transformer {
    
    private Map links;

    /**
     * Set the <code>SourceResolver</code>, objectModel <code>Map</code>,
     * the source and sitemap <code>Parameters</code> used to process the request.
     */
    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par) 
    throws ProcessingException, SAXException, IOException {
        this.links = (Map)objectModel.get(Constants.LINK_OBJECT);
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
