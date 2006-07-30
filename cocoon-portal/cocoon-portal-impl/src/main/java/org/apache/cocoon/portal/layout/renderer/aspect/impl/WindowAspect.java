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
import java.util.List;

import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.coplet.CopletDefinitionFeatures;
import org.apache.cocoon.portal.coplet.CopletInstance;
import org.apache.cocoon.portal.coplet.CopletInstanceFeatures;
import org.apache.cocoon.portal.coplet.adapter.CopletAdapter;
import org.apache.cocoon.portal.coplet.adapter.CopletDecorationProvider;
import org.apache.cocoon.portal.coplet.adapter.DecorationAction;
import org.apache.cocoon.portal.event.Event;
import org.apache.cocoon.portal.event.coplet.CopletInstanceSizingEvent;
import org.apache.cocoon.portal.event.layout.LayoutRemoveEvent;
import org.apache.cocoon.portal.layout.Layout;
import org.apache.cocoon.portal.layout.LayoutFeatures;
import org.apache.cocoon.portal.layout.impl.CopletLayout;
import org.apache.cocoon.portal.layout.renderer.aspect.RendererAspectContext;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Create all relevant tags for generation the coplet window.
 * This includes the title and the window states.
 * An optional enclosing tag
 *
 * <h2>Example XML:</h2>
 * <pre>
 *  &lt;window&gt;
 *    &lt;title&gt;Simply the best&lt;/title&gt;
 *    &lt;instance-id&gt;my_beautiful_coplet&lt;/instance-id&gt;
 *    &lt;size&gt;normal&lt;/size&gt;
 *  &lt;/window&gt;
 * </pre>
 *
 * <h2>Applicable to:</h2>
 * <ul>
 *  <li>{@link org.apache.cocoon.portal.layout.impl.CopletLayout}</li>
 * </ul>
 *
 * <h2>Parameters</h2>
 * <table><tbody>
 *   <tr>
 *     <th>root-tag</th>
 *     <td>Should a tag enclosing the following output be generated?</td>
 *     <td></td>
 *     <td>boolean</td>
 *     <td><code>true</code></td>
 *   </tr>
 *   <tr>
 *     <th>tag-name</th>
 *     <td>Name of tag enclosing follwoing output if requested.</td>
 *     <td></td>
 *     <td>String</td>
 *     <td><code>"window"</code></td>
 *   </tr>
 * </tbody></table>
 *
 * @version $Id$
 */
public final class WindowAspect extends AbstractAspect {

    public static final String SIZE_TAG = "size";

    public static final String INSTANCE_ID_TAG = "instance-id";

    public static final String TITLE_TAG = "title";

    /** Is full-screen enabled? */
    protected boolean enableFullScreen;

    /** Is maximized enabled? */
    protected boolean enableMaximized;

    /**
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize() throws Exception {
        super.initialize();
        this.enableFullScreen = this.portalService.getConfigurationAsBoolean(PortalService.CONFIGURATION_FULL_SCREEN_ENABLED, true);
        this.enableMaximized = this.portalService.getConfigurationAsBoolean(PortalService.CONFIGURATION_MAXIMIZED_ENABLED, true);
    }

    /**
     * @see org.apache.cocoon.portal.layout.renderer.aspect.RendererAspect#toSAX(org.apache.cocoon.portal.layout.renderer.aspect.RendererAspectContext, org.apache.cocoon.portal.layout.Layout, org.apache.cocoon.portal.PortalService, org.xml.sax.ContentHandler)
     */
    public void toSAX(RendererAspectContext rendererContext,
                      Layout                layout,
                      PortalService         service,
                      ContentHandler        contenthandler)
    throws SAXException {
        final PreparedConfiguration config = (PreparedConfiguration)rendererContext.getAspectConfiguration();
        final CopletInstance copletInstanceData = ((CopletLayout)layout).getCopletInstanceData();

        if ( config.rootTag ) {
            XMLUtils.startElement(contenthandler, config.tagName);
        }
        final CopletAdapter adapter = service.getCopletAdapter(copletInstanceData.getCopletDefinition().getCopletType().getCopletAdapterName());

        // stream some general infos about the copet instance data
        this.streamCopletInstanceDataInfos(copletInstanceData, adapter, layout, contenthandler);
        
        // stream the title
        this.streamTitle(copletInstanceData, adapter, layout, contenthandler);

        // stream portlet modes if the coplet type supports this
        this.streamCopletModes(copletInstanceData, adapter, layout, contenthandler);

        // stream remove button
        this.streamRemoveButton(copletInstanceData, adapter, layout, contenthandler);

        // stream the window states and determine if we should invoke the next aspect
        boolean invokeNext = this.streamWindowStates(copletInstanceData, adapter, layout, contenthandler);
        if ( invokeNext ) {
            rendererContext.invokeNext( layout, service, contenthandler );
        }

        if ( config.rootTag ) {
            XMLUtils.endElement(contenthandler, config.tagName);
        }
    }

    protected void streamCopletInstanceDataInfos(CopletInstance cid,
                                                 CopletAdapter      adapter,
                                                 Layout             layout,
                                                 ContentHandler     contenthandler)
    throws SAXException {
        XMLUtils.createElement(contenthandler, WindowAspect.INSTANCE_ID_TAG, cid.getId());
    }

