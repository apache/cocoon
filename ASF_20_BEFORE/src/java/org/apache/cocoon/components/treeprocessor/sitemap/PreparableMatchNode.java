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
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.treeprocessor.InvokeContext;
import org.apache.cocoon.components.treeprocessor.ParameterizableProcessingNode;
import org.apache.cocoon.components.treeprocessor.SimpleSelectorProcessingNode;
import org.apache.cocoon.components.treeprocessor.variables.VariableResolver;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.matching.Matcher;
import org.apache.cocoon.matching.PreparableMatcher;
import org.apache.cocoon.sitemap.PatternException;

import java.util.Map;

/**
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: PreparableMatchNode.java,v 1.4 2004/01/03 12:42:39 vgritsenko Exp $
 */
public class PreparableMatchNode extends SimpleSelectorProcessingNode
                                 implements ParameterizableProcessingNode, Composable, Disposable {

    /** The 'pattern' attribute */
    private String pattern;

    /** The 'name' for the variable anchor */
    private String name;

    private Object preparedPattern;

    private Map parameters;

    /** The matcher, if it's ThreadSafe */
    private PreparableMatcher threadSafeMatcher;

    protected ComponentManager manager;

    public PreparableMatchNode(String type, String pattern, String name) throws PatternException {
        super(type);
        this.pattern = pattern;
        this.name = name;
    }

    public void setParameters(Map parameterMap) {
        this.parameters = parameterMap;
    }


    public void compose(ComponentManager manager) throws ComponentException {
        this.manager = manager;
        setSelector((ComponentSelector)manager.lookup(Matcher.ROLE + "Selector"));

        // Prepare the pattern, and keep matcher if ThreadSafe
        PreparableMatcher matcher = (PreparableMatcher)selector.select(componentName);

        if (matcher instanceof ThreadSafe) {
            this.threadSafeMatcher = matcher;
        }

        try {
            this.preparedPattern = matcher.preparePattern(this.pattern);

        } catch(PatternException pe) {
            String msg = "Invalid pattern '" + this.pattern + "' for matcher at " + this.getLocation();
            throw new ComponentException(null, msg, pe);

        } finally {
            if (this.threadSafeMatcher == null) {
                selector.release(matcher);
            }
        }
    }

    public final boolean invoke(Environment env, InvokeContext context)
    throws Exception {
      
      	// Perform any common invoke functionality 
      	super.invoke(env, context);

        Map objectModel = env.getObjectModel();
        Parameters resolvedParams = VariableResolver.buildParameters(
            this.parameters, context, objectModel
        );

        Map result = null;

        if (this.threadSafeMatcher != null) {
            // Avoid select() and try/catch block (faster !)
            result = this.threadSafeMatcher.preparedMatch(preparedPattern, objectModel, resolvedParams);

        } else {
            // Get matcher from selector
            PreparableMatcher matcher = (PreparableMatcher)this.selector.select(this.componentName);
            try {
                result = matcher.preparedMatch(preparedPattern, objectModel, resolvedParams);

            } finally {
                this.selector.release(matcher);
            }
        }

        if (result != null) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Matcher '" + this.componentName + "' matched prepared pattern '" +
                    this.pattern + "' at " + this.getLocation());
            }

            // Invoke children with the matcher results
            return this.invokeNodes(children, env, context, name, result);

        } else {
            // Matcher failed
            return false;
        }
    }

    /**
     * Disposable Interface
     */
    public void dispose() {
        if (this.threadSafeMatcher != null) {
            selector.release(this.threadSafeMatcher);
            this.threadSafeMatcher = null;
        }
        if (this.selector != null) {
            this.manager.release(this.selector);
            this.selector = null;
        }
        this.manager = null;
    }
}
