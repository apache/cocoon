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

import org.apache.cocoon.components.expression.ExpressionContext;
import org.apache.cocoon.template.jxtg.environment.ErrorHolder;
import org.apache.cocoon.template.jxtg.environment.ExecutionContext;
import org.apache.cocoon.template.jxtg.expression.JXTExpression;
import org.apache.cocoon.template.jxtg.expression.Literal;
import org.apache.cocoon.template.jxtg.expression.Substitutions;
import org.apache.commons.lang.ArrayUtils;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class TextEvent extends Event {
    public TextEvent(Locator location, char[] chars, int start, int length)
            throws SAXException {
        super(location);
        this.raw = new char[length];
        System.arraycopy(chars, start, this.raw, 0, length);
        this.substitutions = new Substitutions(getLocation(), chars, start, length);
    }

    final Substitutions substitutions;
    final char[] raw;

    public char[] getRaw() {
        return raw;
    }

    public Substitutions getSubstitutions() {
        return substitutions;
    }
    
    interface CharHandler {
        public void characters(char[] ch, int offset, int length)
                throws SAXException;
    }

    protected static void characters(ExpressionContext expressionContext,
                                   ExecutionContext executionContext,
                                   TextEvent event, CharHandler handler)
        throws SAXException {
        Iterator iter = event.getSubstitutions().iterator();
        while (iter.hasNext()) {
            Object subst = iter.next();
            char[] chars;
            if (subst instanceof Literal) {
                chars = ((Literal) subst).getCharArray();
            } else {
                JXTExpression expr = (JXTExpression) subst;
                try {
                    Object val = expr.getValue(expressionContext);
                    chars = val != null ? val.toString().toCharArray()
                            : ArrayUtils.EMPTY_CHAR_ARRAY;
                } catch (Exception e) {
                    throw new SAXParseException(e.getMessage(), event
                            .getLocation(), e);
                } catch (Error err) {
                    throw new SAXParseException(err.getMessage(), event
                            .getLocation(), new ErrorHolder(err));
                }
            }
            handler.characters(chars, 0, chars.length);
        }
    }

    
}
