/*
 * Copyright 1999-2002,2004-2005 The Apache Software Foundation.
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
import java.util.List;
import java.util.ArrayList;

import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.event.layout.ChangeTabEvent;
import org.apache.cocoon.portal.event.layout.LayoutChangeParameterEvent;
import org.apache.cocoon.portal.layout.CompositeLayout;
import org.apache.cocoon.portal.layout.Item;
import org.apache.cocoon.portal.layout.Layout;
import org.apache.cocoon.portal.layout.LayoutFeatures;
import org.apache.cocoon.portal.layout.NamedItem;
import org.apache.cocoon.portal.layout.LayoutFeatures.RenderInfo;
import org.apache.cocoon.portal.layout.renderer.aspect.RendererAspectContext;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * <h2>Example XML:</h2>
 * <pre>
 *   &lt;composite&gt;
 *     &lt;named-item name="..." parameter="link-event"/&gt;
 *     &lt;named-item name="..." selected="true"&gt;
 *       &lt;!-- output from processing layout --&gt;
 *     &lt;/named-item&gt;
 *     &lt;named-item name="..." parameter="link-event"/&gt;
 *     &lt;named-item name="..." parameter="link-event"/&gt;
 *   &lt;/composite&gt;
 * </pre>
 *
 * <h2>Example XML with sub-navigation (child-tag-name enabled):</h2>
 * <pre>
 *   &lt;composite&gt;
 *     &lt;named-item name="..." parameter="link-event"/&gt;
 *     &lt;named-item name="..." selected="true"&gt;
 *       &lt;!-- output from processing layout --&gt;
 *     &lt;/named-item&gt;
 *     &lt;named-item name="..." parameter="link-event"/&gt;
 *     &lt;named-item name="..." parameter="link-event"&gt;
 *       &lt;<i>child-tag-name</i>&gt;
 *         &lt;named-item name="..." parameter="link-event"/&gt;
 *         &lt;named-item name="..." parameter="link-event"/&gt;
 *       &lt;/<i>child-tag-name</i>&gt;
 *     &lt;/named-item&gt;
 *   &lt;/composite&gt;
 * </pre>
 *
 *  <h2>Example XML with sub-navigation (show-all-nav without child-tag-name enabled):</h2>
 * <pre>
 *   &lt;composite&gt;
 *     &lt;named-item name="..." parameter="link-event"/&gt;
 *     &lt;named-item name="..." selected="true"&gt;
 *       &lt;!-- output from processing layout --&gt;
 *     &lt;/named-item&gt;
 *     &lt;named-item name="..." parameter="link-event"/&gt;
 *     &lt;named-item name="..." parameter="link-event"&gt;
 *         &lt;named-item name="..." parameter="link-event"/&gt;
 *         &lt;named-item name="..." parameter="link-event"/&gt;
 *     &lt;/named-item&gt;
 *   &lt;/composite&gt;
 * </pre>
 *
 * <h2>Applicable to:</h2>
 * <ul>
 *  <li>{@link org.apache.cocoon.portal.layout.CompositeLayout}</li>
 * </ul>
 *
 * <h2>Parameters</h2>
 * <table><tbody>
 *  <tr><th>store</th><td></td><td>req</td><td>String</td><td><code>null</code></td></tr>
 *  <tr><th>tag-name</th><td>Name of the tag enclosing the following output.</td>
 *      <td></td><td>String</td><td><code>"composite"</code></td></tr>
 *  <tr><th>root-tag</th><td>Should a tag enclosing the following output be generated?</td>
 *      <td></td><td>boolean</td><td><code>true</code></td></tr>
 *  <tr><th>child-tag-name</th><td>The name of the tag to enclose named items (i.e. the subnavigation)
 * of non-selected (default) items. Setting this parameter will enable show-all-nav.</td>
 *      <td></td><td>String</td><td><code>""</code></td></tr>
 *  <tr><th>show-all-nav</th><td>Setting this value to true will output the enclosed named-items</td>
 *      <td></td><td>boolean</td><td><code>false</code></td></tr>
 *  <tr><th>include-selected</th><td>Setting this value to true will output the enclosed named-items of the selected tab too.</td>
 *      <td></td><td>boolean</td><td><code>false</code></td></tr>
 * </tbody></table>
 *
 * @version $Id$
 */
