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

import org.apache.commons.jxpath.CompiledExpression;
import org.apache.commons.jxpath.JXPathContext;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * 
 * 
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>, February 2003
 * @version CVS $Id: TemplateVar.java,v 1.2 2004/03/05 10:07:24 bdelacretaz Exp $
 */
public class TemplateVar extends LocatedEvent {

    /** Our expression. */
    private CompiledExpression expression = null;

    /** The expression as a string. */
    private String data = null;

    /** The expression as a string. */
    private String variable = null;

    /**
     * Create a new <code>TemplateVar</code> instance.
     *
     * @param variable The variable name to create/update..
     * @param data The data of this expression.
     */
    public TemplateVar(String variable, String data) {
        this(null, variable, data);
    }

    /**
     * Create a new <code>TemplateVar</code> instance.
     *
     * @param locator The <code>Locator</code> instance where location
     *                information should be read from.
     * @param data The data of this expression.
     */
    public TemplateVar(Locator locator, String variable, String data) {
        super(locator);
        if (variable == null) {
            throw new TreeException(locator, "No variable name");
        }
        if (data == null) {
            throw new TreeException(locator, "No expression data");
        }
        try {
            this.expression = JXPathContext.compile(data);
            this.variable = variable;
            this.data = data;
        } catch (Exception e) {
            throw new TreeException(locator, "Cannot create expression from \""
                                    + data + "\"", e);
        }
    }

    /**
     * Process this event in the context of the specified <code>Runtime</code>.
     *
     * @param runtime The <code>Runtime</code> receiving events notifications.
     * @throws SAXException In case of error processing this event.
     */
    public void process(Runtime runtime, JXPathContext context)
    throws SAXException {
        try {
            Object value = this.expression.getValue(context);
            context.getVariables().declareVariable(this.variable, value);
        } catch (Exception e) {
            throw new SAXException("Cannot evaluate expression {"
                                   + this.data + "}", e);
        }
    }
}
