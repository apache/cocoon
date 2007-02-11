/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2002 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

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
 * @version CVS $Id: AbstractContentEventAspect.java,v 1.3 2003/10/20 13:36:42 cziegeler Exp $
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
