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

import org.apache.cocoon.el.objectmodel.ObjectModel;
import org.apache.cocoon.el.parsing.StringTemplateParser;
import org.apache.cocoon.el.parsing.Subst;
import org.apache.cocoon.template.environment.ErrorHolder;
import org.apache.cocoon.template.environment.ExecutionContext;
import org.apache.cocoon.template.environment.ParsingContext;
import org.apache.cocoon.template.script.Invoker;
import org.apache.cocoon.template.script.event.Event;
import org.apache.cocoon.template.script.event.StartElement;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.util.NamespacesTable;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @version SVN $Id$
 */
public class ForEach extends Instruction {

    protected static final Iterator NULL_ITER = new Iterator() {
        public boolean hasNext() {
            return true;
        }

        public Object next() {
            return null;
        }

        public void remove() {
            // EMPTY
        }
    };

    private final Subst items;
    private final Subst var;
    private final Subst varStatus;
    private final Subst begin;
    private final Subst end;
    private final Subst step;

    public ForEach(ParsingContext parsingContext, StartElement raw, Attributes attrs, Stack stack)
        throws SAXException {

        super(raw);

        String name = raw.getLocalName();
        Locator locator = getLocation();

        String items = attrs.getValue("items");
        String select = attrs.getValue("select");

        StringTemplateParser expressionCompiler = parsingContext.getStringTemplateParser();
        this.var = expressionCompiler.compileExpr(attrs.getValue("var"), null, locator);
        this.varStatus = expressionCompiler.compileExpr(attrs.getValue("varStatus"), null, locator);
        this.begin = expressionCompiler.compileInt(attrs.getValue("begin"), name, locator);
        this.end = expressionCompiler.compileInt(attrs.getValue("end"), name, locator);
        this.step = expressionCompiler.compileInt(attrs.getValue("step"), name, locator);

        if (items == null) {
            if (select == null && (begin == null || end == null)) {
                throw new SAXParseException("forEach: \"select\", \"items\", or both \"begin\" and \"end\" must be specified",
                                            locator, null);
            }
        } else if (select != null) {
            throw new SAXParseException("forEach: only one of \"select\" or \"items\" may be specified",
                                        locator, null);
        }

        this.items = expressionCompiler.compileExpr(items == null ? select : items, null, locator);
    }

    public Event execute(final XMLConsumer consumer,
                         ObjectModel objectModel, ExecutionContext executionContext,
                         MacroContext macroContext, NamespacesTable namespaces, Event startEvent, Event endEvent)
        throws SAXException {
        Iterator iter = null;
        int begin, end, step;
        String var = null, varStatus = null;
        try {
            iter = (this.items != null )
                    ? this.items.getIterator(objectModel)
                    : NULL_ITER;
            begin = this.begin == null
                ? 0
                : this.begin.getIntValue(objectModel);
            end = this.end == null
                ? Integer.MAX_VALUE
                : this.end.getIntValue(objectModel);
            step = this.step == null
                ? 1
                : this.step.getIntValue(objectModel);

            if ( this.var != null )
                var = this.var.getStringValue(objectModel);

            if ( this.varStatus != null )
                varStatus = this.varStatus.getStringValue(objectModel);
        } catch (Exception exc) {
            throw new SAXParseException(exc.getMessage(),
                                        getLocation(), exc);
        } catch (Error err) {
            throw new SAXParseException(err.getMessage(),
                                        getLocation(), new ErrorHolder(err));
        }
        objectModel.markLocalContext();
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
            objectModel.put(varStatus, status);
        }
        int skipCounter, count = 1;
        while (i <= end && iter.hasNext()) {
            objectModel.markLocalContext();
            Object value = iter.next();
            objectModel.put(ObjectModel.CONTEXTBEAN, value);
            if (var != null) {
                objectModel.put(var, value);
            }
            if (status != null) {
                status.setIndex(i);
                status.setCount(count);
                status.setFirst(i == begin);
                status.setCurrent(value);
                status.setLast((i == end || !iter.hasNext()));
            }
            Invoker.execute(consumer, objectModel, executionContext,
                            macroContext, namespaces, getNext(), getEndInstruction());
            // Skip rows
            skipCounter = step;
            while (--skipCounter > 0 && iter.hasNext()) {
                iter.next();
            }
            // Increase index
            i += step;
            count++;

            objectModel.cleanupLocalContext();
        }
        objectModel.cleanupLocalContext();

        return getEndInstruction().getNext();
    }
}
