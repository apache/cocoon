/*
 * $Id: StringTemplateParser.java 325973 2005-10-17 19:59:39Z lgawron $
 *
 * Created on 2005-09-06
 *
 * Copyright (c) 2005, MobileBox sp. z o.o.
 * All rights reserved.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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

public abstract class AbstractStringTemplateParser extends AbstractLogEnabled
        implements Serviceable, Disposable,ThreadSafe, StringTemplateParser {

    private ServiceManager manager;
    private ExpressionFactory expressionFactory;

    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
        this.expressionFactory = (ExpressionFactory) this.manager.lookup(ExpressionFactory.ROLE);
    }

    public void dispose() {
        this.manager.release(this.expressionFactory);
    }

    protected JXTExpression compile(final String expression) throws Exception {
        return new JXTExpression(expression, this.expressionFactory.getExpression(expression));
    }

    protected JXTExpression compile(final String expression, String language) throws Exception {
        return new JXTExpression(expression, this.expressionFactory.getExpression(language, expression));
    }

    public JXTExpression compileBoolean(String val, String msg, Locator location) throws SAXException {
        JXTExpression res = compileExpr(val, msg, location);
        if (res != null && res.getCompiledExpression() == null && res.getRaw() != null) {
            res.setCompiledExpression(Boolean.valueOf(res.getRaw()));
        }
        return res;
    }

    public JXTExpression compileInt(String val, String msg, Locator location) throws SAXException {
        JXTExpression res = compileExpr(val, msg, location);
        if (res != null && res.getCompiledExpression() == null && res.getRaw() != null) {
            res.setCompiledExpression(Integer.valueOf(res.getRaw()));
        }
        return res;
    }

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
