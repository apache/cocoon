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

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.mail.MailMessageSender;
import org.apache.cocoon.mail.MailSender;

import org.apache.commons.lang.StringUtils;

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
 *   <dt>src</dt>
 *   <dd>A url specifying the source of the text body of the email</dd>
 *   <dt>srcMimeType</dt>
 *   <dd>The optional Mime Type of the  source of the text body of the email if you specified src</dd>
 *   <dt>body</dt>
 *   <dd>the text body of the email, if src is specified, body will be ignored</dd>
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
 *    {@link org.apache.cocoon.servlet.multipart.Part} 
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
 * @author <a href="mailto:andrzej@chaeron.com">Andrzej Taramina</a>
 * @since 2.1
 * @version CVS $Id: Sendmail.java,v 1.8 2004/05/09 20:05:59 haul Exp $
 */
public class Sendmail extends ServiceableAction implements ThreadSafe, Configurable {
    private final static String STATUS = "status";
    private final static String MESSAGE = "message";
    /** Request-Attribute that holds status data*/
    public final static String REQUEST_ATTRIBUTE = "org.apache.cocoon.acting.Sendmail";

    String smtpHost = null;

    public void configure(Configuration conf) throws ConfigurationException {
        if (this.getLogger().isDebugEnabled()) {
            getLogger().debug("SendmailAction: init");
        }

        smtpHost = conf.getAttribute("smtphost", null);

        if (smtpHost != null && this.getLogger().isDebugEnabled()) {
            getLogger().debug(
                "SendmailAction: using " + smtpHost + " as the smtp server");
        }
    }

    public Map act(
        Redirector redirector,
        SourceResolver resolver,
        Map objectModel,
        String source,
        Parameters parameters)
        throws Exception {
        boolean success = false;
        Map status = null;

        MailSender mms = null;
        try {
            if (this.getLogger().isDebugEnabled()) {
                getLogger().debug("SendmailAction: act start");
            }

            Request request = ObjectModelHelper.getRequest(objectModel);

            if (parameters.isParameter("smtphost")) {
                smtpHost = parameters.getParameter("smtphost", null);

                if (this.getLogger().isDebugEnabled()) {
                    getLogger().debug(
                        "SendmailAction: overriding default smtp server, using "
                            + smtpHost);
                }
            }

            mms = (MailSender) this.manager.lookup(MailSender.ROLE);
            if (smtpHost != null) {
            	mms.setSmtpHost(smtpHost);
            }

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

            if (parameters.isParameter("src")) {
                mms.setBodyFromSrc(parameters.getParameter("src", null));
                if (parameters.isParameter("srcMimeType")) {
                    mms.setBodyFromSrcMimeType(
                        parameters.getParameter("srcMimeType", null));
                }
            } else if (parameters.isParameter("body")) {
                mms.setBody(parameters.getParameter("body", null));
            }

            if (parameters.isParameter("attachments")) {
                String fileName[] = StringUtils.split(parameters.getParameter("attachments"));
                for (int i = 0; i < fileName.length; i++) {
                    String srcName = fileName[i];

                    if (srcName.indexOf(":") == -1) {
                        Object obj = request.get(srcName);
                        mms.addAttachment(obj);
                        if (this.getLogger().isDebugEnabled()) {
                            getLogger().debug("request-attachment: " + obj);
                        }
                    } else {
                        mms.addAttachmentURL(
                            srcName,
                            null,
                            srcName.substring(srcName.lastIndexOf('/') + 1));
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
            this.getLogger().error(
                "SendmailAction: AddressException: " + ae.getMessage());

            status = new HashMap(2);
            status.put(Sendmail.STATUS, "user-error");
            status.put(Sendmail.MESSAGE, ae.getMessage());

        } catch (MessagingException me) {
            this.getLogger().error(
                "SendmailAction: MessagingException: "
                    + "An error occured while sending email.",
                me);

            // me contains nested exceptions providing insight on the real
            // cause.
            status = new HashMap(2);
            status.put(Sendmail.STATUS, "server-error");
            status.put(
                Sendmail.MESSAGE,
                "An error occured while sending email: " + me.getMessage());
            
        } catch (ServiceException e) {
            this.getLogger().error(
                    "SendmailAction: An exception was thrown while initializing mail component.",
                    e);

                status = new HashMap(2);
                status.put(Sendmail.STATUS, "server-error");
                status.put(Sendmail.MESSAGE, "An exception was thrown while sending email: "+e.getMessage());

        } catch (Exception e) {
            this.getLogger().error(
                "SendmailAction: An exception was thrown while sending email.",
                e);

            status = new HashMap(2);
            status.put(Sendmail.STATUS, "server-error");
            status.put(Sendmail.MESSAGE, "An exception was thrown while sending email.");

        } finally {
            ObjectModelHelper.getRequest(objectModel).setAttribute(
                Sendmail.REQUEST_ATTRIBUTE,
                status);
            this.manager.release(mms);
        }
        return (success ? status : null);
    }
}
