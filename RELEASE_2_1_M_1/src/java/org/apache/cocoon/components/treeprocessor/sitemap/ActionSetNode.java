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

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentSelector;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.acting.Action;
import org.apache.cocoon.components.treeprocessor.InvokeContext;
import org.apache.cocoon.components.treeprocessor.NamedProcessingNode;
import org.apache.cocoon.components.treeprocessor.SimpleSelectorProcessingNode;
import org.apache.cocoon.components.treeprocessor.variables.VariableResolver;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.SourceResolver;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: ActionSetNode.java,v 1.1 2003/03/09 00:09:20 pier Exp $
 */

public class ActionSetNode extends SimpleSelectorProcessingNode
  implements Disposable, NamedProcessingNode, Composable {

    /** The action types */
    private String[] types;

    /** The 'action' attribute for each action */
    private String[] actionNames;

    /** The actions that are ThreadSafe, to avoid lookups */
    private Action[] threadSafeActions;

    /** The src for each action */
    private VariableResolver[] sources;

    /** The parameters for each action */
    private Map[] parameters;

    /** The component manager */
    protected ComponentManager manager;

    public ActionSetNode(
      String name, String[] types, String[] actionNames,
      VariableResolver[] sources, Map[] parameters) {
        super(name);
        this.types = types;
        this.actionNames = actionNames;
        this.sources = sources;
        this.parameters = parameters;
    }

    public void compose(ComponentManager manager) throws ComponentException {
        this.manager = manager;
        setSelector((ComponentSelector)manager.lookup(Action.ROLE + "Selector"));

        // Get all actions that are thread safe
        this.threadSafeActions = new Action[types.length];

        for (int i = 0; i < this.types.length; i++) {
            this.threadSafeActions[i] = (Action)this.getThreadSafeComponent(this.types[i]);
        }
    }

    public final boolean invoke(Environment env, InvokeContext context)
      throws Exception {
	
        // Perform any common invoke functionalty 
        // super.invoke(env, context);
        String msg = "An action-set cannot be invoked, at " + this.getLocation();
        throw new UnsupportedOperationException(msg);
    }

    /**
     * Call the actions composing the action-set and return the combined result of
     * these actions.
     */
    public final Map call(Environment env, InvokeContext context, Parameters params) throws Exception {

        // Prepare data needed by the actions
        Map            objectModel    = env.getObjectModel();
        Redirector     redirector     = PipelinesNode.getRedirector(env);
        SourceResolver resolver       = getSourceResolver(objectModel);

        String cocoonAction = env.getAction();

        Map result = null;

        // Call each action that either has no cocoonAction, or whose cocoonAction equals
        // the one from the environment.

        for (int i = 0; i < types.length; i++) {

            Map actionResult;
            Action action;

            String actionName = actionNames[i];
            String source = sources[i].resolve(context, objectModel);
            if (actionName == null || actionName.equals(cocoonAction)) {

                Parameters actionParams = VariableResolver.buildParameters(parameters[i], context, objectModel);
                if (actionParams == Parameters.EMPTY_PARAMETERS) {
                    actionParams = params;
                } else {
                    actionParams.merge(params);
                }

                // If action is ThreadSafe, avoid select() and try/catch block (faster !)
                if ((action = this.threadSafeActions[i]) != null) {

                    actionResult = action.act(
                        redirector, resolver, objectModel, source, actionParams);

                } else {

                    action = (Action)this.selector.select(this.types[i]);
                    try {
                        actionResult = action.act(
                            redirector, resolver, objectModel, source, actionParams);
                    } finally {
                        this.selector.release(action);
                    }
                }

                if (actionResult != null) {
                    // Merge the result in the global result, creating it if necessary.
                    if (result == null) {
                        result = new HashMap(actionResult);
                    } else {
                        result.putAll(actionResult);
                    }
                }
            } // if (actionName...
        } // for (int i...

        return result;
    }

    public void dispose() {

        // Dispose ThreadSafe actions
        for (int i = 0; i < this.threadSafeActions.length; i++) {
            this.selector.release(this.threadSafeActions[i]);
        }
        
        this.manager.release(this.selector);        
    }

    /**
     * Implementation of <code>NamedProcessingNode</code>.
     */

    public String getName() {
        return this.componentName;
    }
}
