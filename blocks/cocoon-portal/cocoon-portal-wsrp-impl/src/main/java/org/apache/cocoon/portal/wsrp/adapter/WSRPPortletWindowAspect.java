/*
 * Copyright 2005 The Apache Software Foundation.
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
package org.apache.cocoon.portal.wsrp.adapter;

import java.util.HashMap;
import java.util.Map;

import oasis.names.tc.wsrp.v1.types.LocalizedString;

import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.layout.Layout;
import org.apache.cocoon.portal.layout.impl.CopletLayout;
import org.apache.cocoon.portal.layout.renderer.aspect.RendererAspectContext;
import org.apache.cocoon.portal.layout.renderer.aspect.impl.AbstractAspect;
import org.apache.cocoon.portal.wsrp.consumer.ConsumerEnvironmentImpl;
import org.apache.cocoon.portal.wsrp.consumer.SimplePortletWindowSession;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.wsrp4j.consumer.PortletKey;
import org.apache.wsrp4j.consumer.URLGenerator;
import org.apache.wsrp4j.consumer.User;
import org.apache.wsrp4j.consumer.WSRPPortlet;
import org.apache.wsrp4j.exception.WSRPException;
import org.apache.wsrp4j.util.Constants;
import org.apache.wsrp4j.util.Modes;
import org.apache.wsrp4j.util.WindowStates;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * This aspect draws a portlet window for a wsrp portlet.
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
public final class WSRPPortletWindowAspect 
extends AbstractAspect {

    /** The environment implementation*/
    protected ConsumerEnvironmentImpl environment;

    /** The wsrp adapter. */
    protected WSRPAdapter adapter;

    /** The name of the configured wsrp adapter. */
    protected String adapterName = "wsrp";

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
        this.adapter = (WSRPAdapter)this.portalService.getCopletAdapter(this.adapterName);
        if ( this.adapter != null ) {
            this.environment = this.adapter.getConsumerEnvironment();
        }
        this.getLogger().warn("The wsrp support is disabled as the servlet context is not available.");
    }

    /**
     * @see org.apache.cocoon.portal.layout.renderer.aspect.RendererAspect#toSAX(org.apache.cocoon.portal.layout.renderer.aspect.RendererAspectContext, org.apache.cocoon.portal.layout.Layout, org.apache.cocoon.portal.PortalService, org.xml.sax.ContentHandler)
     */
    public void toSAX(RendererAspectContext context,
                      Layout layout,
                      PortalService service,
                      ContentHandler contenthandler)
    throws SAXException {
        if ( this.environment == null ) {
            context.invokeNext(layout, service, contenthandler);
            return;
        }
        final PreparedConfiguration config = (PreparedConfiguration)context.getAspectConfiguration();
        final CopletInstanceData copletInstanceData = ((CopletLayout)layout).getCopletInstanceData();

        if ( config.rootTag ) {
            XMLUtils.startElement(contenthandler, config.tagName);
        }
        final PortletKey portletKey = (PortletKey)copletInstanceData.getTemporaryAttribute(WSRPAdapter.ATTRIBUTE_NAME_PORTLET_KEY);

        if ( portletKey == null ) {
            // no portlet window, so use a default behaviour
            XMLUtils.createElement(contenthandler, "title", copletInstanceData.getTitle());
        } else {
            LocalizedString localizedTitle = (LocalizedString)copletInstanceData.getTemporaryAttribute(WSRPAdapter.ATTRIBUTE_NAME_PORTLET_TITLE);
            String title;
            if ( localizedTitle == null ) {
                title = copletInstanceData.getTitle();
            } else {
                title = localizedTitle.getValue();
            }
            XMLUtils.createElement(contenthandler, "title", title);            

            final String portletInstanceKey = (String)copletInstanceData.getTemporaryAttribute(WSRPAdapter.ATTRIBUTE_NAME_PORTLET_INSTANCE_KEY);
            final User user = (User)copletInstanceData.getTemporaryAttribute(WSRPAdapter.ATTRIBUTE_NAME_USER);

            final WSRPPortlet portlet = this.environment.getPortletRegistry().getPortlet(portletKey);
            try {
                SimplePortletWindowSession windowSession = this.adapter.getSimplePortletWindowSession(portlet, portletInstanceKey, user);
                if ( this.environment != null && windowSession != null ) {
                    this.adapter.setCurrentCopletInstanceData(copletInstanceData);
                    URLGenerator urlGenerator = this.environment.getURLGenerator();

                    String[] supportedWindowStates = (String[])copletInstanceData.getTemporaryAttribute(WSRPAdapter.ATTRIBUTE_NAME_PORTLET_WINDOWSTATES);
                    String ws = windowSession.getWindowState();
                    if ( ws == null ) {
                        ws = WindowStates._normal;
                    }

                    if ( !ws.equals(WindowStates._minimized) 
                         && ArrayUtils.contains(supportedWindowStates, WindowStates._minimized)) {
                        final Map p = new HashMap();
                        p.put(Constants.URL_TYPE, Constants.URL_TYPE_RENDER);
                        p.put(Constants.WINDOW_STATE, WindowStates._minimized);

                        final String link = urlGenerator.getRenderURL(p);
                        XMLUtils.createElement(contenthandler, "minimize-uri", link);
                    }
                    if ( !ws.equals(WindowStates._normal)
                          && ArrayUtils.contains(supportedWindowStates, WindowStates._normal)) {
                        final Map p = new HashMap();
                        p.put(Constants.URL_TYPE, Constants.URL_TYPE_RENDER);
                        p.put(Constants.WINDOW_STATE, WindowStates._normal);

                        final String link = urlGenerator.getRenderURL(p);
                        XMLUtils.createElement(contenthandler, "normal-uri", link);
                    } 
                    if ( !ws.equals(WindowStates._maximized)
                          && ArrayUtils.contains(supportedWindowStates, WindowStates._maximized)) {
                        final Map p = new HashMap();
                        p.put(Constants.URL_TYPE, Constants.URL_TYPE_RENDER);
                        p.put(Constants.WINDOW_STATE, WindowStates._maximized);

                        final String link = urlGenerator.getRenderURL(p);                        
                        XMLUtils.createElement(contenthandler, "maximize-uri", link);
                    }

                    String[] supportedModes = (String[])copletInstanceData.getTemporaryAttribute(WSRPAdapter.ATTRIBUTE_NAME_PORTLET_MODES);
                    String pm = windowSession.getMode();
                    if ( pm == null ) {
                        pm = Modes._view;
                    }
                    if ( !pm.equals(Modes._edit) 
                         && ArrayUtils.contains(supportedModes, Modes._edit) ) {
                        final Map p = new HashMap();
                        p.put(Constants.URL_TYPE, Constants.URL_TYPE_RENDER);
                        p.put(Constants.PORTLET_MODE, Modes._edit);

                        final String link = urlGenerator.getRenderURL(p);                        
                        XMLUtils.createElement(contenthandler, "edit-uri", link);                    
                    }
                    if ( !pm.equals(Modes._help)
                        && ArrayUtils.contains(supportedModes, Modes._help) ) {
                        final Map p = new HashMap();
                        p.put(Constants.URL_TYPE, Constants.URL_TYPE_RENDER);
                        p.put(Constants.PORTLET_MODE, Modes._help);

                        final String link = urlGenerator.getRenderURL(p);                        
                        XMLUtils.createElement(contenthandler, "help-uri", link);                    
                    }                
                    if ( !pm.equals(Modes._view)
                        && ArrayUtils.contains(supportedModes, Modes._view) ) {
                        final Map p = new HashMap();
                        p.put(Constants.URL_TYPE, Constants.URL_TYPE_RENDER);
                        p.put(Constants.PORTLET_MODE, Modes._view);

                        final String link = urlGenerator.getRenderURL(p);                        
                        XMLUtils.createElement(contenthandler, "view-uri", link);                    
                    } 
                }
            } catch (WSRPException ignore) {
                // we ignore this
            } finally {
                this.adapter.setCurrentCopletInstanceData(null);                
            }

        }

        context.invokeNext( layout, service, contenthandler );

        if ( config.rootTag ) {
            XMLUtils.endElement(contenthandler, config.tagName);
        }
    }

    /**
     * utility-class to get the tags out of the configuration<br/>
     * 
     */
    protected static class PreparedConfiguration {

        /** name of the element */
        public String tagName;

        /** shows if the element is on the highest level */
        public boolean rootTag;

        /**
         * set the configuration to the local attributes<br/>
         * 
         * @param from the object with the configuration-values
         */
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
