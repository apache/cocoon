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
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.SourceResolver;

import java.util.HashMap;
import java.util.Map;
import java.lang.reflect.Method;

/**
 * The <code>AbstractMultiAction</code> provides a way
 * to call methods of an action specified by
 * the <code>method</code> parameter.
 * This can be extremly useful for action-sets.
 *
 * @author <a href="mailto:tcurdt@dff.st">Torsten Curdt</a>
 * @version CVS $Id: AbstractMultiAction.java,v 1.2 2003/03/16 17:49:11 vgritsenko Exp $
 */
public abstract class AbstractMultiAction extends ConfigurableComposerAction {

    private static final String ACTION_METHOD_PREFIX = "do";
    private static final String ACTION_METHOD_PARAMETER = "method";

    private HashMap methodIndex;

    public void configure(Configuration conf) throws ConfigurationException {
        super.configure(conf);

        try {
            Method[] methods = this.getClass().getMethods();
            methodIndex = new HashMap();

            int prefixLen = ACTION_METHOD_PREFIX.length();
            for (int i = 0; i < methods.length; i++) {
                String methodName = methods[i].getName();
                if (methodName.startsWith(ACTION_METHOD_PREFIX)) {
                    String actionName = methodName.substring(prefixLen, prefixLen + 1).toLowerCase() +
                            methodName.substring(prefixLen + 1);
                    methodIndex.put(methodName, methods[i]);
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("registered method \"" + methodName + "\" as action \"" + actionName + "\"");
                    }
                }
            }
        } catch (Exception e) {
            throw new ConfigurationException("cannot get methods by reflection", e);
        }
    }


    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception {
        String actionMethod = parameters.getParameter(ACTION_METHOD_PARAMETER, null);
        if ((actionMethod != null) && (actionMethod.length() > 0)) {
            Method method = (Method) methodIndex.get(actionMethod);
            if (method != null) {
                return ((Map) method.invoke(this, new Object[]{redirector, resolver, objectModel, source, parameters}));
            } else {
                throw new Exception("action has no method \"" + actionMethod + "\"");
            }
        }

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("you need to specify the method with parameter \"" + ACTION_METHOD_PARAMETER + "\"");
        }
        return null;
    }
}
