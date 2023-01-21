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
package org.apache.cocoon.portal.layout.renderer.aspect.impl;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.cocoon.portal.PortalException;
import org.apache.cocoon.portal.layout.renderer.aspect.RendererAspectContext;
import org.apache.cocoon.portal.om.Layout;
import org.apache.cocoon.portal.om.LayoutException;
import org.apache.cocoon.portal.util.XMLUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Add layout parameter to resulting XML stream so that they can be picked
 * up later from a stylesheet for example. When passing parameters to the
 * {@link org.apache.cocoon.portal.layout.renderer.aspect.impl.XSLTAspect}
 * consider it's ability to set XSL parameters directly.
 *
 * <h2>Example XML:</h2>
 * <pre>
 *   &lt;parameter name1="value1" name2="value2" ... &gt;
 *     &lt;!-- output from following renderers --&gt;
 *   &lt;/parameter&gt;
 * </pre>
 *
 * <h2>Applicable to:</h2>
 * <ul>
 *  <li>{@link org.apache.cocoon.portal.om.Layout}</li>
 * </ul>
 *
 * <h2>Parameters</h2>
 * <table><tbody>
 * <tr><th>tag-name</th><td>Name of tag holding key-value pairs as attributes.</td>
 *  <td></td><td>String</td><td><code>"parameter"</code></td></tr>
 * </tbody></table>
 *
 * @version $Id$
 */
public final class ParameterAspect extends AbstractAspect {

    /**
     * @see org.apache.cocoon.portal.layout.renderer.aspect.RendererAspect#toSAX(org.apache.cocoon.portal.layout.renderer.aspect.RendererAspectContext, org.apache.cocoon.portal.om.Layout, org.xml.sax.ContentHandler)
     */
    public void toSAX(RendererAspectContext rendererContext,
                      Layout layout,
                      ContentHandler contenthandler)
    throws SAXException, LayoutException {
        final PreparedConfiguration config = (PreparedConfiguration)rendererContext.getAspectConfiguration();

        Map parameter = layout.getParameters();
        if (parameter.size() > 0) {
            AttributesImpl attributes = new AttributesImpl();
            Map.Entry entry;
            for (Iterator iter = parameter.entrySet().iterator(); iter.hasNext();) {
                entry = (Map.Entry) iter.next();
                XMLUtils.addCDATAAttribute(attributes, (String)entry.getKey(), (String)entry.getValue());
            }
            XMLUtils.startElement(contenthandler, config.tagName, attributes);
        } else {
            XMLUtils.startElement(contenthandler, config.tagName);
        }

        rendererContext.invokeNext( layout, contenthandler );

        XMLUtils.endElement(contenthandler, config.tagName);
    }

    protected static class PreparedConfiguration {
        public String tagName;

        public void takeValues(PreparedConfiguration from) {
            this.tagName = from.tagName;
        }
    }

    /**
     * @see org.apache.cocoon.portal.layout.renderer.aspect.impl.AbstractAspect#prepareConfiguration(java.util.Properties)
     */
    public Object prepareConfiguration(Properties configuration)
    throws PortalException {
        PreparedConfiguration pc = new PreparedConfiguration();
        pc.tagName = configuration.getProperty("tag-name", "parameter");
        return pc;
    }
}
