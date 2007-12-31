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
package org.apache.cocoon.woody.formmodel;

import java.util.Iterator;

import org.apache.cocoon.woody.event.ActionListener;
import org.apache.cocoon.woody.util.DomHelper;
import org.w3c.dom.Element;

/**
 * Builds {@link ActionDefinition}s.
 * 
 * @version $Id$
 */
public class ActionDefinitionBuilder extends AbstractWidgetDefinitionBuilder {
    public WidgetDefinition buildWidgetDefinition(Element widgetElement) throws Exception {
        ActionDefinition actionDefinition = createDefinition();
        setLocation(widgetElement, actionDefinition);
        setId(widgetElement, actionDefinition);
        setDisplayData(widgetElement, actionDefinition);

        String actionCommand = DomHelper.getAttribute(widgetElement, "action-command");
        actionDefinition.setActionCommand(actionCommand);

        Iterator iter = buildEventListeners(widgetElement, "on-action", ActionListener.class).iterator();
        while (iter.hasNext()) {
            actionDefinition.addActionListener((ActionListener)iter.next());
        }

        return actionDefinition;
    }
    
    protected ActionDefinition createDefinition() {
        return new ActionDefinition();
    }
}
