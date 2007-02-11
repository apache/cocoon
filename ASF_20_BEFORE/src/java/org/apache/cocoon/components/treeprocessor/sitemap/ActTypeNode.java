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
package org.apache.cocoon.components.treeprocessor.sitemap;

import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentSelector;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.acting.Action;
import org.apache.cocoon.components.treeprocessor.InvokeContext;
import org.apache.cocoon.components.treeprocessor.ParameterizableProcessingNode;
import org.apache.cocoon.components.treeprocessor.SimpleSelectorProcessingNode;
import org.apache.cocoon.components.treeprocessor.variables.VariableResolver;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.sitemap.PatternException;

/**
 * Handles &lt;map:act type="..."&gt; (action-sets calls are handled by {@link ActSetNode}).
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: ActTypeNode.java,v 1.4 2004/01/21 10:46:43 antonio Exp $
 */

public class ActTypeNode extends SimpleSelectorProcessingNode
  implements ParameterizableProcessingNode, Disposable, Composable {

    /** The parameters of this node */
    private Map parameters;

    /** The 'src' attribute */
    protected VariableResolver source;

    /** The 'name' for the variable anchor */
    protected String name;

    /** Pre-selected action, if it's ThreadSafe */
    protected Action threadSafeAction;

    protected ComponentManager manager;

    protected boolean inActionSet;

    public ActTypeNode(String type, VariableResolver source, String name,
            boolean inActionSet) throws PatternException {
        super(type);
        this.source = source;
        this.name = name;
        this.inActionSet = inActionSet;
    }

    public void setParameters(Map parameterMap) {
        this.parameters = parameterMap;
    }

    public void compose(ComponentManager manager) throws ComponentException {
        this.manager = manager;
        setSelector((ComponentSelector)manager.lookup(Action.ROLE + "Selector"));

        // Get the action, if it's thread safe
        this.threadSafeAction = (Action)this.getThreadSafeComponent();
    }

    public final boolean invoke(Environment env, InvokeContext context)
          throws Exception {

        // Perform any common invoke functionality 
        super.invoke(env, context);

        // Prepare data needed by the action
        Map objectModel = env.getObjectModel();
        Redirector redirector = PipelinesNode.getRedirector(env);
        SourceResolver resolver = getSourceResolver(objectModel);
        String resolvedSource = source.resolve(context, objectModel);
        Parameters resolvedParams =
            VariableResolver.buildParameters(this.parameters,
                    context, objectModel);

        Map actionResult;

        // If in action set, merge parameters
        if (inActionSet) {
            Parameters callerParams =
                (Parameters)env.getAttribute(ActionSetNode.CALLER_PARAMETERS);
            if (resolvedParams == Parameters.EMPTY_PARAMETERS) {
                // Just swap
                resolvedParams = callerParams;
            } else if (callerParams != Parameters.EMPTY_PARAMETERS) {
                // Build new Parameters object, the both we hare are read-only!
                Parameters newParams = new Parameters();
                // And merge both
                newParams.merge(resolvedParams);
                newParams.merge(callerParams);
                resolvedParams = newParams;
            }
        }

        // If action is ThreadSafe, avoid select() and try/catch block (faster !)
        if (this.threadSafeAction != null) {
            actionResult = this.threadSafeAction.act(redirector, resolver,
                    objectModel, resolvedSource, resolvedParams);
        } else {
            Action action = (Action)this.selector.select(this.componentName);
            try {
                actionResult = action.act(redirector, resolver,
                        objectModel, resolvedSource, resolvedParams);
            } finally {
                this.selector.release(action);
            }
        }

        if (redirector.hasRedirected()) {
            return true;
        }

        if (actionResult != null) {
            // Action succeeded : process children if there are some, with the action result
            if (this.children != null) {
                boolean result = this.invokeNodes(this.children, env, context, name, actionResult);

                if (inActionSet) {
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
            }// else {
               // return false; // Return false to continue sitemap invocation
            //}
        }// else {
            return false;   // Action failed
        //}
    }

    public void dispose() {
        if (this.threadSafeAction != null) {
            this.selector.release(this.threadSafeAction);
        }
        this.manager.release(this.selector);
    }
}
