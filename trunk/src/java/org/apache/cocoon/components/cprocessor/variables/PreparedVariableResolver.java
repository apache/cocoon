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
package org.apache.cocoon.components.cprocessor.variables;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.modules.input.InputModule;
import org.apache.cocoon.components.cprocessor.InvokeContext;
import org.apache.cocoon.sitemap.PatternException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Prepared implementation of {@link VariableResolver} for fast evaluation.
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @author <a href="mailto:tcurdt@apache.org">Torsten Curdt</a>
 * @version CVS $Id: PreparedVariableResolver.java,v 1.2 2004/03/08 13:57:37 cziegeler Exp $
 */
final public class PreparedVariableResolver extends VariableResolver implements Disposable {
    
    private ServiceManager manager;
    private ServiceSelector selector;
    
    final private List items = new ArrayList();

    // Special constants used for levels
    // ROOT and ANCHOR are placed first as they need a context to be resolved (see resolve())
    static final int ROOT = 0;
    static final int ANCHOR = -1;
    static final int LITERAL = -2;
    static final int THREADSAFE_MODULE = -3;
    static final int STATEFUL_MODULE = -4;

    private static final Integer ROOT_OBJ = new Integer(ROOT);
    private static final Integer LITERAL_OBJ = new Integer(LITERAL);
    private static final Integer THREADSAFE_MODULE_OBJ = new Integer(THREADSAFE_MODULE);
    private static final Integer STATEFUL_MODULE_OBJ = new Integer(STATEFUL_MODULE);
    private static final Integer ANCHOR_OBJ = new Integer(ANCHOR);
    
    public PreparedVariableResolver(String expr, ServiceManager manager) throws PatternException {
        
        super(expr);
        this.manager = manager;
        

        int length = expr.length();
        int prev = 0; // position after last closing brace

        compile : while(prev < length) {
            // find next unescaped '{'
            int pos = prev;
            while(pos < length &&
                  (pos = expr.indexOf('{', pos)) != -1 &&
                  (pos != 0 && expr.charAt(pos - 1) == '\\')) {
                pos++;
            }

            if (pos >= length || pos == -1) {
                // no more braces : add ending literal
                if (prev < length) {
                    addLiteral(expr.substring(prev));
                }
                break compile;
            }

            // Pass closing brace
            pos++;

            // Add litteral strings between closing and next opening brace
            if (prev < pos-1) {
                addLiteral(expr.substring(prev, pos - 1));
            }

            int end = expr.indexOf('}', pos);
            if (end == -1) {
                throw new PatternException("Unmatched '{' in " + expr);
            }

            int colon = expr.indexOf(':', pos);
            if (colon != -1 && colon < end) {
                if (expr.startsWith("sitemap:", pos)) {
                    // Explicit prefix for sitemap variable
                    String variable = expr.substring(pos + "sitemap:".length(), end);
                    addSitemapVariable(variable);
                } else {
                    
                    String module = expr.substring(pos, colon);
                    String variable = expr.substring(colon + 1, end);

                    if (module.startsWith("#")) {
                        // anchor syntax refering to a name result level
                        addAnchorVariable(module.substring(1), variable);
                    }
                    else {
                        // Module used
                        addModuleVariable(module, variable);
                    }
                }
            } else {
                // Unprefixed name : sitemap variable
                addSitemapVariable(expr.substring(pos, end));
            }

            prev = end + 1;
        }
    }
    
    private void addLiteral(String litteral) {
        this.items.add(LITERAL_OBJ);
        this.items.add(litteral);
    }
    
    private void addSitemapVariable(String variable) {
        if (variable.startsWith("/")) {
            this.items.add(ROOT_OBJ);
            this.items.add(variable.substring(1));
        }
        else {
            // Find level
            int level = 1; // Start at 1 since it will be substracted from list.size()
            int pos = 0;
            while (variable.startsWith("../", pos)) {
                level++;
                pos += "../".length();
            }
            this.items.add(new Integer(level));
            this.items.add(variable.substring(pos));
        }
    }

    private void addAnchorVariable(String anchor, String variable) throws PatternException {
        this.items.add(ANCHOR_OBJ);
        this.items.add(anchor);
        this.items.add(variable);
    }

