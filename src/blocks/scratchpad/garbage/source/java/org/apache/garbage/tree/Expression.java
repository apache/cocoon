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
 * @version CVS $Id: Expression.java,v 1.2 2004/03/05 10:07:24 bdelacretaz Exp $
 */
public class Expression extends LocatedEvent implements Evaluation {

    /** The "null" string. */
    private static final char nul[] = { 'n', 'u', 'l', 'l' };

    /** Our expression. */
    private CompiledExpression expression = null;

    /** The expression as a string. */
    private String data = null;

    /**
     * Create a new <code>Expression</code> instance.
     *
     * @param data The data of this expression.
     */
    public Expression(String data) {
        this(null, data);
    }

    /**
     * Create a new <code>Expression</code> instance.
     *
     * @param locator The <code>Locator</code> instance where location
     *                information should be read from.
     * @param data The data of this expression.
     */
    public Expression(Locator locator, String data) {
        super(locator);
        if (data == null) {
            throw new TreeException(locator, "No expression data");
        }

        try {
            this.expression = JXPathContext.compile(data);
            this.data = data;
        } catch (Exception e) {
            throw new TreeException(locator, "Cannot create expression from \""
                                    + data + "\"", e);
        }
    }

    /**
     * Evaluate the current event and return its <code>String</code> value to
     * be included as a part of an attribute value.
     *
     * @param runtime The <code>Runtime</code> receiving events notifications.
     * @throws SAXException In case of error processing this event.
     */
    public String evaluate(JXPathContext context)
    throws SAXException {
        try {
            Object value = this.expression.getValue(context);
            if (value == null) return(null);
            return(value.toString());
        } catch (Exception e) {
            throw new SAXException("Cannot evaluate expression {"
                                   + this.data + "}", e);
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
        String data = this.evaluate(context);
        if (data == null) runtime.characters(nul);
        else runtime.characters(data.toCharArray());
    }
}
