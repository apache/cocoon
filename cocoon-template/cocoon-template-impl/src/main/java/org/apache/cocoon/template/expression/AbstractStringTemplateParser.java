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

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.expression.ExpressionFactory;
import org.apache.cocoon.template.environment.ErrorHolder;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public abstract class AbstractStringTemplateParser
    extends AbstractLogEnabled
    implements Serviceable, Disposable, ThreadSafe, StringTemplateParser {

    private ServiceManager manager;
    private ExpressionFactory expressionFactory;

    /**
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
        this.expressionFactory = (ExpressionFactory) this.manager.lookup(ExpressionFactory.ROLE);
    }

    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( this.manager != null ) {
            this.manager.release(this.expressionFactory);
            this.manager = null;
            this.expressionFactory = null;
        }
    }

    protected JXTExpression compile(final String expression) throws Exception {
        return new JXTExpression(expression, this.expressionFactory.getExpression(expression));
    }

    protected JXTExpression compile(final String expression, String language) throws Exception {
        return new JXTExpression(expression, this.expressionFactory.getExpression(language, expression));
    }

    /**
     * @see org.apache.cocoon.template.expression.StringTemplateParser#compileBoolean(java.lang.String, java.lang.String, org.xml.sax.Locator)
     */
    public JXTExpression compileBoolean(String val, String msg, Locator location) throws SAXException {
        JXTExpression res = compileExpr(val, msg, location);
        if (res != null && res.getCompiledExpression() == null && res.getRaw() != null) {
            res.setCompiledExpression(Boolean.valueOf(res.getRaw()));
        }
        return res;
    }

    /**
     * @see org.apache.cocoon.template.expression.StringTemplateParser#compileInt(java.lang.String, java.lang.String, org.xml.sax.Locator)
     */
    public JXTExpression compileInt(String val, String msg, Locator location) throws SAXException {
        JXTExpression res = compileExpr(val, msg, location);
        if (res != null && res.getCompiledExpression() == null && res.getRaw() != null) {
            res.setCompiledExpression(Integer.valueOf(res.getRaw()));
        }
        return res;
    }

    /**
     * @see org.apache.cocoon.template.expression.StringTemplateParser#compileExpr(java.lang.String, java.lang.String, org.xml.sax.Locator)
     */
    public JXTExpression compileExpr(String inStr, String errorPrefix, Locator location) throws SAXParseException {
        if (inStr == null) {
            return null;
        }
        StringReader in = new StringReader(inStr.trim());
        List substitutions = parseSubstitutions(in, errorPrefix, location);
        if (substitutions.size() == 0 || !(substitutions.get(0) instanceof JXTExpression))
            return new JXTExpression(inStr, null);

        return (JXTExpression) substitutions.get(0);
    }

    /**
     * @see org.apache.cocoon.template.expression.StringTemplateParser#parseSubstitutions(java.io.Reader, java.lang.String, org.xml.sax.Locator)
     */
    public List parseSubstitutions(Reader in, String errorPrefix, Locator location) throws SAXParseException {
        try {
            return parseSubstitutions(in);
        } catch (Exception exc) {
            throw new SAXParseException(errorPrefix + exc.getMessage(), location, exc);
        } catch (Error err) {
            throw new SAXParseException(errorPrefix + err.getMessage(), location, new ErrorHolder(err));
        }
    }
}
