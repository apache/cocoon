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

import java.util.Date;
import java.util.Map;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;

/**
 * The SendmailAction class sends email. The action needs four parameters:
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
 * @author <a href="mailto:balld@apache.org">Donald Ball</a>
 * @version CVS $Id: SendmailAction.java,v 1.1 2003/03/09 00:04:33 pier Exp $
 */
public class SendmailAction
    extends AbstractAction
    implements ThreadSafe, Configurable {

    Properties default_properties = null;

    public void configure(Configuration conf) throws ConfigurationException {
        if (this.getLogger().isDebugEnabled()) {
            getLogger().debug("SendmailAction: init");
        }
        default_properties = new Properties();
        default_properties.put(
            "mail.smtp.host",
            conf.getAttribute("smtphost", "127.0.0.1"));
        if (this.getLogger().isDebugEnabled()) {
            getLogger().debug(
                "SendmailAction: using "
                + default_properties.get("mail.smtp.host")
                + " as the smtp server");
        }
    }

    public Map act(
        Redirector redirector,
        SourceResolver resolver,
        Map objectModel,
        String source,
        Parameters parameters)
        throws Exception {
        try {
            if (this.getLogger().isDebugEnabled()) {
                getLogger().debug("SendmailAction: act start");
            }
            Request request = ObjectModelHelper.getRequest(objectModel);
            Properties properties = new Properties(default_properties);
            if (parameters.isParameter("smtphost")) {
                properties.put(
                    "mail.smtp.host",
                    parameters.getParameter("smtphost", null));
                if (this.getLogger().isDebugEnabled()) {
                    getLogger().debug(
                        "SendmailAction: overriding default smtp server, using "
                        + properties.get("mail.smtp.host"));
                }
            }
            Session session = Session.getDefaultInstance(properties);
            Message message = new MimeMessage(session);
            String from = null;
            String to = null;
            String subject = null;
            String body = null;
            try {
                if (parameters.isParameter("from")) {
                    from = parameters.getParameter("from", null);
                } else if ((from = request.getParameter("from")) == null) {
                    throw new SendmailActionException("no from address");
                }
                message.setFrom(new InternetAddress(from));
            } catch (AddressException e) {
                throw new SendmailActionException(
                    "invalid from address: " + from + ": " + e.getMessage());
            }
            try {
                if (parameters.isParameter("to")) {
                    to = parameters.getParameter("to", null);
                } else if ((to = request.getParameter("to")) == null) {
                    throw new SendmailActionException("no to address");
                }
                message.setRecipient(
                    Message.RecipientType.TO,
                    new InternetAddress(to));
            } catch (AddressException e) {
                throw new SendmailActionException(
                    "invalid to address: " + to + ": " + e.getMessage());
            }
            if (parameters.isParameter("subject")) {
                subject = parameters.getParameter("subject", null);
            } else if ((subject = request.getParameter("subject")) == null) {
                throw new SendmailActionException("no subject");
            }
            message.setSubject(subject);
            if (parameters.isParameter("body")) {
                body = parameters.getParameter("body", null);
            } else if ((body = request.getParameter("body")) == null) {
                throw new SendmailActionException("no body");
            }
            message.setText(body);
            message.setSentDate(new Date());
            Transport.send(message);
            if (this.getLogger().isDebugEnabled()) {
                getLogger().debug("SendmailAction: act stop");
            }
            return EMPTY_MAP;
        } catch (SendmailActionException e) {
            getLogger().error("SendmailAction: " + e.getMessage());
            return EMPTY_MAP;
        }
    }

    class SendmailActionException extends Exception {

        public SendmailActionException(String message) {
            super(message);
        }

    }

}
