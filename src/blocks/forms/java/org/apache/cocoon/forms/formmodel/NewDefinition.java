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

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

// TODO: The exception messages should use I18n.
/**
 * This is the "{@link WidgetDefinition}" which is used to instantiate a
 * {@link ClassDefinition}. The resolve step replaces this definition with
 * the definitions contained in the referenced {@link ClassDefinition}.
 *
 * @version $Id: NewDefinition.java,v 1.2 2004/04/12 14:05:09 tim Exp $
 */
public class NewDefinition extends AbstractWidgetDefinition {
    private boolean resolving;
    private ClassDefinition classDefinition;

    public NewDefinition() {
        super();
        resolving = false;
        classDefinition = null;
    }

    private ClassDefinition getClassDefinition() throws Exception {
        FormDefinition formDefinition = getFormDefinition();
        WidgetDefinition classDefinition = formDefinition.getWidgetDefinition(getId());
        if (classDefinition == null)
            throw new Exception("NewDefinition: Class with id \"" + getId() + "\" does not exist (" + getLocation() + ")");
        if (!(classDefinition instanceof ClassDefinition))
            throw new Exception("NewDefinition: Id \"" + getId() + "\" is not a class (" + getLocation() + ")");
        return (ClassDefinition)classDefinition;
    }

    // TODO: Should add checking for union defaults which would cause non-terminating recursion.
    public void resolve(List parents, WidgetDefinition parent) throws Exception {
        // Non-terminating recursion detection
        if (resolving) {
            // Search up parent list in hopes of finding a "Union" before finding previous "New" for this "Class".
            ListIterator parentsIt = parents.listIterator(parents.size());
            while(parentsIt.hasPrevious()) {
                WidgetDefinition definition = (WidgetDefinition)parentsIt.previous();
                if (definition instanceof UnionDefinition) break;
                if (definition == this)
                    throw new Exception("NewDefinition: Non-terminating recursion detected in widget definition : "
                        + parent.getId() + " (" + getLocation() + ")");
            }
        }
        // Resolution
        resolving = true;
        parents.add(this);
        classDefinition = getClassDefinition();
        Iterator definitionsIt = classDefinition.getWidgetDefinitions().iterator();
        parents.add(this);
        while (definitionsIt.hasNext()) {
            WidgetDefinition definition = (WidgetDefinition)definitionsIt.next();
            if (definition instanceof ContainerDefinition) {
                ((ContainerDefinition)definition).resolve(parents, parent);
            }
            if (!(definition instanceof NewDefinition)) {
                ((ContainerDefinition)parent).addWidgetDefinition(definition);
            }
        }
        parents.remove(parents.size()-1);
        resolving = false;
    }

    public Widget createInstance() {
        return null;
    }
}
