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
package org.apache.cocoon.woody.event;

import org.apache.cocoon.woody.formmodel.Form;

/**
 * Event raised when a form processing phase is finished.
 * 
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @version CVS $Id: ProcessingPhaseEvent.java,v 1.6 2004/03/09 13:54:13 reinhard Exp $
 */
public class ProcessingPhaseEvent extends WidgetEvent {
    
    private ProcessingPhase phase;

    public ProcessingPhaseEvent(Form form, ProcessingPhase phase) {
        super(form);
        this.phase = phase;
    }
    
    public Form getForm() {
        return (Form)this.getSource();
    }
    
    public ProcessingPhase getPhase() {
        return this.phase;
    }

}
