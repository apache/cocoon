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
package org.apache.cocoon.selection;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.commons.jxpath.JXPathContext;

/**
 * Additional to the inherited functionality from its superclass ExceptionSelector,
 * this selector allows to define xpath expressions to evaluate supplemental information 
 * given in the thrown exception. 
 * The configuration of this selector allows to map not only exceptions but also
 * xpath expressions to symbolic names that are used in the &lt;map:when> alternatives.
 * <p>
 * Example configuration :
 * <pre>
 *   &lt;map:selector type="error" src="....XPathExceptionSelector">
 *     &lt;exception name="denied" class="my.comp.auth.AuthenticationFailure">
 *       &lt;xpath name="PasswordWrong" test="authCode=10"/>
 *       &lt;xpath name="PasswordExpired" test="errorCode=11"/>
 *       &lt;xpath name="AccessForbidden" test="errorCode&gt;11"/>
 *     &lt;/exception>	
 *   &lt;/map:selector>
 * </pre>
 * This example shows several features :
 * <li>the test is the xpath expression that will be evaluated against the exception ,</li>
 * <li>an xpath expression can be given a name, which is used in the &lt;map:when> tests,</li>
 *
 * @author <a href="mailto:juergen.seitz@basf-it-services.com">Jürgen Seitz</a>
 * @since 2.1
 * @version CVS $Id: JXPathExceptionSelector.java,v 1.2 2003/05/07 05:09:38 vgritsenko Exp $
 */

public class JXPathExceptionSelector
    extends ExceptionSelector
    implements Configurable {

    private Map exception2XPath = new HashMap();

    public void configure(Configuration conf) throws ConfigurationException {

        super.configure(conf);

        Configuration[] children = conf.getChildren("exception");
        Configuration[] xPathChildren;

        for (int i = 0; i < children.length; i++) {
            // Check if there are XPath-Expressions configured
            xPathChildren = children[i].getChildren("xpath");
            Map xPathMap = new HashMap();
            for (int j = 0; j < xPathChildren.length; j++) {
                Configuration xPathChild = xPathChildren[j];

                String xPathName = xPathChild.getAttribute("name");
                String xPath = xPathChild.getAttribute("test");

                xPathMap.put(xPath, xPathName);
            }
            if (xPathMap.size() > 0)
                // store xpath - config if there is some
                exception2XPath.put(
                    children[i].getAttribute("name", null),
                    xPathMap);
        }
    }

    /**
     * Compute the exception type, given the configuration and the exception stored in the object model.
     * 
     * @see ObjectModelHelper#getThrowable(java.util.Map)
     */
    public Object getSelectorContext(Map objectModel, Parameters parameters) {

        // get exceptionName from super class
        String exceptionName =
            (String) super.getSelectorContext(objectModel, parameters);

        if (exceptionName != null) {
            // get Throwable
            Throwable t = ObjectModelHelper.getThrowable(objectModel);

            Map xPathMap = (Map) exception2XPath.get(exceptionName);
            if (xPathMap != null) {
                // create a context for the thrown exception
                JXPathContext context = JXPathContext.newContext(t);

                for (Iterator iterator = xPathMap.keySet().iterator();
                    iterator.hasNext();
                    ) {
                    String element = (String) iterator.next();
                    if (context.getValue(element).equals(Boolean.TRUE))
                        // return the configured name if the expression is succesfull
                        return xPathMap.get(element);
                }
            }
        }

        return null;

    }
}
