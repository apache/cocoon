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
package org.apache.cocoon.template.expression;

import java.io.CharArrayReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;

import org.apache.cocoon.components.expression.ExpressionContext;
import org.apache.cocoon.template.environment.ErrorHolder;
import org.apache.cocoon.template.environment.ParsingContext;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @version $Id$
 */
public class Substitutions {

    final private List substitutions;
    final private boolean hasSubstitutions;

    public Substitutions(ParsingContext parsingContext, Locator location, String stringTemplate) throws SAXException {
        this(parsingContext, location, new StringReader(stringTemplate));
    }

    public Substitutions(ParsingContext parsingContext, Locator location, char[] chars, int start, int length)
            throws SAXException {
        this(parsingContext, location, new CharArrayReader(chars, start, length));
    }

    private Substitutions(ParsingContext parsingContext, Locator location, Reader in) throws SAXException {
        this.substitutions = parsingContext.getStringTemplateParser().parseSubstitutions( in, "", location );
        this.hasSubstitutions = !substitutions.isEmpty();
    }

    public boolean hasSubstitutions() {
        return this.hasSubstitutions;
    }

    public Iterator iterator() {
        return this.substitutions.iterator();
    }

    public int size() {
        return this.substitutions.size();
    }

    public Object get(int pos) {
        return this.substitutions.get(pos);
    }

    public String toString(Locator location, ExpressionContext expressionContext) throws SAXException {
        StringBuffer buf = new StringBuffer();
        Iterator iterSubst = iterator();
        while (iterSubst.hasNext()) {
            Subst subst = (Subst) iterSubst.next();
            if (subst instanceof Literal) {
                Literal lit = (Literal) subst;
                buf.append(lit.getValue());
            } else if (subst instanceof JXTExpression) {
                JXTExpression expr = (JXTExpression) subst;
                Object val;
                try {
                    val = expr.getValue(expressionContext);
                } catch (Exception e) {
                    throw new SAXParseException(e.getMessage(), location, e);
                } catch (Error err) {
                    throw new SAXParseException(err.getMessage(), location, new ErrorHolder(err));
                }
                buf.append(val != null ? val.toString() : "");
            }
        }
        return buf.toString();
    }
}
