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
package org.apache.cocoon.template.jxtg.script.event;

import java.util.Iterator;

import org.apache.cocoon.components.expression.ExpressionContext;
import org.apache.cocoon.template.jxtg.environment.ErrorHolder;
import org.apache.cocoon.template.jxtg.environment.ExecutionContext;
import org.apache.cocoon.template.jxtg.expression.JXTExpression;
import org.apache.cocoon.template.jxtg.expression.Literal;
import org.apache.cocoon.template.jxtg.expression.Subst;
import org.apache.cocoon.template.jxtg.script.Invoker;
import org.apache.cocoon.xml.XMLConsumer;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class Characters extends TextEvent {
    public Characters(Locator location, char[] chars, int start, int length)
            throws SAXException {
        super(location, chars, start, length);
    }

    public Event execute(XMLConsumer consumer,
            ExpressionContext expressionContext,
            ExecutionContext executionContext, StartElement macroCall,
            Event startEvent, Event endEvent) throws SAXException {
        Iterator iter = getSubstitutions().iterator();
        while (iter.hasNext()) {
            Subst subst = (Subst) iter.next();
            char[] chars;
            if (subst instanceof Literal) {
                chars = ((Literal) subst).getCharArray();
                consumer.characters(chars, 0, chars.length);
            } else {
                JXTExpression expr = (JXTExpression) subst;
                try {
                    Object val = expr.getNode(expressionContext);
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