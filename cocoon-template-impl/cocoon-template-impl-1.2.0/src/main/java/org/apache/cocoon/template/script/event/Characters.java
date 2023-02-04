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

import java.util.Iterator;

import org.apache.cocoon.el.objectmodel.ObjectModel;
import org.apache.cocoon.el.parsing.Subst;
import org.apache.cocoon.template.environment.ErrorHolder;
import org.apache.cocoon.template.environment.ExecutionContext;
import org.apache.cocoon.template.environment.ParsingContext;
import org.apache.cocoon.template.expression.Literal;
import org.apache.cocoon.template.instruction.MacroContext;
import org.apache.cocoon.template.script.Invoker;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.util.NamespacesTable;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @version SVN $Id$
 */
public class Characters extends TextEvent {
    public Characters(ParsingContext parsingContext, Locator location, char[] chars, int start, int length)
            throws SAXException {
        super(parsingContext, location, chars, start, length);
    }

    public Event execute(XMLConsumer consumer,
            ObjectModel objectModel,
            ExecutionContext executionContext, MacroContext macroContext,
            NamespacesTable namespaces, Event startEvent, Event endEvent) throws SAXException {
        Iterator iter = getSubstitutions().iterator();
        while (iter.hasNext()) {
            Subst subst = (Subst) iter.next();
            char[] chars;
            if (subst instanceof Literal) {
                chars = ((Literal) subst).getCharArray();
                consumer.characters(chars, 0, chars.length);
            } else {
                Subst expr = (Subst) subst;
                try {
                    Object val = expr.getNode(objectModel);
                    Invoker.executeNode(consumer, val);
                } catch (Exception e) {
                    throw new SAXParseException(e.getMessage(), getLocation(), e);
                } catch (Error err) {
                    throw new SAXParseException(err.getMessage(), getLocation(), new ErrorHolder(err));
                }
            }
        }
        return getNext();
    }
}