/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
 * The SendmailAction class sends email. Please use the {@link Sendmail Sendmail}
 * action instead. The action needs four parameters:
 *
 * <dl>
 *   <dt>from</dt>
 *   <dd>the email address the mail appears to be from</dd>
 *   <dt>to</dt>
 *   <dd>the email address(es) the mail it sent to</dd>
 *   <dt>replyTo</dt>
 *   <dd>the email address(es) replies should be sent to</dd>
 *   <dt>subject</dt>
 *   <dd>the subject of the email</dd>
 *   <dt>body</dt>
 *   <dd>the body of the email</dd>
 * </dl>
 *
 * Action attempts to get all of these parameters from the sitemap, but
 * if they do not exist there it will read them from the request parameters.
 *
 * <p>It also supports all of the {@link Sendmail} action sitemap parameters</p>
 *
 * @deprecated Please use the {@link Sendmail Sendmail} action instead.
 * @author <a href="mailto:balld@apache.org">Donald Ball</a>
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @version CVS $Id$
 */
public class SendmailAction extends Sendmail {

    public Map act(Redirector redirector,
                   SourceResolver resolver,
                   Map objectModel,
                   String source,
                   Parameters parameters)
    throws Exception {

        Request request = ObjectModelHelper.getRequest(objectModel);
        if (!parameters.isParameter("from")) {
            parameters.setParameter("from", request.getParameter("from"));
        }
        if (!parameters.isParameter("to")) {
            parameters.setParameter("to", request.getParameter("to"));
        }
        if (!parameters.isParameter("replyTo")) {
            parameters.setParameter("replyTo", request.getParameter("replyTo"));
        }
        if (!parameters.isParameter("subject")) {
            parameters.setParameter("subject", request.getParameter("subject"));
        }
        if (!parameters.isParameter("body")) {
            parameters.setParameter("body", request.getParameter("body"));
        }

        return super.act(redirector, resolver, objectModel, source, parameters);
    }
}
