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
package org.apache.cocoon.portal.event.aspect.impl;

import java.util.StringTokenizer;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.portal.LinkService;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.event.Event;
import org.apache.cocoon.portal.event.Publisher;
import org.apache.cocoon.portal.event.aspect.EventAspect;
import org.apache.cocoon.portal.event.aspect.EventAspectContext;

/**
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * 
 * @version CVS $Id: RequestParameterEventAspect.java,v 1.4 2004/03/05 13:02:12 bdelacretaz Exp $
 */
public class RequestParameterEventAspect
	extends AbstractLogEnabled
	implements EventAspect, ThreadSafe {

    protected void process(EventAspectContext context, Request request, String parameterName) {
        String[] values = request.getParameterValues( parameterName );
        if ( values != null ) {
            final Publisher publisher = context.getEventPublisher();
            for(int i=0; i<values.length; i++) {
                final String current = values[i];
                final Event e = context.getEventConverter().decode(current);
                if ( null != e) {
                    publisher.publish(e);
                }
            }
        }
    }
    
	/* (non-Javadoc)
	 * @see org.apache.cocoon.portal.event.aspect.EventAspect#process(org.apache.cocoon.portal.event.aspect.EventAspectContext, org.apache.cocoon.portal.PortalService)
	 */
	public void process(EventAspectContext context, PortalService service) {
        final Request request = ObjectModelHelper.getRequest( context.getObjectModel() );
        final Parameters config = context.getAspectParameters();
        final String requestParameterNames = config.getParameter("parameter-name", LinkService.DEFAULT_REQUEST_EVENT_PARAMETER_NAME);
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
        context.invokeNext( service );        
	}

}
