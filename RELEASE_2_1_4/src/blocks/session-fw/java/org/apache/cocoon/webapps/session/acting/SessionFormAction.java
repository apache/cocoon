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
 * through the "validate-set" element
 *
 * <pre>
 *    &lt;session:form name="info_form&gt;
 *      &lt;session:action&gt;next_page&lt;/session:action&gt;
 *      &lt;session:content&gt;
 *        &lt;session:inputxml name="name" type="text" context="trackdemo" path="/user/name"/&gt;
 *      &lt;/session:content&gt;
 *      &lt;session:validate src="descriptor.xml"&gt;
 *        &lt;validate-set name="form_a_set"/&gt;
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
 * @version CVS $Id: SessionFormAction.java,v 1.5 2003/08/15 15:53:20 haul Exp $
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
