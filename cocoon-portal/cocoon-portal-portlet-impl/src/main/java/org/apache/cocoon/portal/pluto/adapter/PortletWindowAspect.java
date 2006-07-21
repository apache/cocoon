/*
 * Copyright 1999-2002,2004-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.portal.pluto.adapter;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;

import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.layout.Layout;
import org.apache.cocoon.portal.layout.impl.CopletLayout;
import org.apache.cocoon.portal.layout.renderer.aspect.RendererAspectContext;
import org.apache.cocoon.portal.layout.renderer.aspect.impl.AbstractAspect;
import org.apache.cocoon.portal.pluto.PortletURLProviderImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.pluto.om.portlet.PortletDefinition;
import org.apache.pluto.om.window.PortletWindow;
import org.apache.pluto.services.PortletContainerEnvironment;
import org.apache.pluto.services.information.DynamicInformationProvider;
import org.apache.pluto.services.information.InformationProviderService;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * This aspect draws a portlet window for a JSR-168 implementation.
 *
 * <h2>Example XML:</h2>
 * <pre>
 *   &lt;window&gt;
 *     &lt;title&gt;title&lt;/title&gt;
 *     &lt;maximize-uri&gt;event&lt;/maximize-uri&gt;
 *     &lt;minimize-uri&gt;event&lt;/minimize-uri&gt;
 *     &lt;fullscreen-uri&gt;event&lt;/fullscreen-uri&gt;
 *     &lt;edit-uri&gt;event&lt;/edit-uri&gt;
 *     &lt;help-uri&gt;event&lt;/help-uri&gt;
 *     &lt;view-uri&gt;event&lt;/view-uri&gt;
 *     &lt;!-- output of following renderers --&gt;
 *   &lt;/window&gt;
 * </pre>
 *
 * <h2>Applicable to:</h2>
 * <ul>
 *  <li>{@link org.apache.cocoon.portal.layout.impl.CopletLayout}</li>
 * </ul>
 *
 * <h2>Parameters</h2>
 * <table><tbody>
 * <tr><th>root-tag</th><td>Should a root tag surrounding the following output
 *  be generated?</td><td></td><td>boolean</td><td><code>true</code></td></tr>
 * <tr><th>tag-name</th><td>Name of the root tag if requested.
 *  </td><td></td><td>String</td><td><code>"window"</code></td></tr>
 * </tbody></table>
 *
 * @version $Id$
 */
