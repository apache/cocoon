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
package org.apache.cocoon.portal.event.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentSelector;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.event.Event;
import org.apache.cocoon.portal.event.EventConverter;
import org.apache.cocoon.portal.event.EventManager;
import org.apache.cocoon.portal.event.Publisher;
import org.apache.cocoon.portal.event.Register;
import org.apache.cocoon.portal.event.Subscriber;
import org.apache.cocoon.portal.event.aspect.EventAspect;
import org.apache.cocoon.portal.event.subscriber.impl.DefaultChangeAspectDataEventSubscriber;
import org.apache.cocoon.portal.event.subscriber.impl.DefaultLayoutEventSubscriber;
import org.apache.cocoon.portal.event.subscriber.impl.SizingEventSubscriber;

/**
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * 
 * @version CVS $Id: DefaultEventManager.java,v 1.7 2003/05/26 09:53:00 cziegeler Exp $
 */
public class DefaultEventManager 
    extends AbstractLogEnabled
    implements EventManager, 
                Composable, 
                Initializable, 
                ThreadSafe,
                Configurable,
                Disposable,
                Contextualizable,
                Publisher, Register {
                    
    private final String rootEventType = Event.class.getName();
    private Class eventClass;
    private List subscribers = new ArrayList();
    private ComponentManager manager;
    private Configuration configuration;
    
    protected EventAspectChain chain;
    
    protected ComponentSelector aspectSelector;

    protected Context context;
    
    public Publisher getPublisher() {
        return this;
    }
    
    public Register getRegister() {
        return this;
    }
    
    protected Map getObjectModel() {
        return ContextHelper.getObjectModel( this.context );
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration conf) 
    throws ConfigurationException {
        this.configuration = conf;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if (this.manager != null) {
            if ( this.chain != null) {
                this.chain.dispose( this.aspectSelector );
            }
            this.manager.release( this.aspectSelector );
            this.aspectSelector = null;
            this.manager = null;
        }
    }

    public void initialize()
    throws Exception {
        this.eventClass = Class.forName( rootEventType );
        if ( this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("Initialising eventClass " + eventClass);
        }

        // FIXME (CZ,HIGH) : Make this configurable
        this.subscribe(new DefaultLayoutEventSubscriber(this.manager));
        this.subscribe(new DefaultChangeAspectDataEventSubscriber(this.manager));
        this.subscribe(new SizingEventSubscriber(this.manager));
    }

    public void publish( final Event event ) {
        
        if ( getLogger().isDebugEnabled() ) {
            getLogger().debug("Publishing event " + event.getClass());
        } 
        for ( Iterator e = subscribers.iterator(); e.hasNext(); ){
            Subscriber subscriber = (Subscriber)e.next();
            if (subscriber.getEventType().isAssignableFrom(event.getClass())
            && (subscriber.getFilter() == null || subscriber.getFilter().filter(event))) {
                if ( getLogger().isDebugEnabled() ) {
                    getLogger().info("Informing subscriber "+subscriber+" of event "+event.getClass());
                }
                subscriber.inform(event);
            }
        }
    }
    
    public void subscribe( final Subscriber subscriber )
    throws InvalidEventTypeException {
        if ( !eventClass.isAssignableFrom( subscriber.getEventType() ) ) {
            throw new InvalidEventTypeException();
        }

        if ( getLogger().isDebugEnabled() ) {
            getLogger().debug( "Subscribing event " + subscriber.getEventType().getName() );
        }
        
        // Add to list but prevent duplicate subscriptions
        if ( !subscribers.contains( subscriber ) ) {
            subscribers.add( subscriber );
            if ( getLogger().isDebugEnabled() ) {
                getLogger().debug( "Subscribed Event " + subscriber.getEventType().getName() );
                getLogger().debug( "Subscribers now active: " + subscribers.size() );
            }
        }
    }
    
    public void unsubscribe( Subscriber subscriber )
    throws InvalidEventTypeException {
        
        if ( !eventClass.isAssignableFrom( subscriber.getEventType() ) ) {
            throw new InvalidEventTypeException();
        }
        if ( subscribers.contains( subscriber ) ) {
            subscribers.remove( subscriber );
            if ( getLogger().isDebugEnabled() ) {
                getLogger().debug( "Unsubscribed Event " + subscriber.getEventType().getName() );
                getLogger().debug( "Subscribers now active: " + subscribers.size() );
            }
        } else {
            getLogger().warn( "Subscriber " + subscriber + " not found" );
        }
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.component.Composable#compose(org.apache.avalon.framework.component.ComponentManager)
     */
    public void compose(ComponentManager manager) 
    throws ComponentException {
        this.manager = manager;
    }

    /**
     * Process the events
     */
    public void processEvents()
    throws ProcessingException {
        if ( this.configuration != null ) {
            try {
                this.aspectSelector = (ComponentSelector) this.manager.lookup( EventAspect.ROLE+"Selector");
                this.chain = new EventAspectChain();
                this.chain.configure(this.aspectSelector, this.configuration.getChild("event-aspects"));
            } catch (ConfigurationException ce) {
                throw new ProcessingException("Unable configure component.", ce);
            } catch (ComponentException ce) {
                throw new ProcessingException("Unable to lookup component.", ce);
            }
            this.configuration = null;
        }
        DefaultEventAspectContext context = new DefaultEventAspectContext(this.chain);
        EventConverter converter = null;
        PortalService service = null;
        try {
            service = (PortalService)this.manager.lookup(PortalService.ROLE);
            converter = (EventConverter) this.manager.lookup(EventConverter.ROLE);
            Publisher publisher = this.getPublisher();

            converter.start();
            
            // Invoke aspects
            context.setEventPublisher( publisher );
            context.setObjectModel(this.getObjectModel());
            context.setEventConverter(converter);
            context.invokeNext( service );

            converter.finish();

        } catch (ComponentException ce) {
            throw new ProcessingException("Unable to lookup component.", ce);
        } finally {
            this.manager.release(converter);
            this.manager.release(service);
        }

    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context context) 
    throws ContextException {
        this.context = context;
    }

}
