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

import java.util.Iterator;
import java.util.Stack;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.expression.ExpressionContext;
import org.apache.cocoon.environment.TemplateObjectModelHelper;
import org.apache.cocoon.template.environment.ErrorHolder;
import org.apache.cocoon.template.environment.ExecutionContext;
import org.apache.cocoon.template.environment.ParsingContext;
import org.apache.cocoon.template.expression.JXTExpression;
import org.apache.cocoon.template.expression.Literal;
import org.apache.cocoon.template.expression.Subst;
import org.apache.cocoon.template.script.Invoker;
import org.apache.cocoon.template.script.event.AttributeEvent;
import org.apache.cocoon.template.script.event.CopyAttribute;
import org.apache.cocoon.template.script.event.Event;
import org.apache.cocoon.template.script.event.StartDocument;
import org.apache.cocoon.template.script.event.StartElement;
import org.apache.cocoon.template.script.event.SubstituteAttribute;
import org.apache.cocoon.xml.XMLConsumer;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @version SVN $Id$
 */
public class Import extends Instruction {

    private final AttributeEvent uri;
    private final JXTExpression select;

    public Import(ParsingContext parsingContext, StartElement raw, Attributes attrs, Stack stack)
        throws SAXException {

        super(raw);

        // <import uri="${root}/foo/bar.xml" context="${foo}"/>
        Locator locator = getLocation();
        Iterator iter = raw.getAttributeEvents().iterator();
        AttributeEvent uri = null;
        JXTExpression select = null;
        while (iter.hasNext()) {
            AttributeEvent e = (AttributeEvent) iter.next();
            if (e.getLocalName().equals("uri")) {
                uri = e;
                break;
            }
        }
        if (uri != null) {
            // If "context" is present then its value will be used
            // as the context object in the imported template
            String context = attrs.getValue("context");
            if (context != null) {
                select = parsingContext.getStringTemplateParser().compileExpr(context, "import: \"context\": ", locator);
            }
        } else {
            throw new SAXParseException("import: \"uri\" is required", locator, null);
        }
        this.uri = uri;
        this.select = select;
    }

    public Event execute(final XMLConsumer consumer,
                         ExpressionContext expressionContext, ExecutionContext executionContext,
                         MacroContext macroContext, Event startEvent, Event endEvent) 
        throws SAXException {
        String uri;
        AttributeEvent e = this.uri;
        if (e instanceof CopyAttribute) {
            CopyAttribute copy = (CopyAttribute) e;
            uri = copy.getValue();
        } else {
            StringBuffer buf = new StringBuffer();
            SubstituteAttribute substAttr = (SubstituteAttribute) e;
            Iterator i = substAttr.getSubstitutions().iterator();
            while (i.hasNext()) {
                Subst subst = (Subst) i.next();
                if (subst instanceof Literal) {
                    Literal lit = (Literal) subst;
                    buf.append(lit.getValue());
                } else if (subst instanceof JXTExpression) {
                    JXTExpression expr = (JXTExpression) subst;
                    Object val;
                    try {
                        val = expr.getValue(expressionContext);
                    } catch (Exception exc) {
                        throw new SAXParseException(exc.getMessage(), getLocation(), exc);
                    } catch (Error err) {
                        throw new SAXParseException(err.getMessage(),
                                                    getLocation(), new ErrorHolder(err));
                    }
                    buf.append(val != null ? val.toString() : "");
                }
            }
            uri = buf.toString();
        }
        StartDocument doc;
        try {
            doc = executionContext.getScriptManager().resolveTemplate(uri);
        } catch (ProcessingException exc) {
            throw new SAXParseException(exc.getMessage(), getLocation(), exc);
        }
        ExpressionContext selectExpressionContext = expressionContext;
        if (this.select != null) {
            try {
                Object obj = this.select.getValue(expressionContext);
                selectExpressionContext = new ExpressionContext(expressionContext);
                selectExpressionContext.setContextBean(obj);
                TemplateObjectModelHelper.fillContext(obj, selectExpressionContext);
            } catch (Exception exc) {
                throw new SAXParseException(exc.getMessage(), getLocation(), exc);
            } catch (Error err) {
                throw new SAXParseException(err.getMessage(), getLocation(),
                                            new ErrorHolder(err));
            }
        }
        try {
            Invoker.execute(consumer, expressionContext, executionContext,
                            macroContext, doc.getNext(), doc.getEndDocument());
        } catch (Exception exc) {
            throw new SAXParseException(
                                        "Exception occurred in imported template " + uri
                                        + ": " + exc.getMessage(), getLocation(), exc);
        }
        return getEndInstruction().getNext();
    }
}
