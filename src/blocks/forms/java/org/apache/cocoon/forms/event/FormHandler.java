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
package org.apache.cocoon.forms.event;

/**
 * A FormHandler can be registered with a {@link org.apache.cocoon.woody.formmodel.Form Form},
 * and will then receive all events fired by widgets on the form.
 *
 * <p>It provides an alternative way of handling events, instead of specifying the eventhandlers
 * in the form definition.
 *
 * <p>It is useful when you want to write your event-handling code in Java, have all events
 * handled by one class (which could of course again delegate to other classes), and when
 * you want the event handler to have access to objects it would not be able to get access
 * to if they were part of the form definition.
 * 
 * @version CVS $Id: FormHandler.java,v 1.1 2004/03/09 10:33:45 reinhard Exp $
 */
public interface FormHandler {

    public void handleEvent(WidgetEvent widgetEvent);

}
