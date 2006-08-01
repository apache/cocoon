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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.coplet.CopletInstance;
import org.apache.cocoon.portal.event.Event;
import org.apache.cocoon.portal.event.aspect.EventAspect;
import org.apache.cocoon.portal.event.aspect.EventAspectContext;

/**
 * This event processes all wsrp related urls and fires {@link org.apache.cocoon.portal.wsrp.adapter.WSRPEvent}s.
 *
 * @version $Id$
 */
public class WSRPEventAspect implements EventAspect, ThreadSafe {

    public static final String REQUEST_PARAMETER_NAME = "cocoon-wsrpevent";

    /**
     * @see org.apache.cocoon.portal.event.aspect.EventAspect#process(org.apache.cocoon.portal.event.aspect.EventAspectContext, org.apache.cocoon.portal.PortalService)
     */
    public void process(EventAspectContext context, PortalService service) {
        final Request request = ObjectModelHelper.getRequest(context.getObjectModel());
        final String[] values = request.getParameterValues("cocoon-wsrpevent");
        if ( values != null && values.length == 1 ) {
            // create a wsrp event, first build map of parameters
            final Map parameters = new HashMap();
            final Enumeration parameterNames = request.getParameterNames();
            while ( parameterNames.hasMoreElements() ) {
                final String name = (String)parameterNames.nextElement();
                if ( !REQUEST_PARAMETER_NAME.equals(name) ) {
                    final String value = request.getParameter(name);
                    parameters.put(name, value);
                }
            }
            final String copletid = values[0];
            final CopletInstance cid = service.getProfileManager().getCopletInstance(copletid);

            final Event e = new WSRPEvent(cid, parameters);
            service.getEventManager().send(e);
        }
        context.invokeNext(service);
    }
}
