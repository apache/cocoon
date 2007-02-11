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
package org.apache.cocoon.mail;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;

import org.apache.cocoon.mail.datasource.FilePartDataSource;
import org.apache.cocoon.mail.datasource.SourceDataSource;
import org.apache.cocoon.servlet.multipart.Part;

import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * A helper class used by the {@link org.apache.cocoon.acting.Sendmail}
 * and the <code>sendmail.xsl</code> logicsheet for sending an email message.
 *
 * <h3>Configuration</h3>
 * <table><tbody>
 * <tr><th>smtp-host</th><td>SMTP server to use sending mail.</td><td>opt</td><td>String</td><td><code>localhost</code></td></tr>
 * <tr><th>smtp-user</th><td>User name for authentication</td><td>opt</td><td>String</td></tr>
 * <tr><th>smtp-password</th><td>Password for authentication</td><td>opt</td><td>String</td></tr>
 * </tbody></table>
 *
 * @since 2.1
 * @version $Id$
 */
public class MailMessageSender
        extends AbstractLogEnabled
        implements MailSender, Configurable, Serviceable, Initializable {

    private ServiceManager manager;

    private Session session;

    private String smtpHost;
    private String smtpUser;
    private String smtpPswd;

    private String from;
    private String to;
    private String replyTo;
    private String cc;
    private String bcc;
    private String subject;
    private String charset;
    private String src;
    private String srcMimeType;
    private String body;
    private List attachments;
    private Exception exception;

    /**
     * Helper class for attachment data.
     */
    private static class Attachment {
        private Object obj;
        private String type;
        private String name;
        protected boolean isURL = false;

        /**
         * Create a new attachment object encapsulating obj.
         * @param obj attachment
         */
        public Attachment(Object obj) {
            this(obj, null, null);
        }

        /**
         * Create a new attachment object encapsulating obj
         * @param obj attachment
         * @param type override mime type
         * @param name override attachment name
         */
        public Attachment(Object obj, String type, String name) {
            this(obj, type, name, false);
        }

        /**
         * Create a new attachment object encapsulating obj
         * @param obj attachment
         * @param type override mime type
         * @param name override attachment name
         * @param isURI obj is an instance of String and contains a URL
         */
        public Attachment(Object obj, String type, String name, boolean isURI) {
            this.obj = obj;
            this.type = type;
            this.name = name;
            this.isURL = isURI;
            if (isNullOrEmpty(this.type))
                this.type = null;
            if (isNullOrEmpty(this.name))
                this.name = null;
        }

        /**
         * Check String for null or empty.
         * @param str
         * @return true if str is null, empty string, or equals "null"
         */
        private boolean isNullOrEmpty(String str) {
            return (str == null || "".equals(str) || "null".equals(str));
        }

        /**
         * Is the encapsulated object a URL?
         * @return true if URL
         */
        public boolean isURL() {
            return this.isURL;
        }

        /**
         * Return attachment name. The argument overrides the stored name.
         * @param name
         * @return stored name or otherwise parameter
         */
        public String getName(String name) {
            return (this.name == null ? name : this.name);
        }

        /**
         * Return attachment type. The argument overrides the stored type.
         * @param type  attachment type
         * @return stored type or otherwise parameter
         */
        public String getType(String type) {
            return (this.type == null ? type : this.type);
        }

        /**
         * Returns encapsulated object
         */
        public Object getObject() {
            return this.obj;
        }
    }

    public MailMessageSender() {
    }

    /**
     * Creates a new instance of MailMessageSender
     * Keep constructor for backwards compatibility.
     * @param smtpHost The host name or ip-address of a host to accept
     *                 the email for delivery.
     * @deprecated Since 2.1.5. Please use {@link MailSender} component instead.
     */
    public MailMessageSender(String smtpHost) {
        smtpHost = smtpHost.trim();
        setSmtpHost(smtpHost);
        initialize();
    }

    public void service(ServiceManager manager) {
        this.manager = manager;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.parameters.Parameterizable#parameterize(org.apache.avalon.framework.parameters.Parameters)
     */
    public void configure(Configuration config) throws ConfigurationException {
        this.smtpHost = config.getChild("smtp-host").getValue(null);
        this.smtpUser = config.getChild("smtp-user").getValue(null);
        this.smtpPswd = config.getChild("smtp-password").getValue(null);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize() {
        initSession();
        this.attachments = new ArrayList();
    }

    private void initSession() {
        Properties properties = new Properties();
        if (smtpHost == null || smtpHost.equals("") || smtpHost.equals("null")) {
            properties.put("mail.smtp.host", "127.0.0.1");
        } else {
            properties.put("mail.smtp.host", smtpHost);
        }

        if (smtpUser == null || smtpUser.equals("") || smtpUser.equals("null")) {
            this.session = Session.getInstance(properties);
        } else {
            properties.put("mail.smtp.auth", "true");
            this.session = Session.getInstance(properties, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(smtpUser, smtpPswd);
                }
            });
        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.mail.MailSender#setSmtpHost(java.lang.String)
     */
    public void setSmtpHost(String hostname) {
        this.smtpHost = hostname;
        initSession();
    }

    public void setSmtpHost(String hostname, String username, String password) {
        this.smtpUser = username;
        this.smtpPswd = password;
        setSmtpHost(hostname);
    }


    /**
     * Assemble the message from the defined fields and send it.
     * @throws AddressException when problems with email addresses are found
     * @throws MessagingException when message could not be send.
     */
    public void send() throws AddressException, MessagingException {
        SourceResolver resolver = null;
        try {
            resolver = (SourceResolver) this.manager.lookup(SourceResolver.ROLE);
            doSend(resolver);
        } catch(ServiceException se) {
            throw new CascadingRuntimeException("Cannot get Source Resolver to send mail", se);
        } finally {
            this.manager.release(resolver);
        }
    }

    /**
     * Assemble the message from the defined fields and send it.
     * @throws AddressException when problems with email addresses are found
     * @throws MessagingException when message could not be send.
     * @deprecated Since 2.1.5. Use {@link #send()} which doesn't require passing the source resolver
     */
    public void send(org.apache.cocoon.environment.SourceResolver resolver)
    throws AddressException, MessagingException {
        // resolver is automatically down-casted
        doSend(resolver);
    }

    private void doSend(SourceResolver resolver)
    throws AddressException, MessagingException {
        final List sourcesList = new ArrayList();

        final MimeMessage message = new MimeMessage(this.session);

        if (this.from == null) {
            throw new AddressException("No from address");
        } else {
            try {
                message.setFrom(new InternetAddress(this.from));
            } catch (AddressException e) {
                throw new AddressException("Invalid from address: " + this.from + ": " +
                                           e.getMessage());
            }
        }

        if (this.to == null) {
            throw new AddressException("no to address");
        } else {
            try {
                message.setRecipients(RecipientType.TO,
                                      InternetAddress.parse(this.to));
            } catch (AddressException e) {
                throw new AddressException("Invalid to address: " + this.to + ": " +
                                           e.getMessage());
            }
        }

        if (this.replyTo != null) {
            try {
                message.setReplyTo(InternetAddress.parse(this.replyTo));
            } catch (AddressException e) {
                throw new AddressException("Invalid replyTo address: " + this.replyTo + ": " +
                                           e.getMessage());
            }
        }

        if (this.cc != null) {
            try {
                message.setRecipients(RecipientType.CC,
                                      InternetAddress.parse(this.cc));
            } catch (AddressException e) {
                throw new AddressException("Invalid cc address: " + this.cc + ": " +
                                           e.getMessage());
            }
        }

        if (this.bcc != null) {
            try {
                message.setRecipients(RecipientType.BCC,
                                      InternetAddress.parse(this.bcc));
            } catch (AddressException e) {
                throw new AddressException("Invalid bcc address: " + this.bcc + ": " +
                                           e.getMessage());
            }
        }

        if (this.subject != null) {
            message.setSubject(this.subject);
        }

        message.setSentDate(new Date());

        Attachment a = null;
        try {
            if (this.attachments.isEmpty()) {
                if (this.src != null) {
                    DataSource ds = null;

                    Source source = resolver.resolveURI(this.src);
                    sourcesList.add(source);
                    if (source.exists()) {
                        ds =
                            new SourceDataSource(
                                source,
                                (this.srcMimeType == null
                                    ? source.getMimeType()
                                    : this.srcMimeType),
                                this.src.substring(this.src.lastIndexOf('/') + 1));
                    }

                    message.setDataHandler(new DataHandler(ds));

                } else if (this.body != null) {
                    if (this.charset != null) {
                        message.setText(this.body, this.charset);
                    } else {
                        message.setText(this.body);
                    }
                }
            } else {
                Multipart multipart = new MimeMultipart();
                BodyPart bodypart = new MimeBodyPart();
                multipart.addBodyPart(bodypart);
                message.setContent(multipart);

                if (this.src != null) {
                    DataSource ds = null;

                    Source source = resolver.resolveURI(this.src);
                    sourcesList.add(source);
                    if (source.exists()) {
                        ds =
                            new SourceDataSource(
                                source,
                                (this.srcMimeType == null
                                    ? source.getMimeType()
                                    : this.srcMimeType),
                                this.src.substring(this.src.lastIndexOf('/') + 1));
                    }

                    bodypart.setDataHandler(new DataHandler(ds));
                    bodypart.setFileName(ds.getName());

                } else if (this.body != null) {
                    bodypart.setText(this.body);
                }

                for (Iterator i = this.attachments.iterator(); i.hasNext();) {
                    a = (Attachment) i.next();
                    DataSource ds = null;
                    if (a.isURL) {
                        String name = (String) a.getObject();
                        Source src = resolver.resolveURI(name);
                        sourcesList.add(src);
                        if (src.exists()) {
                            ds =
                                new SourceDataSource(
                                    src,
                                    a.getType(src.getMimeType()),
                                    a.getName(name.substring(name.lastIndexOf('/') + 1)));
                        }
                    } else {
                        if (a.getObject() instanceof Part) {
                            Part part = (Part) a.getObject();
                            ds =
                                new FilePartDataSource(
                                    part,
                                    a.getType(part.getMimeType()),
                                    a.getName(part.getUploadName()));
                        } else {
                            // TODO: other classes?
                            throw new AddressException("Not yet supported: " + a.getObject());
                        }
                    }

                    bodypart = new MimeBodyPart();
                    bodypart.setDataHandler(new DataHandler(ds));
                    bodypart.setFileName(ds.getName());
                    multipart.addBodyPart(bodypart);
                }
            }

            message.saveChanges();
            Transport.send(message);
        } catch (MessagingException me) {
            throw new MessagingException(me.getMessage());
        } catch (MalformedURLException e) {
            throw new AddressException("Malformed attachment URL: " +
                                       a.getObject() + " error " + e.getMessage());
        } catch (IOException e) {
            throw new AddressException("IOException accessing attachment URL: " +
                                       a.getObject() + " error " + e.getMessage());
        } finally {
            if (sourcesList != null) {
                for (Iterator j = sourcesList.iterator(); j.hasNext();) {
                    resolver.release((Source) j.next());
                }
            }
        }
    }

    /**
     * Invokes the {@link #send()} method but catches any exception thrown. This
     * method is intended to be used from the sendmail logicsheet.
     * @return true when successful
     */
    public boolean sendIt() {
        this.exception = null;
        try {
            send();
        } catch (Exception e) {
            this.exception = e;
        }
        return exception == null;
    }

    /**
     * Invokes the {@link #send(org.apache.cocoon.environment.SourceResolver)}
     * method but catches any exception thrown. This
     * method is intended to be used from the sendmail logicsheet.
     *
     * @return true when successful
     * @deprecated Since 2.1.5. Use {@link #sendIt()} which doesn't require passing the source resolver
     */
    public boolean sendIt(org.apache.cocoon.environment.SourceResolver resolver) {
        this.exception = null;
        try {
            send(resolver);
        } catch (Exception e) {
            this.exception = e;
        }
        return exception == null;
    }

    /**
     * Accesses any Exception caught by
     * {@link #sendIt(org.apache.cocoon.environment.SourceResolver)}.
     *
     * @return AddressException or MessagingException
     */
    public Exception getException() {
        return this.exception;
    }


    /**
     * Set the <CODE>from</CODE> address of the message.
     * @param from The address the message appears to be from.
     */
    public void setFrom(String from) {
        if (!("".equals(from) || "null".equals(from))) {
            this.from = from.trim();
        }
    }

    /**
     * Sets the destination address(es) for the message. The address
     * is in the format, that
     * {@link javax.mail.internet.InternetAddress#parse(String)} can handle
     * (one or more email addresses separated by a commas).
     * @param to the destination address(es)
     * @see javax.mail.internet.InternetAddress#parse(String)
     */
    public void setTo(String to) {
        if (!("".equals(to) || "null".equals(to))) {
            this.to = to.trim();
        }
    }

    /**
     * Sets the reply-to address(es) for the message. The address
     * is in the format, that
     * {@link javax.mail.internet.InternetAddress#parse(String)} can handle
     * (one or more email addresses separated by a commas).
     * @param replyTo the address(es) that replies should be sent to
     * @see javax.mail.internet.InternetAddress#parse(String)
     */
    public void setReplyTo(String replyTo) {
        if (!("".equals(replyTo) || "null".equals(replyTo))) {
            this.replyTo = replyTo.trim();
        }
    }

    /**
     * Sets the address(es), which should receive a carbon copy of the
     * message. The address is in the format, that
     * {@link javax.mail.internet.InternetAddress#parse(String)} can handle
     * (one or more email addresses separated by a commas).
     * @param cc the address(es), which should receive a carbon copy.
     * @see javax.mail.internet.InternetAddress#parse(String)
     */
    public void setCc(String cc) {
        if (!("".equals(cc) || "null".equals(cc))) {
            this.cc = cc.trim();
        }
    }

    /**
     * Sets the address(es), which should receive a black carbon copy of
     * the message. The address is in the format, that
     * {@link javax.mail.internet.InternetAddress#parse(String)} can handle
     * (one or more email addresses separated by a commas).
     * @param bcc the address(es), which should receive a black carbon copy.
     * @see javax.mail.internet.InternetAddress#parse(String)
     */
    public void setBcc(String bcc) {
        if (!("".equals(bcc) || "null".equals(bcc))) {
            this.bcc = bcc.trim();
        }
    }

    /**
     * Sets the character set for encoding the message. This has no effect,
     * if any attachments are send in the message.
     * @param charset the character set to be used for enbcoding the message
     */
    public void setCharset(String charset) {
        if (!("".equals(charset) || "null".equals(charset))) {
            this.charset = charset.trim();
        }
    }

    /**
     * Sets the subject line of the message.
     * @param subject the subject line of the message
     */
    public void setSubject(String subject) {
        if (!("".equals(subject) || "null".equals(subject))) {
            this.subject = subject;
        }
    }

    /**
     * Sets the body text of the email message.
     * If both a text body and a body read from a source are set,
     * only the latter will be used.
     *
     * @param body The body text of the email message
     */
    public void setBody(String body) {
        if (!("".equals(body) || "null".equals(body))) {
            this.body = body;
        }
    }

    /**
     * Sets the body source URL of the email message.
     * If both a text body and a body read from a source are set,
     * only the latter will be used.
     *
     * @param src The body source URL of the email message
     */
    public void setBodyFromSrc(String src) {
        if (!("".equals(src) || "null".equals(src))) {
            this.src = src;
        }
    }

    /**
     * Sets the optional body source Mime Type of the email message.
     * @param srcMimeType The optional body source Mime Type of the email message
     */
    public void setBodyFromSrcMimeType(String srcMimeType) {
        if (!("".equals(srcMimeType) || "null".equals(srcMimeType))) {
            this.srcMimeType = srcMimeType;
        }
    }

    /**
     * Add an attachement to the message to be send. The attachment can
     * be of type <CODE>org.apache.excalibur.source.Source</CODE> or
     * {@link org.apache.cocoon.servlet.multipart.Part} or its
     * subclasses.
     * @param attachment to be send with the message
     * @see org.apache.excalibur.source.Source
     */
    public void addAttachment(Object attachment) {
        if (attachment != null) {
            attachments.add(new Attachment(attachment));
        }
    }

    /**
     * Add an attachement to the message to be send. The attachment can
     * be of type <CODE>org.apache.excalibur.source.Source</CODE> or
     * {@link org.apache.cocoon.servlet.multipart.Part} or its
     * subclasses.
     * @param attachment to be send with the message
     * @param type mime type (optional)
     * @param name attachment name (optional)
     * @see org.apache.excalibur.source.Source
     */
    public void addAttachment(Object attachment, String type, String name) {
        if (attachment != null) {
            attachments.add(new Attachment(attachment, type, name));
        }
    }

    /**
     * Add an attachement to the message to be send. The attachment can
     * be of type <CODE>org.apache.excalibur.source.Source</CODE> or
     * {@link org.apache.cocoon.servlet.multipart.Part} or its
     * subclasses.
     * @param url URL to attach to the message
     * @see org.apache.excalibur.source.Source
     */
    public void addAttachmentURL(String url) {
        if (url != null) {
            attachments.add(new Attachment(url, null, null, true));
        }
    }

    /**
     * Add an attachement to the message to be send. The attachment can
     * be of type <CODE>org.apache.excalibur.source.Source</CODE> or
     * {@link org.apache.cocoon.servlet.multipart.Part} or its
     * subclasses.
     * @param url URL to attach to the message
     * @param type mime type (optional)
     * @param name attachment name (optional)
     * @see org.apache.excalibur.source.Source
     */
    public void addAttachmentURL(String url, String type, String name) {
        if (url != null) {
            attachments.add(new Attachment(url, type, name, true));
        }
    }
}
