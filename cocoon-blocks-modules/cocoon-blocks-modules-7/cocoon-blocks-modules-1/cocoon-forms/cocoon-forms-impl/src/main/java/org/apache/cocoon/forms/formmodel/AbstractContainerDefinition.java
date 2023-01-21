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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * The {@link AbstractContainerDefinition} corresponding to an {@link AbstractContainerWidget}.
 *
 * @version $Id$
 */
public abstract class AbstractContainerDefinition
        extends AbstractWidgetDefinition implements ContainerDefinition {
    protected WidgetDefinitionList definitions;

    public AbstractContainerDefinition() {
        definitions = new WidgetDefinitionList(this);
    }

    public void createWidget(Widget parent, String id) {
        definitions.createWidget(parent, id);
    }

    public void createWidgets(Widget parent) {
        definitions.createWidgets(parent);
    }
    
    /**
     * initialize this definition with the other, sort of like a copy constructor
     */
    public void initializeFrom(WidgetDefinition definition) throws Exception {
    	super.initializeFrom(definition);
    	
    	if(definition instanceof AbstractContainerDefinition) {
    		AbstractContainerDefinition other = (AbstractContainerDefinition)definition;
    		
    		Iterator otherwidgets = other.definitions.getWidgetDefinitions().iterator();
    		while(otherwidgets.hasNext()) {
    			try {
    				WidgetDefinition def = (WidgetDefinition)otherwidgets.next();
    				this.definitions.addWidgetDefinition(def);
    			} catch(DuplicateIdException ignore) {}
    		}
    	} else {
    		throw new Exception("Definition to inherit from is not of the right type! (at "+getLocation()+")");
    	}
    }
    
    /**
     * checks completeness of this definition
     */
    public void checkCompleteness() throws IncompletenessException {
        super.checkCompleteness();
        this.definitions.checkCompleteness();
    }

    public void addWidgetDefinition(WidgetDefinition definition) throws Exception, DuplicateIdException {
        //FIXME: cannot enforce this check here as more children are added in the resolve() phase
        //checkMutable();
        definition.setParent(this);
        definitions.addWidgetDefinition(definition);
    }

    public void resolve(List parents, WidgetDefinition parent) throws Exception {
        definitions.resolve(parents, parent);
    }

    public boolean hasWidget(String id) {
        return definitions.hasWidget(id);
    }

    public WidgetDefinition getWidgetDefinition(String id) {
        return definitions.getWidgetDefinition(id);
    }

    public Collection getWidgetDefinitions() {
        return definitions.getWidgetDefinitions();
    }
}
