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
package org.apache.cocoon.components.treeprocessor.sitemap;

import java.util.ArrayList;
import java.util.List;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.components.flow.Interpreter;
import org.apache.cocoon.components.flow.InterpreterSelector;
import org.apache.cocoon.components.treeprocessor.AbstractProcessingNode;
import org.apache.cocoon.components.treeprocessor.InvokeContext;
import org.apache.cocoon.components.treeprocessor.variables.VariableResolver;
import org.apache.cocoon.components.treeprocessor.variables.VariableResolverFactory;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.sitemap.PatternException;

/**
 * Node to handle &lt;map:continue with="..."&gt;
 *
 * @author <a href="mailto:ovidiu@apache.org">Ovidiu Predescu</a>
 * @since March 25, 2002
 * @version CVS $Id: ContinueNode.java,v 1.5 2004/03/05 13:02:51 bdelacretaz Exp $
 */
public class ContinueNode
        extends AbstractProcessingNode
        implements Configurable, Composable
{
    protected String continuationId;
    protected List parameters;
    protected VariableResolver continuationIdResolver;
    protected ComponentManager manager;

    public ContinueNode(String contId) {
        this.continuationId = contId;
    }

    public void configure(Configuration config)
            throws ConfigurationException
    {
        parameters = new ArrayList();

        Configuration[] params = config.getChildren("parameter");
        for (int i = 0; i < params.length; i++) {
            Configuration param = params[i];
            String name = param.getAttribute("name", null);
            String value = param.getAttribute("value", null);
            parameters.add(new Interpreter.Argument(name, value));
        }

        try {
            // The continuation id should would need to be resolved at all
            // times, but who knows...
            if (VariableResolverFactory.needsResolve(continuationId)) {
                this.continuationIdResolver
                        = VariableResolverFactory.getResolver(continuationId, this.manager);
            }
        } catch (PatternException ex) {
            throw new ConfigurationException(ex.toString());
        }
    }

    public void compose(ComponentManager manager) {
        this.manager = manager;
    }

    public boolean invoke(Environment env, InvokeContext context)
            throws Exception
    {
        List params = null;

        // Resolve parameters
        if (this.parameters != null) {
            params = CallFunctionNode.resolveList(this.parameters, this.manager,
                                                  context, env.getObjectModel());
        }

        String contId = continuationId;

        if (continuationIdResolver != null) {
            contId = continuationIdResolver.resolve(context, env.getObjectModel());
        }

        InterpreterSelector selector
                = (InterpreterSelector)manager.lookup(Interpreter.ROLE);

        // FIXME: How to detect the language associated with the
        // continuation object? Use the default language for now, but it
        // should be fixed ASAP.
        String language = selector.getDefaultLanguage();

        // Obtain the Interpreter instance for this language
        Interpreter interpreter = (Interpreter)selector.select(language);

        try {
            interpreter.handleContinuation(contId, params, PipelinesNode.getRedirector(env));
        } finally {
            selector.release((Component)interpreter);
        }

        return true;
    }
}
