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
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.ObjectModelHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.Enumeration;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * The <code>AbstractMultiAction</code> provides a way
 * to call methods of an action specified by
 * the <code>method</code> parameter or request parameter.
 * This can be extremly useful for action-sets or as
 * action-sets replacement.
 *
 * Example:
 * <input type="submit" name="doSave" value="Save it"/>
 * will call the method "doSave" of the MultiAction
 *
 * @author <a href="mailto:tcurdt@dff.st">Torsten Curdt</a>
 * @version CVS $Id: AbstractMultiAction.java,v 1.6 2003/09/24 21:26:51 cziegeler Exp $
 */
public abstract class AbstractMultiAction extends ConfigurableComposerAction {

    private static final String ACTION_METHOD_PREFIX = "do";
    private static final String ACTION_METHOD_PARAMETER = "method";

    private HashMap methodIndex;

    private static final String removePrefix( String name ) {
        int prefixLen = ACTION_METHOD_PREFIX.length();
        return name.substring(prefixLen, prefixLen + 1).toLowerCase() + name.substring(prefixLen + 1);
    }

    public void configure(Configuration conf) throws ConfigurationException {
        super.configure(conf);

        try {
            Method[] methods = this.getClass().getMethods();
            methodIndex = new HashMap();

            for (int i = 0; i < methods.length; i++) {
                String methodName = methods[i].getName();
                if (methodName.startsWith(ACTION_METHOD_PREFIX)) {
                    String actionName = removePrefix(methodName);
                    methodIndex.put(actionName, methods[i]);
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

        if (actionMethod == null) {
            Request req = ObjectModelHelper.getRequest(objectModel);
            if (req != null) {
                // checking request for action method parameters
                String name;
                for (Enumeration e = req.getParameterNames(); e.hasMoreElements();) {
                    name = (String) e.nextElement();
                    if (name.startsWith(ACTION_METHOD_PREFIX)) {
                        if (name.endsWith(".x") || name.endsWith(".y")) {
                            name = name.substring(ACTION_METHOD_PREFIX.length(), name.length() - 2);
                        }
                        actionMethod = removePrefix(name);
                        break;
                    }
                }
            }
        }

        if((actionMethod != null) && (actionMethod.length() > 0)) {
            Method method = (Method) methodIndex.get(actionMethod);
            if (method != null) {
                try {
                    return ((Map) method.invoke(this, new Object[]{redirector, resolver, objectModel, source, parameters}));
                } catch (InvocationTargetException ite) {
                    if ((ite.getTargetException() != null) && (ite.getTargetException() instanceof Exception)) {
                        throw (Exception)ite.getTargetException();
                    } else {
                        throw ite;
                    }
                }
            } else {
                throw new Exception("action has no method \"" + actionMethod + "\"");
            }
        }

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("you need to specify the method with parameter \"" + ACTION_METHOD_PARAMETER + "\" or have a request parameter starting with \"" + ACTION_METHOD_PREFIX + "\"");
        }
        return null;
    }
}
