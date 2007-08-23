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
package org.apache.cocoon.template.instruction;

import java.util.Stack;

import org.apache.cocoon.el.objectmodel.ObjectModel;
import org.apache.cocoon.el.parsing.Subst;
import org.apache.cocoon.template.environment.ErrorHolder;
import org.apache.cocoon.template.environment.ParsingContext;
import org.apache.cocoon.template.expression.JXTExpression;
import org.apache.cocoon.template.script.event.AttributeEvent;
import org.apache.cocoon.template.script.event.CopyAttribute;
import org.apache.cocoon.template.script.event.StartElement;
import org.apache.cocoon.template.script.event.SubstituteAttribute;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @version SVN $Id$
 */
public class ParameterInstance extends Instruction {
    final String name;
    private final Object value;

    public ParameterInstance(ParsingContext parsingContext, StartElement raw, Attributes attrs, Stack stack)
            throws SAXException {
        super(raw);
        Locator locator = getLocation();
        if (stack.size() == 0 || !(stack.peek() instanceof Call)) {
            throw new SAXParseException("<parameter> not allowed here", locator, null);
        } else {
            this.name = attrs.getValue("name");
            if (this.name == null) {
                throw new SAXParseException("parameter: \"name\" is required", locator, null);
            }

            String val = attrs.getValue("value");
            if (val == null)
                throw new SAXParseException("parameter: \"value\" is required", locator, null);

            this.value = parsingContext.getStringTemplateParser().compileExpr(val, "parameter: \"value\": ", locator);
        }
    }

    public ParameterInstance(AttributeEvent event) {
        super((Locator) null);
        this.name = event.getLocalName();
        this.value = event;
    }

    public String getName() {
        return name;
    }

    public Object getValue(ObjectModel objectModel) throws SAXException {
        if (this.value instanceof Subst)
            return getExpressionValue((Subst) this.value, objectModel);
        else if (this.value instanceof CopyAttribute) {
            CopyAttribute copy = (CopyAttribute) this.value;
            return copy.getValue();
        } else if (this.value instanceof SubstituteAttribute) {
            SubstituteAttribute substEvent = (SubstituteAttribute) this.value;
            if (substEvent.getSubstitutions().size() == 1
                    && substEvent.getSubstitutions().get(0) instanceof JXTExpression)
                return getExpressionValue((Subst) substEvent.getSubstitutions().get(0), objectModel);
            else
                return substEvent.getSubstitutions().toString(getLocation(), objectModel);

        } else {
            throw new Error("this shouldn't have happened");
        }
    }

    private Object getExpressionValue(Subst expr, ObjectModel objectModel) throws SAXException {
        Object val;
        try {
            val = expr.getNode(objectModel);
        } catch (Exception e) {
            throw new SAXParseException(e.getMessage(), getLocation(), e);
        } catch (Error err) {
            throw new SAXParseException(err.getMessage(), getLocation(), new ErrorHolder(err));
        }
        return val != null ? val : "";
    }
}
