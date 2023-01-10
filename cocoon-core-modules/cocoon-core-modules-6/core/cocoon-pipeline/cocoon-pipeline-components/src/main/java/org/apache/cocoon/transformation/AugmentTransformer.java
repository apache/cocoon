/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Augments all <code>href</code> attributes with the full path to
 * the request. You can optionally specify the <code>mount</code>
 * parameter.
 *
 * <p>The set of attributes to augment can be specified in the
 * <code>attributes</code> parameter (defaults to href if
 * <code>attributes</code> is not present).  Any blank character, comma or colon
 * is considered as a separator to delimit attributes.
 *
 * @cocoon.sitemap.component.documentation
 * Augments all <code>href</code> attributes with the full path to
 * the request. You can optionally specify the <code>mount</code>
 * parameter.
 * @cocoon.sitemap.component.name   augment
 * @cocoon.sitemap.component.documentation.caching No
 *
 * @since October 10, 2001
 * @version $Id$
 */
public class AugmentTransformer extends AbstractTransformer {

    protected Map objectModel;
    protected Request request;
    protected String baseURI;
    protected Set augmentedAttributes;

    public static final String AUGMENTED_ATTRIBUTES="attributes";


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

        augmentedAttributes = new HashSet();
        myAugmentedAttributes(parameters);

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("List of attributes to augment: " + augmentedAttributes);
        }
    }

    public void startElement(String uri,
                             String name,
                             String qname,
                             Attributes attrs)
    throws SAXException {
        AttributesImpl newAttrs = null;

        for (int i = 0, size = attrs.getLength(); i < size; i++) {
            String attrName = attrs.getLocalName(i);
            if (augmentedAttributes.contains(attrName)) {
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

    /**
     * Parses list of attributes names in form of <code>attr1 attr2 attr3</code>
     * and adds them to <code>augmentedAttributes</code>.
     * @param parameters
     */
    private void myAugmentedAttributes(Parameters parameters) {
        String augmentedAttributesStr = parameters.getParameter(AUGMENTED_ATTRIBUTES, "href");
        if (augmentedAttributesStr != null) {
            StringTokenizer t = new StringTokenizer(augmentedAttributesStr," \t\r\n\f,:");
            while ( t.hasMoreTokens()) {
                String attr = t.nextToken();
                attr = attr.trim();
                if ( attr.length() > 0){
                    augmentedAttributes.add(attr);
                }
            }
        }
    }
}
