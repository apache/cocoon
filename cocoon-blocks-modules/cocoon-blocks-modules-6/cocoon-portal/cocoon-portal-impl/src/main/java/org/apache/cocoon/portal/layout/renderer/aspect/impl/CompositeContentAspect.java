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
import org.apache.cocoon.portal.om.Item;
import org.apache.cocoon.portal.om.Layout;
import org.apache.cocoon.portal.om.LayoutException;
import org.apache.cocoon.portal.util.XMLUtils;
import org.apache.commons.lang.BooleanUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

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
 *  <li>{@link org.apache.cocoon.portal.om.CompositeLayout}</li>
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
 * @version $Id$
 */
public class CompositeContentAspect extends AbstractCompositeAspect {

    protected static final String ITEM_STRING = "item";

    /**
     * @see org.apache.cocoon.portal.layout.renderer.aspect.RendererAspect#toSAX(org.apache.cocoon.portal.layout.renderer.aspect.RendererAspectContext, org.apache.cocoon.portal.om.Layout, org.xml.sax.ContentHandler)
     */
    public void toSAX(RendererAspectContext rendererContext,
                      Layout                layout,
                      ContentHandler        handler)
    throws SAXException, LayoutException {
        PreparedConfiguration config = (PreparedConfiguration)rendererContext.getAspectConfiguration();
        if ( config.rootTag) {
            final AttributesImpl ai = new AttributesImpl();
            if ( config.rootTagClass != null ) {
                XMLUtils.addCDATAAttribute(ai, "class", config.rootTagClass);
            }
            if ( config.rootTagId != null ) {
                XMLUtils.addCDATAAttribute(ai, "id", config.rootTagId);
            }
            XMLUtils.startElement(handler, config.tagName, ai);
        }
        super.toSAX(rendererContext, layout, handler);
        if ( config.rootTag ) {
            XMLUtils.endElement(handler, config.tagName);
        }
    }

	/**
	 * @see org.apache.cocoon.portal.layout.renderer.aspect.impl.AbstractCompositeAspect#processItem(org.apache.cocoon.portal.om.Item, org.xml.sax.ContentHandler, org.apache.cocoon.portal.PortalService)
	 */
	protected void processItem(RendererAspectContext rendererContext,
                               Item                  item,
		                       ContentHandler        handler)
    throws SAXException, LayoutException {
        final PreparedConfiguration config = (PreparedConfiguration)rendererContext.getAspectConfiguration();
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
    				XMLUtils.addCDATAAttribute(attributes, (String)entry.getKey(), (String)entry.getValue());
    			}
                XMLUtils.startElement(handler, config.itemTagName, attributes);
            }
        }
        processLayout(layout, rendererContext.getPortalService(), handler);
        if ( config.itemTag ) {
            XMLUtils.endElement(handler, config.itemTagName);
        }
	}

	/**
	 * @see org.apache.cocoon.portal.layout.renderer.aspect.impl.AbstractCompositeAspect#processMaximizedItem(org.apache.cocoon.portal.om.Item, org.apache.cocoon.portal.om.Layout, org.xml.sax.ContentHandler, org.apache.cocoon.portal.PortalService)
	 */
	protected void processMaximizedItem(RendererAspectContext rendererContext,
                                        Item                  item,
                                        Layout                maximizedLayout,
                                        ContentHandler        handler)
    throws SAXException, LayoutException {
        final PreparedConfiguration config = (PreparedConfiguration)rendererContext.getAspectConfiguration();
        Map parameters = item.getParameters();
        if ( config.itemTag ) {
            if (parameters.size() == 0) {
                XMLUtils.startElement(handler, config.itemTagName);
            } else {
                AttributesImpl attributes = new AttributesImpl();

    			Map.Entry entry;
    			for (Iterator iter = parameters.entrySet().iterator(); iter.hasNext();) {
    				entry = (Map.Entry) iter.next();
    				XMLUtils.addCDATAAttribute(attributes, (String)entry.getKey(), (String)entry.getValue());
    			}
                XMLUtils.startElement(handler, config.itemTagName, attributes);
            }
        }
        processLayout(maximizedLayout, rendererContext.getPortalService(), handler);
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
     * @see org.apache.cocoon.portal.layout.renderer.aspect.RendererAspect#prepareConfiguration(java.util.Properties)
     */
    public Object prepareConfiguration(Properties configuration)
    throws PortalException {
        PreparedConfiguration pc = new PreparedConfiguration();
        pc.tagName = configuration.getProperty("tag-name", "composite");
        pc.rootTag = BooleanUtils.toBoolean(configuration.getProperty("root-tag", "true"));
        pc.itemTag = BooleanUtils.toBoolean(configuration.getProperty("item-tag", "true"));
        pc.itemTagName = configuration.getProperty("item-tag-name", ITEM_STRING);
        pc.rootTagId = configuration.getProperty("root-tag-id", null);
        pc.rootTagClass = configuration.getProperty("root-tag-class", null);
        return pc;
    }

}
