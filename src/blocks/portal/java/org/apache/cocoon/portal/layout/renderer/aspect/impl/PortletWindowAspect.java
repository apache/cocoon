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

import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.portal.PortalManager;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.impl.PortletPortalManager;
import org.apache.cocoon.portal.layout.Layout;
import org.apache.cocoon.portal.layout.impl.CopletLayout;
import org.apache.cocoon.portal.layout.renderer.aspect.RendererAspectContext;
import org.apache.cocoon.portal.pluto.PortletURLProviderImpl;
import org.apache.cocoon.portal.pluto.om.PortletWindowImpl;
import org.apache.cocoon.servlet.CocoonServlet;
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
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * 
 * @version CVS $Id$
 */
public final class PortletWindowAspect 
extends AbstractAspect 
implements Contextualizable {

    /** The environment */
    protected PortletContainerEnvironment environment;
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context context) throws ContextException {
        try {
            // now get the portal manager
            ServletConfig servletConfig = (ServletConfig) context.get(CocoonServlet.CONTEXT_SERVLET_CONFIG);
            PortletPortalManager portalManager = (PortletPortalManager) servletConfig.getServletContext().getAttribute(PortalManager.ROLE);
            if ( portalManager != null ) {
                this.environment = portalManager.getPortletContainerEnvironment();
            }
        } catch (ContextException ignore) {
            // we ignore the context exception
            // this avoids startup errors if the portal is configured for the CLI
            // environment
            this.getLogger().warn("The JSR-168 support is disabled as the servlet context is not available.", ignore);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.layout.renderer.RendererAspect#toSAX(org.apache.cocoon.portal.layout.renderer.RendererAspectContext, org.apache.cocoon.portal.layout.Layout, org.apache.cocoon.portal.PortalService, org.xml.sax.ContentHandler)
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
            XMLUtils.createElement(contenthandler, "title", copletInstanceData.getTitle());
        } else {
            if ( ((PortletWindowImpl)window).getLayout() == null ) {
                ((PortletWindowImpl)window).setLayout((CopletLayout)layout);
            }
            
            String title = (String) copletInstanceData.getTemporaryAttribute("dynamic-title");
            if ( title == null ) {
                final PortletDefinition def = window.getPortletEntity().getPortletDefinition();
                try {
                    title = def.getDisplayName(def.getLanguageSet().getDefaultLocale()).getDisplayName();
                } catch (Exception ignore)  {
                    title = copletInstanceData.getTitle();
                }
            }
            XMLUtils.createElement(contenthandler, "title", title);            
        

            if ( this.environment != null ) {
                InformationProviderService ips = (InformationProviderService) this.environment.getContainerService(InformationProviderService.class);
                DynamicInformationProvider dip = ips.getDynamicProvider((HttpServletRequest) context.getObjectModel().get("portlet-request"));
                
                // Sizing
                WindowState ws = (WindowState)copletInstanceData.getTemporaryAttribute("window-state"); 
                if ( ws == null ) {
                    ws = WindowState.NORMAL;
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
                    XMLUtils.createElement(contenthandler, "maximize-uri", url.toString());
                }

                if ( !ws.equals(WindowState.MAXIMIZED)) {
                    PortletURLProviderImpl url = (PortletURLProviderImpl)dip.getPortletURLProvider(window);
                    url.clearParameters();
                    url.setWindowState(WindowState.MAXIMIZED);
                    XMLUtils.createElement(contenthandler, "fullscreen-uri", url.toString());
                }

                // portlet modes
                PortletMode pm = (PortletMode)copletInstanceData.getTemporaryAttribute("portlet-mode"); 
                if ( pm == null ) {
                    pm = PortletMode.VIEW;
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

    protected class PreparedConfiguration {
        public String tagName;
        public boolean rootTag;
        
        public void takeValues(PreparedConfiguration from) {
            this.tagName = from.tagName;
            this.rootTag = from.rootTag;
        }
    }
    
    /* (non-Javadoc)
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
