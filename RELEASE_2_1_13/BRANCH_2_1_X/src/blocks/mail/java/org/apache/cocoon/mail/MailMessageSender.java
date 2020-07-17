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
package org.apache.cocoon.mail;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Authenticator;
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
import javax.mail.internet.MimePart;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;

import org.apache.cocoon.mail.datasource.AbstractDataSource;
import org.apache.cocoon.mail.datasource.FilePartDataSource;
import org.apache.cocoon.mail.datasource.InputStreamDataSource;
import org.apache.cocoon.mail.datasource.SourceDataSource;
import org.apache.cocoon.servlet.multipart.Part;

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
 * @author <a href="mailto:frank.ridderbusch@gmx.de">Frank Ridderbusch</a>
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @version $Id$
 */
public class MailMessageSender extends AbstractLogEnabled
                               implements MailSender, Configurable, Serviceable,
                                          Initializable, Component {

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

    private Attachment body;
    private String bodyType;
    private String bodySrcType;
    private List attachments;

    private Exception exception;


    /** Java 1.3 Accessor */
    private Logger getMyLogger() {
        return getLogger();
    }

    /**
     * Check string for null, empty, and "null".
     * @param str
     * @return true if str is null, empty string, or equals "null"
     */
    private boolean isNullOrEmpty(String str) {
        return str == null || "".equals(str) || "null".equals(str);
    }

    /**
     * Helper class for attachment data.
     */
    private class Attachment {
        private Object obj;
        private String type;
        private String name;
        protected boolean isURL;

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

            if (isNullOrEmpty(this.type)) {
                this.type = null;
            }

            if (isNullOrEmpty(this.name)) {
                this.name = null;
            }
        }

        /**
         * Is the encapsulated object a URL?
         * @return true if URL
         */
        public boolean isURL() {
            return this.isURL;
        }

        /**
         * Is the encapsulated object a text?
         * @return true if text (String object)
         */
        public boolean isText() {
            return !isURL() && this.obj instanceof String;
        }

        /**
         * Return attachment name.
         */
        public String getName() {
            return this.name;
        }

        /**
         * Return attachment type.
         */
        public String getType() {
            return this.type;
        }

        /**
         * Returns encapsulated object
         */
        public Object getObject() {
            return this.obj;
        }

        public String getText() {
            return (String) this.obj;
        }

        public DataSource getDataSource(SourceResolver resolver, List sources)
        throws IOException, MessagingException {
            AbstractDataSource ds = null;

            if (isURL) {
                String url = (String) getObject();
                Source src = resolver.resolveURI(url);
                sources.add(src);
                if (src.exists()) {
                    ds = new SourceDataSource(src, getType(), getName());
                }
            } else if (getObject() instanceof Part) {
                Part part = (Part) getObject();
                ds = new FilePartDataSource(part, getType(), getName());
            } else if (getObject() instanceof InputStream) {
                InputStream in = (InputStream) getObject();
                ds = new InputStreamDataSource(in, getType(), getName());
            } else if (getObject() instanceof byte[]) {
                byte[] data = (byte[]) getObject();
                ds = new InputStreamDataSource(data, getType(), getName());
            } else {
                // TODO: other classes?
                throw new MessagingException("Not yet supported: " + getObject());
            }

            if (ds != null) {
                ds.enableLogging(getMyLogger());
            }
            return ds;
        }

        public void setContentTo(SourceResolver resolver, List sources, MimePart part)
        throws IOException, MessagingException {
            if (isText()) {
                // Set text
                if (type != null) {
                    part.setContent(getText(), type);
                } else {
                    // Let JavaMail decide on character encoding.
                    part.setText(getText());
                }
                if (name != null) {
                    part.setFileName(name);
                }
            } else {
                // Set data
                DataSource ds = getDataSource(resolver, sources);
                part.setDataHandler(new DataHandler(ds));
                String name = ds.getName();
                if (name != null) {
                    part.setFileName(name);
                }
            }
        }

        public MimeBodyPart getBodyPart(SourceResolver resolver, List sources)
        throws IOException, MessagingException {
            MimeBodyPart part = new MimeBodyPart();
            setContentTo(resolver, sources, part);
            return part;
        }
    }

    private class Body extends Attachment {
        public Body(Object obj) {
            super(obj);
        }

        public Body(Object obj, String type) {
            super(obj, type, null);
        }

        public Body(Object obj, String type, boolean isURI) {
            super(obj, type, null, isURI);
        }

        // Override to set name to null: body can not have name.
        public DataSource getDataSource(SourceResolver resolver, List sources)
        throws IOException, MessagingException {
            AbstractDataSource ds = (AbstractDataSource) super.getDataSource(resolver, sources);
            ds.setName(null);
            return ds;
        }
    }


    public MailMessageSender() {
    }

    /**
     * Creates a new instance of MailMessageSender.
     * Keep constructor for backwards compatibility.
     *
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

    public void configure(Configuration config) throws ConfigurationException {
        this.smtpHost = config.getChild("smtp-host").getValue(null);
        this.smtpUser = config.getChild("smtp-user").getValue(null);
        this.smtpPswd = config.getChild("smtp-password").getValue(null);
    }

    public void initialize() {
        initSession();
        this.attachments = new ArrayList();
    }

    private void initSession() {
        Properties properties = new Properties();
        if (smtpHost == null || smtpHost.length() == 0 || smtpHost.equals("null")) {
            properties.put("mail.smtp.host", "127.0.0.1");
        } else {
            properties.put("mail.smtp.host", smtpHost);
        }

        if (smtpUser == null || smtpUser.length() == 0 || smtpUser.equals("null")) {
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
     *
     * @throws AddressException when problems with email addresses are found
     * @throws MessagingException when message could not be send.
     */
    public void send() throws AddressException, MessagingException {
        SourceResolver resolver = null;
        try {
            resolver = (SourceResolver) this.manager.lookup(SourceResolver.ROLE);
            doSend(resolver);
        } catch (ServiceException se) {
            throw new CascadingRuntimeException("Cannot get Source Resolver to send mail", se);
        } finally {
            this.manager.release(resolver);
        }
    }

    /**
     * Assemble the message from the defined fields and send it.
     *
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
        final List sources = new ArrayList();
        try {
            if (this.attachments.isEmpty()) {
                // Message consists of single part
                if (this.body != null) {
                    a = this.body;
                    a.setContentTo(resolver, sources, message);
                }
            } else {
                // Message consists of multiple parts
                Multipart multipart = new MimeMultipart();
                message.setContent(multipart);

                // Body part
                if (this.body != null) {
                    a = this.body;
                    multipart.addBodyPart(a.getBodyPart(resolver, sources));
                }

                // Attachment parts
                for (Iterator i = this.attachments.iterator(); i.hasNext();) {
                    a = (Attachment) i.next();
                    multipart.addBodyPart(a.getBodyPart(resolver, sources));
                }
            }

            message.saveChanges();
            Transport.send(message);
        } catch (MalformedURLException e) {
            throw new MessagingException("Malformed attachment URL: " +
                                         a.getObject() + " error " + e.getMessage());
        } catch (IOException e) {
            throw new MessagingException("IOException accessing attachment URL: " +
                                         a.getObject() + " error " + e.getMessage());
        } finally {
            for (Iterator j = sources.iterator(); j.hasNext();) {
                resolver.release((Source) j.next());
            }
        }
    }

    /**
     * Invokes the {@link #send()} method but catches any exception thrown. This
     * method is intended to be used from the sendmail logicsheet.
     *
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
     * Set the <code>from</code> address of the message.
     *
     * @param from The address the message appears to be from.
     */
    public void setFrom(String from) {
        if (!isNullOrEmpty(from)) {
            this.from = from.trim();
        }
    }

    /**
     * Sets the destination address(es) for the message. The address
     * is in the format, that
     * {@link javax.mail.internet.InternetAddress#parse(String)} can handle
     * (one or more email addresses separated by a commas).
     *
     * @param to the destination address(es)
     * @see javax.mail.internet.InternetAddress#parse(String)
     */
    public void setTo(String to) {
        if (!isNullOrEmpty(to)) {
            this.to = to.trim();
        }
    }

    /**
     * Sets the reply-to address(es) for the message. The address
     * is in the format, that
     * {@link javax.mail.internet.InternetAddress#parse(String)} can handle
     * (one or more email addresses separated by a commas).
     *
     * @param replyTo the address(es) that replies should be sent to
     * @see javax.mail.internet.InternetAddress#parse(String)
     */
    public void setReplyTo(String replyTo) {
        if (!isNullOrEmpty(replyTo)) {
            this.replyTo = replyTo.trim();
        }
    }

    /**
     * Sets the address(es), which should receive a carbon copy of the
     * message. The address is in the format, that
     * {@link javax.mail.internet.InternetAddress#parse(String)} can handle
     * (one or more email addresses separated by a commas).
     *
     * @param cc the address(es), which should receive a carbon copy.
     * @see javax.mail.internet.InternetAddress#parse(String)
     */
    public void setCc(String cc) {
        if (!isNullOrEmpty(cc)) {
            this.cc = cc.trim();
        }
    }

    /**
     * Sets the address(es), which should receive a black carbon copy of
     * the message. The address is in the format, that
     * {@link javax.mail.internet.InternetAddress#parse(String)} can handle
     * (one or more email addresses separated by a commas).
     *
     * @param bcc the address(es), which should receive a black carbon copy.
     * @see javax.mail.internet.InternetAddress#parse(String)
     */
    public void setBcc(String bcc) {
        if (!isNullOrEmpty(bcc)) {
            this.bcc = bcc.trim();
        }
    }

    /**
     * Sets the subject line of the message.
     *
     * @param subject the subject line of the message
     */
    public void setSubject(String subject) {
        if (!isNullOrEmpty(subject)) {
            this.subject = subject;
        }
    }

    /**
     * Sets the character set for encoding the message. This has no effect,
     * if any attachments are send in the message.
     *
     * @param charset the character set to be used for enbcoding the message
     */
    public void setCharset(String charset) {
        if (!isNullOrEmpty(charset)) {
            this.bodyType = "text/plain; charset=" + charset.trim();
            if (this.body != null && this.body.isText() && this.body.type == null) {
                this.body.type = this.bodyType;
            }
        }
    }

    /**
     * Sets the body text of the email message.
     * If both a text body and a body read from a source are set,
     * only the latter will be used.
     *
     * @param body The body text of the email message
     * @deprecated Since 2.1.10. Use {@link #setBody(Object)}
     */
    public void setBody(String body) {
        if (!isNullOrEmpty(body)) {
            setBody(body, bodyType);
        }
    }

    /**
     * Sets the body source URL of the email message.
     * If both a text body and a body read from a source are set,
     * only the latter will be used.
     *
     * @param src The body source URL of the email message
     * @deprecated Since 2.1.10. Use {@link #setBodyURL(String)}
     */
    public void setBodyFromSrc(String src) {
        if (!isNullOrEmpty(src)) {
           setBodyURL(src, bodySrcType);
        }
    }

    /**
     * Sets the optional body source Mime Type of the email message.
     *
     * @param srcMimeType The optional body source Mime Type of the email message
     * @deprecated Since 2.1.10. Use {@link #setBodyURL(String, String)}
     */
    public void setBodyFromSrcMimeType(String srcMimeType) {
        if (!isNullOrEmpty(srcMimeType)) {
            this.bodySrcType = srcMimeType;
            // Pass into this.body if it was set.
            if (this.body != null && this.body.isURL() && this.body.type == null) {
                this.body.type = srcMimeType;
            }
        }
    }

    /**
     * Sets the body content of the email message.
     *
     * <p>The body can be any of: {@link org.apache.excalibur.source.Source},
     * {@link org.apache.cocoon.servlet.multipart.Part}, {@link java.io.InputStream},
     * <code>byte[]</code>, {@link String}, or a subclass.
     *
     * @param body The body text of the email message
     */
    public void setBody(Object body) {
        setBody(body, null);
    }

    /**
     * Sets the body content of the email message.
     *
     * <p>The body can be any of: {@link org.apache.excalibur.source.Source},
     * {@link org.apache.cocoon.servlet.multipart.Part}, {@link java.io.InputStream},
     * <code>byte[]</code>, {@link String}, or a subclass.
     *
     * @param body The body text of the email message
     * @param type mime type (optional)
     */
    public void setBody(Object body, String type) {
        if (body != null) {
            this.body = new Body(body, type);
        }
    }

    /**
     * Sets the body content of the email message.
     *
     * @param url URL to use as message body
     * @see org.apache.excalibur.source.Source
     */
    public void setBodyURL(String url) {
        setBodyURL(url, null);
    }

    /**
     * Sets the body content of the email message.
     *
     * @param url URL to use as message body
     * @param type mime type (optional)
     * @see org.apache.excalibur.source.Source
     */
    public void setBodyURL(String url, String type) {
        if (url != null) {
            this.body = new Body(url, type, true);
        }
    }

    /**
     * Add an attachement to the message to be send.
     *
     * <p>The attachment can be any of: {@link org.apache.excalibur.source.Source},
     * {@link org.apache.cocoon.servlet.multipart.Part}, {@link java.io.InputStream},
     * <code>byte[]</code>, {@link String}, or a subclass.
     *
     * @param attachment to be send with the message
     */
    public void addAttachment(Object attachment) {
        if (attachment != null) {
            attachments.add(new Attachment(attachment));
        }
    }

    /**
     * Add an attachement to the message to be send.
     *
     * <p>The attachment can be any of: {@link org.apache.excalibur.source.Source},
     * {@link org.apache.cocoon.servlet.multipart.Part}, {@link java.io.InputStream},
     * <code>byte[]</code>, {@link String}, or a subclass.
     *
     * @param attachment to be send with the message
     * @param type mime type (optional)
     * @param name attachment name (optional)
     */
    public void addAttachment(Object attachment, String type, String name) {
        if (attachment != null) {
            attachments.add(new Attachment(attachment, type, name));
        }
    }

    /**
     * Add an attachement to the message to be send.
     *
     * @param url URL to attach to the message
     * @see org.apache.excalibur.source.Source
     */
    public void addAttachmentURL(String url) {
        if (url != null) {
            attachments.add(new Attachment(url, null, null, true));
        }
    }

    /**
     * Add an attachement to the message to be send.
     *
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
