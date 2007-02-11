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
package org.apache.garbage.tree;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.jxpath.CompiledExpression;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * 
 * 
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>, February 2003
 * @version CVS $Id: TemplateIf.java,v 1.2 2004/03/05 10:07:24 bdelacretaz Exp $
 */
public class TemplateIf extends LocatedEvent {

    /** Our list of conditions */
    private ArrayList conditions = new ArrayList();

    /**
     * Create a new <code>TemplateIf</code> instance.
     *
     * @param data The data of this expression.
     */
    public TemplateIf() {
        this(null);
    }

    /**
     * Create a new <code>TemplateIf</code> instance.
     *
     * @param locator The <code>Locator</code> instance where location
     *                information should be read from.
     * @param data The data of this expression.
     */
    public TemplateIf(Locator locator) {
        super(locator);
    }

    /**
     * Add a new always-true condition to the list of conditions contained
     * in this <code>TemplateIf</code>.
     */
    public Events addCondition(Locator locator) {
        return(this.addCondition(locator, null));
    }

    /**
     * Add a newcondition to the list of conditions contained in this
     * <code>TemplateIf</code>.
     *
     * @param data The XPath expression to evaluate.
     */
    public Events addCondition(Locator locator, String data) {
        Conditional conditional = new Conditional(locator, data);
        this.conditions.add(conditional);
        return(conditional);
    }

    /**
     * Process this event in the context of the specified <code>Runtime</code>.
     *
     * @param runtime The <code>Runtime</code> receiving events notifications.
     * @throws SAXException In case of error processing this event.
     */
    public void process(Runtime runtime, JXPathContext context)
    throws SAXException {
        Iterator iterator = this.conditions.iterator();
        while (iterator.hasNext()) {
            if (((Conditional)iterator.next()).process(runtime, context)) {
                return;
            }
        }
    }

    private static final class Conditional extends LocatedEvents {
        private String data = null;
        private CompiledExpression expression = null;

        private Conditional(Locator locator, String data) {
            super(locator);
            if (data != null) try {
                this.data = "boolean(" + data + ")";
                this.expression = JXPathContext.compile(this.data);
            } catch (Exception e) {
                throw new TreeException(locator, "Cannot create expression "
                                        + "from \"" + data + "\"", e);
            }
        }

        public boolean process(Runtime runtime, JXPathContext context)
        throws SAXException {
            boolean execute = (this.expression == null);

            if (! execute) try {
                Boolean k = (Boolean)this.expression.getValue(context);
                if (k != null) execute = k.booleanValue();
            } catch (Exception e) {
                throw new SAXException("Cannot evaluate expression {"
                                       + this.data + "}", e);
            }

            if (execute) {
                Pointer ptr = context.getContextPointer();
                JXPathContext ctx = context.getRelativeContext(ptr);
                VariableScope scp = new VariableScope(ctx);
                Iterator iterator = this.iterator();
                while(iterator.hasNext()) {
                    ((Event)iterator.next()).process(runtime, ctx);
                }
            }

            return(execute);
        }
    }
}
