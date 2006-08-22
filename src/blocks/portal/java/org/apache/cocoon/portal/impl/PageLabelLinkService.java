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

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import org.apache.cocoon.portal.event.Event;
import org.apache.cocoon.portal.event.impl.ChangeAspectDataEvent;
import org.apache.cocoon.portal.layout.CompositeLayout;
import org.apache.cocoon.portal.layout.Item;
import org.apache.cocoon.portal.layout.NamedItem;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.components.ContextHelper;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;

/**
 * The PageLabelLinkService generates links for named items defined in the layout portal.xml. Links
 * for other entities are passed to the DefaultLinkService to be resolved.
 *
 * @author Ralph Goers
 * @version CVS $Id:$
 */
public class PageLabelLinkService extends DefaultLinkService
{

    protected String apectName;

    /**
     * The label manager
     */
    protected PageLabelManager labelManager;


    /* (non-Javadoc)
    * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
    */
    public void service(ServiceManager manager) throws ServiceException {
        super.service(manager);
        this.labelManager = (PageLabelManager) this.manager.lookup(PageLabelManager.ROLE);
    }


    /**
     * @see org.apache.cocoon.portal.LinkService#getLinkURI(java.util.List)
     */
    public String getLinkURI(List events, Boolean secure) {
        List eventList = new ArrayList();
        Iterator iter = events.iterator();
        Event aspectEvent = null;
        while (iter.hasNext()) {
            Event event = (Event)iter.next();
            if (event instanceof ChangeAspectDataEvent &&
                ((ChangeAspectDataEvent) event).getTarget() instanceof CompositeLayout) {
                aspectEvent = event;
            } else {
                eventList.add(event);
            }
        }
        if (aspectEvent != null) {
            eventList.add(0, aspectEvent);
        }
        return super.getLinkURI(eventList, secure);
    }

    protected String initBuffer(LinkInfo info, Event event, Boolean secure) {
        StringBuffer base = new StringBuffer(info.getBase(secure));
        if (this.labelManager == null || !this.labelManager.isUrlPath()) {
            return base.toString();
        }

        String label;
        if (event != null && event instanceof ChangeAspectDataEvent &&
            ((ChangeAspectDataEvent) event).getTarget() instanceof CompositeLayout) {
            label = getLabel((ChangeAspectDataEvent)event);
        } else {
            label = this.labelManager.getCurrentLabel();
        }
        if (label.length() > 0) {
            base.setLength(0);
            base.append(label);
        }

        return base.toString();
    }

    protected String initBuffer(LinkInfo info, List events, Boolean secure) {
        if (this.labelManager == null || !this.labelManager.isUrlPath()) {
            return info.getBase(secure);
        }

        Iterator iter = events.iterator();
        Event aspectEvent = null;
        while (iter.hasNext()) {
            Event event = (Event) iter.next();
            if (event instanceof ChangeAspectDataEvent &&
                ((ChangeAspectDataEvent) event).getTarget() instanceof CompositeLayout) {
                aspectEvent = event;
            }
        }
        return initBuffer(info, aspectEvent, secure);
    }

    /**
     * Add one event to the buffer
     *
     * @return Returns true, if the link contains a parameter
     */
    protected boolean addEvent(StringBuffer buffer, Event event, boolean hasParams) {
        if (this.labelManager != null && this.labelManager.isUrlPath() &&
            event instanceof ChangeAspectDataEvent &&
            ((ChangeAspectDataEvent) event).getTarget() instanceof CompositeLayout) {
            return true;
        }

        return super.addEvent(buffer, event, hasParams);
    }


    protected String processEvent(Event event, StringBuffer value) {
        if (this.labelManager != null  &&
            event instanceof ChangeAspectDataEvent &&
            ((ChangeAspectDataEvent) event).getTarget() instanceof CompositeLayout) {
            value.append(getLabel((ChangeAspectDataEvent)event));
            return this.labelManager.getRequestParameterName();
        }
        return super.processEvent(event, value);
    }

    /* (non-Javadoc)
    * @see org.apache.avalon.framework.activity.Disposable#dispose()
    */
    public void dispose() {
        if (this.manager != null) {
            if (this.labelManager != null) {
                this.manager.release(this.labelManager);
                this.labelManager = null;
            }
        }
        super.dispose();
    }

    /**
     * Return the current info for the request.
     *
     * @return A LinkInfo object.
     */
    protected LinkInfo getInfo() {
        if (!labelManager.isUrlPath())
        {
            return super.getInfo();
        }
        final Request request = ContextHelper.getRequest(this.context);
        LinkInfo info = (LinkInfo) request.getAttribute(DefaultLinkService.class.getName());
        if (info == null) {
            synchronized (this) {
                info = (LinkInfo) request.getAttribute(DefaultLinkService.class.getName());
                if (info == null) {
                    info = new PageLabelLinkInfo(labelManager, request, this.defaultPort, this.defaultSecurePort);
                    request.setAttribute(DefaultLinkService.class.getName(), info);
                }
            }
        }
        return info;
    }


    private String getLabel(ChangeAspectDataEvent event) {
        CompositeLayout layout = (CompositeLayout) event.getTarget();
        int i = ((Integer) event.getData()).intValue();

        Item item = layout.getItem(i);
        StringBuffer key = new StringBuffer("");
        if (item instanceof NamedItem) {
            getKey(item, key);
        }
        return key.toString();
    }
    /*
    * Generates the page label.
    * @param item An Item.
    * @param key The StringBuffer in which to create the page label.
    */
    private void getKey(Item item, StringBuffer key) {
        CompositeLayout parentLayout = item.getParent();
        Item parentItem = parentLayout.getParent();

        if (parentItem != null) {
            getKey(parentItem, key);
        }

        if (key.length() > 0) {
            key.append('.');
        }
        if (item instanceof NamedItem) {
            key.append(((NamedItem) item).getName());
        } else {
            key.append(parentLayout.getItems().indexOf(item));
        }
    }
}
