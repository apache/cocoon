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

import java.util.Properties;

import org.apache.cocoon.portal.Constants;
import org.apache.cocoon.portal.PortalException;
import org.apache.cocoon.portal.layout.renderer.aspect.RendererAspectContext;
import org.apache.cocoon.portal.om.CopletAdapter;
import org.apache.cocoon.portal.om.CopletInstance;
import org.apache.cocoon.portal.om.CopletLayout;
import org.apache.cocoon.portal.om.Layout;
import org.apache.cocoon.portal.om.LayoutException;
import org.apache.cocoon.portal.om.LayoutFeatures;
import org.apache.cocoon.portal.services.PortalManager;
import org.apache.cocoon.portal.util.IncludeXMLConsumer;
import org.apache.cocoon.portal.util.XMLUtils;
import org.apache.commons.lang.BooleanUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * This aspect directly invokes the coplet adapter to stream out the coplet content.
 * An alternative solution is to generate only cinclude tags by using the
 * {@link org.apache.cocoon.portal.layout.renderer.aspect.impl.CIncludeCopletAspect}
 * and include the coplet contents later. That would allow caching up to the point
 * of the cinclude transformer.
 *
 * <h2>Example XML:</h2>
 * <pre>
 * &lt;content&gt;
 *   &lt;!-- content streamed from coplet --&gt;
 * &lt;/content&gt;
 * </pre>
 *
 * <h2>Applicable to:</h2>
 * <ul>
 *  <li>{@link org.apache.cocoon.portal.om.CopletLayout}</li>
 * </ul>
 *
 * @version $Id$
 */
public class DefaultCopletAspect extends AbstractAspect {

	/**
	 * @see org.apache.cocoon.portal.layout.renderer.aspect.RendererAspect#toSAX(org.apache.cocoon.portal.layout.renderer.aspect.RendererAspectContext, org.apache.cocoon.portal.om.Layout, org.xml.sax.ContentHandler)
	 */
	public void toSAX(RendererAspectContext rendererContext,
                      Layout layout,
                      ContentHandler handler)
	throws SAXException, LayoutException {
        LayoutFeatures.checkLayoutClass(layout, CopletLayout.class, true);
        final PreparedConfiguration config = (PreparedConfiguration)rendererContext.getAspectConfiguration();

        if ( config.rootTag ) {
            XMLUtils.startElement(handler, config.tagName);
        }

        final CopletInstance cid = this.getCopletInstance(((CopletLayout)layout).getCopletInstanceId());
        // if ajax is used and the current request is not an ajax request, we just send some javascript stuff back
        if ( config.useAjax && !rendererContext.getPortalService().getRequestContext().isAjaxRequest() ) {
            final String uri = rendererContext.getPortalService().getLinkService().getRefreshLinkURI();
            final char separator = (uri.indexOf('?') == -1 ? '?' : '&');
            final StringBuffer buffer = new StringBuffer("cocoon.portal.process(\"");
            buffer.append(uri);
            buffer.append(separator);
            buffer.append(PortalManager.PROPERTY_RENDER_COPLET);
            buffer.append('=');
            buffer.append(cid.getId());
            buffer.append("\");");
            final AttributesImpl a = new AttributesImpl();
            XMLUtils.addCDATAAttribute(a, "type", "text/javascript");
            XMLUtils.startElement(handler, "script", a);
            XMLUtils.data(handler, buffer.toString());
            XMLUtils.endElement(handler, "script");
        } else {
            final CopletAdapter copletAdapter = cid.getCopletDefinition().getCopletType().getCopletAdapter();
            copletAdapter.toSAX(cid, new IncludeXMLConsumer(handler));
        }

        if ( config.rootTag ) {
            XMLUtils.endElement(handler, config.tagName);
        }
        rendererContext.invokeNext(layout, handler);
	}

    protected static class PreparedConfiguration {

        public String tagName;
        public boolean rootTag;
        public boolean useAjax;

        public void takeValues(PreparedConfiguration from) {
            this.tagName = from.tagName;
            this.rootTag = from.rootTag;
            this.useAjax = from.useAjax;
        }
    }

    /**
     * @see org.apache.cocoon.portal.layout.renderer.aspect.RendererAspect#prepareConfiguration(Properties)
     */
    public Object prepareConfiguration(Properties configuration)
    throws PortalException {
        PreparedConfiguration pc = new PreparedConfiguration();
        pc.tagName = configuration.getProperty("tag-name", "content");
        pc.rootTag = BooleanUtils.toBoolean(configuration.getProperty("root-tag", "true"));
        pc.useAjax = this.portalService.getConfigurationAsBoolean(Constants.CONFIGURATION_USE_AJAX, Constants.DEFAULT_CONFIGURATION_USE_AJAX);
        return pc;
    }
}
