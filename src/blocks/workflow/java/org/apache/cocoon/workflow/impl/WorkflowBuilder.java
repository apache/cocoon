/*
$Id: WorkflowBuilder.java,v 1.1 2004/03/01 12:30:49 cziegeler Exp $
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
package org.apache.cocoon.workflow.impl;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.cocoon.workflow.Action;
import org.apache.cocoon.workflow.Condition;
import org.apache.cocoon.workflow.Event;
import org.apache.cocoon.workflow.Workflow;
import org.apache.cocoon.workflow.WorkflowException;
import org.apache.cocoon.xml.DocumentHelper;
import org.apache.log4j.Category;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Utility class to build a workflow schema from a file.
 *
 * @author <a href="mailto:andreas@apache.org">Andreas Hartmann</a>
 * @version $Id: WorkflowBuilder.java,v 1.1 2004/03/01 12:30:49 cziegeler Exp $
 */
public class WorkflowBuilder {

    private static final Category log = Category.getInstance(WorkflowBuilder.class);

    /**
     * Ctor.
     */
    protected WorkflowBuilder() {
    }

    /**
     * Builds a workflow schema from a file.
     * @param file The file.
     * @return A workflow schema implementation.
     * @throws WorkflowException if the file does not represent a valid workflow schema.
     */
    public static WorkflowImpl buildWorkflow(File file) throws WorkflowException {
        WorkflowImpl workflow;

        try {
            Document document = DocumentHelper.readDocument(file);
            workflow = buildWorkflow(document);
        } catch (Exception e) {
            throw new WorkflowException(e);
        }

        return workflow;
    }

    /**
     * Builds a workflow object from an XML document.
     * @param document The XML document.
     * @return A workflow implementation.
     * @throws ParserConfigurationException when something went wrong.
     * @throws SAXException when something went wrong.
     * @throws IOException when something went wrong.
     * @throws WorkflowException when something went wrong.
     */
    protected static WorkflowImpl buildWorkflow(Document document)
        throws ParserConfigurationException, SAXException, IOException, WorkflowException {

        Element root = document.getDocumentElement();
        StateImpl initialState = null;

        Map states = new HashMap();
        Map events = new HashMap();
        Map variables = new HashMap();

        // load states
        NodeList stateElements = root.getElementsByTagNameNS(Workflow.NAMESPACE, STATE_ELEMENT);

        for (int i = 0; i < stateElements.getLength(); i++) {
            Element element = (Element) stateElements.item(i);
            StateImpl state = buildState(element);
            String id = state.getId();
            states.put(id, state);

            if (isInitialStateElement(element)) {
                initialState = state;
            }
        }

        WorkflowImpl workflow = new WorkflowImpl(initialState);

        // load variables
        NodeList variableElements =
            root.getElementsByTagNameNS(Workflow.NAMESPACE, VARIABLE_ELEMENT);

        for (int i = 0; i < variableElements.getLength(); i++) {
            Element element = (Element) variableElements.item(i);
            BooleanVariableImpl variable = buildVariable(element);
            variables.put(variable.getName(), variable);
            workflow.addVariable(variable);
        }

        // load events
        NodeList eventElements = root.getElementsByTagNameNS(Workflow.NAMESPACE, EVENT_ELEMENT);

        for (int i = 0; i < eventElements.getLength(); i++) {
            EventImpl event = buildEvent((Element) eventElements.item(i));
            String id = event.getName();
            events.put(id, event);
            workflow.addEvent(event);
        }

        // load transitions
        NodeList transitionElements =
            root.getElementsByTagNameNS(Workflow.NAMESPACE, TRANSITION_ELEMENT);

        for (int i = 0; i < transitionElements.getLength(); i++) {
            TransitionImpl transition =
                buildTransition((Element) transitionElements.item(i), states, events, variables);
            workflow.addTransition(transition);
        }

        return workflow;
    }

    /**
     * Checks if a state element contains the initial state. 
     * @param element An XML element.
     * @return A boolean value.
     */
    protected static boolean isInitialStateElement(Element element) {
        String initialAttribute = element.getAttribute(INITIAL_ATTRIBUTE);

        return (initialAttribute != null)
            && (initialAttribute.equals("yes") || initialAttribute.equals("true"));
    }

    protected static final String STATE_ELEMENT = "state";
    protected static final String TRANSITION_ELEMENT = "transition";
    protected static final String EVENT_ELEMENT = "event";
    protected static final String CONDITION_ELEMENT = "condition";
    protected static final String ACTION_ELEMENT = "action";
    protected static final String ID_ATTRIBUTE = "id";
    protected static final String INITIAL_ATTRIBUTE = "initial";
    protected static final String SOURCE_ATTRIBUTE = "source";
    protected static final String DESTINATION_ATTRIBUTE = "destination";
    protected static final String CLASS_ATTRIBUTE = "class";
    protected static final String VARIABLE_ELEMENT = "variable";
    protected static final String ASSIGNMENT_ELEMENT = "assign";
    protected static final String VARIABLE_ATTRIBUTE = "variable";
    protected static final String VALUE_ATTRIBUTE = "value";
    protected static final String NAME_ATTRIBUTE = "name";
    protected static final String SYNCHRONIZED_ATTRIBUTE = "synchronized";

