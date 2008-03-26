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
import org.apache.cocoon.mail.MailSender;

import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

/**
 * The <code>Sendmail</code> action class sends email.
 *
 * <p>Action supports following parameters:
 * <dl>
 *   <dt>smtp-host</dt>
 *   <dd>The smtp server to send the mail through. If not specified,
 *       default from cocoon.xconf will be used.</dd>
 *   <dt>smtp-user</dt>
 *   <dd>The smtp user. If smtp-user and smtp-host not specified,
 *       default from cocoon.xconf will be used.</dd>
 *   <dt>smtp-password</dt>
 *   <dd>The smtp user's password. If smtp-user and smtp-host not
 *       specified, default from cocoon.xconf will be used.</dd>
 *   <dt>from</dt>
 *   <dd>the email address the mail appears to be from</dd>
 *   <dt>to</dt>
 *   <dd>the email address(es) the mail it sent to. This can
 *       be multiple addresses separated with commas.</dd>
 *   <dt>replyTo</dt>
 *   <dd>the email address(es) replies should be sent to. This can
 *       be multiple addresses separated with commas.</dd>
 *   <dt>cc</dt>
 *   <dd>an email address(es) of someone, who should receive a
 *       carbon copy. This can also be a list of multiple addresses
 *       separated by commas.</dd>
 *   <dt>bcc</dt>
 *   <dd>an email address(es) of someone, who should receive a black
 *       carbon copy. This can also be a list of multiple addresses
 *       separated by commas.</dd>
 *   <dt>subject</dt>
 *   <dd>the subject line of the email</dd>
 *   <dt>src</dt>
 *   <dd>A url specifying the source of the text body of the email</dd>
 *   <dt>srcMimeType</dt>
 *   <dd>The optional Mime Type of the  source of the text body of the email
 *       if you specified src</dd>
 *   <dt>body</dt>
 *   <dd>the text body of the email, if src is specified, body will be ignored</dd>
 *   <dt>charset</dt>
 *   <dd>the character set, which should be used the encode the body text.
 *       This parameter is only used, if no attachements are send.</dd>
 *   <dt>attachments</dt>
 *   <dd>One or more attachments, separated by whitespace, which should be
 *       attached to the email message. If the argument contains a ':', it is
 *       assumed, that the argument describes a
 *       <code>org.apache.excalibur.source.Source</code> object. Otherwise, it
 *       is assumed, that the argument describes a request parameter of an
 *       uploaded file, which Cocoon has internally turned into a
 *       {@link org.apache.cocoon.servlet.multipart.Part}
 *       object.</dd>
 * </dl>
 *
 * <p>
 * Minimally, <code>from</code>, <code>to</code>, <code>body</code> parameters
 * should be specified. Rest of parameters are optional.</p>
 *
 * <p>
 * The class loads all of these parameters from the sitemap, except the
 * attachements, which may come from file upload request parameters.
 * Note it's strongly recommended that the to, cc and bcc addresses be
 * specified by the sitemap, not the request, to prevent possible abuse of the
 * SendmailAction as a spam source.</p>
 *
 * <p>
 * One or two parameters are returned to the sitemap depending on the outcome
 * of sending the message: <code>status</code> and <code>message</code>.</p>
 *
 * <p>
 * If the email message could be successfully delivered only the parameter
 * <code>status</code> with the value <code>success</code> is returned.</p>
 *
 * <p>
 * If there was a problem sending the message, <code>status</code> can have
 * the value <code>user-error</code> and the <code>message</code>
 * parameter is set to an explainatory text. This usually indicates problems with
 * one or more email addresses. Other problems lead to a value of
 * <code>server-error</code> for <code>status</code> and
 * <code>message</code> contains a corresponding message.</p>
 *
 * @cocoon.sitemap.component.documentation
 * The <code>Sendmail</code> action class sends email.
 *
 * @since 2.1
 * @version $Id$
 */
