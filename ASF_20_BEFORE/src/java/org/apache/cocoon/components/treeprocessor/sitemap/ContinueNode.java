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
 * @version CVS $Id: ContinueNode.java,v 1.4 2004/02/20 18:48:23 sylvain Exp $
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
