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
package org.apache.cocoon.portal.impl;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;

import org.apache.cocoon.portal.event.Event;
import org.apache.cocoon.portal.event.impl.ChangeAspectDataEvent;
import org.apache.cocoon.portal.layout.CompositeLayout;
import org.apache.cocoon.portal.layout.Item;
import org.apache.cocoon.portal.layout.NamedItem;
import org.apache.cocoon.util.NetUtils;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;

/**
 * The PageLabelLinkService generates links for named items defined in the layout portal.xml.
 * Links for other entities are passed to the DefaultLinkService to be resolved.
 *
 * @author Ralph Goers
 *
 * @version CVS $Id:$
 */
public class PageLabelLinkService extends DefaultLinkService {

    protected String apectName;

    /** The label manager */
    protected PageLabelManager labelManager;

    /* (non-Javadoc)
    * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
    */
    public void service(ServiceManager manager) throws ServiceException {
        super.service(manager);
        this.labelManager = (PageLabelManager)this.manager.lookup(PageLabelManager.ROLE);
    }

    /**
     * Get the uri for the coplet containing event
     *
     * @param event The event to find
     * @return A URI
     */
    public String getLinkURI(Event event) {
        return getLinkURI(event, null);
    }

    /**
     * Get the uri for the coplet containing event
     *
     * @param event The event to find
     * @param secure true if a secure protocol is required, false otherwise.
     * @return A URI
     */
    public String getLinkURI(Event event, Boolean secure) {
        if (event == null) {
            return this.getRefreshLinkURI(secure);
        }
        if (this.labelManager == null) {
            return super.getLinkURI(event);
        }

        String requestParameterName = this.labelManager.getRequestParameterName();

        if (event instanceof ChangeAspectDataEvent &&
            ((ChangeAspectDataEvent) event).getTarget() instanceof CompositeLayout) {

            ChangeAspectDataEvent e = (ChangeAspectDataEvent)event;
            CompositeLayout layout = (CompositeLayout)e.getTarget();
            int i = ((Integer)e.getData()).intValue();

            Item item = layout.getItem(i);
            if (item instanceof NamedItem) {
                StringBuffer key = new StringBuffer("");
                getKey(item, key);

                if (this.labelManager.getPageLabelEvents(key.toString()) != null) {
                    final LinkInfo info = this.getInfo();
                    boolean hasParams = info.hasParameters();
                    final StringBuffer buffer = new StringBuffer(info.getBase(secure));
                    if (hasParams) {
                        buffer.append('&');
                    }
                    else {
                        buffer.append('?');
                    }
                    try {
                        String encodedKey = NetUtils.encode(key.toString(), "utf-8");
                        buffer.append(requestParameterName).append('=').append(encodedKey);
                    } catch (UnsupportedEncodingException uee) {
                        // ignore this as utf-8 is always supported
                    }
                    return buffer.toString();
                }
            }
        }

        String label = this.labelManager.getCurrentLabel();

        return getLink(super.getLinkURI(event, secure), requestParameterName, label);
    }

    /**
     * Get the uri for this coplet containing the additional events.
     *
     * @param events The events that will be processed by the generated uri.
     * @return A URI
     */
    public String getLinkURI(List events) {
        return getLinkURI(events, null);
    }

    /**
     * Get the uri for this coplet containing the additional events.
     *
     * @param events The events that will be processed by the generated uri.
     * @return A URI
     */
    public String getLinkURI(List events, Boolean secure) {
        if (events == null || events.size() == 0) {
            return this.getRefreshLinkURI(secure);
        }
        if (this.labelManager == null) {
            return super.getLinkURI(events);
        }

        String requestParameterName = this.labelManager.getRequestParameterName();
        final LinkInfo info = this.getInfo();
        final StringBuffer buffer = new StringBuffer(info.getBase(secure));
        boolean hasParams = info.hasParameters();
        Iterator iter = events.iterator();
        StringBuffer value = new StringBuffer("");

        while (iter.hasNext())
        {
            Event event = (Event)iter.next();

            if (event instanceof ChangeAspectDataEvent &&
                ((ChangeAspectDataEvent) event).getTarget() instanceof CompositeLayout) {

                ChangeAspectDataEvent e = (ChangeAspectDataEvent) event;
                CompositeLayout layout = (CompositeLayout) e.getTarget();
                int i = ((Integer) e.getData()).intValue();

                Item item = layout.getItem(i);
                if (value.length() > 0) {
                    value.append('.');
                }
                if (item instanceof NamedItem) {
                    if(value.length()>0) {
                    	value.append(((NamedItem)item).getName());
                    }
                    else {
                        StringBuffer key = new StringBuffer("");
                        getKey(item, key);
                	    value.append(key.toString());
                    }
                }
                else {
                    value.append(Integer.toString(i));
                }
            }
            else {
                String label = this.labelManager.getCurrentLabel();

                return getLink(super.getLinkURI(events, secure), requestParameterName, label);
            }
        }

        if (value.length() > 0 && this.labelManager.getPageLabelEvents(value.toString()) != null) {
            if (hasParams) {
                buffer.append('&');
            }
            else {
                buffer.append('?');
            }
            try {
                buffer.append(requestParameterName).append('=')
                      .append(NetUtils.encode(value.toString(), "utf-8"));
            } catch (UnsupportedEncodingException uee) {
                // ignore this as utf-8 is always supported
            }

            return buffer.toString();
        }

        String label = this.labelManager.getCurrentLabel();

        return getLink(super.getLinkURI(events), requestParameterName, label);
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
        }
        else {
            key.append(parentLayout.getItems().indexOf(item));
        }
    }

    /*
     * Append the page label to the link.
     * @param link The link to add the label to.
     * @param parmName The request parameter name.
     * @param label The page label.
     * @return The modified link.
     */
    private String getLink(String link, String parmName, String label)
    {
        if (label == null) {
            return link;
        }
        StringBuffer uri = new StringBuffer(link);
        if (link.indexOf('?') >= 0) {
            uri.append('&');
        } else {
            uri.append('?');
        }
        try {
            String encodedLabel = NetUtils.encode(label, "utf-8");
            uri.append(parmName).append('=').append(encodedLabel);
        } catch (UnsupportedEncodingException uee) {
            // ignore this as utf-8 is always supported
        }
        return uri.toString();
    }
}