    private void addModuleVariable(String moduleName, String variable) throws PatternException {
        if (this.selector == null) {
            try {
                // First access to a module : lookup selector
                this.selector = (ServiceSelector)this.manager.lookup(InputModule.ROLE + "Selector");
            } catch(ServiceException ce) {
                throw new PatternException("Cannot access input modules selector", ce);
            }
        }
        
        // Get the module
        InputModule module;
        try {
            module = (InputModule)this.selector.select(moduleName);
        } catch(ServiceException ce) {
            throw new PatternException("Cannot get InputModule named '" + moduleName +
                "' in expression '" + this.originalExpr + "'", ce);
        }
        
        // Is this module threadsafe ?
        if (module instanceof ThreadSafe) {
            this.items.add(THREADSAFE_MODULE_OBJ);
            this.items.add(module);
            this.items.add(variable);
        } else {
            // Statefull module : release it
            this.selector.release(module);
            this.items.add(STATEFUL_MODULE_OBJ);
            this.items.add(moduleName);
            this.items.add(variable);
        }
    }
    
    public final String resolve(InvokeContext context, Map objectModel) throws PatternException {
        List mapStack = null; // get the stack only when necessary - lazy inside the loop
        int stackSize = 0;

        StringBuffer result = new StringBuffer();
        
        for (int i = 0; i < this.items.size(); i++) {
            int type = ((Integer)this.items.get(i)).intValue();
            
            if (type >= ANCHOR && mapStack == null) {
                if (context == null) {
                    throw new PatternException("Need an invoke context to resolve " + this);
                }
                mapStack = context.getMapStack();
                stackSize = mapStack.size();
            }                
            
            if (type > 0) {
                // relative sitemap variable
                if (type > stackSize) {
                    throw new PatternException("Error while evaluating '" + this.originalExpr +
                        "' : not so many levels");
                }

                Object key = this.items.get(++i);
                Object value = ((Map)mapStack.get(stackSize - type)).get(key);
                if (value != null) {
                    result.append(value);
                }
                
            } else {
                // other variable types
                switch(type) {
                    case LITERAL :
                        result.append(items.get(++i));
                    break;

                    case ROOT :
                    {
                        Object key = this.items.get(++i);
                        Object value = ((Map)mapStack.get(0)).get(key);
                        if (value != null) {
                            result.append(value);
                        }
                    }
                    break;

                    case ANCHOR:
                    {
                        String name = (String) this.items.get(++i);
                        Object variable = this.items.get(++i);
                        Map levelResult = context.getMapByAnchor(name);

                        if (levelResult == null) {
                          throw new PatternException("Error while evaluating '" + this.originalExpr +
                            "' : no anchor '" + String.valueOf(name) + "' found in context");
                        }

                        Object value = levelResult.get(variable);
                        if (value != null) {
                            result.append(value);
                        }
                    }
                    break;

                    case THREADSAFE_MODULE :
                    {
                        InputModule module = (InputModule)items.get(++i);
                        String variable = (String)items.get(++i);
                        
                        try {                    
                            Object value = module.getAttribute(variable, null, objectModel);
                            
                            if (value != null) {
                                result.append(value);
                            }
    
                        } catch(ConfigurationException confEx) {
                            throw new PatternException("Cannot get variable '" + variable +
                                "' in expression '" + this.originalExpr + "'", confEx);
                        }
                    }
                    break;
                    
                    case STATEFUL_MODULE :
                    {
                        InputModule module = null;
                        String moduleName = (String)items.get(++i);
                        String variableName = (String)items.get(++i);
                        try {
                            module = (InputModule)this.selector.select(moduleName);
                            
                            Object value = module.getAttribute(variableName, null, objectModel);
                            
                            if (value != null) {
                                result.append(value);
                            }
                            
                        } catch(ServiceException compEx) {
                            throw new PatternException("Cannot get module '" + moduleName +
                                "' in expression '" + this.originalExpr + "'", compEx);
                                
                        } catch(ConfigurationException confEx) {
                            throw new PatternException("Cannot get variable '" + variableName +
                                "' in expression '" + this.originalExpr + "'", confEx);
                                
                        } finally {
                            this.selector.release(module);
                        }
                    }
                    break;
                }
            }
        }
        
        return result.toString();
        
    }
    
    public final void dispose() {
        if (this.selector != null) {
            for (int i = 0; i < this.items.size(); i++) {
                int type = ((Integer) this.items.get(i)).intValue();

                switch (type) {
                    case ROOT:
                        i++; // variable
                        break;

                    case LITERAL:
                        i++; // literal string
                        break;

                    case ANCHOR:
                        i += 2; // anchor name, variable
                        break;

                    case THREADSAFE_MODULE:
                        i++; // module
                        this.selector.release((InputModule) this.items.get(i));
                        i++; // variable
                        break;

                    case STATEFUL_MODULE:
                        i += 2; // module name, variable
                        break;

                    default:
                        // relative sitemap variable
                        i++; // variable
                }
            }
            this.manager.release(this.selector);
            this.selector = null;
            this.manager = null;
        }
    }
}
