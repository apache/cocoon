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
package org.apache.cocoon.acting;

import org.apache.avalon.framework.parameters.Parameters;

import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;

import java.util.Map;

/**
 * The SendmailAction class sends email. Please use the {@link Sendmail Sendmail} action instead. 
 * The action needs four parameters:
 *
 * <dl>
 *   <dt>smtphost</dt>
 *   <dd>the smtp server to send the mail through</dd>
 *   <dt>from</dt>
 *   <dd>the email address the mail appears to be from</dd>
 *   <dt>to</dt>
 *   <dd>the email address the mail it sent to</dd>
 *   <dt>subject</dt>
 *   <dd>the subject of the email</dd>
 *   <dt>body</dt>
 *   <dd>the body of the email</dd>
 * </dl>
 *
 * The class attempts to load all of these parameters from the sitemap, but
 * if they do not exist there it will read them from the request. The exception
 * is the smtphost parameter, which is assumed to be localhost if not specified
 * in the sitemap. Note it's strongly recommended that the to address be
 * specified by the sitemap, not the request, to prevent possible abuse of the
 * SendmailAction as a spam source.
 *
 * @deprecated
 * @author <a href="mailto:balld@apache.org">Donald Ball</a>
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @version CVS $Id: SendmailAction.java,v 1.3 2004/03/05 13:02:00 bdelacretaz Exp $
 */
public class SendmailAction extends Sendmail {

    public Map act(
        Redirector redirector,
        SourceResolver resolver,
        Map objectModel,
        String source,
        Parameters parameters) 
        throws Exception {
            if (this.getLogger().isDebugEnabled()) {
                getLogger().debug("SendmailAction: act start");
            }
            Request request = ObjectModelHelper.getRequest(objectModel);
            if (!parameters.isParameter("from")) {
                parameters.setParameter("from", request.getParameter("from"));
            }
            if (!parameters.isParameter("to")) {
                parameters.setParameter("to", request.getParameter("to"));
            }
            if (!parameters.isParameter("subject")) {
                parameters.setParameter("subject", request.getParameter("subject"));
            }
            if (!parameters.isParameter("body")) {
                parameters.setParameter("body", request.getParameter("body"));
            }
            if (this.getLogger().isDebugEnabled()) {
                getLogger().debug("SendmailAction: act stop");
            }
            return super.act(redirector, resolver, objectModel, source, parameters);
        }

}