    protected void streamTitle(CopletInstance cid,
                               CopletAdapter      adapter,
                               Layout             layout,
                               ContentHandler     contenthandler)
    throws SAXException {
        String title = null;
        if ( adapter instanceof CopletDecorationProvider ) {
            title = ((CopletDecorationProvider)adapter).getTitle(cid);
        }
        if ( title == null ) {
            title = cid.getTitle();
        }
        XMLUtils.createElement(contenthandler, WindowAspect.TITLE_TAG, title);
    }

    protected void streamCopletModes(CopletInstance cid,
                                     CopletAdapter      adapter,
                                     Layout             layout,
                                     ContentHandler     contenthandler)
    throws SAXException {
        // Does the coplet type provide the window states for us?
        if ( adapter instanceof CopletDecorationProvider ) {
            List windowStates = ((CopletDecorationProvider)adapter).getPossibleCopletModes(cid);
            final Iterator i = windowStates.iterator();
            while ( i.hasNext() ) {
                final DecorationAction action = (DecorationAction)i.next();
                XMLUtils.createElement(contenthandler, action.getName(), action.getUrl());
            }
        }
    }

    protected void streamRemoveButton(CopletInstance cid,
                                      CopletAdapter      adapter,
                                      Layout             layout,
                                      ContentHandler     contenthandler)
    throws SAXException {
        boolean mandatory = CopletDefinitionFeatures.isMandatory(cid.getCopletDefinition());
        if ( !mandatory ) {
            LayoutRemoveEvent lre = new LayoutRemoveEvent(layout);
            XMLUtils.createElement(contenthandler, "remove-uri", this.portalService.getLinkService().getLinkURI(lre));
        }
    }

    protected boolean streamWindowStates(CopletInstance cid,
                                         CopletAdapter      adapter,
                                         Layout             layout,
                                         ContentHandler     contenthandler)
    throws SAXException {
        boolean showContent = true;

        final boolean sizable = CopletDefinitionFeatures.isSizable(cid.getCopletDefinition());
        if ( sizable ) {
            final int size = cid.getSize();

            // stream out the current size
            XMLUtils.createElement(contenthandler, WindowAspect.SIZE_TAG, CopletInstanceFeatures.sizeToString(size));

            // Does the coplet type provide the window states for us?
            if ( adapter instanceof CopletDecorationProvider ) {
                List windowStates = ((CopletDecorationProvider)adapter).getPossibleWindowStates(cid);
                final Iterator i = windowStates.iterator();
                while ( i.hasNext() ) {
                    final DecorationAction action = (DecorationAction)i.next();
                    XMLUtils.createElement(contenthandler, action.getName(), action.getUrl());
                }
            } else {
                Event event;

                if ( size != CopletInstance.SIZE_MINIMIZED ) {
                    event = new CopletInstanceSizingEvent(cid, CopletInstance.SIZE_MINIMIZED);
                    XMLUtils.createElement(contenthandler, DecorationAction.WINDOW_STATE_MINIMIZED, this.portalService.getLinkService().getLinkURI(event));
                }
                if ( size != CopletInstance.SIZE_NORMAL) {
                    event = new CopletInstanceSizingEvent(cid, CopletInstance.SIZE_NORMAL);
                    XMLUtils.createElement(contenthandler, DecorationAction.WINDOW_STATE_NORMAL, this.portalService.getLinkService().getLinkURI(event));
                }
                if ( this.enableMaximized ) {
                    if ( size != CopletInstance.SIZE_MAXIMIZED ) {
                        event = new CopletInstanceSizingEvent(cid, CopletInstance.SIZE_MAXIMIZED);
                        XMLUtils.createElement(contenthandler, DecorationAction.WINDOW_STATE_MAXIMIZED, this.portalService.getLinkService().getLinkURI(event));
                    }
                }

                if ( this.enableFullScreen ) {
                    boolean supportsFullScreen = CopletDefinitionFeatures.supportsFullScreenMode(cid.getCopletDefinition());
                    if ( supportsFullScreen ) {
                        final Layout rootLayout = this.portalService.getProfileManager().getPortalLayout(null, null);
                        final Layout fullScreenLayout = LayoutFeatures.getFullScreenInfo(rootLayout);
                        if ( fullScreenLayout != null && fullScreenLayout.equals( layout )) {
                            event = new CopletInstanceSizingEvent( cid, CopletInstance.SIZE_NORMAL );
                            XMLUtils.createElement(contenthandler, DecorationAction.WINDOW_STATE_NORMAL, this.portalService.getLinkService().getLinkURI(event));
                        } else {
                            event = new CopletInstanceSizingEvent( cid, CopletInstance.SIZE_FULLSCREEN );
                            XMLUtils.createElement(contenthandler, DecorationAction.WINDOW_STATE_FULLSCREEN, this.portalService.getLinkService().getLinkURI(event));
                        }
                    }
                }
            }
            if (!CopletDefinitionFeatures.handlesSizing(cid.getCopletDefinition())
                && size == CopletInstance.SIZE_MINIMIZED) {
                showContent = false;
            }
        }
        return showContent;
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
