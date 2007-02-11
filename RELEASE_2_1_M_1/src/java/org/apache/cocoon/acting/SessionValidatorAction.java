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
package org.apache.cocoon.acting;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.Constants;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.util.Tokenizer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * This is the action used to validate Session parameters (attributes).
 * The parameters are described via the external xml
 * file (its format is defined in AbstractValidatorAction).
 *
 * <h3>Variant 1</h3>
 * <pre>
 * &lt;map:act type="session-validator"&gt;
 *         &lt;parameter name="descriptor" value="context://descriptor.xml"&gt;
 *         &lt;parameter name="validate" value="username,password"&gt;
 * &lt;/map:act&gt;
 * </pre>
 * The list of parameters to be validated is specified as a comma separated
 * list of their names. descriptor.xml can therefore be used among many
 * various actions.
 *
 * <h3>Variant 2</h3>
 * <pre>
 * <pre>
 * &lt;map:act type="session-validator"&gt;
 *         &lt;parameter name="descriptor" value="context://descriptor.xml"&gt;
 *         &lt;parameter name="validate-set" value="is-logged-in"&gt;
 * &lt;/map:act&gt;
 * </pre>
 *
 * The parameter "validate-set" tells to take a given "constraint-set"
 * from description
 * file and test all parameters against given criteria. This variant is more
 * powerful, more aspect oriented and more flexibile than the previous one,
 * becuase it allows the comparsion constructs, etc. See
 * AbstractValidatorAction documentation.
 *
 * This action returns null when validation fails, otherwise it provides
 * all validated parameters to the sitemap via {name} expression.
 *
 * @author <a href="mailto:Martin.Man@seznam.cz">Martin Man</a>
 * @version CVS $Id: SessionValidatorAction.java,v 1.1 2003/03/09 00:08:40 pier Exp $
 */
public class SessionValidatorAction extends AbstractValidatorAction implements ThreadSafe
{
    /**
     * Main invocation routine.
     */
    public Map act (Redirector redirector, SourceResolver resolver, Map objectModel, String src,
            Parameters parameters) throws Exception {
        Request req = ObjectModelHelper.getRequest(objectModel);

        /* check session validity */
        Session session = req.getSession (false);
        if (session == null) {
            if (this.getLogger().isDebugEnabled()) {
                getLogger ().debug ("No session object");
            }
            return null;
        }

        // read global parameter settings
        boolean reloadable = Constants.DESCRIPTOR_RELOADABLE_DEFAULT;
        if (this.settings.containsKey("reloadable")) {
            reloadable = Boolean.valueOf((String) this.settings.get("reloadable")).booleanValue();
        }

        try {
            Configuration conf = this.getConfiguration (
                    parameters.getParameter ("descriptor", (String) this.settings.get("descriptor")), resolver,
            parameters.getParameterAsBoolean("reloadable",reloadable));

            String valsetstr = parameters.getParameter ( "validate-set", (String) settings.get("validate-set") );
            String valstr = parameters.getParameter ( "validate", (String) settings.get("validate") );

            Configuration[] desc = conf.getChildren ("parameter");
            Configuration[] csets = conf.getChildren ("constraint-set");
            HashMap actionMap = new HashMap ();
            /*
             * old obsoleted method
             */
            if (valstr != null && !"".equals (valstr.trim ())) {
                if (this.getLogger().isDebugEnabled()) {
                    getLogger ().debug ("Validating parameters "
                        + "as specified via 'validate' parameter");
                }
                /* get list of params to be validated */
                String[] rparams = Tokenizer.tokenize (valstr, ",", false);

                /* perform actuall validation */
                ValidatorActionHelper result = null;
                String name = null;
                HashMap params = new HashMap (rparams.length);
                /* put required params into hash */
                for (int i = 0; i < rparams.length; i ++) {
                    name = rparams[i];
                    if (name == null || "".equals (name.trim ())) {
                        if (this.getLogger().isDebugEnabled()) {
                            getLogger ().debug ("Wrong syntax of the 'validate' parameter");
                        }
                        return null;
                    }
                    name = name.trim ();
                    params.put (name, session.getAttribute (name));
                }
                for (int i = 0; i < rparams.length; i ++) {
                    name = rparams[i].trim ();
                    result = validateParameter (name, null, desc,
                            params, false);
                    if (!result.isOK()) {
                        if (this.getLogger().isDebugEnabled()) {
                            getLogger().debug ("Validation failed for parameter " + name);
                        }
                        return null;
                    }
                    session.setAttribute (name, result.getObject());
                    actionMap.put (name, result.getObject());
                }
            }
            /*
             * new set-based method
             */
            if (valsetstr != null && !"".equals (valsetstr.trim ())) {
                if (this.getLogger().isDebugEnabled()) {
                    getLogger ().debug ("Validating parameters "
                        + "from given constraint-set " + valsetstr);
                }
                Configuration cset = null;
                String setname = null;
                int j = 0;
                boolean found = false;
                for (j = 0; j < csets.length; j ++) {
                    setname = csets[j].getAttribute ("name", "");
                    if (valsetstr.trim().equals (setname.trim ())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    if (this.getLogger().isDebugEnabled()) {
                        getLogger ().debug ("Given set "
                            + valsetstr
                            + " does not exist in a description file");
                    }
                    return null;
                }
                cset = csets[j];
                /* get the list of params to be validated */
                Configuration[] set = cset.getChildren ("validate");

                /* perform actuall validation */
                ValidatorActionHelper result = null;
                String name = null;
                HashMap params = new HashMap (set.length);
                if (this.getLogger().isDebugEnabled()) {
                    getLogger ().debug ("Given set "
                        + valsetstr
                        + " contains " + set.length + " rules");
                }
                /* put required params into hash */
                for (int i = 0; i < set.length; i ++) {
                    name = set[i].getAttribute ("name", "");
                    if ("".equals (name.trim ())) {
                        if (this.getLogger().isDebugEnabled()) {
                            getLogger ().debug ("Wrong syntax "
                                + " of 'validate' children nr. " + i);
                        }
                        return null;
                    }
                    name = name.trim ();
                    params.put (name, session.getAttribute (name));
                }
                for (int i = 0; i < set.length; i ++) {
                    name = set[i].getAttribute ("name", null);
                    result = validateParameter(name, set[i],
                            desc, params, false);
                    if (!result.isOK()) {
                        if (this.getLogger().isDebugEnabled()) {
                            getLogger().debug("Validation failed for parameter " + name);
                        }
                        return null;
                    }
                    session.setAttribute (name, result.getObject());
                    actionMap.put (name, result.getObject());
                }
            }
            if (this.getLogger().isDebugEnabled()) {
                getLogger().debug("All session params validated");
            }
            return Collections.unmodifiableMap (actionMap);
        } catch (Exception e) {
            getLogger().warn ("exception: ", e);
        }
        return null;
    }
}