    /**
     * Builds a state from an XML element.
     * @param element An XML element.
     * @return A state.
     */
    protected static StateImpl buildState(Element element) {
        String id = element.getAttribute(ID_ATTRIBUTE);
        StateImpl state = new StateImpl(id);

        return state;
    }

    /**
     * Builds a transition from an XML element.
     * @param element An XML element.
     * @param states A map from state IDs to states.
     * @param events A map from event IDs to events.
     * @param variables A map from variable names to variables.
     * @return A transition.
     * @throws WorkflowException when something went wrong.
     */
    protected static TransitionImpl buildTransition(
        Element element,
        Map states,
        Map events,
        Map variables)
        throws WorkflowException {

        if (log.isDebugEnabled()) {
            log.debug("Building transition");
        }

        String sourceId = element.getAttribute(SOURCE_ATTRIBUTE);
        String destinationId = element.getAttribute(DESTINATION_ATTRIBUTE);

        StateImpl source = (StateImpl) states.get(sourceId);
        StateImpl destination = (StateImpl) states.get(destinationId);

        TransitionImpl transition = new TransitionImpl(source, destination);

        // set event
        Element eventElement =
            (Element) element.getElementsByTagNameNS(Workflow.NAMESPACE, EVENT_ELEMENT).item(0);
        String id = eventElement.getAttribute(ID_ATTRIBUTE);
        Event event = (Event) events.get(id);
        transition.setEvent(event);

        if (log.isDebugEnabled()) {
            log.debug("    Event: [" + event + "]");
        }

        // load conditions
        NodeList conditionElements =
            element.getElementsByTagNameNS(Workflow.NAMESPACE, CONDITION_ELEMENT);

        for (int i = 0; i < conditionElements.getLength(); i++) {
            Condition condition = buildCondition((Element) conditionElements.item(i));
            transition.addCondition(condition);
        }

        // load assignments
        NodeList assignmentElements =
            element.getElementsByTagNameNS(Workflow.NAMESPACE, ASSIGNMENT_ELEMENT);

        for (int i = 0; i < assignmentElements.getLength(); i++) {
            BooleanVariableAssignmentImpl action =
                buildAssignment(variables, (Element) assignmentElements.item(i));
            transition.addAction(action);
        }

        // load actions
        NodeList actionElements =
            element.getElementsByTagNameNS(Workflow.NAMESPACE, ACTION_ELEMENT);

        for (int i = 0; i < actionElements.getLength(); i++) {
            Action action = buildAction((Element) actionElements.item(i));
            transition.addAction(action);
        }

        // set synchronization
        if (element.hasAttribute(SYNCHRONIZED_ATTRIBUTE)) {
            Boolean isSynchronized = Boolean.valueOf(element.getAttribute(SYNCHRONIZED_ATTRIBUTE));
            transition.setSynchronized(isSynchronized.booleanValue());
        }

        return transition;
    }

    /**
     * Builds an event from an XML element.
     * @param element An XML element.
     * @return An event.
     */
    protected static EventImpl buildEvent(Element element) {
        String id = element.getAttribute(ID_ATTRIBUTE);
        EventImpl event = new EventImpl(id);

        return event;
    }

    /**
     * Builds a condition from an XML element.
     * @param element An XML element.
     * @return A condition.
     * @throws WorkflowException when something went wrong.
     */
    protected static Condition buildCondition(Element element) throws WorkflowException {
        String className = element.getAttribute(CLASS_ATTRIBUTE);
        String expression = DocumentHelper.getSimpleElementText(element);
        Condition condition = ConditionFactory.createCondition(className, expression);

        return condition;
    }

    /**
     * Builds an action from an XML element.
     * @param element An XML element.
     * @return An action.
     */
    protected static Action buildAction(Element element) {
        String id = element.getAttribute(ID_ATTRIBUTE);
        Action action = new ActionImpl(id);

        return action;
    }

    /**
     * Builds a boolean variable from an XML element.
     * @param element An XML element.
     * @return A boolean variable.
     */
    protected static BooleanVariableImpl buildVariable(Element element) {
        String name = element.getAttribute(NAME_ATTRIBUTE);
        String value = element.getAttribute(VALUE_ATTRIBUTE);

        return new BooleanVariableImpl(name, Boolean.getBoolean(value));
    }

    /**
     * Builds an assignment object from an XML element.
     * @param variables A map from variable names to variables.
     * @param element An XML element.
     * @return An assignment object.
     * @throws WorkflowException when something went wrong.
     */
    protected static BooleanVariableAssignmentImpl buildAssignment(Map variables, Element element)
        throws WorkflowException {
        String variableName = element.getAttribute(VARIABLE_ATTRIBUTE);

        String valueString = element.getAttribute(VALUE_ATTRIBUTE);
        boolean value = Boolean.valueOf(valueString).booleanValue();

        BooleanVariableImpl variable = (BooleanVariableImpl) variables.get(variableName);

        return new BooleanVariableAssignmentImpl(variable, value);
    }
}
