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
 * @version CVS $Id: SendmailAction.java,v 1.2 2003/04/17 20:30:03 haul Exp $
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