public final class PortletWindowAspect 
    extends AbstractAspect
    implements Parameterizable {

    /** The environment. */
    protected PortletContainerEnvironment environment;

    /** The name of the configured portlet adapter. */
    protected String adapterName = "portlet";

    /**
     * @see org.apache.avalon.framework.parameters.Parameterizable#parameterize(org.apache.avalon.framework.parameters.Parameters)
     */
    public void parameterize(Parameters params) throws ParameterException {
        this.adapterName = params.getParameter("adapter-name", this.adapterName);
    }

    /**
     * @see org.apache.cocoon.portal.impl.AbstractComponent#initialize()
     */
    public void initialize() throws Exception {
        super.initialize();
        PortletAdapter adapter = (PortletAdapter)this.portalService.getCopletAdapter(this.adapterName);
        if ( adapter != null ) {
            this.environment = adapter.getPortletContainerEnvironment();
        }
        if ( this.environment == null ) {
            this.getLogger().warn("The JSR-168 support is disabled as the servlet context is not available.");            
        }
    }

    /**
     * @see org.apache.cocoon.portal.layout.renderer.aspect.RendererAspect#toSAX(org.apache.cocoon.portal.layout.renderer.aspect.RendererAspectContext, org.apache.cocoon.portal.layout.Layout, org.apache.cocoon.portal.PortalService, org.xml.sax.ContentHandler)
     */
    public void toSAX(RendererAspectContext context,
                      Layout layout,
                      PortalService service,
                      ContentHandler contenthandler)
    throws SAXException {
        final PreparedConfiguration config = (PreparedConfiguration)context.getAspectConfiguration();
        final CopletInstanceData copletInstanceData = ((CopletLayout)layout).getCopletInstanceData();

        if ( config.rootTag ) {
            XMLUtils.startElement(contenthandler, config.tagName);
        }
        final PortletWindow window = (PortletWindow)copletInstanceData.getTemporaryAttribute("window");
        if ( window == null ) {
            // no portlet window, so use a default behaviour
            if ( copletInstanceData.getTitle() == null ) {
                XMLUtils.createElement(contenthandler, "title", "No title");
            } else {
                XMLUtils.createElement(contenthandler, "title", copletInstanceData.getTitle());
            }
        } else {
            String title = (String) copletInstanceData.getTemporaryAttribute("dynamic-title");
            if ( title == null ) {
                final PortletDefinition def = window.getPortletEntity().getPortletDefinition();
                try {
                    title = def.getDisplayName(def.getLanguageSet().getDefaultLocale()).getDisplayName();
                } catch (Exception ignore)  {
                    title = copletInstanceData.getTitle();
                }
            }
            if ( title != null ) {
                XMLUtils.createElement(contenthandler, "title", title);            
            }

            if ( this.environment != null ) {
                InformationProviderService ips = (InformationProviderService) this.environment.getContainerService(InformationProviderService.class);
                DynamicInformationProvider dip = ips.getDynamicProvider((HttpServletRequest) context.getObjectModel().get("portlet-request"));

                // Sizing
                final String wsString = (String)copletInstanceData.getTemporaryAttribute("window-state");
                final WindowState ws; 
                if ( wsString == null ) {
                    ws = WindowState.NORMAL;
                } else {
                    ws = new WindowState(wsString);
                }

                if ( !ws.equals(WindowState.MINIMIZED) && !ws.equals(WindowState.MAXIMIZED)) {
                    PortletURLProviderImpl url = (PortletURLProviderImpl)dip.getPortletURLProvider(window);
                    url.clearParameters();
                    url.setWindowState(WindowState.MINIMIZED);
                    
                    XMLUtils.createElement(contenthandler, "minimize-uri", url.toString());
                }

                if ( !ws.equals(WindowState.NORMAL)) {
                    PortletURLProviderImpl url = (PortletURLProviderImpl)dip.getPortletURLProvider(window);
                    url.clearParameters();
                    url.setWindowState(WindowState.NORMAL);
                    XMLUtils.createElement(contenthandler, "normal-uri", url.toString());
                }

                if ( !ws.equals(WindowState.MAXIMIZED)) {
                    PortletURLProviderImpl url = (PortletURLProviderImpl)dip.getPortletURLProvider(window);
                    url.clearParameters();
                    url.setWindowState(WindowState.MAXIMIZED);
                    XMLUtils.createElement(contenthandler, "maximize-uri", url.toString());
                }

                // portlet modes
                final String pmString = (String)copletInstanceData.getTemporaryAttribute("portlet-mode");
                final PortletMode pm; 
                if ( pmString == null ) {
                    pm = PortletMode.VIEW;
                } else {
                    pm = new PortletMode(pmString);
                }
                if ( !pm.equals(PortletMode.EDIT) ) {
                    PortletURLProviderImpl url = (PortletURLProviderImpl)dip.getPortletURLProvider(window);
                    url.clearParameters();
                    url.setPortletMode(PortletMode.EDIT);
                    XMLUtils.createElement(contenthandler, "edit-uri", url.toString());                    
                }
                if ( !pm.equals(PortletMode.HELP) ) {
                    PortletURLProviderImpl url = (PortletURLProviderImpl)dip.getPortletURLProvider(window);
                    url.clearParameters();
                    url.setPortletMode(PortletMode.HELP);
                    XMLUtils.createElement(contenthandler, "help-uri", url.toString());                    
                }                
                if ( !pm.equals(PortletMode.VIEW) ) {
                    PortletURLProviderImpl url = (PortletURLProviderImpl)dip.getPortletURLProvider(window);
                    url.clearParameters();
                    url.setPortletMode(PortletMode.VIEW);
                    XMLUtils.createElement(contenthandler, "view-uri", url.toString());                    
                }                
            }
        }

        context.invokeNext( layout, service, contenthandler );

        if ( config.rootTag ) {
            XMLUtils.endElement(contenthandler, config.tagName);
        }
    }

    protected static class PreparedConfiguration {
        public String tagName;
        public boolean rootTag;
        
        public void takeValues(PreparedConfiguration from) {
            this.tagName = from.tagName;
            this.rootTag = from.rootTag;
        }
    }

    /**
     * @see org.apache.cocoon.portal.layout.renderer.aspect.RendererAspect#prepareConfiguration(org.apache.avalon.framework.parameters.Parameters)
     */
    public Object prepareConfiguration(Parameters configuration) 
    throws ParameterException {
        PreparedConfiguration pc = new PreparedConfiguration();
        pc.tagName = configuration.getParameter("tag-name", "window");
        pc.rootTag = configuration.getParameterAsBoolean("root-tag", true);
        return pc;
    }
}
