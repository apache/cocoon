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
package org.apache.cocoon.template.jxtg.instruction;

import java.util.Stack;

import org.apache.cocoon.components.expression.ExpressionContext;
import org.apache.cocoon.template.jxtg.environment.ErrorHolder;
import org.apache.cocoon.template.jxtg.expression.JXTExpression;
import org.apache.cocoon.template.jxtg.script.event.AttributeEvent;
import org.apache.cocoon.template.jxtg.script.event.CopyAttribute;
import org.apache.cocoon.template.jxtg.script.event.StartElement;
import org.apache.cocoon.template.jxtg.script.event.StartInstruction;
import org.apache.cocoon.template.jxtg.script.event.SubstituteAttribute;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class StartParameterInstance extends StartInstruction {
    final String name;
    private final Object value;

    public StartParameterInstance(StartElement raw, Attributes attrs,
            Stack stack) throws SAXException {
        super(raw);
        Locator locator = getLocation();
        if (stack.size() == 0 || !(stack.peek() instanceof StartCall)) {
            throw new SAXParseException("<parameter> not allowed here",
                    locator, null);
        } else {
            this.name = attrs.getValue("name");
            if (this.name == null) {
                throw new SAXParseException("parameter: \"name\" is required",
                        locator, null);
            }

            String val = attrs.getValue("value");
            if (val == null)
                throw new SAXParseException("parameter: \"value\" is required",
                        locator, null);

            this.value = JXTExpression.compileExpr(val,
                    "parameter: \"value\": ", locator);
        }
    }

    public StartParameterInstance(AttributeEvent event) {
        super( (Locator) null );
        this.name = event.getLocalName();
        this.value = event;
    }

    public String getName() {
        return name;
    }

    public Object getValue(ExpressionContext expressionContext)
            throws SAXException {
        if (this.value instanceof CopyAttribute) {
            CopyAttribute copy = (CopyAttribute) this.value;
            return copy.getValue();
        } else if (this.value instanceof SubstituteAttribute) {
            SubstituteAttribute substEvent = (SubstituteAttribute) this.value;
            if (substEvent.getSubstitutions().size() == 1
                    && substEvent.getSubstitutions().get(0) instanceof JXTExpression) {
                JXTExpression expr = (JXTExpression) substEvent
                        .getSubstitutions().get(0);
                Object val;
                try {
                    val = expr.getNode(expressionContext);
                } catch (Exception e) {
                    throw new SAXParseException(e.getMessage(), getLocation(),
                            e);
                } catch (Error err) {
                    throw new SAXParseException(err.getMessage(),
                            getLocation(), new ErrorHolder(err));
                }
                return val != null ? val : "";
            } else {
                return substEvent.getSubstitutions().toString(getLocation(),
                        expressionContext);
            }
        } else {
            throw new Error("this shouldn't have happened");
        }

    }
}
