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
package org.apache.cocoon.workflow.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.cocoon.workflow.Event;
import org.apache.cocoon.workflow.Situation;
import org.apache.cocoon.workflow.State;
import org.apache.cocoon.workflow.SynchronizedWorkflowInstances;
import org.apache.cocoon.workflow.WorkflowException;
import org.apache.cocoon.workflow.WorkflowInstance;
import org.apache.log4j.Category;

/**
 * An object of this class encapsulates a set of synchronized
 * workflow instances.
 *
 * FIXME - Remove dependency to log4j
 * 
 * @author <a href="mailto:andreas@apache.org">Andreas Hartmann</a>
 */
public class SynchronizedWorkflowInstancesImpl implements SynchronizedWorkflowInstances {

    private static final Category log = Category.getInstance(SynchronizedWorkflowInstancesImpl.class);

    /**
     * Ctor.
     */
    public SynchronizedWorkflowInstancesImpl() {
    }

    /**
     * Ctor.
     * @param instances The set of workflow instances to synchronize.
     * @param mainInstance The main workflow instance to invoke for non-synchronized transitions.
     */
    public SynchronizedWorkflowInstancesImpl(
        WorkflowInstance[] instances,
        WorkflowInstance mainInstance) {
        setInstances(instances);
        setMainInstance(mainInstance);
    }

    /**
     * Sets the main workflow instance.
     * @param mainInstance The main workflow instance to invoke for non-synchronized transitions.
     */
    public void setMainInstance(WorkflowInstance mainInstance) {
        this.mainInstance = mainInstance;
    }

    private WorkflowInstance[] instances;
    private WorkflowInstance mainInstance;

    public void setInstances(WorkflowInstance[] instances) {
        this.instances = instances;
    }

    public WorkflowInstance[] getInstances() {
        return instances;
    }

    /**
     * Returns all executable events.
     * @see WorkflowInstance#getExecutableEvents(Situation)
     */
    public Event[] getExecutableEvents(Situation situation) throws WorkflowException {
        if (log.isDebugEnabled()) {
            log.debug("Resolving executable events");
        }

        WorkflowInstance[] instances = getInstances();
        if (instances.length == 0) {
            throw new WorkflowException("The set must contain at least one workflow instance!");
        }

        Event[] events = mainInstance.getExecutableEvents(situation);
        Set executableEvents = new HashSet(Arrays.asList(events));

        for (int i = 0; i < events.length; i++) {
            Event event = events[i];
            if (mainInstance.isSynchronized(event)) {

                boolean canFire = true;
                if (log.isDebugEnabled()) {
                    log.debug("    Transition for event [" + event + "] is synchronized.");
                }

                boolean sameState = true;
                State currentState = mainInstance.getCurrentState();
                int j = 0;
                while (j < instances.length && sameState) {
                    sameState = instances[j].getCurrentState().equals(currentState);
                    j++;
                }
                if (log.isDebugEnabled()) {
                    log.debug("    All instances are in the same state: [" + sameState + "]");
                }

                if (sameState) {
                    for (int k = 0; k < instances.length; k++) {
                        WorkflowInstanceImpl instance = (WorkflowInstanceImpl) instances[k];
                        if (instance != mainInstance && !instance.getNextTransition(event).canFire(situation, instance)) {
                            canFire = false;
                            if (log.isDebugEnabled()) {
                                log.debug("    Workflow instance [" + instance + "] can not fire.");
                            }
                        }
                    }
                } else {
                    canFire = false;
                }

                if (!canFire) {
                    executableEvents.remove(event);
                    if (log.isDebugEnabled()) {
                        log.debug("    Event [" + event + "] can not fire - removing from executable events.");
                    }
                }
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("    Resolving executable events completed.");
        }

        return (Event[]) executableEvents.toArray(new Event[executableEvents.size()]);
    }

    /**
     * Invokes an event on all documents.
     * @see WorkflowInstance#invoke(Situation, Event)
     */
    public void invoke(Situation situation, Event event) throws WorkflowException {
        
        if (mainInstance.isSynchronized(event)) {
            for (int i = 0; i < instances.length; i++) {
                instances[i].invoke(situation, event);
            }
        }
        else {
            mainInstance.invoke(situation, event);
        }
    }

}
