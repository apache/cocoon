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
import org.apache.cocoon.components.treeprocessor.InvokeContext;
import org.apache.cocoon.components.treeprocessor.ParameterizableProcessingNode;
import org.apache.cocoon.components.treeprocessor.ProcessingNode;
import org.apache.cocoon.components.treeprocessor.SimpleSelectorProcessingNode;
import org.apache.cocoon.components.treeprocessor.variables.VariableResolver;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.selection.Selector;
import org.apache.cocoon.selection.SwitchSelector;
import org.apache.cocoon.sitemap.PatternException;

import java.util.Map;

/**
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: SwitchSelectNode.java,v 1.4 2004/01/03 12:42:39 vgritsenko Exp $
 */
public class SwitchSelectNode extends SimpleSelectorProcessingNode
                              implements ParameterizableProcessingNode, Composable, Disposable {

    /** The parameters of this node */
    private Map parameters;

    /** Pre-selected selector, if it's ThreadSafe */
    protected SwitchSelector threadSafeSelector;

    private ProcessingNode[][] whenNodes;

    private VariableResolver[] whenTests;

    private ProcessingNode[] otherwhiseNodes;

    private ComponentManager manager;

    public SwitchSelectNode(String name) throws PatternException {
        super(name);
    }

    public void setParameters(Map parameterMap) {
        this.parameters = parameterMap;
    }

    public void setCases(ProcessingNode[][] whenNodes, VariableResolver[] whenTests, ProcessingNode[] otherwhiseNodes) {
        this.whenNodes = whenNodes;
        this.whenTests = whenTests;
        this.otherwhiseNodes = otherwhiseNodes;
    }

    public void compose(ComponentManager manager) throws ComponentException {
        this.manager = manager;

        setSelector((ComponentSelector)manager.lookup(Selector.ROLE + "Selector"));

        // Get the selector, if it's ThreadSafe
        this.threadSafeSelector = (SwitchSelector)this.getThreadSafeComponent();
    }

    public final boolean invoke(Environment env, InvokeContext context)
    throws Exception {
	
      	// Perform any common invoke functionality 
        super.invoke(env, context);

        // Prepare data needed by the action
        Map objectModel = env.getObjectModel();
        Parameters resolvedParams = VariableResolver.buildParameters(this.parameters, context, objectModel);

        // If selector is ThreadSafe, avoid select() and try/catch block (faster !)
        if (this.threadSafeSelector != null) {

            Object ctx = this.threadSafeSelector.getSelectorContext(objectModel, resolvedParams);

            for (int i = 0; i < this.whenTests.length; i++) {
                if (this.threadSafeSelector.select(whenTests[i].resolve(context, objectModel), ctx)) {
                    return invokeNodes(this.whenNodes[i], env, context);
                }
            }

            if (this.otherwhiseNodes != null) {
                return invokeNodes(this.otherwhiseNodes, env, context);
            }

            return false;

        } else {
            SwitchSelector selector = (SwitchSelector)this.selector.select(this.componentName);

            Object ctx = selector.getSelectorContext(objectModel, resolvedParams);
           
            try {
                for (int i = 0; i < this.whenTests.length; i++) {
                    if (selector.select(whenTests[i].resolve(context, objectModel), ctx)) {
                        return invokeNodes(this.whenNodes[i], env, context);
                    }
                }

                if (this.otherwhiseNodes != null) {
                    return invokeNodes(this.otherwhiseNodes, env, context);
                }

                return false;
            } finally {
                this.selector.release(selector);
            }
        }
    }

    public void dispose() {
        if (this.threadSafeSelector != null) {
            this.selector.release(this.threadSafeSelector);
            this.threadSafeSelector = null;
        }
        if (this.selector == null) {
            this.manager.release(this.selector);
            this.selector = null;
        }
        this.manager = null;
    }
}
