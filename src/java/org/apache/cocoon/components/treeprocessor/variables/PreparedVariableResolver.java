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
package org.apache.cocoon.components.treeprocessor.variables;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentSelector;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.modules.input.InputModule;
import org.apache.cocoon.components.treeprocessor.InvokeContext;
import org.apache.cocoon.sitemap.PatternException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Prepared implementation of {@link VariableResolver} for fast evaluation.
 *
 * @author <a href="mailto:uv@upaya.co.uk">Upayavira</a>
 * @version CVS $Id: PreparedVariableResolver.java,v 1.5 2004/04/03 20:41:26 upayavira Exp $
 */
final public class PreparedVariableResolver extends VariableResolver implements Disposable {
    
    private ComponentManager manager;
    private ComponentSelector selector;
    private List tokens;
    private boolean needsMapStack;
    
    private static final int OPEN = -2;
    private static final int CLOSE = -3;
    private static final int COLON = -4;
    private static final int TEXT = -5;
    private static final int EVAL = -6;
    private static final int EXPR = -7;
    private static final int SITEMAP_VAR = -8;
    private static final int PREFIXED_SITEMAP_VAR = -9;
    private static final int THREADSAFE_MODULE = -10;
    private static final int STATEFUL_MODULE = -11;
    private static final int ROOT_SITEMAP_VARIABLE = 0;
    private static final int ANCHOR_VAR = -1;
    
    private static Token COLON_TOKEN = new Token(COLON);
    private static Token OPEN_TOKEN = new Token(OPEN);
    private static Token CLOSE_TOKEN = new Token(CLOSE);    

//  @TODO Allow escaping braces.

    public PreparedVariableResolver(String expr, ComponentManager manager) throws PatternException {
        
        super(expr);
        this.manager = manager;
        this.selector = null;
        
        int openCount = 0;
        int closeCount = 0;
        
        needsMapStack = false;
            
        tokens = new ArrayList();
        int pos=0;
        int i;
        boolean escape = false;
        for (i=0;i< expr.length();i++) {
            char c = expr.charAt(i);
            if (escape) {
                escape = false;
            } else if (c=='\\' && i< expr.length()) {
                char nextChar = expr.charAt(i+1);
                if (nextChar == '{' || nextChar == '}') {
                    expr = expr.substring(0, i) + expr.substring(i+1);
                    escape = true;
                    i--;
                }
            } else if (c=='{') {
                if (i> pos) {
                    tokens.add(new Token(expr.substring(pos, i)));
                }
                openCount++;
                tokens.add(OPEN_TOKEN);
                int colonPos = indexOf(expr, ":", i);
                int closePos = indexOf(expr, "}", i);
                int openPos  = indexOf(expr, "{", i);
                if (openPos < colonPos && openPos < closePos) {
                    throw new PatternException("Invalid '{' at position "+ i + " in expression " + expr);
                } else if (colonPos < closePos) {
                    // we've found a module
                    Token token;
                    String module = expr.substring(i+1, colonPos);
                    
                    if (module.equals("sitemap")) {
                        // Explicit prefix for sitemap variable
                        needsMapStack = true;
                        token = new Token(PREFIXED_SITEMAP_VAR);
                    } else if (module.startsWith("#")) {
                        // anchor syntax refering to a name result level
                        needsMapStack = true;
                        token = new Token(ANCHOR_VAR, module.substring(1));
                    } else {
                        // Module used
                        token = getNewModuleToken(module);
                    }
                    tokens.add(token);
                    i = colonPos-1;
                } else {
                    // Unprefixed name : sitemap variable
                    needsMapStack = true;
                    tokens.add(getNewSitemapToken(expr.substring(i+1, closePos)));
                    i = closePos-1;
                }
                pos=i+1;
            } else if (c=='}') {
                if (i>0 && expr.charAt(i-1) == '\\') {
                    continue;
                }
                if (i> pos) {
                    tokens.add(new Token(expr.substring(pos, i)));
                }
                closeCount++;
                tokens.add(CLOSE_TOKEN);
                pos=i+1;
            } else if (c==':') {
                if (tokens.size()>0) {
                    int lastTokenType = ((Token)tokens.get(tokens.size()-1)).getType();
                    if (lastTokenType != PREFIXED_SITEMAP_VAR &&
                        lastTokenType != ANCHOR_VAR &&
                        lastTokenType != THREADSAFE_MODULE &&
                        lastTokenType != STATEFUL_MODULE) {
                            continue;
                    }
                }
                if (i != pos) {
                    // this colon isn't part of a module reference 
                    continue;
                }
                if (i> pos) {
                    tokens.add(new Token(expr.substring(pos, i)));
                }
                tokens.add(COLON_TOKEN);
                pos=i+1;
            }
        }
        if (i> pos) {
            tokens.add(new Token(expr.substring(pos, i)));
        }
        if (openCount != closeCount) {
            throw new PatternException("Mismatching braces in expression: " + expr);
        }
    }
    
