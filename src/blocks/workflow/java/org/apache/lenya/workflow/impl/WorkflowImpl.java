/*
$Id: WorkflowImpl.java,v 1.1 2004/02/29 17:34:47 gregor Exp $
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.lenya.workflow.State;
import org.apache.lenya.workflow.Transition;
import org.apache.lenya.workflow.Workflow;
import org.apache.lenya.workflow.WorkflowException;


/**
 *
 * @author  andreas
 */
public class WorkflowImpl implements Workflow {
    
    /**
     * Creates a new instance of WorkflowImpl.
     * @param initialState the initial state of the workflow.
     */
    protected WorkflowImpl(StateImpl initialState) {
        this.initialState = initialState;
        addState(initialState);
    }

    private State initialState;

    /** Returns the initial state of this workflow.
     * @return The initial state.
     *
     */
    public State getInitialState() {
        return initialState;
    }

    private Set transitions = new HashSet();
    private Map states = new HashMap();

    /**
     * Adds a state.
     * @param state A state.
     */
    private void addState(StateImpl state) {
        states.put(state.getId(), state);
    }

    /**
     * Adds a transition.
     * @param transition The transition.
     */
    protected void addTransition(TransitionImpl transition) {
        assert transition != null;
        transitions.add(transition);
        addState(transition.getSource());
        addState(transition.getDestination());
    }

    /**
     * Returns the transitions.
     * @return An array of transitions.
     */
    protected TransitionImpl[] getTransitions() {
        return (TransitionImpl[]) transitions.toArray(new TransitionImpl[transitions.size()]);
    }

    /** Returns the destination state of a transition.
     * @param transition A transition.
     * @return The destination state.
     *
     */
    protected State getDestination(Transition transition) {
        assert transition instanceof TransitionImpl;

        return ((TransitionImpl) transition).getDestination();
    }

    /** Returns the transitions that leave a state.
     * @param state A state.
     * @return The transitions that leave the state.
     *
     */
    public Transition[] getLeavingTransitions(State state) {
        Set leavingTransitions = new HashSet();
        TransitionImpl[] transitions = getTransitions();

        for (int i = 0; i < transitions.length; i++) {
            if (transitions[i].getSource() == state) {
                leavingTransitions.add(transitions[i]);
            }
        }

        return (Transition[]) leavingTransitions.toArray(new Transition[leavingTransitions.size()]);
    }

    /**
     * Checks if this workflow contains a state.
     * @param state The state to check.
     * @return <code>true</code> if the state is contained, <code>false</code> otherwise.
     */
    protected boolean containsState(State state) {
        return states.containsValue(state);
    }

    /**
     * Returns the states.
     * @return An array of states.
     */
    protected StateImpl[] getStates() {
        return (StateImpl[]) states.values().toArray(new StateImpl[states.size()]);
    }

    /**
     * Returns the state with a certain name.
     * @param name The state name.
     * @return A state.
     * @throws WorkflowException when the state does not exist.
     */
    protected StateImpl getState(String name) throws WorkflowException {
        if (!states.containsKey(name)) {
            throw new WorkflowException("Workflow does not contain the state '" + name + "'!");
        }

        return (StateImpl) states.get(name);
    }

    private Map events = new HashMap();

    /**
     * Adds an event.
     * @param event An event.
     */
    protected void addEvent(EventImpl event) {
        assert event != null;
        events.put(event.getName(), event);
    }

    /**
     * DOCUMENT ME!
     *
     * @param name DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws WorkflowException DOCUMENT ME!
     */
    public EventImpl getEvent(String name) throws WorkflowException {
        if (!events.containsKey(name)) {
            throw new WorkflowException("Workflow does not contain the event '" + name + "'!");
        }

        return (EventImpl) events.get(name);
    }

    private Map variables = new HashMap();

    /**
     * Adds a variable.
     * @param variable A variable.
     */
    protected void addVariable(BooleanVariableImpl variable) {
        assert variable != null;
        variables.put(variable.getName(), variable);
    }

    /**
     * DOCUMENT ME!
     *
     * @param name DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws WorkflowException DOCUMENT ME!
     */
    public BooleanVariableImpl getVariable(String name)
        throws WorkflowException {
        if (!variables.containsKey(name)) {
            throw new WorkflowException("Workflow does not contain the variable '" + name + "'!");
        }

        return (BooleanVariableImpl) variables.get(name);
    }

    /**
     * Returns the variables.
     * @return An array of variables.
     */
    protected BooleanVariableImpl[] getVariables() {
        return (BooleanVariableImpl[]) variables.values().toArray(new BooleanVariableImpl[variables.size()]);
    }

    /**
     * @see org.apache.lenya.workflow.Workflow#getVariableNames()
     */
    public String[] getVariableNames() {
        BooleanVariableImpl[] variables = getVariables();
        String[] names = new String[variables.length];
        for (int i = 0; i <names.length; i++) {
            names[i] = variables[i].getName();
        }
        return names;
    }
}
