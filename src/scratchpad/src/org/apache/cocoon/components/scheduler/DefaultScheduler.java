/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

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
package org.apache.cocoon.components.scheduler;

import java.util.Map;

import org.apache.avalon.cornerstone.services.scheduler.Target;
import org.apache.avalon.cornerstone.services.scheduler.TimeScheduler;
import org.apache.avalon.cornerstone.services.scheduler.TimeTrigger;
import org.apache.avalon.cornerstone.services.scheduler.TimeTriggerFactory;
import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.excalibur.event.Queue;
import org.apache.excalibur.event.Sink;
import org.apache.excalibur.event.command.Command;

/**
 * This component can either schedule tasks or directly call one.
 * 
 * @since 2.1.1
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: DefaultScheduler.java,v 1.1 2003/08/21 12:44:18 cziegeler Exp $
 */
public class DefaultScheduler 
    extends AbstractLogEnabled
    implements Component,
               Serviceable,
               Disposable,
               ThreadSafe,
               Scheduler,
               Configurable,
               Contextualizable {
    
    protected ServiceManager manager;
    
    protected TimeScheduler scheduler;
    
    protected Sink commandSink;

    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
        this.scheduler = (TimeScheduler) this.manager.lookup(TimeScheduler.ROLE);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.tasks.TaskManager#addTriggeredTarget(java.lang.String, org.apache.avalon.cornerstone.services.scheduler.TimeTrigger, java.lang.String, org.apache.avalon.framework.parameters.Parameters, java.util.Map)
     */
    public void addTriggeredTarget(String name,
                                   TimeTrigger trigger,
                                   String target,
                                   Parameters parameters,
                                   Map objects) {
        Target t = this.getTarget(target, parameters, objects);
        this.scheduler.addTrigger(name, trigger, t);
    }

    /**
     * @param target
     * @param parameters
     * @param objects
     * @return
     */
    protected Target getTarget(String target, Parameters parameters, Map objects) {
        Target t = new TargetWrapper(this.manager, target, parameters, objects);
        return t;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.tasks.TaskManager#addTriggeredTarget(java.lang.String, org.apache.avalon.cornerstone.services.scheduler.TimeTrigger, java.lang.String)
     */
    public void addTriggeredTarget(String name,
                                   TimeTrigger trigger,
                                   String target) {           
        this.addTriggeredTarget(name, trigger, target, null, null);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.tasks.TaskManager#fireTarget(java.lang.String, org.apache.avalon.framework.parameters.Parameters, java.util.Map)
     */
    public boolean fireTarget(String target, Parameters parameters, Map objects) {        
        return this.commandSink.tryEnqueue(this.getCommand(target, parameters, objects));
    }

    /**
     * @param name
     * @param parameters
     * @param objects
     * @return
     */
    private Command getCommand(String target, Parameters parameters, Map objects) {
        Command t = new TargetWrapper(this.manager, target, parameters, objects);
        return t;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.tasks.TaskManager#fireTarget(java.lang.String)
     */
    public boolean fireTarget(String target) {
        return this.fireTarget(target, null, null);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.tasks.TaskManager#removeTriggeredTarget(java.lang.String)
     */
    public void removeTriggeredTarget(String name) {
        this.scheduler.removeTrigger(name);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.tasks.TaskManager#resetTriggeredTarget(java.lang.String)
     */
    public void resetTriggeredTarget(String name) {
        this.scheduler.resetTrigger(name);
    }

    /**
     * Initializable
     */
    public void configure(Configuration config)
    throws ConfigurationException {
        Configuration[] triggers = config.getChild("triggers").getChildren("trigger");
        if ( triggers != null ) {
            TimeTriggerFactory factory = new TimeTriggerFactory();
            for (int i=0; i < triggers.length; i++) {
                final Configuration current = triggers[i];
                final String name = current.getAttribute("name"); 
                final String target = current.getAttribute("target"); 

                Configuration[] triggerConf = current.getChildren("timed");
                if ( triggerConf != null ) {
                    for(int m=0; m < triggerConf.length; m++) {
                        TimeTrigger timeTrigger = factory.createTimeTrigger(triggerConf[m]);
                        this.addTriggeredTarget(name, timeTrigger, target);
                    }
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context context) throws ContextException {
        this.commandSink = (Sink) context.get(Queue.ROLE);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( this.manager != null ) {
            this.manager.release( this.scheduler );
            this.scheduler = null;
            this.manager = null;
        }

    }

    class TargetWrapper implements Target, Command {
        
        protected ServiceManager manager;
        protected Parameters     parameters;
        protected Map            objects;
        protected String         target;
        
        public TargetWrapper(ServiceManager manager, String target,
                             Parameters p, Map o) {
            this.manager = manager;
            this.parameters = p;
            this.objects = o;
            this.target = target;
        }
        
        /* (non-Javadoc)
         * @see org.apache.avalon.cornerstone.services.scheduler.Target#targetTriggered(java.lang.String)
         */
        public void targetTriggered(String name) {
            // lookup real target
            Target t = null;
            try {
                t = (Target) this.manager.lookup(this.target);
                if ( t instanceof ConfigurableTarget ) {
                    ((ConfigurableTarget)t).setup(this.parameters, this.objects);
                }
                t.targetTriggered(name);
            } catch (ServiceException se) {
                throw new CascadingRuntimeException("Unable to lookup target " + this.target, se); 
            } finally {
                this.manager.release(t);
            }
        }

        /* (non-Javadoc)
         * @see org.apache.avalon.framework.activity.Executable#execute()
         */
        public void execute() throws Exception {
            // lookup real target
            Target t = null;
            try {
                t = (Target) this.manager.lookup(this.target);
                if ( t instanceof ConfigurableTarget ) {
                    ((ConfigurableTarget)t).setup(this.parameters, this.objects);
                }
                t.targetTriggered("direct");
            } finally {
                this.manager.release(t);
            }
        }

    }
}
