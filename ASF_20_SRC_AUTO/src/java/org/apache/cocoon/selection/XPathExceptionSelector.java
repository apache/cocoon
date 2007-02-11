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
package org.apache.cocoon.selection;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.jxpath.CompiledExpression;
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
 * @author <a href="mailto:juergen.seitz@basf-it-services.com">J&uuml;rgen Seitz</a>
 * @author <a href="mailto:bluetkemeier@s-und-n.de">Bj&ouml;rn L&uuml;tkemeier</a>
 * @since 2.1
 * @version CVS $Id: XPathExceptionSelector.java,v 1.8 2004/03/05 13:02:57 bdelacretaz Exp $
 */
public class XPathExceptionSelector extends ExceptionSelector
  implements Configurable {

    private Map exception2XPath = new HashMap();

    public void configure(Configuration conf) throws ConfigurationException {

        super.configure(conf);

        Configuration[] children = conf.getChildren("exception");
        Configuration[] xPathChildren;

        for (int i = 0; i < children.length; i++) {
            // Check if there are XPath-Expressions configured
            xPathChildren = children[i].getChildren("xpath");
            Map xPathMap = new LinkedMap(11);

            for (int j = 0; j < xPathChildren.length; j++) {
                Configuration xPathChild = xPathChildren[j];

                String xPathName = xPathChild.getAttribute("name");
                CompiledExpression xPath = JXPathContext.compile(xPathChild.getAttribute("test"));

                xPathMap.put(xPathName, xPath);
            }
            if (xPathMap.size() > 0) {
                // store xpath - config if there is some
                exception2XPath.put(children[i].getAttribute("name", null),
                                    xPathMap);
            }
        }
    }

    /**
     * Compute the exception type, given the configuration and the exception stored in the object model.
     */
    public Object getSelectorContext(Map objectModel, Parameters parameters) {

        // get exception from super class
        FindResult selectorContext = (FindResult) super.getSelectorContext(objectModel,
                                         parameters);

        if (selectorContext != null) {
            String exceptionName = selectorContext.getName();
            Throwable t = selectorContext.getThrowable();

            Map xPathMap = (Map) exception2XPath.get(exceptionName);

            if (xPathMap != null) {
                // create a context for the thrown exception
                JXPathContext context = JXPathContext.newContext(t);

                for (Iterator iterator = xPathMap.entrySet().iterator(); iterator.hasNext(); ) {
                    Map.Entry entry = (Map.Entry) iterator.next();

                    if (((CompiledExpression) entry.getValue()).getValue(context).equals(Boolean.TRUE)) {
                        // set the configured name if the expression is succesfull
                        selectorContext.setName((String) entry.getKey());
                        return selectorContext;
                    }
                }
            }
        }

        return selectorContext;
    }
}
