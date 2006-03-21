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
import org.apache.cocoon.portal.layout.Item;
import org.apache.cocoon.portal.layout.Layout;
import org.apache.cocoon.portal.layout.renderer.aspect.RendererAspectContext;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Add several contents.
 *
 * <h2>Example XML:</h2>
 * <pre>
 *  &lt;composite&gt;
 *   &lt;item param1="value1" param2="value2"&gt;
 *     &lt;!-- included content from following renderers for this item's layout--&gt;
 *   &lt;/item&gt;
 *   &lt;item&gt;
 *     &lt;!-- included content from following renderers for this item's layout--&gt;
 *   &lt;/item&gt;
 *   &lt;item param1="value1"&gt;
 *     &lt;!-- included content from following renderers for this item's layout--&gt;
 *   &lt;/item&gt;
 *  &lt;/composite&gt;
 * </pre>
 *
 * <h2>Applicable to:</h2>
 * <ul>
 *  <li>{@link org.apache.cocoon.portal.layout.CompositeLayout}</li>
 * </ul>
 *
 * <h2>Parameters</h2>
 * <table><tbody>
 * <tr><th>root-tag</th><td>Enclose result in root tag?</td><td></td><td>boolean</td><td><code>true</code></td></tr>
 * <tr><th>tag-name</th><td>Name of root tag to use.</td><td></td><td>String</td><td><code>"composite"</code></td></tr>
 * <tr><th>item-tag</th><td>Enclose each item in item tag?</td><td></td><td>boolean</td><td><code>true</code></td></tr>
 * <tr><th>item-tag-name</th><td>Name of item tag to use.</td><td></td><td>String</td><td><code>"item"</code></td></tr>
 * <tr><th>root-tag-id</th><td>Value of optional id attribute for the root tag.</td><td></td><td>String</td><td><code>-</code></td></tr>
 * <tr><th>root-tag-class</th><td>Value of optional class attribute for the root tag.</td><td></td><td>String</td><td><code>-</code></td></tr>
 * </tbody></table>
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 *
 * @version CVS $Id:CompositeContentAspect.java 30932 2004-07-29 17:35:38Z vgritsenko $
 */
public class CompositeContentAspect extends AbstractCompositeAspect {

    protected static final String ITEM_STRING = "item";

    /**
     * @see org.apache.cocoon.portal.layout.renderer.aspect.impl.AbstractCompositeAspect#toSAX(org.apache.cocoon.portal.layout.renderer.aspect.RendererAspectContext, org.apache.cocoon.portal.layout.Layout, org.apache.cocoon.portal.PortalService, org.xml.sax.ContentHandler)
     */
    public void toSAX(RendererAspectContext context,
                      Layout                layout,
                      PortalService         service,
                      ContentHandler        handler)
    throws SAXException {
        PreparedConfiguration config = (PreparedConfiguration)context.getAspectConfiguration();
        if ( config.rootTag) {
            final AttributesImpl ai = new AttributesImpl();
            if ( config.rootTagClass != null ) {
                ai.addCDATAAttribute("class", config.rootTagClass);
            }
            if ( config.rootTagId != null ) {
                ai.addCDATAAttribute("id", config.rootTagId);
            }
            XMLUtils.startElement(handler, config.tagName, ai);
        }
        super.toSAX(context, layout, service, handler);
        if ( config.rootTag) {
            XMLUtils.endElement(handler, config.tagName);
        }

    }

	/**
	 * @see org.apache.cocoon.portal.layout.renderer.aspect.impl.AbstractCompositeAspect#processItem(org.apache.cocoon.portal.layout.renderer.aspect.RendererAspectContext, org.apache.cocoon.portal.layout.Item, org.xml.sax.ContentHandler, org.apache.cocoon.portal.PortalService)
	 */
	protected void processItem(RendererAspectContext context,
                               Item                  item,
		                       ContentHandler        handler,
		                       PortalService         service)
    throws SAXException {
        final PreparedConfiguration config = (PreparedConfiguration)context.getAspectConfiguration();
        Layout layout = item.getLayout();

        if ( config.itemTag ) {
            Map parameters = item.getParameters();
            if (parameters.size() == 0) {
                XMLUtils.startElement(handler, config.itemTagName);
            } else {
                AttributesImpl attributes = new AttributesImpl();
    
    			Map.Entry entry;
    			for (Iterator iter = parameters.entrySet().iterator(); iter.hasNext();) {
    				entry = (Map.Entry) iter.next();
    				attributes.addCDATAAttribute((String)entry.getKey(), (String)entry.getValue());
    			}
                XMLUtils.startElement(handler, config.itemTagName, attributes);
            }
        }
        processLayout(layout, service, handler);
        if ( config.itemTag ) {
            XMLUtils.endElement(handler, config.itemTagName);
        }
	}

    protected class PreparedConfiguration {
        public String tagName;
        public boolean rootTag;
        public boolean itemTag;
        public String itemTagName;
        public String rootTagId;
        public String rootTagClass;

        public void takeValues(PreparedConfiguration from) {
            this.tagName = from.tagName;
            this.rootTag = from.rootTag;
            this.itemTag = from.itemTag;
            this.itemTagName = from.itemTagName;
            this.rootTagId = from.rootTagId;
            this.rootTagClass = from.rootTagClass;
        }
    }
    
    /**
     * @see org.apache.cocoon.portal.layout.renderer.aspect.RendererAspect#prepareConfiguration(org.apache.avalon.framework.parameters.Parameters)
     */
    public Object prepareConfiguration(Parameters configuration) 
    throws ParameterException {
        PreparedConfiguration pc = new PreparedConfiguration();
        pc.tagName = configuration.getParameter("tag-name", "composite");
        pc.rootTag = configuration.getParameterAsBoolean("root-tag", true);
        pc.itemTag = configuration.getParameterAsBoolean("item-tag", true);
        pc.itemTagName = configuration.getParameter("item-tag-name", ITEM_STRING);
        pc.rootTagId = configuration.getParameter("root-tag-id", null);
        pc.rootTagClass = configuration.getParameter("root-tag-class", null);
        return pc;
    }

}
