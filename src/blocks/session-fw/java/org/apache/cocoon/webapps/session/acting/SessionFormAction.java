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
package org.apache.cocoon.webapps.session.acting;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.cocoon.acting.FormValidatorAction;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.webapps.session.SessionConstants;

import java.util.Map;

/**
 * This is the action used to validate Request parameters.
 * The validation rules are either embedded within the form
 * 
 * <pre>
 *    &lt;session:form name="info_form"&gt;
 *      &lt;session:action&gt;next_page&lt;/session:action&gt;
 *      &lt;session:content&gt;
 *        &lt;session:inputxml name="name" type="text" context="trackdemo" path="/user/name"/&gt;
 *      &lt;/session:content&gt;
 *      &lt;session:validate&gt;
 *        &lt;root&gt;
 *          &lt;parameter name="name" type="string" nullable="no"/&gt;
 *          &lt;constraint-set name="form_a_set"&gt;
 *            &lt;validate name="name"/&gt;
 *          &lt;/constraint-set&gt;
 *        &lt;/root&gt;
 *      &lt;/session:validate&gt;
 *    &lt;/session:form&gt;
 * </pre>
 * 
 * or described via the external xml file referenced
 * through the "src" attribute
 * (the format is defined in AbstractValidatorAction).
 * Then the constraint-set to be used has to be identified
 * through the "constraint-set" element
 *
 * <pre>
 *    &lt;session:form name="info_form&gt;
 *      &lt;session:action&gt;next_page&lt;/session:action&gt;
 *      &lt;session:content&gt;
 *        &lt;session:inputxml name="name" type="text" context="trackdemo" path="/user/name"/&gt;
 *      &lt;/session:content&gt;
 *      &lt;session:validate src="descriptor.xml"&gt;
 *        &lt;constraint-set name="form_a_set"/&gt;
 *      &lt;/session:validate&gt;
 *    &lt;/session:form&gt;
 * </pre>
 *
 * Since the validation rules are contained within the form document
 * they are read and supplied by the SessionTransformer.
 * So this action has to be used in conjunction with the SessionTransformer.
 * The "next_page" pipeline might look like this:
 * 
 * <pre>
 *     &lt;map:match pattern="next_page"&gt;
 *       &lt;map:act type="session-form"&gt;
 *           &lt;map:generate src="next_page.xml"/&gt;
 *           &lt;map:transform type="session"/&gt;
 *           &lt;map:transform src="simple2html.xsl"/&gt;
 *           &lt;map:serialize/&gt;
 *       &lt;/map:act&gt;
 *       &lt;map:generate src="first_page.xml"/&gt;
 *       &lt;map:transform type="session"/&gt;
 *       &lt;map:transform src="simple2html.xsl"/&gt;
 *       &lt;map:serialize/&gt;
 *     &lt;/map:match&gt;
 * </pre>
 *
 * where "session-form" is configured as SessionFormAction
 * 
 * @see org.apache.cocoon.acting.FormValidatorAction
 * @see org.apache.cocoon.acting.AbstractValidatorAction
 * 
 * @author <a href="mailto:gcasper@s-und-n.de">Guido Casper</a>
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @version CVS $Id: SessionFormAction.java,v 1.7 2004/04/03 03:01:35 joerg Exp $
*/
public class SessionFormAction extends FormValidatorAction implements ThreadSafe {

    /* (non-Javadoc)
     * @see org.apache.cocoon.acting.AbstractValidatorAction#getDescriptor(org.apache.cocoon.environment.SourceResolver, org.apache.avalon.framework.parameters.Parameters)
     */
    protected Configuration getDescriptor(
        SourceResolver resolver,
        Map objectModel,
        Parameters parameters) {

        Session session = ObjectModelHelper.getRequest(objectModel).getSession(true);
        return (Configuration) session.getAttribute(
            ObjectModelHelper.getRequest(objectModel).getParameter(
                SessionConstants.SESSION_FORM_PARAMETER));
    }

}
