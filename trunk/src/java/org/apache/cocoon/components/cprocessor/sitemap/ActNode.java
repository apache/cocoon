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
package org.apache.cocoon.components.cprocessor.sitemap;

import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.acting.Action;
import org.apache.cocoon.components.cprocessor.InvokeContext;
import org.apache.cocoon.components.cprocessor.ProcessingNode;
import org.apache.cocoon.components.cprocessor.SimpleParentProcessingNode;
import org.apache.cocoon.components.cprocessor.variables.VariableResolver;
import org.apache.cocoon.components.cprocessor.variables.VariableResolverFactory;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.environment.internal.EnvironmentHelper;
import org.apache.cocoon.sitemap.PatternException;

/**
 * Handles &lt;map:act type="..."&gt; (action-sets calls are handled by {@link ActSetNode}).
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @author <a href="mailto:unico@apache.org">Unico Hommes</a>
 * @version CVS $Id: ActNode.java,v 1.3 2004/01/27 13:41:42 cziegeler Exp $
 * 
 * @avalon.component
 * @avalon.service type=ProcessingNode
 * @x-avalon.lifestyle type=singleton
 * @x-avalon.info name=act-node
 */
public class ActNode extends SimpleParentProcessingNode
implements ProcessingNode, Initializable, Disposable {
    
    /** The 'name' for the variable anchor */
    private String m_name;
    
    /** The 'src' attribute */
    private VariableResolver m_source;
    
    /** The lookup key for the Action */
    private String m_actionRole;
    
    /** ActionSetNode in case of <map:act set=".." */
    private ActionSetNode m_actionSetNode;
    
    /** Pre-selected action, if it's ThreadSafe */
    private Action m_threadSafeAction;

    protected boolean m_inActionSet;

    public ActNode() {
    }

    /**
     * @return <code>true</code>
     */
    protected boolean hasParameters() {
        return true;
    }

    public void configure(Configuration config) throws ConfigurationException {
        super.configure(config);
        m_name = config.getAttribute("name", null);
        try {
            m_source = VariableResolverFactory.getResolver
                (config.getAttribute("src", null),super.m_manager);
        }
        catch (PatternException e) {
            // TODO: better error handling
            throw new ConfigurationException(e.toString());
        }
        m_inActionSet = config.getAttributeAsBoolean("in-action-set",false);
        String actionSet = config.getAttribute("set",null);
        if (actionSet != null) {
            if (m_inActionSet) {
                String msg = "Cannot call an action set from an action set at " 
                    + getConfigLocation(config);
                throw new ConfigurationException(msg);
            }
            try {
                m_actionSetNode = (ActionSetNode) super.m_manager.lookup(
                    ProcessingNode.ROLE + "/" + actionSet);
            }
            catch (ServiceException e) {
                // TODO: better exception handling
                throw new ConfigurationException(e.toString());
            }
        }
        else {
            m_actionRole = Action.ROLE;
            String type = config.getAttribute("type",null);
            if (type != null) {
                m_actionRole += "/" + type;
            }
        }
    }
    
    public void initialize() throws Exception {
        Action action = (Action) super.m_manager.lookup(m_actionRole);
        if (action instanceof ThreadSafe) {
            m_threadSafeAction = action;
        }
        else {
            super.m_manager.release(action);
        }
    }
    
    public final boolean invoke(Environment env, InvokeContext context) throws Exception {
      
        if (m_actionSetNode != null) {
            // Perform any common invoke functionality 
            super.invoke(env, context);

            Parameters resolvedParams = VariableResolver.buildParameters(
                super.m_parameters,
                context,
                env.getObjectModel()
            );

            Map result = m_actionSetNode.call(env, context, resolvedParams);

            if (EnvironmentHelper.getRedirector(env).hasRedirected()) {
                return true;

            } else if (result == null) {
                return false;

            } else if (getChildNodes() == null) {
                return true;

            } else {
                return this.invokeNodes(getChildNodes(), env, context, null, result);
            }
        }
        
        // Perform any common invoke functionality 
        super.invoke(env, context);

        // Prepare data needed by the action
        Map            objectModel    = env.getObjectModel();
        Redirector     redirector     = EnvironmentHelper.getRedirector(env);
        SourceResolver resolver       = EnvironmentHelper.getSourceResolver(env);
        String         resolvedSource = m_source.resolve(context, objectModel);
        Parameters     resolvedParams = VariableResolver.buildParameters(super.m_parameters, context, objectModel);

        Map actionResult;
        
        // If in action set, merge parameters
        if (m_inActionSet) {
            Parameters callerParams = (Parameters) env.getAttribute(ActionSetNode.CALLER_PARAMETERS);
            if (resolvedParams == Parameters.EMPTY_PARAMETERS) {
                // Just swap
                resolvedParams = callerParams;
            } else if (callerParams != Parameters.EMPTY_PARAMETERS) {
                // Build a new Parameters object since the both we hare are read-only
                Parameters newParams = new Parameters();
                // And merge both
                newParams.merge(resolvedParams);
                newParams.merge(callerParams);
                resolvedParams = newParams;
            }
        }

        // If action is ThreadSafe, avoid select() and try/catch block (faster !)
        if (m_threadSafeAction != null) {
            actionResult = m_threadSafeAction.act(
                redirector, resolver, objectModel, resolvedSource, resolvedParams);
        } else {
            Action action = (Action) super.m_manager.lookup(m_actionRole);
            try {
                actionResult = action.act(
                redirector, resolver, objectModel, resolvedSource, resolvedParams);

            } finally {
                super.m_manager.release(action);
            }
        }

        if (redirector.hasRedirected()) {
            return true;
        }

        if (actionResult == null) {
            // Action failed
            return false;

        } else {
            // Action succeeded : process children if there are some, with the action result
            ProcessingNode[] children = getChildNodes();
            if (children != null) {
                boolean result = this.invokeNodes(children, env, context, m_name, actionResult);
                
                if (m_inActionSet) {
                    // Merge child action results, if any
                    Map childMap = (Map)env.getAttribute(ActionSetNode.ACTION_RESULTS);
                    if (childMap != null) {
                        Map newResults = new HashMap(childMap);
                        newResults.putAll(actionResult);
                        env.setAttribute(ActionSetNode.ACTION_RESULTS, newResults);
                    } else {
                        // No previous results
                        env.setAttribute(ActionSetNode.ACTION_RESULTS, actionResult);
                    }
                }
                
                return result;


            } else {
                // Return false to continue sitemap invocation
                return false;
            }
        }
    }

    public void dispose() {
        if (m_threadSafeAction != null) {
            super.m_manager.release(m_threadSafeAction);
        }
    }


}
