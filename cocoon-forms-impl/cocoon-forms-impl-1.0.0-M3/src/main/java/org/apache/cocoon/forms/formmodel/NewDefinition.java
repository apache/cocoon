/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import org.apache.cocoon.forms.FormsException;

// TODO: The exception messages should use I18n.
/**
 * This is the "{@link WidgetDefinition}" which is used to instantiate a
 * {@link ClassDefinition}. The resolve step replaces this definition with
 * the definitions contained in the referenced {@link ClassDefinition}.
 *
 * @version $Id$
 */
public class NewDefinition extends AbstractWidgetDefinition {

    private boolean resolving;


    public NewDefinition() {
        super();
    }

    private ClassDefinition getClassDefinition() throws Exception {
        FormDefinition formDefinition = getFormDefinition();
        WidgetDefinition classDefinition = null;

        // we found a form definition to ask
        if (formDefinition != null) {
            classDefinition = formDefinition.getWidgetDefinition(getId());

            if (classDefinition == null) { // not found in local form, try library
                classDefinition = formDefinition.getLocalLibrary().getDefinition(getId());
            }
        }

        if (classDefinition == null && getEnclosingLibrary() != null) { // not found in form's library, so ask enclosing library
            classDefinition = getEnclosingLibrary().getDefinition(getId());
        }

        if (classDefinition == null) {
            throw new FormsException("NewDefinition: Class with id '" + getId() + "' does not exist.",
                                     getLocation());
        }

        if (!(classDefinition instanceof ClassDefinition)) {
            throw new FormsException("NewDefinition: Id '" + getId() + "' is not a class.",
                                     getLocation());
        }

        return (ClassDefinition) classDefinition;
    }

    // TODO: Should add checking for union defaults which would cause non-terminating recursion.
    public void resolve(List parents, WidgetDefinition parent) throws Exception {
        // Non-terminating recursion detection
        if (resolving) {
            // Search up parent list in hopes of finding a "Union" before finding previous "New" for this "Class".
            ListIterator parentsIt = parents.listIterator(parents.size());
            while(parentsIt.hasPrevious()) {
                WidgetDefinition definition = (WidgetDefinition)parentsIt.previous();
                if (definition instanceof UnionDefinition) {
                    break;
                }

                if (definition == this) {
                    throw new FormsException("NewDefinition: Non-terminating recursion detected in widget definition '" + parent.getId() + "'.",
                                             getLocation());
                }
            }
        }

        // Resolution
        resolving = true;
        parents.add(this);
        Iterator definitionsIt = getClassDefinition().getWidgetDefinitions().iterator();
        parents.add(this);
        while (definitionsIt.hasNext()) {
            WidgetDefinition definition = (WidgetDefinition) definitionsIt.next();
            // Recursively resolve containers
            if (definition instanceof ContainerDefinition) {
                ((ContainerDefinition) definition).resolve(parents, parent);
            }

            // Add the current definition if it's not itself a "fd:new"
            if (definition instanceof NewDefinition) {
                ((NewDefinition) definition).resolve(parents, parent);
            } else {
                ((ContainerDefinition) parent).addWidgetDefinition(definition);
            }
        }
        parents.remove(parents.size() - 1);
        resolving = false;
    }

    public Widget createInstance() {
        return null;
    }
}
