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
package org.apache.cocoon.matching.modular;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.component.ComponentSelector;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.cocoon.components.modules.input.InputModule;

import org.apache.cocoon.matching.AbstractRegexpMatcher;

import java.util.Map;

/**
 * Matches against a regular expression. Needs an input module to
 * obtain value to match against.
 *
 * <p><b>Global and local configuration</b></p>
 * <table border="1">
 * <tr><td><code>input-module</code></td><td>Name of the input module used to obtain the value</td></tr>
 * <tr><td><code>parameter-name</code></td><td>Name of the parameter to match * against</td></tr>
 * </table>
 *
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id: CachingRegexpMatcher.java,v 1.2 2004/02/15 21:30:00 haul Exp $
 */
public class CachingRegexpMatcher extends AbstractRegexpMatcher
    implements Configurable,  Initializable, Composable, Disposable
{

    /** The component manager instance */
    protected ComponentManager manager;

    private String defaultParam;
    private String defaultInput = "request-param"; // default to request parameters
    private Configuration inputConf = null; // will become an empty configuration object
                                            // during configure() so why bother here...
    String INPUT_MODULE_ROLE = InputModule.ROLE;
    String INPUT_MODULE_SELECTOR = INPUT_MODULE_ROLE+"Selector";

    private boolean initialized = false;
    private InputModule input = null;
    private ComponentSelector inputSelector = null;

    /**
     * Set the current <code>ComponentManager</code> instance used by this
     * <code>Composable</code>.
     */
    public void compose(ComponentManager manager) throws ComponentException {

        this.manager=manager;
    }



    public void configure(Configuration config) throws ConfigurationException {

        this.defaultParam = config.getChild("parameter-name").getValue(null);
        this.inputConf = config.getChild("input-module");
        this.defaultInput = this.inputConf.getAttribute("name",this.defaultInput);
    }



    public void initialize() {

        try {
            // obtain input module
            this.inputSelector=(ComponentSelector) this.manager.lookup(INPUT_MODULE_SELECTOR); 
            if (this.defaultInput != null && 
                this.inputSelector != null && 
                this.inputSelector.hasComponent(this.defaultInput)
                ){
                this.input = (InputModule) this.inputSelector.select(this.defaultInput);
                if (!(this.input instanceof ThreadSafe && this.inputSelector instanceof ThreadSafe) ) {
                    this.inputSelector.release(this.input);
                    this.manager.release(this.inputSelector);
                    this.input = null;
                    this.inputSelector = null;
                }
                this.initialized = true;
            } else {
                if (getLogger().isErrorEnabled())
                    getLogger().error("A problem occurred setting up '" + this.defaultInput 
                                      + "': Selector is "+(this.inputSelector!=null?"not ":"")
                                      +"null, Component is "
                                      +(this.inputSelector!=null&&this.inputSelector.hasComponent(this.defaultInput)?"known":"unknown"));
            }
        } catch (Exception e) {
            if (getLogger().isWarnEnabled()) 
                getLogger().warn("A problem occurred setting up '" + this.defaultInput + "': " + e.getMessage());
        }
    }



    public void dispose() {

        if (!this.initialized) 
            if (getLogger().isErrorEnabled()) 
                getLogger().error("Uninitialized Component! FAILING");
        else 
            if (this.inputSelector != null) {
                if (this.input != null)
                    this.inputSelector.release(this.input);
                this.manager.release(this.inputSelector);
            }
    }



    protected String getMatchString(Map objectModel, Parameters parameters) {

        String paramName = parameters.getParameter("parameter-name", this.defaultParam);
        String inputName = parameters.getParameter("input-module", this.defaultInput);

        if (!this.initialized) {
            if (getLogger().isErrorEnabled()) 
                getLogger().error("Uninitialized Component! FAILING");
            return null;
        }
        if (paramName == null) {
            if (getLogger().isWarnEnabled()) 
                getLogger().warn("No parameter name given. Trying to Continue");
        }
        if (inputName == null) {
            if (getLogger().isWarnEnabled()) 
                getLogger().warn("No input module given. FAILING");
            return null;
        }

        Object result = null;

        if (this.input != null && inputName.equals(this.defaultInput)) {
            // input module is thread safe
            // thus we still have a reference to it
            try {
                if (this.input != null) {
                    result = this.input.getAttribute(paramName, this.inputConf, objectModel);
                }
            } catch (Exception e) {
                if (getLogger().isWarnEnabled()) 
                    getLogger().warn("A problem occurred acquiring Parameter '" + paramName 
                                      + "' from '" + inputName + "': " + e.getMessage());
            }
        } else {
            // input was not thread safe
            // so acquire it again
            ComponentSelector iputSelector = null;
            InputModule iput = null;
            try {
                // obtain input module
                iputSelector=(ComponentSelector) this.manager.lookup(INPUT_MODULE_SELECTOR); 
                if (inputName != null && iputSelector != null && iputSelector.hasComponent(inputName)){
                    iput = (InputModule) iputSelector.select(inputName);
                }
                if (iput != null) {
                    result = iput.getAttribute(paramName, this.inputConf, objectModel);
                }
            } catch (Exception e) {
                if (getLogger().isWarnEnabled()) 
                    getLogger().warn("A problem occurred acquiring Parameter '" + paramName 
                                     + "' from '" + inputName + "': " + e.getMessage());
            } finally {
                // release components
                if (iputSelector != null) {
                    if (iput != null)
                        iputSelector.release(iput);
                    this.manager.release(iputSelector);
                }
            }
        }

        if (result instanceof String) {
            return (String) result;
        } else {
            return result.toString();
        }
    }
}
