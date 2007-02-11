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
package org.apache.cocoon.forms.samples;

import org.apache.cocoon.forms.event.AbstractFormHandler;
import org.apache.cocoon.forms.event.ActionEvent;
import org.apache.cocoon.forms.event.ValueChangedEvent;

/**
 * Example FormHandler for the "Form1" sample form.
 * This implementation currently doesn't do anything interesting.
 * 
 * @version $Id: Form1Handler.java,v 1.1 2004/03/09 10:34:08 reinhard Exp $
 */
public class Form1Handler extends AbstractFormHandler {
    public void handleActionEvent(ActionEvent actionEvent) {
        //System.out.println("action event reported to Form1Handler: " + actionEvent.getActionCommand());
    }

    public void handleValueChangedEvent(ValueChangedEvent valueChangedEvent) {
        //System.out.println("value changed reported to Form1Handler");
    }
}
