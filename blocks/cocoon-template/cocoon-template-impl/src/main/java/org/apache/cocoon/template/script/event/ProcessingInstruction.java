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
package org.apache.cocoon.template.script.event;

import org.apache.cocoon.el.objectmodel.ObjectModel;
import org.apache.cocoon.template.environment.ExecutionContext;
import org.apache.cocoon.template.instruction.MacroContext;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.util.NamespacesTable;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * @version SVN $Id$
 */
public class ProcessingInstruction extends Event {
    public ProcessingInstruction(Locator location, String target, String data) {
        super(location);
        this.target = target;
        this.data = data;
    }

    public String getData() {
        return data;
    }

    public String getTarget() {
        return target;
    }

    private final String target;
    private final String data;
    
    
    public Event execute(XMLConsumer consumer,
            ObjectModel objectModel,
            ExecutionContext executionContext, MacroContext macroContext,
            NamespacesTable namespaces, Event startEvent, Event endEvent) throws SAXException {
        consumer.processingInstruction(getTarget(), getData());
        return getNext();
    }
}