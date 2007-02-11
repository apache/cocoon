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
package org.apache.cocoon.components.variables;

import java.util.ArrayList;
import java.util.List;

import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.components.modules.input.InputModule;
import org.apache.cocoon.sitemap.PatternException;

/**
 * Prepared implementation of {@link VariableResolver} for fast evaluation.
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @author <a href="mailto:tcurdt@apache.org">Torsten Curdt</a>
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: PreparedVariableResolver.java,v 1.4 2004/03/05 13:02:07 bdelacretaz Exp $
 */
public class PreparedVariableResolver 
    extends NOPVariableResolver {
    
    protected ServiceManager  manager;
    protected ServiceSelector selector;
    protected Context           context;
        
    protected List items = new ArrayList();

    // Special constants used for levels
    static final int LITERAL = -2;
    static final int THREADSAFE_MODULE = -3;
    static final int STATEFUL_MODULE = -4;

    private static final Integer LITERAL_OBJ = new Integer(LITERAL);
    private static final Integer THREADSAFE_MODULE_OBJ = new Integer(THREADSAFE_MODULE);
    private static final Integer STATEFUL_MODULE_OBJ = new Integer(STATEFUL_MODULE);
    
    public PreparedVariableResolver(String expr, ServiceManager manager, Context context) 
    throws PatternException {
        
        super(null);
        this.expression = expr;
        this.manager = manager;
        this.context = context;
                
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
                    
                String module = expr.substring(pos, colon);
                String variable = expr.substring(colon + 1, end);

                // Module used
                addModuleVariable(module, variable);
            } else {
                throw new PatternException("Unknown variable format " + expr.substring(pos, end));
            }

            prev = end + 1;
        }
    }
    
    protected void addLiteral(String litteral) {
        this.items.add(LITERAL_OBJ);
        this.items.add(litteral);
    }
    

    protected void addModuleVariable(String moduleName, String variable) throws PatternException {
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
                "' in expression '" + this.expression + "'", ce);
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
    
    public String resolve() 
    throws PatternException {

        StringBuffer result = new StringBuffer();
        
        for (int i = 0; i < this.items.size(); i++) {
            int type = ((Integer)this.items.get(i)).intValue();
            
            switch(type) {
                case LITERAL :
                    result.append(items.get(++i));
                break;

                case THREADSAFE_MODULE :
                {
                    InputModule module = (InputModule)items.get(++i);
                    String variable = (String)items.get(++i);
                    
                    try {                    
                        Object value = module.getAttribute(variable, null, ContextHelper.getObjectModel(this.context));
                        
                        if (value != null) {
                            result.append(value);
                        }

                    } catch(ConfigurationException confEx) {
                        throw new PatternException("Cannot get variable '" + variable +
                            "' in expression '" + this.expression + "'", confEx);
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
                        
                        Object value = module.getAttribute(variableName, null, ContextHelper.getObjectModel(this.context));
                        
                        if (value != null) {
                            result.append(value);
                        }
                        
                    } catch(ServiceException compEx) {
                        throw new PatternException("Cannot get module '" + moduleName +
                            "' in expression '" + this.expression + "'", compEx);
                            
                    } catch(ConfigurationException confEx) {
                        throw new PatternException("Cannot get variable '" + variableName +
                            "' in expression '" + this.expression + "'", confEx);
                            
                    } finally {
                        this.selector.release(module);
                    }
                }
                break;
            }
        }
        
        return result.toString();
        
    }
    
    public void dispose() {
        super.dispose();
        if (this.selector != null) {
            for (int i = 0; i < this.items.size(); i++) {
                int type = ((Integer) this.items.get(i)).intValue();

                switch (type) {
                    case LITERAL:
                        i++; // literal string
                        break;

                    case THREADSAFE_MODULE:
                        i++; // module
                        this.selector.release(this.items.get(i));
                        i++; // variable
                        break;

                    case STATEFUL_MODULE:
                        i += 2; // module name, variable
                        break;

                    default:
                }
            }
            this.manager.release(this.selector);
            this.selector = null;
            this.manager = null;
        }
    }
}
