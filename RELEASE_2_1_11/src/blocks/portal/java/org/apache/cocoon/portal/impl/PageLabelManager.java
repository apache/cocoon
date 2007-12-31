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
package org.apache.cocoon.portal.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.event.impl.ChangeAspectDataEvent;
import org.apache.cocoon.portal.layout.CompositeLayout;
import org.apache.cocoon.portal.layout.Item;
import org.apache.cocoon.portal.layout.Layout;
import org.apache.cocoon.portal.layout.NamedItem;

/**
 * Manages the various activities required for page labels.
 *
 * The name of the request parameter used to identify the page labelmay be configured
 * here by declaring
 * <br><parameter-name><i>request-parm-name</i></parameter-name><br>
 * in the configuration for this component. The default request parameter name is
 * 'pageLabel'.
 * @author Ralph Goers
 *
 * @version CVS $Id$
 */
public class PageLabelManager
    extends AbstractLogEnabled
    implements ThreadSafe, Serviceable, Configurable, Contextualizable, Component, Disposable {

    public static final String ROLE = PageLabelManager.class.getName();

    /** The service manager */
    protected ServiceManager manager;
    /** The portal service */
    protected PortalService portalService;
    /** The cocoon context */
    protected Context context;
    protected String aspectName = null;
    private String requestParameterName;

    /** boolean to determine if page label should use directory structure */
    private boolean useUrlPath;
    private boolean nonStickyTabs;
    private boolean marshallEvents;

    protected static final String LABEL_ARRAY = PageLabelManager.class.getName() + "A";
    protected static final String LABEL_MAP = "page-labels";
    protected static final String EVENT_MAP = PageLabelManager.class.getName() + "E";
    private static final String DEFAULT_REQUEST_PARAMETER_NAME = "pageLabel";

    /**
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
        this.portalService = (PortalService) this.manager.lookup(PortalService.ROLE);
    }

    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if (this.manager != null) {
            this.manager.release(this.portalService);
            this.manager = null;
        }
    }

    /* (non-Javadoc)
    * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
    */
    public void configure(Configuration config) {
        this.requestParameterName =
            config.getChild("parameterName").getValue(DEFAULT_REQUEST_PARAMETER_NAME);
        this.aspectName = config.getChild("aspectName").getValue("tab");
        this.nonStickyTabs =
            Boolean.valueOf(config.getChild("nonStickyTabs").getValue("false")).booleanValue();
        this.marshallEvents =
            Boolean.valueOf(config.getChild("marshallEvents").getValue("false")).booleanValue();
        this.useUrlPath =
            Boolean.valueOf(config.getChild("urlPath").getValue("false")).booleanValue();
    }

    /* (non-Javadoc)
    * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
    */
    public void contextualize(Context context) throws ContextException {
        this.context = context;
    }

    /**
     * Return the current page label.
     * @return The current page label.
     */
    public String getCurrentLabel() {
        String[] labels = getLabels();

        return labels[0];
    }

    /**
     * Return the page label from the previous request.
     * @return The previous page label.
     */
    public String getPreviousLabel() {
        String[] labels = getLabels();

        return labels[1];
    }

    /**
     * Sets the current page label.
     * @return The current page label.
     */
    public String  setCurrentLabel() {
        final Request request =
            ObjectModelHelper.getRequest(ContextHelper.getObjectModel(this.context));
        String value;
        if (this.useUrlPath) {
            value = request.getSitemapURI();
            if (!isLabel(value)) {
                value = null;
            }
        } else {
            value = request.getParameter(this.requestParameterName);
        }

        String[] labels = getLabels();

        if (value != null) {
            labels[1] = labels[0];
            labels[0] = value;
        }
        return labels[0];
    }

    /**
     * Returns whether directory structure should be used
     * @return A boolean specifying if directory structure should be used.
     */
    public boolean isUrlPath() {
        return this.useUrlPath;
    }

    /**
     * Returns the request parameter being used to identify the page label.
     * @return A String containing the request parameter name used for page labels.
     */
    public String getRequestParameterName() {
        return this.requestParameterName;
    }

    /**
     * Return the Map that contains events for all the page labels.
     * @return The Map to use for converting events to and from urls.
     */
    public Map getPageEventMap() {
        Map map = (Map) portalService.getAttribute(EVENT_MAP);
        if (null == map) {
            map = new HashMap();
            portalService.setAttribute(EVENT_MAP, map);
        }

        return map;
    }

    protected Map getLabelMap() {
        final Layout rootLayout = portalService.getComponentManager().getProfileManager().getPortalLayout(null, null);
        Map map = (Map) rootLayout.getAspectData(LABEL_MAP);
        if (null == map) {
            map = this.initializeLabels();
            rootLayout.setAspectData(LABEL_MAP, map);
        }
        return map;
    }

    /**
     * Retrieve the events associated with the specified page label.
     *
     * @param pageLabel The label to retrieve the events for.
     * @return A List containing all the events associated with the page label in the order they
     *         should occur.
     */
    public List getPageLabelEvents(String pageLabel) {
        final Map labelMap = this.getLabelMap();
        List list = (List) labelMap.get(pageLabel);

        if (list == null) {
            list = new ArrayList();
            labelMap.put(pageLabel, list);
        }

        return list;
    }

    public boolean isLabel(String pageLabel) {
        final Map labelMap = this.getLabelMap();

        return labelMap.containsKey(pageLabel);
    }

    /**
     * Returns true if events are not to be exposed as request parameters
     */
    public boolean isMarshallEvents() {
        return this.marshallEvents;
    }

    /**
     * Return the array containing the current and previous labels.
     */
    private String[] getLabels() {
        String[] labels = (String[]) portalService.getAttribute(LABEL_ARRAY);
        if (null == labels) {
            labels = new String[2];
            labels[0] = getRoot();
            portalService.setAttribute(LABEL_ARRAY, labels);
        }
        return labels;
    }

    /**
     * Create the page label event map and return it.
     *
     * @param service The portal service
     * @return The page label map.
     */
    private Map initializeLabels() {
        final Map map = new HashMap();

        final Layout portalLayout =
            portalService.getComponentManager().getProfileManager().getPortalLayout(null, null);

        if (portalLayout instanceof CompositeLayout) {
            this.populate((CompositeLayout) portalLayout, map, "", new ArrayList());
        }

        return map;
    }

    private String getRoot() {
        final Layout portalLayout =
            portalService.getComponentManager().getProfileManager().getPortalLayout(null, null);

        if (portalLayout instanceof CompositeLayout) {
            return getRoot((CompositeLayout)portalLayout, new StringBuffer(""));
        }
        return "";
    }

    private String getRoot(CompositeLayout layout, StringBuffer root) {
        for (int j=0; j < layout.getSize(); j++) {
            Item tab = layout.getItem(j);
            if (tab instanceof NamedItem) {
                if (root.length() > 0) {
                    root.append(".");
                }
                root.append(((NamedItem)tab).getName());
                Layout child = tab.getLayout();
                if (child != null && child instanceof CompositeLayout) {
                    getRoot((CompositeLayout)child, root);
                }
                break;
            }
        }
        return root.toString();
    }


    /**
     * Populate the event map
     *
     * @param layout
     * @param map
     * @param name
     * @param parentEvents
     */
    private List populate(CompositeLayout layout, Map map, String name, List parentEvents) {
        List lhList = null;
        for (int j = 0; j < layout.getSize(); j++) {
            Item tab = layout.getItem(j);
            ChangeAspectDataEvent event =
                new ChangeAspectDataEvent(layout, this.aspectName, new Integer(j));
            StringBuffer label = new StringBuffer(name);
            if (label.length() > 0) {
                label.append(".");
            }
            label.append((tab instanceof NamedItem) ? ((NamedItem) tab).getName() :
                Integer.toString(j));
            List events = new ArrayList(parentEvents);
            events.add(event);
            Layout child = tab.getLayout();
            List allEvents = null;
            if (child != null && child instanceof CompositeLayout) {
                allEvents = populate((CompositeLayout) child, map, label.toString(), events);
            }
            if (this.nonStickyTabs) {
                // With non-sticky tabs the non-leaf nodes always display
                // the left-most child tabs
                if (lhList == null) {
                    if (allEvents != null) {
                        lhList = allEvents;
                    } else {
                        lhList = events;
                    }
                }
                if (allEvents != null) {
                    map.put(label.toString(), allEvents);
                } else {
                    map.put(label.toString(), events);
                }
            } else {
                map.put(label.toString(), events);
            }
        }
        return lhList;
    }
}