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

*/
package org.apache.cocoon.components.cprocessor.sitemap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.cprocessor.AbstractProcessingNode;
import org.apache.cocoon.components.cprocessor.InvokeContext;
import org.apache.cocoon.components.cprocessor.ProcessingNode;
import org.apache.cocoon.components.cprocessor.variables.VariableResolver;
import org.apache.cocoon.components.cprocessor.variables.VariableResolverFactory;
import org.apache.cocoon.components.flow.Interpreter;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.sitemap.PatternException;

/**
 * Node handler for calling functions and resuming continuations in
 * the control flow layer.
 *
 * @author <a href="mailto:ovidiu@apache.org">Ovidiu Predescu</a>
 * @author <a href="mailto:unico@apache.org">Unico Hommes</a>
 * 
 * @since March 13, 2002
 * @version CVS $Id: CallFunctionNode.java,v 1.7 2004/02/20 19:06:21 cziegeler Exp $
 * 
 * @avalon.component
 * @avalon.service type=ProcessingNode
 * @x-avalon.lifestyle type=singleton
 * @x-avalon.info name=call-function
 */
public class CallFunctionNode extends AbstractProcessingNode {
    
    private static final String FUNCTION_ATTR = "function";
    private static final String CONTINUATION_ATTR = "continuation";
    
    private VariableResolver m_functionName;
    private VariableResolver m_continuationId;
    
    private Collection m_arguments;
    private Interpreter m_interpreter;
    
    public CallFunctionNode() {
    }

    /**
     * Obtain the configuration specific to this node type and update
     * the internal state.
     *
     * @param config a <code>Configuration</code> value
     * @exception ConfigurationException if an error occurs
     */
    public void configure(Configuration config) throws ConfigurationException {
        super.configure(config);
        
        String functionName = config.getAttribute(FUNCTION_ATTR, null);
        String continuationId = config.getAttribute(CONTINUATION_ATTR, null);
        
        if (functionName == null && continuationId == null) {
            String msg = "<map:call> must have either a 'resource', a 'function' " + "or a 'continuation' attribute!";
            throw new ConfigurationException(msg);
        }
        
        try {
            m_functionName = VariableResolverFactory.getResolver(functionName, super.m_manager);
            m_continuationId = VariableResolverFactory.getResolver(continuationId, super.m_manager);
        }
        catch (PatternException e) {
            throw new ConfigurationException(e.toString());
        }
        
        m_arguments = new ArrayList();

        Configuration[] params = config.getChildren("parameter");
        for (int i = 0; i < params.length; i++) {
            Configuration param = params[i];
            String name = param.getAttribute("name", null);
            VariableResolver value;
            try {
                value = VariableResolverFactory.getResolver(
                    param.getAttribute("value",null),super.m_manager);
            }
            catch (PatternException e) {
                throw new ConfigurationException(e.toString());
            }
            m_arguments.add(new Argument(name,value));
        }
        
        try {
            FlowNode flowNode = (FlowNode) super.m_manager.lookup(ProcessingNode.ROLE + "/flow");
            m_interpreter = flowNode.getInterpreter();
        }
        catch (ServiceException e) {
            String msg = "This sitemap contains no control flows defined, cannot call at " + 
                getLocation() + ". Define a control flow using <map:flow>, " +
                "with embedded <map:script> elements.";
            throw new ConfigurationException(msg);
        }    
    }

    public boolean invoke(Environment env, InvokeContext context) throws Exception {
        Redirector redirector = context.getRedirector();
        
        List params = null;

        // Resolve parameters
        if (m_parameters != null) {
            params = resolveAll(m_arguments,context,env.getObjectModel());
        }

        String continuation = m_continuationId.resolve(context, env.getObjectModel());

        // If the continuation id is not null, it takes precedence over
        // the function call, so we invoke it here.
        if (continuation != null && continuation.length() > 0) {
            m_interpreter.handleContinuation(continuation, params, redirector);
            if (!redirector.hasRedirected()) {
                String msg = "<map:call continuation> did not send a response, at " + getLocation();
                throw new ProcessingException(msg);
            }
            
            return true;
        }

        // We don't have a continuation id passed in <map:call>, so invoke
        // the specified function
        String name = m_functionName.resolve(context, env.getObjectModel());

        if (name != null && name.length() > 0) {
            m_interpreter.callFunction(name, params, redirector);
            if (!redirector.hasRedirected()) {
                String msg = "<map:call function='" + name + "'> did not send a response, at " + getLocation();
                throw new ProcessingException(msg);
            }
            
            return true;
        }

        return false;
    }

    private List resolveAll(Collection expressions, InvokeContext context, Map objectModel)
    throws PatternException {
        int size;
        if (expressions == null || (size = expressions.size()) == 0)
            return Collections.EMPTY_LIST;

        List result = new ArrayList(size);
        Iterator iter = expressions.iterator();
        while (iter.hasNext()) {
            Argument arg = (Argument) iter.next();
            String value = arg.m_value.resolve(context,objectModel);
            result.add(new Interpreter.Argument(arg.m_name, value));
        }

        return result;
    }
    
    private static final class Argument {
        private final String m_name;
        private final VariableResolver m_value;
        private Argument(String name, VariableResolver value) {
            m_name = name;
            m_value = value;
        }
    }
}
