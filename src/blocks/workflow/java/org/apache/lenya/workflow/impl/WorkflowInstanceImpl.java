/*
$Id: WorkflowInstanceImpl.java,v 1.1 2004/02/29 17:34:47 gregor Exp $
<License>

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

 4. The names "Apache Lenya" and  "Apache Software Foundation"  must  not  be
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
 Michael Wechner <michi@apache.org>. For more information on the Apache Soft-
 ware Foundation, please see <http://www.apache.org/>.

 Lenya includes software developed by the Apache Software Foundation, W3C,
 DOM4J Project, BitfluxEditor, Xopus, and WebSHPINX.
</License>
*/
package org.apache.lenya.workflow.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lenya.workflow.Action;
import org.apache.lenya.workflow.BooleanVariable;
import org.apache.lenya.workflow.BooleanVariableInstance;
import org.apache.lenya.workflow.Event;
import org.apache.lenya.workflow.Situation;
import org.apache.lenya.workflow.State;
import org.apache.lenya.workflow.Transition;
import org.apache.lenya.workflow.Workflow;
import org.apache.lenya.workflow.WorkflowException;
import org.apache.lenya.workflow.WorkflowInstance;
import org.apache.lenya.workflow.WorkflowListener;
import org.apache.log4j.Category;


/**
 *
 * @author  andreas
 */
public abstract class WorkflowInstanceImpl implements WorkflowInstance {
    
    private static final Category log = Category.getInstance(WorkflowInstanceImpl.class);
    
    /**
     * Creates a new instance of WorkflowInstanceImpl.
     */
    protected WorkflowInstanceImpl() {
    }

    private WorkflowImpl workflow;

    /**
     * Returns the workflow object of this instance.
     * @return A workflow object.
     */
    public Workflow getWorkflow() {
        return getWorkflowImpl();
    }

    /**
     * Returns the workflow object of this instance.
     * @return A workflow object.
     */
    protected WorkflowImpl getWorkflowImpl() {
        return workflow;
    }

