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
import java.util.List;
import java.util.Properties;

import org.apache.cocoon.portal.Constants;
import org.apache.cocoon.portal.PortalException;
import org.apache.cocoon.portal.event.Event;
import org.apache.cocoon.portal.event.coplet.CopletInstanceSizingEvent;
import org.apache.cocoon.portal.event.layout.RemoveLayoutEvent;
import org.apache.cocoon.portal.layout.renderer.aspect.RendererAspectContext;
import org.apache.cocoon.portal.om.CopletAdapter;
import org.apache.cocoon.portal.om.CopletDecorationProvider;
import org.apache.cocoon.portal.om.CopletDefinitionFeatures;
import org.apache.cocoon.portal.om.CopletInstance;
import org.apache.cocoon.portal.om.CopletInstanceFeatures;
import org.apache.cocoon.portal.om.CopletLayout;
import org.apache.cocoon.portal.om.DecorationAction;
import org.apache.cocoon.portal.om.Layout;
import org.apache.cocoon.portal.om.LayoutException;
import org.apache.cocoon.portal.om.LayoutFeatures;
import org.apache.cocoon.portal.util.XMLUtils;
import org.apache.commons.lang.BooleanUtils;
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
 *  <li>{@link org.apache.cocoon.portal.om.CopletLayout}</li>
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
     * Initialize this component.
     */
    public void init() {
        this.enableFullScreen = this.portalService.getConfigurationAsBoolean(Constants.CONFIGURATION_FULL_SCREEN_ENABLED, Constants.DEFAULT_CONFIGURATION_FULL_SCREEN_ENABLED);
        this.enableMaximized = this.portalService.getConfigurationAsBoolean(Constants.CONFIGURATION_MAXIMIZED_ENABLED, Constants.DEFAULT_CONFIGURATION_MAXIMIZED_ENABLED);
    }

    /**
     * @see org.apache.cocoon.portal.layout.renderer.aspect.RendererAspect#toSAX(org.apache.cocoon.portal.layout.renderer.aspect.RendererAspectContext, org.apache.cocoon.portal.om.Layout, org.xml.sax.ContentHandler)
     */
    public void toSAX(RendererAspectContext rendererContext,
                      Layout                layout,
                      ContentHandler        contenthandler)
    throws SAXException, LayoutException {
        final PreparedConfiguration config = (PreparedConfiguration)rendererContext.getAspectConfiguration();
        final CopletInstance copletInstance = this.getCopletInstance(((CopletLayout)layout).getCopletInstanceId());

        if ( config.rootTag ) {
            XMLUtils.startElement(contenthandler, config.tagName);
        }
        final CopletAdapter adapter = copletInstance.getCopletDefinition().getCopletType().getCopletAdapter();

        // stream some general infos about the copet instance data
        this.streamCopletInstanceDataInfos(copletInstance, adapter, layout, contenthandler);

        // stream the title
        this.streamTitle(copletInstance, adapter, layout, contenthandler);

        // stream portlet modes if the coplet type supports this
        this.streamCopletModes(copletInstance, adapter, layout, contenthandler);

        // stream remove button
        this.streamRemoveButton(copletInstance, adapter, layout, contenthandler);

        // stream the window states and determine if we should invoke the next aspect
        boolean invokeNext = this.streamWindowStates(copletInstance, adapter, layout, contenthandler);
        if ( invokeNext ) {
            rendererContext.invokeNext( layout, contenthandler );
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
        XMLUtils.startElement(contenthandler, WindowAspect.INSTANCE_ID_TAG);
        XMLUtils.data(contenthandler, cid.getId());
        XMLUtils.endElement(contenthandler, WindowAspect.INSTANCE_ID_TAG);
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
        XMLUtils.startElement(contenthandler, WindowAspect.TITLE_TAG);
        XMLUtils.data(contenthandler, title);
        XMLUtils.endElement(contenthandler, WindowAspect.TITLE_TAG);
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
                XMLUtils.startElement(contenthandler, action.getName());
                XMLUtils.data(contenthandler, action.getUrl());
                XMLUtils.endElement(contenthandler, action.getName());
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
            RemoveLayoutEvent lre = new RemoveLayoutEvent(layout);
            XMLUtils.startElement(contenthandler, "remove-uri");
            XMLUtils.data(contenthandler, this.portalService.getLinkService().getLinkURI(lre));
            XMLUtils.endElement(contenthandler, "remove-uri");
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
            XMLUtils.startElement(contenthandler, WindowAspect.SIZE_TAG);
            XMLUtils.data(contenthandler, CopletInstanceFeatures.sizeToString(size));
            XMLUtils.endElement(contenthandler, WindowAspect.SIZE_TAG);

            // Does the coplet type provide the window states for us?
            if ( adapter instanceof CopletDecorationProvider ) {
                List windowStates = ((CopletDecorationProvider)adapter).getPossibleWindowStates(cid);
                final Iterator i = windowStates.iterator();
                while ( i.hasNext() ) {
                    final DecorationAction action = (DecorationAction)i.next();
                    XMLUtils.startElement(contenthandler, action.getName());
                    XMLUtils.data(contenthandler, action.getUrl());
                    XMLUtils.endElement(contenthandler, action.getName());
                }
            } else {
                Event event;

                if ( size != CopletInstance.SIZE_MINIMIZED ) {
                    event = new CopletInstanceSizingEvent(cid, CopletInstance.SIZE_MINIMIZED);
                    XMLUtils.startElement(contenthandler, DecorationAction.WINDOW_STATE_MINIMIZED);
                    XMLUtils.data(contenthandler, this.portalService.getLinkService().getLinkURI(event));
                    XMLUtils.endElement(contenthandler, DecorationAction.WINDOW_STATE_MINIMIZED);
                }
                if ( size != CopletInstance.SIZE_NORMAL) {
                    event = new CopletInstanceSizingEvent(cid, CopletInstance.SIZE_NORMAL);
                    XMLUtils.startElement(contenthandler, DecorationAction.WINDOW_STATE_NORMAL);
                    XMLUtils.data(contenthandler, this.portalService.getLinkService().getLinkURI(event));
                    XMLUtils.endElement(contenthandler, DecorationAction.WINDOW_STATE_NORMAL);
                }
                if ( this.enableMaximized ) {
                    if ( size != CopletInstance.SIZE_MAXIMIZED ) {
                        event = new CopletInstanceSizingEvent(cid, CopletInstance.SIZE_MAXIMIZED);
                        XMLUtils.startElement(contenthandler, DecorationAction.WINDOW_STATE_MAXIMIZED);
                        XMLUtils.data(contenthandler, this.portalService.getLinkService().getLinkURI(event));
                        XMLUtils.endElement(contenthandler, DecorationAction.WINDOW_STATE_MAXIMIZED);
                    }
                }

                if ( this.enableFullScreen ) {
                    boolean supportsFullScreen = CopletDefinitionFeatures.supportsFullScreenMode(cid.getCopletDefinition());
                    if ( supportsFullScreen ) {
                        final Layout fullScreenLayout = LayoutFeatures.getFullScreenInfo(this.portalService);
                        if ( fullScreenLayout != null && fullScreenLayout.equals( layout )) {
                            event = new CopletInstanceSizingEvent( cid, CopletInstance.SIZE_NORMAL );
                            XMLUtils.startElement(contenthandler, DecorationAction.WINDOW_STATE_NORMAL);
                            XMLUtils.data(contenthandler, this.portalService.getLinkService().getLinkURI(event));
                            XMLUtils.endElement(contenthandler, DecorationAction.WINDOW_STATE_NORMAL);
                        } else {
                            event = new CopletInstanceSizingEvent( cid, CopletInstance.SIZE_FULLSCREEN );
                            XMLUtils.startElement(contenthandler, DecorationAction.WINDOW_STATE_FULLSCREEN);
                            XMLUtils.data(contenthandler, this.portalService.getLinkService().getLinkURI(event));
                            XMLUtils.endElement(contenthandler, DecorationAction.WINDOW_STATE_FULLSCREEN);
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
     * @see org.apache.cocoon.portal.layout.renderer.aspect.impl.AbstractAspect#prepareConfiguration(java.util.Properties)
     */
    public Object prepareConfiguration(Properties configuration)
    throws PortalException {
        PreparedConfiguration pc = new PreparedConfiguration();
        pc.tagName = configuration.getProperty("tag-name", "window");
        pc.rootTag = BooleanUtils.toBoolean(configuration.getProperty("root-tag", "true"));
        return pc;
    }
}
