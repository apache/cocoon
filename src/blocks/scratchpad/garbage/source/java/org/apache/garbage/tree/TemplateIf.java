/* ============================================================================ *
 *                   The Apache Software License, Version 1.1                   *
 * ============================================================================ *
 *                                                                              *
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved. *
 *                                                                              *
 * Redistribution and use in source and binary forms, with or without modifica- *
 * tion, are permitted provided that the following conditions are met:          *
 *                                                                              *
 * 1. Redistributions of  source code must  retain the above copyright  notice, *
 *    this list of conditions and the following disclaimer.                     *
 *                                                                              *
 * 2. Redistributions in binary form must reproduce the above copyright notice, *
 *    this list of conditions and the following disclaimer in the documentation *
 *    and/or other materials provided with the distribution.                    *
 *                                                                              *
 * 3. The end-user documentation included with the redistribution, if any, must *
 *    include  the following  acknowledgment:  "This product includes  software *
 *    developed  by the  Apache Software Foundation  (http://www.apache.org/)." *
 *    Alternately, this  acknowledgment may  appear in the software itself,  if *
 *    and wherever such third-party acknowledgments normally appear.            *
 *                                                                              *
 * 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be *
 *    used to  endorse or promote  products derived from  this software without *
 *    prior written permission. For written permission, please contact          *
 *    apache@apache.org.                                                        *
 *                                                                              *
 * 5. Products  derived from this software may not  be called "Apache", nor may *
 *    "Apache" appear  in their name,  without prior written permission  of the *
 *    Apache Software Foundation.                                               *
 *                                                                              *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, *
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND *
 * FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE *
 * APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT, *
 * INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU- *
 * DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS *
 * OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON *
 * ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT *
 * (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF *
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.            *
 *                                                                              *
 * This software  consists of voluntary contributions made  by many individuals *
 * on  behalf of the Apache Software  Foundation.  For more  information on the *
 * Apache Software Foundation, please see <http://www.apache.org/>.             *
 *                                                                              *
 * ============================================================================ */
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
 * @version CVS $Id: TemplateIf.java,v 1.1 2003/09/04 12:42:32 cziegeler Exp $
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