public class Sendmail extends ServiceableAction
                      implements ThreadSafe, Configurable {

    private static final String STATUS = "status";
    private static final String MESSAGE = "message";

    /** Request-Attribute that holds status data*/
    public static final String REQUEST_ATTRIBUTE = "org.apache.cocoon.acting.Sendmail";

    private String smtpHost;
    private String smtpUser;
    private String smtpPassword;

    public void configure(Configuration conf) throws ConfigurationException {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("configure");
        }

        // FIXME Remove support of old "smtphost" attribute.
        smtpHost = conf.getChild("smtp-host").getValue(conf.getAttribute("smtphost", null));
        smtpUser = conf.getChild("smtp-user").getValue(null);
        smtpPassword = conf.getChild("smtp-password").getValue(null);

        if (getLogger().isDebugEnabled()) {
            if (smtpHost != null)
                getLogger().debug("Using " + smtpHost + " as the smtp server");
            if (smtpUser != null)
                getLogger().debug("Using " + smtpUser + " as the smtp user");
        }
    }

    public Map act(Redirector redirector,
                   SourceResolver resolver,
                   Map objectModel,
                   String source,
                   Parameters parameters)
    throws Exception {
        boolean success = false;
        Map status = null;

        MailSender mms = null;
        try {
            Request request = ObjectModelHelper.getRequest(objectModel);

            // FIXME Remove support of old smtphost parameter
            String smtpHost = parameters.getParameter("smtp-host", parameters.getParameter("smtphost", this.smtpHost));
            String smtpUser = parameters.getParameter("smtp-user", this.smtpUser);
            String smtpPassword = parameters.getParameter("smtp-password", this.smtpPassword);

            // Empty parameter means absent parameter
            if ("".equals(smtpHost)) {
                smtpHost = this.smtpHost;
            }
            if ("".equals(smtpUser)) {
                smtpUser = this.smtpUser;
            }
            if ("".equals(smtpPassword)) {
                smtpPassword = this.smtpPassword;
            }

            mms = (MailSender) this.manager.lookup(MailSender.ROLE);

            // Initialize non-default session if host or user specified.
            if (smtpHost != null || smtpUser != null) {
                mms.setSmtpHost(smtpHost, smtpUser, smtpPassword);
            }

            if (parameters.isParameter("from")) {
                mms.setFrom(parameters.getParameter("from", null));
            }
            if (parameters.isParameter("to")) {
                mms.setTo(parameters.getParameter("to", null));
            }
            if (parameters.isParameter("replyTo")) {
                mms.setReplyTo(parameters.getParameter("replyTo", null));
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

            String bodyURL = parameters.getParameter("src", "");
            String body = parameters.getParameter("body", "");
            if (bodyURL.length() > 0) {
                String type = null;
                if (parameters.isParameter("srcMimeType")) {
                    type = parameters.getParameter("srcMimeType", null);
                }
                mms.setBodyURL(bodyURL, type);
            } else if (body.length() > 0) {
                String type = null;
                String charset = parameters.getParameter("charset", "");
                if (charset.length() > 0) {
                    type = "text/plain; charset=" + charset;
                }
                mms.setBody(body, type);
            }

            if (parameters.isParameter("attachments")) {
                String fileName[] = StringUtils.split(parameters.getParameter("attachments"));
                for (int i = 0; i < fileName.length; i++) {
                    String srcName = fileName[i];

                    if (srcName.indexOf(":") == -1) {
                        Object obj = request.get(srcName);
                        mms.addAttachment(obj);
                        if (getLogger().isDebugEnabled()) {
                            getLogger().debug("request-attachment: " + obj);
                        }
                    } else {
                        mms.addAttachmentURL(srcName);
                        if (getLogger().isDebugEnabled()) {
                            getLogger().debug("sitemap-attachment: " + srcName);
                        }
                    }
                }
            }

            mms.send();

            success = true;
            status = new HashMap(3);
            status.put(Sendmail.STATUS, "success");

        } catch (AddressException e) {
            getLogger().warn("AddressException: ", e);

            status = new HashMap(3);
            status.put(Sendmail.STATUS, "user-error");
            status.put(Sendmail.MESSAGE, e.getMessage());

        } catch (MessagingException e) {
            getLogger().warn("MessagingException: " +
                             "An error occured while sending email.", e);

            status = new HashMap(3);
            status.put(Sendmail.STATUS, "server-error");
            status.put(Sendmail.MESSAGE,
                       "An error occured while sending email: " + e.getMessage());

        } catch (ServiceException e) {
            getLogger().error("ServiceException: " +
                              "An error occured while initializing.", e);

            status = new HashMap(3);
            status.put(Sendmail.STATUS, "server-error");
            status.put(Sendmail.MESSAGE,
                       "An exception was thrown while sending email: " + e.getMessage());

        } catch (Exception e) {
            getLogger().error("An exception was thrown while sending email.", e);

            status = new HashMap(3);
            status.put(Sendmail.STATUS, "server-error");
            status.put(Sendmail.MESSAGE, "An exception was thrown while sending email.");

        } finally {
            ObjectModelHelper.getRequest(objectModel).setAttribute(Sendmail.REQUEST_ATTRIBUTE,
                                                                   status);
            this.manager.release(mms);
        }

        return success ? status : null;
    }
}
