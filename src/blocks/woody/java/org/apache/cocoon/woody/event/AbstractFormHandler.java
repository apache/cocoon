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

/**
 * Abstract implementation of {@link FormHandler}, which checks the type
 * of the WidgetEvent and calls the more specific {@link #handleActionEvent(ActionEvent)}
 * or {@link #handleValueChangedEvent(ValueChangedEvent)} methods.
 * 
 * @version CVS $Id: AbstractFormHandler.java,v 1.4 2004/03/05 13:02:30 bdelacretaz Exp $
 */
public abstract class AbstractFormHandler implements FormHandler {

    public void handleEvent(WidgetEvent widgetEvent) {
        if (widgetEvent instanceof ActionEvent)
            handleActionEvent((ActionEvent)widgetEvent);
        else if (widgetEvent instanceof ValueChangedEvent)
            handleValueChangedEvent((ValueChangedEvent)widgetEvent);
    }

    /**
     * Called when an ActionEvent occured.
     */
    public abstract void handleActionEvent(ActionEvent actionEvent);

    /**
     * Called when an ValueChangedEvent occured.
     */
    public abstract void handleValueChangedEvent(ValueChangedEvent valueChangedEvent);

}
