/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.components.variables;

import java.util.ArrayList;
import java.util.List;

import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentSelector;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
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
 * @version CVS $Id: PreparedVariableResolver.java,v 1.2 2003/08/04 03:06:30 joerg Exp $
 */
public class PreparedVariableResolver 
    extends NOPVariableResolver {
    
    protected ComponentManager  manager;
    protected ComponentSelector selector;
    protected Context           context;
        
    protected List items = new ArrayList();

    // Special constants used for levels
    static final int LITERAL = -2;
    static final int THREADSAFE_MODULE = -3;
    static final int STATEFUL_MODULE = -4;

    private static final Integer LITERAL_OBJ = new Integer(LITERAL);
    private static final Integer THREADSAFE_MODULE_OBJ = new Integer(THREADSAFE_MODULE);
    private static final Integer STATEFUL_MODULE_OBJ = new Integer(STATEFUL_MODULE);
    
    public PreparedVariableResolver(String expr, ComponentManager manager, Context context) 
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
                        
                    } catch(ComponentException compEx) {
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
                        this.selector.release((InputModule) this.items.get(i));
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
