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

import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.modules.input.InputModule;
import org.apache.cocoon.matching.AbstractRegexpMatcher;
import org.apache.cocoon.matching.Matcher;

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
 * @author <a href="mailto:haul@informatik.tu-darmstadt.de">Christian Haul</a>
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id: CachingRegexpMatcher.java,v 1.3 2004/01/05 08:17:31 cziegeler Exp $
 * 
 * @avalon.component
 * @avalon.service type=Matcher
 * @x-avalon.lifestyle type=singleton
 */
public class CachingRegexpMatcher extends AbstractRegexpMatcher
    implements Matcher, Serviceable, Configurable, Initializable, Disposable
{

    /** The component manager instance */
    protected ServiceManager manager;

    private String defaultParam;
    private String defaultInput = "request-param"; // default to request parameters
    private Configuration inputConf = null; // will become an empty configuration object
                                            // during configure() so why bother here...

    private boolean initialized = false;
    private InputModule input = null;

    /**
     * Set the current <code>ComponentManager</code> instance used by this
     * <code>Composable</code>.
     */
    public void service(ServiceManager manager) throws ServiceException {

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
            if (this.defaultInput != null && this.manager.hasService(InputModule.ROLE + "/" + this.defaultInput)
                ){
                this.input = (InputModule) this.manager.lookup(InputModule.ROLE + "/" + this.defaultInput);
                if (!(this.input instanceof ThreadSafe) ) {
                    this.manager.release(this.input);
                    this.input = null;
                }
                this.initialized = true;
            } else {
                if (getLogger().isErrorEnabled()) {
                    getLogger().error("A problem occurred setting up '" + 
                                      this.defaultInput + "'. Component is unknown.");
                }
            }
        } catch (Exception e) {
            if (getLogger().isWarnEnabled()) {
                getLogger().warn("A problem occurred setting up '" + 
                                 this.defaultInput + "': " + e.getMessage());
            }
        }
    }



    public void dispose() {
        if (!this.initialized) {
            if (getLogger().isErrorEnabled()) {
                getLogger().error("Uninitialized Component! FAILING");
            }
        }
        else {
            if (this.input != null) {
                this.manager.release(this.input);
            }
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
            InputModule module = null;
            try {
                if (inputName != null && this.manager.hasService(InputModule.ROLE + "/" + inputName)){
                    module = (InputModule) this.manager.lookup(InputModule.ROLE + "/" + inputName);
                }
                if (module != null) {
                    result = module.getAttribute(paramName, this.inputConf, objectModel);
                }
            } catch (Exception e) {
                if (getLogger().isWarnEnabled()) 
                    getLogger().warn("A problem occurred acquiring Parameter '" + paramName 
                                     + "' from '" + inputName + "': " + e.getMessage());
            } finally {
                // release components
                if (module != null) {
                    this.manager.release(module);
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