    private int indexOf(String expr, String chr, int pos) {
        int location;
        return (location = expr.indexOf(chr, pos+1))!=-1 ? location : expr.length();
    }
    
    private Token getNewSitemapToken(String variable) { 
        if (variable.startsWith("/")) {
            return new Token(ROOT_SITEMAP_VARIABLE, variable.substring(1));
        } else {
            // Find level
            int level = 1; // Start at 1 since it will be substracted from list.size()
            int pos = 0;
            while (variable.startsWith("../", pos)) {
                level++;
                pos += "../".length();
            }
            return new Token(level, variable.substring(pos));
        }
    }


    private Token getNewModuleToken(String moduleName) throws PatternException {
        if (this.selector == null) {
            try {
                // First access to a module : lookup selector
                this.selector = (ComponentSelector)this.manager.lookup(InputModule.ROLE + "Selector");
            } catch(ComponentException ce) {
                throw new PatternException("Cannot access input modules selector", ce);
            }
        }
        
        // Get the module
        InputModule module;
        try {
            module = (InputModule)this.selector.select(moduleName);
        } catch(ComponentException ce) {
            throw new PatternException("Cannot get InputModule named '" + moduleName +
                "' in expression '" + this.originalExpr + "'", ce);
        }
        Token token;
        
        // Is this module threadsafe ?
        if (module instanceof ThreadSafe) {
            token = new Token(THREADSAFE_MODULE, module);
        } else {
            // Stateful module : release it and get a new one each time
            this.selector.release(module);
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
                    if (lastButOne.hasType(COLON)) {
                        Token module = (Token)stack.pop();
                        stack.pop(); // Pop the OPEN
                        result = processModule(module, expr, objectModel, context, mapStack, stackSize);
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
                case EVAL:
                case SITEMAP_VAR:
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
                im = (InputModule)this.selector.select(moduleName);
                                
                Object result = im.getAttribute(expr.getStringValue(), null, objectModel);
                return new Token(EXPR, result==null ? "" : result.toString());

            } catch(ComponentException compEx) {
                throw new PatternException("Cannot get module '" + moduleName +
                    "' in expression '" + this.originalExpr + "'", compEx);
                                    
            } catch(ConfigurationException confEx) {
                throw new PatternException("Cannot get variable '" + expr.getStringValue() +
                    "' in expression '" + this.originalExpr + "'", confEx);
                                    
            } finally {
                this.selector.release(im);
            }
        } else if (type == PREFIXED_SITEMAP_VAR) {
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
        } else {
            // relative sitemap variable
            if (type > stackSize) {
                throw new PatternException("Error while evaluating '" + this.originalExpr +
                    "' : not so many levels");
            }
    
            Object result = ((Map)mapStack.get(stackSize - type)).get(value);
            return new Token(EXPR, result==null ? "" : result.toString());
        }
    }
    
    public final void dispose() {
        if (this.selector != null) {
            for (Iterator i = tokens.iterator(); i.hasNext();) {
                Token token = (Token)i.next();
                if (token.hasType(THREADSAFE_MODULE)) {
                    InputModule im = (InputModule) token.getModule();
                    this.selector.release(im);
                }
            }
            this.manager.release(this.selector);
            this.selector = null;
            this.manager = null;
        }
    }

    private static class Token {
    
        private Object value;
        private int type;

        public Token(int type) {
            this.value = null;
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
        
        public Token(char c) {
            this.value = null;
            if (c=='{') {
                this.type = OPEN;
            } else if (c=='}') {
                this.type = CLOSE;
            } else if (c==':') {
                this.type = COLON;
            }
        }

        public Token(String value) {
            this.value = null;
            if (value.equals("{")) {
                this.type = OPEN;
            } else if (value.equals("}")) {
                this.type = CLOSE;
            } else if (value.equals(":")) {
                this.type = COLON;
            } else {
                this.type = TEXT;
                this.value = value;
            }
        }
      
        public int getType() {
          return type;
        }

        public String getStringValue() {
            if (value instanceof String) {
                return (String)this.value;
            } else {
                return null;
            }
        }
        
        public boolean hasType(int type){
            return this.type == type;
        }
        
        public boolean equals(Object o) {
            if (o instanceof Token) {
                return ((Token)o).hasType(this.type);
            } else {
                return false;
            }
        }
      
        public void merge(Token newToken) {
            this.value = this.value + newToken.getStringValue();
        }

        public InputModule getModule() {
            if (value instanceof InputModule) {
                return (InputModule)value;
            } else {
                return null;
            }
        }
    }
}
