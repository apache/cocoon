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
import java.util.Stack;

import org.apache.cocoon.components.expression.ExpressionContext;
import org.apache.cocoon.template.jxtg.environment.ErrorHolder;
import org.apache.cocoon.template.jxtg.environment.ExecutionContext;
import org.apache.cocoon.template.jxtg.expression.JXTExpression;
import org.apache.cocoon.template.jxtg.script.Invoker;
import org.apache.cocoon.xml.XMLConsumer;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class StartForEach extends StartInstruction {

    private final JXTExpression items;
    private final JXTExpression var;
    private final JXTExpression varStatus;
    private final JXTExpression begin;
    private final JXTExpression end;
    private final JXTExpression step;
    private final Boolean lenient;

    public StartForEach(StartElement raw, Attributes attrs, Stack stack)
        throws SAXException {

        super(raw);

        String name = raw.getLocalName();
        Locator locator = getLocation();

        String items = attrs.getValue("items");
        String select = attrs.getValue("select");
        this.var = JXTExpression.compileExpr(attrs.getValue("var"), null, locator);
        this.varStatus = JXTExpression.compileExpr(attrs.getValue("varStatus"), null, locator);
        this.begin = JXTExpression.compileInt(attrs.getValue("begin"), name, locator);
        this.end = JXTExpression.compileInt(attrs.getValue("end"), name, locator);
        this.step = JXTExpression.compileInt(attrs.getValue("step"), name, locator);
        String lenientValue = attrs.getValue("lenient");
        this.lenient = (lenientValue == null) ? null : Boolean.valueOf(lenientValue);

        if (items == null) {
            if (select == null && (begin == null || end == null)) {
                throw new SAXParseException("forEach: \"select\", \"items\", or both \"begin\" and \"end\" must be specified",
                                            locator, null);
            }
        } else if (select != null) {
            throw new SAXParseException("forEach: only one of \"select\" or \"items\" may be specified",
                                        locator, null);
        }
        this.items = JXTExpression.compileExpr(items == null ? select : items, null, locator);
    }

    public Event execute(final XMLConsumer consumer,
                         ExpressionContext expressionContext, ExecutionContext executionContext,
                         StartElement macroCall, Event startEvent, Event endEvent) 
        throws SAXException {
        Iterator iter = null;
        int begin, end, step;
        String var, varStatus;
        try {
            iter = this.items.getIterator(expressionContext);
            begin = this.begin == null
                ? 0
                : this.begin.getIntValue(expressionContext);
            end = this.end == null
                ? Integer.MAX_VALUE
                : this.end.getIntValue(expressionContext);
            step = this.step == null
                ? 1
                : this.step.getIntValue(expressionContext);
            var = this.var.getStringValue(expressionContext);
            varStatus = this.varStatus.getStringValue(expressionContext);
        } catch (Exception exc) {
            throw new SAXParseException(exc.getMessage(),
                                        getLocation(), exc);
        } catch (Error err) {
            throw new SAXParseException(err.getMessage(),
                                        getLocation(), new ErrorHolder(err));
        }
        ExpressionContext localExpressionContext =
            new ExpressionContext(expressionContext);
        int i = 0;
        // Move to the begin row
        while (i < begin && iter.hasNext()) {
            iter.next();
            i++;
        }
        LoopTagStatus status = null;
        if (varStatus != null) {
            status = new LoopTagStatus();
            status.setBegin(begin);
            status.setEnd(end);
            status.setStep(step);
            status.setFirst(true);
            localExpressionContext.put(varStatus, status);
        }
        int skipCounter, count = 1;
        while (i <= end && iter.hasNext()) {
            Object value = iter.next();
            localExpressionContext.setContextBean(value);
            if (var != null) {
                localExpressionContext.put(var, value);
            }
            if (status != null) {
                status.setIndex(i);
                status.setCount(count);
                status.setFirst(i == begin);
                status.setCurrent(value);
                status.setLast((i == end || !iter.hasNext()));
            }
            Invoker.execute(consumer, localExpressionContext, executionContext,
                            macroCall, getNext(), getEndInstruction());
            // Skip rows
            skipCounter = step;
            while (--skipCounter > 0 && iter.hasNext()) {
                iter.next();
            }
            // Increase index
            i += step;
            count++;
        }
        return getEndInstruction().getNext();
    }
}
