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

import java.util.Iterator;
import java.util.Stack;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.expression.ExpressionContext;
import org.apache.cocoon.environment.TemplateObjectModelHelper;
import org.apache.cocoon.template.jxtg.environment.ErrorHolder;
import org.apache.cocoon.template.jxtg.environment.ExecutionContext;
import org.apache.cocoon.template.jxtg.expression.JXTExpression;
import org.apache.cocoon.template.jxtg.expression.Literal;
import org.apache.cocoon.template.jxtg.expression.Subst;
import org.apache.cocoon.template.jxtg.script.event.AttributeEvent;
import org.apache.cocoon.template.jxtg.script.event.CopyAttribute;
import org.apache.cocoon.template.jxtg.script.event.Event;
import org.apache.cocoon.template.jxtg.script.event.StartElement;
import org.apache.cocoon.template.jxtg.script.event.StartDocument;
import org.apache.cocoon.template.jxtg.script.event.StartInstruction;
import org.apache.cocoon.template.jxtg.script.event.SubstituteAttribute;
import org.apache.cocoon.template.jxtg.script.Invoker;
import org.apache.cocoon.xml.XMLConsumer;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class StartImport extends StartInstruction {

    private final AttributeEvent uri;
    private final JXTExpression select;

    public StartImport(StartElement raw, Attributes attrs, Stack stack)
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
                select = JXTExpression.compileExpr(context, "import: \"context\": ", locator);
            }
        } else {
            throw new SAXParseException("import: \"uri\" is required", locator, null);
        }
        this.uri = uri;
        this.select = select;
    }

    public Event execute(final XMLConsumer consumer,
                         ExpressionContext expressionContext, ExecutionContext executionContext,
                         StartElement macroCall, Event startEvent, Event endEvent) 
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
                            macroCall, doc.getNext(), doc.getEndDocument());
        } catch (Exception exc) {
            throw new SAXParseException(
                                        "Exception occurred in imported template " + uri
                                        + ": " + exc.getMessage(), getLocation(), exc);
        }
        return getEndInstruction().getNext();
    }
}
