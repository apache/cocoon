/*
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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
package org.apache.cocoon.portal.layout.renderer.aspect.impl;

import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.layout.Layout;
import org.apache.cocoon.portal.layout.renderer.aspect.RendererAspectContext;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

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
 *  <li>{@link org.apache.cocoon.portal.layout.Layout}</li>
 * </ul> 
 *
 * <h2>Parameters</h2>
 * <table><tbody>
 * <tr><th>tag-name</th><td>Name of tag holding key-value pairs as attributes.</td>
 *  <td></td><td>String</td><td><code>"parameter"</code></td></tr>
 * </tbody></table> 
 * 
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * 
 * @version CVS $Id: ParameterAspect.java,v 1.3 2004/04/25 20:09:34 haul Exp $
 */
public final class ParameterAspect extends AbstractAspect {

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.layout.renderer.RendererAspect#toSAX(org.apache.cocoon.portal.layout.renderer.RendererAspectContext, org.apache.cocoon.portal.layout.Layout, org.apache.cocoon.portal.PortalService, org.xml.sax.ContentHandler)
     */
    public void toSAX(RendererAspectContext context,
                        Layout layout,
                        PortalService service,
                        ContentHandler contenthandler)
    throws SAXException {
        
        final PreparedConfiguration config = (PreparedConfiguration)context.getAspectConfiguration();

        Map parameter = layout.getParameters();
        if (parameter.size() > 0) {
            AttributesImpl attributes = new AttributesImpl();
            Map.Entry entry;
            for (Iterator iter = parameter.entrySet().iterator(); iter.hasNext();) {
                entry = (Map.Entry) iter.next();
                attributes.addCDATAAttribute((String)entry.getKey(), (String)entry.getValue());
            }
            XMLUtils.startElement(contenthandler, config.tagName, attributes);
        } else {
            XMLUtils.startElement(contenthandler, config.tagName);
        }

        context.invokeNext( layout, service, contenthandler );

        XMLUtils.endElement(contenthandler, config.tagName);
    }

    protected class PreparedConfiguration {
        public String tagName;
        
        public void takeValues(PreparedConfiguration from) {
            this.tagName = from.tagName;
        }
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.layout.renderer.aspect.RendererAspect#prepareConfiguration(org.apache.avalon.framework.parameters.Parameters)
     */
    public Object prepareConfiguration(Parameters configuration) 
    throws ParameterException {
        PreparedConfiguration pc = new PreparedConfiguration();
        pc.tagName = configuration.getParameter("tag-name", "parameter");
        return pc;
    }
}
