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
package org.apache.cocoon.portal.event.aspect.impl;

import java.util.List;
import java.util.ArrayList;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.LinkService;
import org.apache.cocoon.portal.event.Event;

import org.apache.cocoon.portal.event.ConvertableEventFactory;
import org.apache.cocoon.portal.event.ConvertableEvent;
import org.apache.cocoon.portal.event.aspect.EventAspect;
import org.apache.cocoon.portal.event.aspect.EventAspectContext;
import org.apache.excalibur.source.SourceUtil;

/**
 * Process all convertable event request parameters, creating the events and saving
 * them in request attributes for processing by EventAspects that follow.
 *
 * @author <a href="mailto:rgoers@apache.org">Ralph Goers</a>
 * @version SVN $Id:  $
 */
public class ConvertableEventAspect extends AbstractLogEnabled
	implements EventAspect, ThreadSafe, Serviceable {

    protected ServiceManager manager;

    /**
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

	/* (non-Javadoc)
	 * @see org.apache.cocoon.portal.event.aspect.EventAspect#process(org.apache.cocoon.portal.event.aspect.EventAspectContext, org.apache.cocoon.portal.PortalService)
	 */
	public void process(EventAspectContext context, PortalService service) {
        final Request request = ObjectModelHelper.getRequest(context.getObjectModel());
        final Parameters config = context.getAspectParameters();
        final String parameterName = config.getParameter("parameter-name",
            LinkService.DEFAULT_CONVERTABLE_EVENT_PARAMETER_NAME);

        String[] parm = request.getParameterValues(parameterName);

        if (parm != null) {
            for (int i=0; i < parm.length; ++i) {
                int index = parm[i].indexOf('(');
                if (index > 0 && parm[i].endsWith(")")) {
                    String eventKey = parm[i].substring(0, index);
                    String eventParm =
                        SourceUtil.decodePath(parm[i].substring(index+1, parm[i].length()-1));
                    Event event = getEvent(service, eventKey, eventParm);
                    String key = "org.apache.cocoon.portal." +
                        ((ConvertableEvent)event).getRequestParameterName();
                    List list = (List)request.getAttribute(key);
                    if (list == null) {
                        list = new ArrayList();
                    }
                    list.add(event);
                    request.setAttribute(key, list);
                }
            }
        }

        context.invokeNext( service );
	}

    private ConvertableEvent getEvent(PortalService service, String factoryName, String eventData) {
        ServiceSelector selector = null;
        ConvertableEventFactory factory = null;
        ConvertableEvent event;
        try {
            selector = (ServiceSelector) this.manager.lookup(ConvertableEventFactory.ROLE + "Selector");
            factory = (ConvertableEventFactory) selector.select(factoryName);
            event = factory.createEvent(service, eventData);
        } catch (ServiceException ce) {
            if (getLogger().isErrorEnabled()) {
                getLogger().error("Unable to create event for " + factoryName +
                    " using " + eventData);
            }
            event = null;
        } finally {
            if (selector != null) {
                selector.release(factory);
            }
            this.manager.release(selector);
        }
        return event;
    }
}
