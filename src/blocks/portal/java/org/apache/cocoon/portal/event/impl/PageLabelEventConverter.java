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
package org.apache.cocoon.portal.event.impl;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.portal.impl.PageLabelManager;
import org.apache.cocoon.portal.event.Event;
import org.apache.cocoon.portal.event.EventConverter;

/**
 * Convert events from and into strings.
 * @author Ralph Goers
 *
 * @version CVS $Id:  $
 */
public class PageLabelEventConverter extends AbstractLogEnabled
    implements EventConverter, Serviceable, ThreadSafe {

    protected PageLabelManager labelManager;

    private static final String ENCODE = "&ENCODE";
    private static final String DECODE = "&DECODE";

    protected ServiceManager manager;

    /* (non-Javadoc)
    * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
    */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
        this.labelManager = (PageLabelManager)manager.lookup(PageLabelManager.ROLE);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.event.EventConverter#encode(org.apache.cocoon.portal.event.Event)
     */
    public String encode(Event event) {

        String pageLabel = this.labelManager.getCurrentLabel();
        if (pageLabel == null) {
            pageLabel = "";
        }
        Map map = this.labelManager.getPageEventMap();
        String encode = pageLabel + ENCODE;
        List list = (List)map.get(encode);

        if (null == list) {
            list = new ArrayList();
            map.put(encode, list);
        }

        int index = list.indexOf(event);
        if ( index == -1 ) {
          list.add(event);
          index = list.size() - 1;
        }
        return String.valueOf(index);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.event.EventConverter#decode(java.lang.String)
     */
    public Event decode(String value) {
        String pageLabel = this.labelManager.getCurrentLabel();
        if (pageLabel == null) {
            pageLabel = "";
        }
        Map map = this.labelManager.getPageEventMap();
        List list = (List)map.get(pageLabel + DECODE);

        if ( null != list ) {
            int index = new Integer(value).intValue();
            if (index < list.size()) {
                return (Event)list.get(index);
            }
        }
        return null;
    }

    /* (non-Javadoc)
    * @see org.apache.cocoon.portal.event.EventConverter#start()
    */
    public void start() {
        String label = this.labelManager.setCurrentLabel();
        Map map = this.labelManager.getPageEventMap();
        if (label == null) {
            label = "";
        }
        String encode = label + ENCODE;
        String decode = label + DECODE;

        List list = (List)map.get(encode);

        if (null != list) {
            map.put(decode, list);
            map.remove(encode);
        }
    }

    /* (non-Javadoc)
    * @see org.apache.cocoon.portal.event.EventConverter#finish()
    */
    public void finish() {
        // nothing to do         
    }

    /* (non-Javadoc)
    * @see org.apache.cocoon.portal.event.EventConverter#isMarshallEvents()
    */
    public boolean isMarshallEvents() {
        return this.labelManager.isMarshallEvents();
    }
}