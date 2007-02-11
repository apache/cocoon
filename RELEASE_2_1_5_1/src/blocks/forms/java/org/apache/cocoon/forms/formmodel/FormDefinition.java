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

import java.util.ArrayList;
import java.util.List;

import org.apache.cocoon.forms.event.ProcessingPhaseEvent;
import org.apache.cocoon.forms.event.ProcessingPhaseListener;
import org.apache.cocoon.forms.event.WidgetEventMulticaster;

/**
 * The {@link WidgetDefinition} part of a Form widget, see {@link Form} for more information.
 * 
 * @version $Id: FormDefinition.java,v 1.1 2004/03/09 10:33:50 reinhard Exp $
 */
public class FormDefinition extends AbstractContainerDefinition {
    private ProcessingPhaseListener listener;

    public FormDefinition() {
        super();
    }

    public void resolve() throws Exception {
        List parents = new ArrayList();
        parents.add(this);
        resolve(parents, this);
    }

    public Widget createInstance() {
        Form form = new Form(this);
        createWidgets(form);
        return form;
    }
    
    public void addProcessingPhaseListener(ProcessingPhaseListener listener) {
        this.listener = WidgetEventMulticaster.add(this.listener, listener);
    }
    
    public boolean hasProcessingPhaseListeners() {
        return this.listener != null;
    }
    
    public void fireEvent(ProcessingPhaseEvent event) {
        if (this.listener != null) {
            this.listener.phaseEnded(event);
        }
    }
}
