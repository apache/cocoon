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
package org.apache.cocoon.workflow;


/**
 * <p>
 * Synchronized workflow instances.
 * </p>
 * 
 * <p>
 * A set of workflow instances with the same workflow schema can be synchronized.
 * If a transition in this schema is marked as synchronized, it can only be invoked
 * on all instances in the set at the same time.
 * </p>
 *
 * <p>
 * When a workflow event is invoked on a set of synchronized workflow instances,
 * the transition is invoked only if
 * </p>
 * <ul>
 * <li>all instances are in the source state of the transition, and</li>
 * <li>all conditions of the transition are complied for all instances.</li>
 * </ul>
 * 
 * <p>
 * Then the transition is invoked for all instances in the set.
 * </p>
 * <p>
 * A common usecase of this concept is the simultaneous publishing of
 * a set of documents (all language versions of a document, a section, ...).
 * </p>
 * 
 * @author <a href="mailto:andreas@apache.org">Andreas Hartmann</a>
 */
public interface SynchronizedWorkflowInstances {
    
    /**
     * Returns all executable events.
     * @see WorkflowInstance#getExecutableEvents(Situation)
     */
    Event[] getExecutableEvents(Situation situation) throws WorkflowException;
    
    /**
     * Invokes an event on all documents.
     * @see WorkflowInstance#invoke(Situation, Event)
     */
    void invoke(Situation situation, Event event) throws WorkflowException;
}