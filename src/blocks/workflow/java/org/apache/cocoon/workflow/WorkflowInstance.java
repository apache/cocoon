/*
$Id: WorkflowInstance.java,v 1.1 2004/03/01 12:30:53 cziegeler Exp $
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
package org.apache.cocoon.workflow;

/**
 * <p>A workflow instance is an incarnation of a workflow schema. It consists of</p>
 * <ul>
 * <li>a current state,</li>
 * <li>a mapping which assigns values to all state variables.</li>
 * </ul>
 *
 * @author <a href="mailto:andreas@apache.org">Andreas Hartmann</a>
 * @version $Id: WorkflowInstance.java,v 1.1 2004/03/01 12:30:53 cziegeler Exp $
 */
public interface WorkflowInstance {
    /**
     * Returns the workflow this instance belongs to.
     * @return A Workflow object.
     */
    Workflow getWorkflow();

    /**
     * Returns the current state of this WorkflowInstance.
     * 
     * @return the current state
     */
    State getCurrentState();

    /**
     * Returns the executable events in a certain situation.
     * @param situation The situation.
     * @return An array of events.
     * @throws WorkflowException when something went wrong.
     */
    Event[] getExecutableEvents(Situation situation) throws WorkflowException;

    /**
     * Indicates that the user invoked an event.
     * 
     * @param situation The situation in which the event was invoked.
     * @param event The event that was invoked.
     * @throws WorkflowException when something went wrong.
     */
    void invoke(Situation situation, Event event) throws WorkflowException;

    /**
     * Returns the current value of a variable.
     * @param variableName A variable name.
     * @return A boolean value.
     * @throws WorkflowException when the variable does not exist.
     */
    boolean getValue(String variableName) throws WorkflowException;

    /**
     * Adds a workflow listener.
     * @param listener The listener to add.
     */
    void addWorkflowListener(WorkflowListener listener);

    /**
     * Removes a workflow listener.
     * @param listener The listener to remove.
     */
    void removeWorkflowListener(WorkflowListener listener);

    /**
     * Returns if the transition for a certain event is synchronized.
     * @param event An event.
     * @return A boolean value.
     */
    boolean isSynchronized(Event event) throws WorkflowException;
}