    /** Returns the events that can be invoked in a certain situation.
     * @param situation The situation to check.
     * @return The events that can be invoked.
     * @throws WorkflowException when something went wrong.
     */
    public Event[] getExecutableEvents(Situation situation) throws WorkflowException {
        
        if (log.isDebugEnabled()) {
            log.debug("Resolving executable events");
        }
        
        Transition[] transitions = getWorkflow().getLeavingTransitions(getCurrentState());
        Set executableEvents = new HashSet();

        for (int i = 0; i < transitions.length; i++) {
            if (transitions[i].canFire(situation, this)) {
                executableEvents.add(transitions[i].getEvent());
                if (log.isDebugEnabled()) {
                    log.debug("    [" + transitions[i].getEvent() + "] can fire.");
                }
            }
            else {
                if (log.isDebugEnabled()) {
                    log.debug("    [" + transitions[i].getEvent() + "] can not fire.");
                }
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("    Resolving executable events completed.");
        }
        
        return (Event[]) executableEvents.toArray(new Event[executableEvents.size()]);
    }

    /** Invoke an event on this workflow instance.
     * @param situation The situation when the event was invoked.
     * @param event The event that was invoked.
     * @throws WorkflowException when the event may not be invoked.
     */
    public void invoke(Situation situation, Event event)
        throws WorkflowException {
        if (!Arrays.asList(getExecutableEvents(situation)).contains(event)) {
            throw new WorkflowException("The event '" + event +
                "' cannot be invoked in the situation '" + situation + "'.");
        }

        fire(getNextTransition(event));

        for (Iterator iter = listeners.iterator(); iter.hasNext();) {
            WorkflowListener listener = (WorkflowListener) iter.next();
            listener.transitionFired(this, situation, event);
        }
    }

    /**
     * Returns the transition that would fire for a given event.
     * @param event The event.
     * @return A transition.
     * @throws WorkflowException if no single transition would fire.
     */
    protected TransitionImpl getNextTransition(Event event) throws WorkflowException {
        TransitionImpl nextTransition = null;
        Transition[] transitions = getWorkflow().getLeavingTransitions(getCurrentState());

        for (int i = 0; i < transitions.length; i++) {
            if (transitions[i].getEvent().equals(event)) {
                
                if (nextTransition != null) {
                    throw new WorkflowException("More than one transition found for event [" + event + "]!");
                }
                
                nextTransition = (TransitionImpl) transitions[i];
            }
        }
        
        if (nextTransition == null) {
            throw new WorkflowException("No transition found for event [" + event + "]!");
        }
        
        return nextTransition;
    }

    /**
     * Invokes a transition.
     * @param transition The transition to invoke.
     * @throws WorkflowException if something goes wrong.
     */
    protected void fire(TransitionImpl transition) throws WorkflowException {
        Action[] actions = transition.getActions();

        for (int i = 0; i < actions.length; i++) {
            actions[i].execute(this);
        }

        setCurrentState(transition.getDestination());
    }

    private State currentState;

    /**
     * Sets the current state of this instance.
     * @param state The state to set.
     */
    protected void setCurrentState(State state) {
        assert (state != null) && ((WorkflowImpl) getWorkflow()).containsState(state);
        this.currentState = state;
    }

    /** Returns the current state of this WorkflowInstance.
     * @return A state object.
     */
    public State getCurrentState() {
        return currentState;
    }

    /**
     * Sets the workflow of this instance.
     * @param workflow A workflow object.
     */
    protected void setWorkflow(WorkflowImpl workflow) {
        assert workflow != null;
        this.workflow = workflow;
        setCurrentState(getWorkflow().getInitialState());
        initVariableInstances();
    }

    /**
     * Sets the workflow of this instance.
     * @param workflowName The identifier of the workflow.
     * @throws WorkflowException if something goes wrong.
     */
    protected void setWorkflow(String workflowName) throws WorkflowException {
        assert (workflowName != null) && !"".equals(workflowName);
        setWorkflow(getWorkflow(workflowName));
    }

    /**
     * Factory method to create a workflow object for a given identifier.
     * @param workflowName The workflow identifier.
     * @return A workflow object.
     * @throws WorkflowException when the workflow could not be created.
     */
    protected abstract WorkflowImpl getWorkflow(String workflowName)
        throws WorkflowException;

    /**
     * Returns a workflow state for a given name.
     * @param id The state id.
     * @return A workflow object.
     * @throws WorkflowException when the state was not found.
     */
    protected State getState(String id) throws WorkflowException {
        return getWorkflowImpl().getState(id);
    }

    private Map variableInstances = new HashMap();

    /**
     * Initializes the variable instances in the initial state.
     */
    protected void initVariableInstances() {
        variableInstances.clear();

        BooleanVariable[] variables = getWorkflowImpl().getVariables();

        for (int i = 0; i < variables.length; i++) {
            BooleanVariableInstance instance = new BooleanVariableInstanceImpl();
            instance.setValue(variables[i].getInitialValue());
            variableInstances.put(variables[i], instance);
        }
    }

    /**
     * Returns the corresponding instance of a workflow variable.
     * @param variable A variable of the corresponding workflow.
     * @return A variable instance object.
     * @throws WorkflowException when the variable instance was not found.
     */
    protected BooleanVariableInstance getVariableInstance(BooleanVariable variable)
        throws WorkflowException {
        if (!variableInstances.containsKey(variable)) {
            throw new WorkflowException("No instance for variable '" + variable.getName() + "'!");
        }

        return (BooleanVariableInstance) variableInstances.get(variable);
    }

    /**
     * @see org.apache.lenya.workflow.WorkflowInstance#getValue(java.lang.String)
     */
    public boolean getValue(String variableName) throws WorkflowException {
        BooleanVariable variable = getWorkflowImpl().getVariable(variableName);
        BooleanVariableInstance instance = getVariableInstance(variable);

        return instance.getValue();
    }

    /**
     * Sets the value of a state variable.
     * @param variableName The variable name.
     * @param value The value to set.
     * @throws WorkflowException when the variable was not found.
     */
    protected void setValue(String variableName, boolean value)
        throws WorkflowException {
        BooleanVariable variable = getWorkflowImpl().getVariable(variableName);
        BooleanVariableInstance instance = getVariableInstance(variable);
        instance.setValue(value);
    }

    private List listeners = new ArrayList();

    /**
     * @see org.apache.lenya.workflow.WorkflowInstance#addWorkflowListener(org.apache.lenya.workflow.WorkflowListener)
     */
    public void addWorkflowListener(WorkflowListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * @see org.apache.lenya.workflow.WorkflowInstance#removeWorkflowListener(org.apache.lenya.workflow.WorkflowListener)
     */
    public void removeWorkflowListener(WorkflowListener listener) {
        listeners.remove(listener);
    }

    /**
     * @see org.apache.lenya.workflow.WorkflowInstance#isSynchronized(org.apache.lenya.workflow.Situation, org.apache.lenya.workflow.Event)
     */
    public boolean isSynchronized(Event event) throws WorkflowException {
        Transition nextTransition = getNextTransition(event);
        return nextTransition.isSynchronized();
    }

}
