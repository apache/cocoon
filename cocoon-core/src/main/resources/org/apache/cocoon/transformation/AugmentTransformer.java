/*
* Copyright 1999-2004 The Apache Software Foundation
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.cocoon.transformation;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.io.IOException;
import java.util.Map;

/**
* @cocoon.sitemap.component.documentation
 * Augments all <code>href</code> attributes with the full path to
 * the request. You can optionally specify the <code>mount</code>
 * parameter.
 * 
 * @cocoon.sitemap.component.name   augment
 * @cocoon.sitemap.component.logger sitemap.transformer.augment
 * 
 * @since October 10, 2001
 * @version $Id$
 */
public class AugmentTransformer
    extends AbstractTransformer {
        
    protected Map objectModel;
    protected Request request;
    protected String baseURI;
  
    public void setup(SourceResolver resolver,
                      Map objectModel,
                      String source,
                      Parameters parameters)
    throws ProcessingException, SAXException, IOException {
        this.objectModel = objectModel;
        this.request = ObjectModelHelper.getRequest( this.objectModel );
    
        String mountPoint = parameters.getParameter("mount", null);
        
        StringBuffer uribuf = new StringBuffer();
        boolean isSecure = this.request.isSecure();
        int port = this.request.getServerPort();
    
        if (isSecure) {
            uribuf.append("https://");
        } else {
            uribuf.append("http://");
        }
        uribuf.append(request.getServerName());
    
        if (isSecure) {
            if (port != 443) {
                uribuf.append(":").append(port);
            }
        } else {
            if (port != 80) {
                uribuf.append(":").append(port);
            }
        }
        if (mountPoint == null) {
            String requestedURI = this.request.getRequestURI();
            requestedURI = requestedURI.substring(0, requestedURI.lastIndexOf("/"));
            uribuf.append(requestedURI);
            uribuf.append("/");
        } else {
            uribuf.append(request.getContextPath());
            uribuf.append("/");
            uribuf.append(mountPoint);
        }
        this.baseURI = uribuf.toString();
    }

    public void startElement(String uri,
                             String name,
                             String qname,
                             Attributes attrs)
    throws SAXException {
        AttributesImpl newAttrs = null;
    
        for (int i = 0, size = attrs.getLength(); i < size; i++) {
            String attrName = attrs.getLocalName(i);
            if (attrName.equals("href")) {
                String value = attrs.getValue(i);

                // Don't touch the attribute if it's an absolute URL
                if (value.startsWith("http:") || value.startsWith("https:")) {
                    continue;
                }

                if (newAttrs == null) {
                    newAttrs = new AttributesImpl(attrs);
                }

                String newValue = baseURI + value;
                newAttrs.setValue(i, newValue);
            }
        }

        if (newAttrs == null) {
            super.startElement(uri, name, qname, attrs);
        } else {
            super.startElement(uri, name, qname, newAttrs);
        }
    }

    /**
     * Recyclable
     */
    public void recycle() {
        this.objectModel = null;
        this.request = null;
        this.baseURI = null;
        super.recycle();
    }
}
