/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.forms.formmodel;

/**
 * Abstract repeater action. Subclasses will typically just self-add an
 * event handler that will act on the repeater.
 * 
 * @see RepeaterActionDefinitionBuilder
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @version CVS $Id: RepeaterActionDefinition.java,v 1.1 2004/03/09 10:33:50 reinhard Exp $
 */
public abstract class RepeaterActionDefinition extends ActionDefinition {

    private String name = null;
    
    /**
     * Builds an action whose target repeater is the parent of this widget
     */
    public RepeaterActionDefinition() {
    }
    
    /**
     * Builds an action whose target is a sibling of this widget
     * @param repeaterName the name of the repeater
     */
    public RepeaterActionDefinition(String repeaterName) {
        this.name = repeaterName;
    }

    public Widget createInstance() {
        return new RepeaterAction(this);
    }
    
    /**
     * Get the name of the repeater on which to act. If <code>null</code>, the repeater
     * is the parent of the current widget (i.e. actions are in repeater rows). Otherwise,
     * the repeater is a sibling of the current widget.
     * 
     * @return the repeater name (can be <code>null</code>).
     */
    public String getRepeaterName() {
        return this.name;
    }
    
}