public class TabContentAspect 
    extends CompositeContentAspect {

    /**
     * @see org.apache.cocoon.portal.layout.renderer.aspect.RendererAspect#toSAX(org.apache.cocoon.portal.layout.renderer.aspect.RendererAspectContext, org.apache.cocoon.portal.layout.Layout, org.apache.cocoon.portal.PortalService, org.xml.sax.ContentHandler)
     */
    public void toSAX(RendererAspectContext rendererContext,
                      Layout layout,
                      PortalService service,
                      ContentHandler handler)
    throws SAXException {
        if (layout instanceof CompositeLayout) {
        	// check for maximized information
        	final RenderInfo maximizedInfo = LayoutFeatures.getRenderInfo(layout);

            final TabPreparedConfiguration config = (TabPreparedConfiguration)rendererContext.getAspectConfiguration();

            if ( config.rootTag ) {
                XMLUtils.startElement(handler, config.tagName);
            }

            final AttributesImpl attributes = new AttributesImpl();
            final CompositeLayout tabLayout = (CompositeLayout) layout;

            // selected tab
            String selectedTabName = (String)layout.getTemporaryAttribute(LayoutFeatures.ATTRIBUTE_TAB);
            int selectedTabIndex = 0;
            if ( selectedTabName != null && !config.useNames) {
                selectedTabIndex = Integer.valueOf(selectedTabName).intValue();
            }

            // loop over all tabs
            for (int j = 0; j < tabLayout.getSize(); j++) {
                final Item tab = tabLayout.getItem(j);

                // open named-item tag
                attributes.clear();
                if ( tab instanceof NamedItem ) {
                    attributes.addCDATAAttribute("name", ((NamedItem)tab).getName());
                }
                boolean selected = false;
                if ( config.useNames ) {
                    if ( selectedTabName == null ) {
                        selected = (j == 0);
                    } else {
                        if ( tab instanceof NamedItem ) {
                            selected = selectedTabName.equalsIgnoreCase(((NamedItem)tab).getName());
                        }
                    }
                } else {
                    selected = (j == selectedTabIndex);
                }
                if ( selected ) {
                    attributes.addCDATAAttribute("selected", "true");
                }
                final LayoutChangeParameterEvent event;
                event = new ChangeTabEvent(tab, config.useNames);
                attributes.addCDATAAttribute("parameter", service.getLinkService().getLinkURI(event)); 

                // add parameters
                final Iterator iter = tab.getParameters().entrySet().iterator();
                while ( iter.hasNext() ) {
                    final Map.Entry entry = (Map.Entry) iter.next();
                    attributes.addCDATAAttribute((String)entry.getKey(), (String)entry.getValue());
                }

                XMLUtils.startElement(handler, "named-item", attributes);
                if (selected) {
                	if ( maximizedInfo != null && maximizedInfo.item.equals(tab) ) {
                		this.processLayout(maximizedInfo.layout, service, handler);
                	} else {
	                    this.processLayout(tab.getLayout(), service, handler);
                	}
                    if (config.includeSelected) {
                        List events = new ArrayList();
                        events.add(event);
                        this.processNavigation(tab.getLayout(), service, handler, events, config);
                    }
                } else if (config.showAllNav) {
                    List events = new ArrayList();
                    events.add(event);
                    this.processNavigation(tab.getLayout(), service, handler, events, config);
                }

                // close named-item tag
                XMLUtils.endElement(handler, "named-item");
            }

            if ( config.rootTag ) {
                XMLUtils.endElement(handler, config.tagName);
            }
        } else {
            throw new SAXException("Wrong layout type, TabLayout expected: " + layout.getClass().getName());
        }
    }

    /**
     * Generate the sub navigation for non-selected tabs
     * @param context
     * @param layout
     * @param service
     * @param handler
     * @throws SAXException
     */
    private void processNavigation(Layout                   layout,
                                   PortalService            service,
                                   ContentHandler           handler,
                                   List                     parentEvents,
                                   TabPreparedConfiguration config)
        throws SAXException {
        if (layout instanceof CompositeLayout) {
            CompositeLayout tabLayout = (CompositeLayout)layout;

            if (tabLayout.getSize() == 0) {
                return;
            }
            AttributesImpl attributes = new AttributesImpl();
            boolean subNav = false;

            // loop over all tabs
            for (int j = 0; j < tabLayout.getSize(); j++) {
                Item tab = tabLayout.getItem(j);

                // open named-item tag
                attributes.clear();
                if (tab instanceof NamedItem) {
                    if (!subNav && !config.childTagName.equals("")) {
                        XMLUtils.startElement(handler, config.childTagName);
                        subNav = true;
                    }
                    attributes.addCDATAAttribute("name", ((NamedItem) tab).getName());
                    final LayoutChangeParameterEvent event;
                    event = new ChangeTabEvent(tab, config.useNames);
                    List events = new ArrayList(parentEvents);
                    events.add(event);

                    attributes.addCDATAAttribute("parameter",
                        service.getLinkService().getLinkURI(events));

                    // add parameters
                    final Iterator iter = tab.getParameters().entrySet().iterator();
                    while (iter.hasNext()) {
                        final Map.Entry entry = (Map.Entry) iter.next();
                        attributes.addCDATAAttribute((String) entry.getKey(),
                            (String) entry.getValue());
                    }

                    XMLUtils.startElement(handler, "named-item", attributes);

                    this.processNavigation(tab.getLayout(), service, handler, events, config);

                    // close named-item tag
                    XMLUtils.endElement(handler, "named-item");
                }
            }
            // close sub-nav tag
            if (subNav) {
                XMLUtils.endElement(handler, config.childTagName);
            }
        }
    }

    protected class TabPreparedConfiguration extends PreparedConfiguration {
        public boolean showAllNav = false;
        public boolean includeSelected = false;
        public String childTagName;
        public boolean useNames = false;

        public void takeValues(TabPreparedConfiguration from) {
            super.takeValues(from);
            this.showAllNav = from.showAllNav;
            this.includeSelected = from.includeSelected;
            this.childTagName = from.childTagName;
            this.useNames = from.useNames;
        }
    }

    /**
     * @see org.apache.cocoon.portal.layout.renderer.aspect.RendererAspect#prepareConfiguration(org.apache.avalon.framework.parameters.Parameters)
     */
    public Object prepareConfiguration(Parameters configuration) 
    throws ParameterException {
        TabPreparedConfiguration pc = new TabPreparedConfiguration();
        pc.takeValues((PreparedConfiguration)super.prepareConfiguration(configuration));
        pc.childTagName = configuration.getParameter("child-tag-name", "");
        if (!pc.childTagName.equals("")) {
            pc.showAllNav = true;
        } else {
            pc.showAllNav = configuration.getParameterAsBoolean("show-all-nav", false);
        }
        pc.includeSelected = configuration.getParameterAsBoolean("include-selected", false);
        pc.useNames = configuration.getParameterAsBoolean("use-names", false);
        return pc;
    }
}
