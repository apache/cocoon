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

import org.apache.cocoon.workflow.Situation;
import org.apache.cocoon.workflow.WorkflowException;
import org.apache.cocoon.workflow.WorkflowInstance;
import org.apache.log4j.Category;

/**
 * Implementation of a boolean variable condition.
 *
 * FIXME - Remove dependency to log4j
 * 
 * @author <a href="mailto:andreas@apache.org">Andreas Hartmann</a>
 * @version $Id: BooleanVariableCondition.java,v 1.3 2004/03/01 21:00:27 cziegeler Exp $
 */
public class BooleanVariableCondition extends AbstractCondition {
    
    private static final Category log = Category.getInstance(BooleanVariableCondition.class);

    private String variableName;
    private boolean value;

    /**
     * Returns the variable value to check.
     * @return A boolean value.
     */
    protected boolean getValue() {
        return value;
    }

    /**
     * Returns the variable name to check.
     * @return A string.
     */
    protected String getVariableName() {
        return variableName;
    }

    /**
     * @see org.apache.cocoon.workflow.Condition#setExpression(String)
     */
    public void setExpression(String expression) throws WorkflowException {
        super.setExpression(expression);
        String[] sides = expression.split("=");
        if (sides.length != 2) {
            throw new WorkflowException(
                "The expression '" + expression + "' must be of the form 'name = [true|false]'");
        }
        
        variableName = sides[0].trim();
        value = Boolean.valueOf(sides[1].trim()).booleanValue();
        
        if (log.isDebugEnabled()) {
            log.debug("Expression:    [" + sides[1].trim() + "]");
            log.debug("Setting value: [" + value + "]");
        }
    }

    /**
     * @see org.apache.cocoon.workflow.Condition#isComplied(Situation, WorkflowInstance)
     */
    public boolean isComplied(Situation situation, WorkflowInstance instance) throws WorkflowException {
        if (log.isDebugEnabled()) {
            log.debug("Checking boolean variable condition");
            log.debug("    Condition value: [" + getValue() + "]");
            log.debug("    Variable value:  [" + instance.getValue(getVariableName()) + "]");
        }
        return instance.getValue(getVariableName()) == getValue();
    }

}
