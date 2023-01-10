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
package org.apache.cocoon.components.treeprocessor.variables;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.modules.input.InputModule;
import org.apache.cocoon.components.treeprocessor.InvokeContext;
import org.apache.cocoon.el.Expression;
import org.apache.cocoon.el.ExpressionException;
import org.apache.cocoon.el.ExpressionFactory;
import org.apache.cocoon.el.objectmodel.ObjectModel;
import org.apache.cocoon.sitemap.PatternException;

/**
 * Prepared implementation of {@link VariableResolver} for fast evaluation.
 *
 * @version $Id$
 */
final public class PreparedVariableResolver extends VariableResolver
                                            implements Disposable {
    
    private static final int ROOT_SITEMAP_VARIABLE = 0;
    private static final int ANCHOR_VAR = -1;
    private static final int OPEN = -2;
    private static final int CLOSE = -3;
    private static final int COLON = -4;
    private static final int TEXT = -5;
    private static final int EXPR = -7;
    private static final int SITEMAP_VAR = -9;
    private static final int THREADSAFE_MODULE = -10;
    private static final int STATEFUL_MODULE = -11;

    protected static final Token COLON_TOKEN = new Token(COLON);
    protected static final Token OPEN_TOKEN = new Token(OPEN);
    protected static final Token CLOSE_TOKEN = new Token(CLOSE);
    protected static final Token EMPTY_TOKEN = new Token(EXPR);
    
    private ServiceManager manager;
    protected List tokens;
    protected boolean needsMapStack;


    public PreparedVariableResolver() {
        super();
    }

    public PreparedVariableResolver(String expression, ServiceManager manager) throws PatternException {
        setManager(manager);
        setExpression(expression);
    }

    public ServiceManager getManager() {
        return manager;
    }

    public void setManager(ServiceManager manager) {
        this.manager = manager;
    }

    public void setExpression(String expr) throws PatternException {
        this.originalExpr = expr;
        this.tokens = new ArrayList();

        VariableExpressionTokenizer.tokenize(expr, new VariableExpressionTokenizer.TokenReciever() {
            public void addToken(int type, String value) throws PatternException {
                switch (type) {
                    case VariableExpressionTokenizer.TokenReciever.COLON:
                        tokens.add(COLON_TOKEN);
                        break;
                    case VariableExpressionTokenizer.TokenReciever.OPEN:
                        tokens.add(OPEN_TOKEN);
                        break;
                    case VariableExpressionTokenizer.TokenReciever.CLOSE:
                        tokens.add(CLOSE_TOKEN);
                        break;
                    case VariableExpressionTokenizer.TokenReciever.TEXT:
                        tokens.add(new Token(value));
                        break;
                    case VariableExpressionTokenizer.TokenReciever.NEW_EXPRESSION:
                        tokens.add(new Token(NEW_EXPRESSION, value));
                        break;
                    case VariableExpressionTokenizer.TokenReciever.MODULE:
                        Token token;
                        if (value.equals("sitemap")) {
                            // Explicit prefix for sitemap variable
                            needsMapStack = true;
                            token = new Token(SITEMAP_VAR);
                        } else if (value.startsWith("#")) {
                            // anchor syntax refering to a name result level
                            needsMapStack = true;
                            token = new Token(ANCHOR_VAR, value.substring(1));
                        } else {
                            // Module used
                            token = getNewModuleToken(value);
                        }
                        tokens.add(token);
                        break;
                    case VariableExpressionTokenizer.TokenReciever.VARIABLE:
                        needsMapStack = true;
                        tokens.add(getNewVariableToken(value));
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown token type: " + type);
                }
            }
        });
    }

    protected Token getNewVariableToken(String variable) {
        if (variable.startsWith("/")) {
            return new Token(ROOT_SITEMAP_VARIABLE, variable.substring(1));
        }
        // Find level
        int level = 1; // Start at 1 since it will be substracted from list.size()
        int pos = 0;
        while (variable.startsWith("../", pos)) {
            level++;
            pos += "../".length();
        }
        return new Token(level, variable.substring(pos));
    }


    protected Token getNewModuleToken(String moduleName) throws PatternException {
        // Get the module
        InputModule module;
        try {
            module = (InputModule) this.manager.lookup(InputModule.ROLE + '/' + moduleName);
        } catch (ServiceException e) {
            throw new PatternException("Cannot get module named '" + moduleName +
                                       "' in expression '" + this.originalExpr + "'", e);
        }

        Token token;
        // Is this module threadsafe ?
        if (module instanceof ThreadSafe) {
            token = new Token(THREADSAFE_MODULE, module);
        } else {
            // Stateful module : release it and get a new one each time
            this.manager.release(module);
            token = new Token(STATEFUL_MODULE, moduleName);
        }
        return token;
    }

    public final String resolve(InvokeContext context, Map objectModel) throws PatternException {
        List mapStack = null; // get the stack only when necessary - lazy inside the loop
        int stackSize = 0;

        if (needsMapStack) {
            if (context == null) {
                throw new PatternException("Need an invoke context to resolve " + this);
            }
            mapStack = context.getMapStack();
            stackSize = mapStack.size();
        }

        Stack stack = new Stack();

        for (Iterator i = tokens.iterator(); i.hasNext();) {
            Token token = (Token) i.next();
            Token last;
            switch (token.getType()){
                case TEXT:
                    if (stack.empty()) {
                        stack.push(new Token(EXPR, token.getStringValue()));
                    } else {
                        last = (Token)stack.peek();
                        if (last.hasType(EXPR)) {
                            last.merge(token);
                        } else {
                            stack.push(new Token(EXPR, token.getStringValue()));
                        }
                    }
                    break;
                case CLOSE:
                    Token expr = (Token)stack.pop();
                    Token lastButOne = (Token)stack.pop();
                    Token result;
                    if (expr.hasType(COLON)) { // i.e. nothing was specified after the colon
                        stack.pop(); // Pop the OPEN
                        result = processModule(lastButOne, EMPTY_TOKEN, objectModel, context, mapStack, stackSize);
                    } else if (lastButOne.hasType(COLON)) {
                        Token module = (Token)stack.pop();
                        stack.pop(); // Pop the OPEN
                        result = processModule(module, expr, objectModel, context, mapStack, stackSize);
                    } else if (lastButOne.hasType(VariableExpressionTokenizer.TokenReciever.NEW_EXPRESSION)) {
                        stack.pop(); // Pop the OPEN
                        ExpressionFactory expressionFactory = null;
                        ObjectModel newObjectModel = null;
                        try {
                            expressionFactory = (ExpressionFactory)manager.lookup(ExpressionFactory.ROLE);
                            newObjectModel = (ObjectModel)manager.lookup(ObjectModel.ROLE);
                            result = processNewExpression(lastButOne, expressionFactory, newObjectModel);
                        } catch (ServiceException e) {
                            throw new PatternException("Cannot obtain necessary components to evaluate new expression '"
                                    + lastButOne.getStringValue() + "' in expression '" + this.originalExpr + "'", e);
                        } finally {
                            if (expressionFactory != null)
                                manager.release(expressionFactory);
                            if (newObjectModel != null)
                                manager.release(newObjectModel);
                        }
                    } else {
                        result = processVariable(expr, mapStack, stackSize);
                    }
                    if (stack.empty()) {
                        stack.push(result);
                    } else {
                        last = (Token)stack.peek();
                        if (last.hasType(EXPR)) {
                            last.merge(result);
                        } else {
                            stack.push(result);
                        }
                    }
                    break;
                case OPEN:
                case COLON:
                case ANCHOR_VAR:
                case THREADSAFE_MODULE:
                case STATEFUL_MODULE:
                case ROOT_SITEMAP_VARIABLE:
                default: {
                    stack.push(token);
                    break;
                }
            }
        }
        if (stack.size() !=1) {
            throw new PatternException("Evaluation error in expression: " + originalExpr);
        }
        return ((Token)stack.pop()).getStringValue();
    }

    private Token processModule(Token module, Token expr, Map objectModel, InvokeContext context, List mapStack, int stackSize) throws PatternException {
        int type = module.getType();

        if (type == ANCHOR_VAR) {
            Map levelResult = context.getMapByAnchor(module.getStringValue());

            if (levelResult == null) {
              throw new PatternException("Error while evaluating '" + this.originalExpr +
                "' : no anchor '" + String.valueOf(module.getStringValue()) + "' found in context");
            }

            Object result = levelResult.get(expr.getStringValue());
            return new Token(EXPR, result==null ? "" : result.toString());
        } else if (type == THREADSAFE_MODULE) {
            try {
                InputModule im = module.getModule();
                Object result = im.getAttribute(expr.getStringValue(), null, objectModel);
                return new Token(EXPR, result==null ? "" : result.toString());

            } catch(ConfigurationException confEx) {
                throw new PatternException("Cannot get variable '" + expr.getStringValue() +
                    "' in expression '" + this.originalExpr + "'", confEx);
            }

        } else if (type == STATEFUL_MODULE) {
            InputModule im = null;
            String moduleName = module.getStringValue();
            try {
                im = (InputModule) this.manager.lookup(InputModule.ROLE + '/' + moduleName);

                Object result = im.getAttribute(expr.getStringValue(), null, objectModel);
                return new Token(EXPR, result==null ? "" : result.toString());

            } catch(ServiceException e) {
                throw new PatternException("Cannot get module '" + moduleName +
                                           "' in expression '" + this.originalExpr + "'", e);

            } catch(ConfigurationException confEx) {
                throw new PatternException("Cannot get variable '" + expr.getStringValue() +
                    "' in expression '" + this.originalExpr + "'", confEx);

            } finally {
                this.manager.release(im);
            }
        } else if (type == SITEMAP_VAR) {
            // Prefixed sitemap variable must be parsed at runtime
            String variable = expr.getStringValue();
            Token token;
            if (variable.startsWith("/")) {
                token = new Token(ROOT_SITEMAP_VARIABLE, variable.substring(1));
            } else {
                // Find level
                int level = 1; // Start at 1 since it will be substracted from list.size()
                int pos = 0;
                while (variable.startsWith("../", pos)) {
                    level++;
                    pos += "../".length();
                }
                token = new Token(level, variable.substring(pos));
            }
            return processVariable(token, mapStack, stackSize);
        } else {
            throw new PatternException("Unknown token type: " + expr.getType());
        }
    }

    private Token processVariable(Token expr, List mapStack, int stackSize) throws PatternException {
        int type = expr.getType();
        String value = expr.getStringValue();
        if (type == ROOT_SITEMAP_VARIABLE) {
            Object result = ((Map)mapStack.get(0)).get(value);
            return new Token(EXPR, result==null ? "" : result.toString());
        }
        // relative sitemap variable
        if (type > stackSize) {
            throw new PatternException("Error while evaluating '" + this.originalExpr +
                "' : not so many levels");
        }

        Object result = ((Map)mapStack.get(stackSize - type)).get(value);
        return new Token(EXPR, result==null ? "" : result.toString());
    }
    
    private Token processNewExpression(Token expr, ExpressionFactory expressionFactory, ObjectModel newObjectModel) throws PatternException {
        Object result;
        try {
            Expression newExpression = expressionFactory.getExpression(expr.getStringValue());
            result = newExpression.evaluate(newObjectModel);
        } catch (ExpressionException e) {
            throw new PatternException("Cannot evaluate new expression '" + expr.getStringValue() + "' in expression "
                                       + "'" + this.originalExpr + "'", e);
        }
        return new Token(EXPR, result == null ? "" : result.toString());
    }

    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public final void dispose() {
        if (this.manager != null) {
            for (Iterator i = tokens.iterator(); i.hasNext();) {
                Token token = (Token)i.next();
                if (token.hasType(THREADSAFE_MODULE)) {
                    InputModule im = token.getModule();
                    this.manager.release(im);
                }
            }
            this.tokens.clear();
            this.manager = null;
        }
    }

    private static final class Token {

        private Object value;
        private int type;

        public Token(int type) {
            if (type==EXPR) {
                this.value="";
            } else {
                this.value = null;
            }
            this.type = type;
        }

        public Token(int type, String value) {
            this.value = value;
            this.type = type;
        }

        public Token(int type, InputModule module) {
            this.value = module;
            this.type = type;
        }

        public Token(String value) {
            this.type = TEXT;
            this.value = value;
        }

        public int getType() {
          return type;
        }

        public String getStringValue() {
            if (value instanceof String) {
                return (String) this.value;
            }
            return null;
        }

        public boolean hasType(int type){
            return this.type == type;
        }

        public boolean equals(Object o) {
            if (o instanceof Token) {
                return ((Token) o).hasType(this.type);
            }
            return false;
        }

        public void merge(Token newToken) {
            this.value = this.value + newToken.getStringValue();
        }

        public InputModule getModule() {
            if (value instanceof InputModule) {
                return (InputModule) value;
            }
            return null;
        }
    }
}
