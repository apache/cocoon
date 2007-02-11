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
package org.apache.cocoon.woody.formmodel;

import org.w3c.dom.Element;

/**
 * Builds {@link MessagesDefinition}s.
 * 
 * @version $Id: MessagesDefinitionBuilder.java,v 1.6 2004/03/09 13:53:55 reinhard Exp $
 */
public class MessagesDefinitionBuilder extends AbstractWidgetDefinitionBuilder {
    public WidgetDefinition buildWidgetDefinition(Element widgetElement) throws Exception {
        MessagesDefinition messagesDefinition = new MessagesDefinition();
        setLocation(widgetElement, messagesDefinition);
        setId(widgetElement, messagesDefinition);
        setDisplayData(widgetElement, messagesDefinition);
        return messagesDefinition;
    }
}
