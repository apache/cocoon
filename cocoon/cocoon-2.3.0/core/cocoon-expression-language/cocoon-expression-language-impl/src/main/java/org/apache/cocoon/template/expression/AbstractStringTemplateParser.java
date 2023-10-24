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

import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import org.apache.cocoon.el.ExpressionFactory;
import org.apache.cocoon.el.parsing.StringTemplateParser;
import org.apache.cocoon.el.parsing.Subst;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @version $Id$
 */
public abstract class AbstractStringTemplateParser implements StringTemplateParser {

    private ExpressionFactory expressionFactory;


    public ExpressionFactory getExpressionFactory() {
        return expressionFactory;
    }

    public void setExpressionFactory(ExpressionFactory expressionFactory) {
        this.expressionFactory = expressionFactory;
    }


    protected Subst compile(final String expression) throws Exception {
        return new JXTExpression(expression, this.expressionFactory.getExpression(expression));
    }

    protected Subst compile(final String expression, String language) throws Exception {
        return new JXTExpression(expression, this.expressionFactory.getExpression(language, expression));
    }

    /**
     * @see org.apache.cocoon.el.parsing.StringTemplateParser#compileBoolean(String, String, Locator)
     */
    public Subst compileBoolean(String val, String msg, Locator location) throws SAXException {
        Subst res = compileExpr(val, msg, location);
        if (res instanceof Literal) {
            res = new Literal(Boolean.valueOf(res.getRaw()));
        }

        return res;
    }

    /**
     * @see org.apache.cocoon.el.parsing.StringTemplateParser#compileInt(String, String, Locator)
     */
    public Subst compileInt(String val, String msg, Locator location) throws SAXException {
        Subst res = compileExpr(val, msg, location);
        if (res instanceof Literal) {
            res = new Literal(Integer.valueOf(res.getRaw()));
        }

        return res;
    }

    /**
     * @see org.apache.cocoon.el.parsing.StringTemplateParser#compileExpr(String, String, Locator)
     */
    public Subst compileExpr(String inStr, String errorPrefix, Locator location) throws SAXParseException {
        if (inStr == null) {
            return null;
        }

        StringReader in = new StringReader(inStr.trim());
        List substitutions = parseSubstitutions(in, errorPrefix, location);
        if (substitutions.size() == 0 || !(substitutions.get(0) instanceof JXTExpression)) {
            return new Literal(inStr);
        }

        return (JXTExpression) substitutions.get(0);
    }

    /**
     * @see org.apache.cocoon.el.parsing.StringTemplateParser#parseSubstitutions(Reader, String, Locator)
     */
    public List parseSubstitutions(Reader in, String errorPrefix, Locator location) throws SAXParseException {
        try {
            return parseSubstitutions(in);
        } catch (Exception e) {
            throw new SAXParseException(errorPrefix + e.getMessage(), location, e);
        }
    }

    protected abstract List parseSubstitutions(Reader in) throws Exception;
}
