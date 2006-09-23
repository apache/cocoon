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

import java.util.StringTokenizer;
import java.util.List;

import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.portal.event.Event;
import org.apache.cocoon.portal.event.EventManager;
import org.apache.cocoon.portal.event.aspect.EventAspect;
import org.apache.cocoon.portal.event.aspect.EventAspectContext;
import org.apache.cocoon.portal.services.LinkService;
import org.apache.cocoon.util.AbstractLogEnabled;

/**
 *
 * @version $Id$
 */
public class RequestParameterEventAspect
	extends AbstractLogEnabled
	implements EventAspect, ThreadSafe {

    protected void process(EventAspectContext context,
                           Request            request, 
                           String             parameterName) {
        String[] values = request.getParameterValues( parameterName );
        final EventManager publisher = context.getPortalService().getEventManager();
        if ( values != null ) {
            for(int i=0; i<values.length; i++) {
                final String current = values[i];
                final Event e = context.getPortalService().getEventConverter().decode(current);
                if ( null != e) {
                    publisher.send(e);
                }
            }
        } else {
            List list = (List) request.getAttribute("org.apache.cocoon.portal." + parameterName);
            if (list != null) {
                Event[] events = (Event[]) list.toArray(new Event[0]);
                for (int i = 0; i < events.length; i++) {
                    publisher.send(events[i]);
                }
            }
        }
    }

	/**
	 * @see org.apache.cocoon.portal.event.aspect.EventAspect#process(org.apache.cocoon.portal.event.aspect.EventAspectContext)
	 */
	public void process(EventAspectContext context) {
        final Request request = ObjectModelHelper.getRequest(context.getPortalService().getProcessInfoProvider().getObjectModel());
        final String requestParameterNames = context.getAspectProperties().getProperty("parameter-name", LinkService.DEFAULT_REQUEST_EVENT_PARAMETER_NAME);
        boolean processedDefault = false;

        StringTokenizer tokenizer = new StringTokenizer(requestParameterNames, ", ");
        while ( tokenizer.hasMoreTokens() ) {
            final String currentName = tokenizer.nextToken();
            this.process(context, request, currentName);
            if ( LinkService.DEFAULT_REQUEST_EVENT_PARAMETER_NAME.equals(currentName) ) {
                processedDefault = true;
            }
        }
        if ( !processedDefault ) {
            this.process( context, request, LinkService.DEFAULT_REQUEST_EVENT_PARAMETER_NAME );
        }
        context.invokeNext();        
	}
}
