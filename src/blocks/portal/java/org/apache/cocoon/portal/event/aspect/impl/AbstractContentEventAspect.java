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
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.event.Event;
import org.apache.cocoon.portal.event.Publisher;
import org.apache.cocoon.portal.event.aspect.EventAspect;
import org.apache.cocoon.portal.event.aspect.EventAspectContext;
import org.apache.cocoon.portal.layout.Layout;

/**
 *
 * @author <a href="mailto:juergen.seitz@basf-it-services.com">J&uuml;rgen Seitz</a>
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: AbstractContentEventAspect.java,v 1.4 2004/03/05 13:02:12 bdelacretaz Exp $
 */
public abstract class AbstractContentEventAspect
    extends AbstractLogEnabled
    implements EventAspect, ThreadSafe, Serviceable {

    protected ServiceManager manager;

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

    protected abstract String getRequestParameterName();
    
    protected abstract int getRequiredValueCount();
    
    /**
     * Custom publishing of an event.
     * The real values for the event are contained in the array
     * starting with index 2!
     * @param layout  The corresponding layout
     * @param values  The values contained in the request
     */
    protected abstract void publish(Publisher publisher, Layout layout, String[] values);
    
    /**
     * Publish the event.
     * This method gets the layout object from the first two
     * values and invokes {@link #publish(Publisher, Layout, String[])}.
     * @param values The values contained in the request
     */
    protected void publish( PortalService service, Publisher publisher, String[] values) {
        Layout layout = service.getComponentManager().getProfileManager().getPortalLayout(values[0], values[1] );
        if ( layout != null ) {
            this.publish( publisher, layout, values);
        }
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.event.aspect.EventAspect#process(org.apache.cocoon.portal.event.aspect.EventAspectContext, org.apache.cocoon.portal.PortalService)
     */
    public void process(EventAspectContext context, PortalService service) {
        final Request request = ObjectModelHelper.getRequest(context.getObjectModel());
        String[] values = request.getParameterValues(this.getRequestParameterName());
        if (values != null) {
            final Publisher publisher = context.getEventPublisher();
            for (int i = 0; i < values.length; i++) {
                // first try to make an event out of the value of the parameter
                final String value = values[i];
                Event e = null;
                try {
                    e = context.getEventConverter().decode(value);
                    if (null != e) {
                        publisher.publish(e);
                    }
                } catch (Exception ignore) {
                }
                // the event could not be generated, so try different approach
                if (e == null) {
                    // Use '|' character as delimiter between Group, ID and URI
					StringTokenizer tokenizer = new StringTokenizer(value, "|");
					int tokenNumber = 0;
					int tokenCount = tokenizer.countTokens();
					// if only 2 params are in the String
                    // the groupKey is missing and defaults to null
					if (tokenCount == this.getRequiredValueCount()-1) {
                        tokenNumber = tokenNumber + 1;
                        tokenCount++;
					}
                    
                    if ( tokenCount == this.getRequiredValueCount() ) {
                        String [] eventValues = new String[tokenCount];
                                        
                        while (tokenizer.hasMoreTokens()) {
                            eventValues[tokenNumber] = tokenizer.nextToken();
                        
                            tokenNumber = tokenNumber + 1;
                        } 
                    
                        this.publish( service, publisher, eventValues );

                    } else {
                        this.getLogger().warn("Data for Event is not set correctly");
                    }
                }
            }
        }
        // and invoke next one
        context.invokeNext(service);
    }

}
