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

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;

//import org.apache.cocoon.servlet.multipart.MultipartHttpServletRequest;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.mail.MailMessageSender;
import org.apache.cocoon.util.Tokenizer;

import java.util.HashMap;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

/** The Sendmail action class sends email. The action minimally needs four parameters:
 *
 * <dl>
 *   <dt>smtphost</dt>
 *   <dd>the smtp server to send the mail through. <code>localhost</code>
 *     by default if this parameter is not sprecified.</dd>
 *   <dt>from</dt>
 *   <dd>the email address the mail appears to be from</dd>
 *   <dt>to</dt>
 *   <dd>the email address the mail it sent to. This can
 *     be multiple addresses separated with commas.</dd>
 *   <dt>subject</dt>
 *   <dd>the subject line of the email</dd>
 *   <dt>body</dt>
 *   <dd>the text body of the email</dd>
 * </dl>
 *
 * The following optionals parameters can be used:
 *
 * <dl>
 *  <dt>cc</dt>
 *  <dd>an email address of someone, who should receive a
 *    carbon copy. This can also be a list of multiple addresses
 *    separated by commas.</dd>
 *  <dt>bcc</dt>
 *  <dd>an email address of someone, who should receive a black
 *    carbon copy. This can also be a list of multiple addresses
 *    separated by commas.</dd>
 *  <dt>charset</dt>
 *  <dd>the character set, which should be used the encode the body text.
 *    This parameter is only used, if no attachements are send.</dd>
 *  <dt>attachments</dt>
 *  <dd>One or more attachments, separated by whitespace, which should be
 *    attached to the email message. If the argument contains a ':', it is
 *    assumed, that the argument describes a
 *    <code>org.apache.excalibur.source.Source</code> object. Otherwise, it
 *    is assumed, that the argument describes a request parameter of an
 *    uploaded file, which
 *    Cocoon has internally turned into a 
 *    {@link org.apache.cocoon.components.request.multipart.FilePart} 
 *    object.</dd>
 * </dl>
 * <p>
 * The class loads all of these parameters from the sitemap, except the 
 * attachements, which may come from file upload request parameters.
 * Note it's strongly recommended that the to, cc and bcc addresses be
 * specified by the sitemap, not the request, to prevent possible abuse of the
 * SendmailAction as a spam source.</p>
 * <p>
 * One or two parameters are returned to the sitemap depending on the outcome
 * of sending the message: <code>status</code> and <code>message</code>.</p>
 * <p>
 * If the email message could be successfully delivered only the parameter
 * <code>status</code> with the value <code>success</code> is returned.</p>
 * <p>
 * If there was a problem sending the message, <code>status</code> can have
 * the value <code>user-error</code> and the <code>message</code>
 * parameter is set to an explainatory text. This usually indicates problems with 
 * one or more email addresses. Other problems lead to a value of
 * <code>server-error</code> for <code>status</code> and
 * <code>message</code> contains a corresponding message.</p>
 *
 * @author <a href="mailto:frank.ridderbusch@gmx.de">Frank Ridderbusch</a>
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @author <a href="mailto:balld@apache.org">Donald Ball</a>
 * @since 2.1
 * @version CVS $Id: Sendmail.java,v 1.1 2003/04/17 20:30:03 haul Exp $
 */
public class Sendmail extends AbstractAction implements ThreadSafe, Configurable {
    private final static String STATUS = "status";
    private final static String MESSAGE = "message";
    /** Request-Attribute that holds status data*/
    public final static String REQUEST_ATTRIBUTE = "org.apache.cocoon.acting.Sendmail";

    String smtpHost = null;

    public void configure(Configuration conf) throws ConfigurationException {
        if (this.getLogger().isDebugEnabled()) {
            getLogger().debug("SendmailAction: init");
        }

        smtpHost = conf.getAttribute("smtphost", "127.0.0.1");

        if (this.getLogger().isDebugEnabled()) {
            getLogger().debug("SendmailAction: using " + smtpHost + " as the smtp server");
        }
    }

    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters)
        throws Exception {
        boolean success = false;
        Map status = null;

        try {
            if (this.getLogger().isDebugEnabled()) {
                getLogger().debug("SendmailAction: act start");
            }

            Request request = ObjectModelHelper.getRequest(objectModel);

            if (parameters.isParameter("smtphost")) {
                smtpHost = parameters.getParameter("smtphost", null);

                if (this.getLogger().isDebugEnabled()) {
                    getLogger().debug("SendmailAction: overriding default smtp server, using " + smtpHost);
                }
            }

            MailMessageSender mms = new MailMessageSender(smtpHost);

            if (parameters.isParameter("from")) {
                mms.setFrom(parameters.getParameter("from", null));
            }

            if (parameters.isParameter("to")) {
                mms.setTo(parameters.getParameter("to", null));
            }

            if (parameters.isParameter("cc")) {
                mms.setCc(parameters.getParameter("cc", null));
            }

            if (parameters.isParameter("bcc")) {
                mms.setBcc(parameters.getParameter("bcc", null));
            }

            if (parameters.isParameter("subject")) {
                mms.setSubject(parameters.getParameter("subject", null));
            }

            if (parameters.isParameter("charset")) {
                mms.setCharset(parameters.getParameter("charset", null));
            }

            if (parameters.isParameter("body")) {
                mms.setBody(parameters.getParameter("body", null));
            }

            if (parameters.isParameter("attachments")) {
                Tokenizer tz = new Tokenizer(parameters.getParameter("attachments"));

                while (tz.hasMoreTokens()) {
                    String srcName = tz.nextToken();

                    if (srcName.indexOf(":") == -1) {
                        //if (request instanceof MultipartHttpServletRequest) {
                            Object obj = request.get(srcName);
                            mms.addAttachment(obj);
                            if (this.getLogger().isDebugEnabled()) {
                                getLogger().debug("request-attachment: " + obj);
                            }
                        //}
                    } else {
                        mms.addAttachmentURL(srcName, null, srcName.substring(srcName.lastIndexOf('/') + 1));
                        if (this.getLogger().isDebugEnabled()) {
                            getLogger().debug("sitemap-attachment: " + srcName);
                        }
                    }
                }
            }

            mms.send(resolver);

            if (this.getLogger().isDebugEnabled()) {
                getLogger().debug("SendmailAction: act stop");
            }

            success = true;
            status = new HashMap(1);
            status.put(Sendmail.STATUS, "success");

        } catch (AddressException ae) {
            this.getLogger().error("SendmailAction: AddressException: " + ae.getMessage());

            status = new HashMap(2);
            status.put(Sendmail.STATUS, "user-error");
            status.put(Sendmail.MESSAGE, ae.getMessage());

        } catch (MessagingException me) {
            this.getLogger().error(
                "SendmailAction: MessagingException: " + "An error occured while sending email.",
                me);

            // me contains nested exceptions providing insight on the real
            // cause.
            status = new HashMap(2);
            status.put(Sendmail.STATUS, "server-error");
            status.put(Sendmail.MESSAGE, "An error occured while sending email: " + me.getMessage());

        } catch (Exception e) {
            this.getLogger().error("SendmailAction: An exception was thrown while sending email.", e);

            status = new HashMap(2);
            status.put(Sendmail.STATUS, "server-error");
            status.put(Sendmail.MESSAGE, "An exception was thrown while sending email.");

        } finally {
            ObjectModelHelper.getRequest(objectModel).setAttribute(Sendmail.REQUEST_ATTRIBUTE, status);
            return (success ? status : null);
        }
    }

}
