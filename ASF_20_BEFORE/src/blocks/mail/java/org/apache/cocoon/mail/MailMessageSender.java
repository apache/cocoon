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
package org.apache.cocoon.mail;

import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.mail.datasource.FilePartDataSource;
import org.apache.cocoon.mail.datasource.SourceDataSource;
import org.apache.cocoon.servlet.multipart.Part;

import org.apache.excalibur.source.Source;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;

import javax.mail.BodyPart;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/** A helper class used by the {@link org.apache.cocoon.acting.Sendmail}
 * and the <CODE>sendmail.xsl</CODE> logicsheet for sending an email message.
 *
 * @author <a href="mailto:frank.ridderbusch@gmx.de">Frank Ridderbusch</a>
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @since 2.1
 * @version CVS $Id: MailMessageSender.java,v 1.9 2004/02/18 21:23:03 joerg Exp $
 */
public class MailMessageSender {

    private MimeMessage message;
    private String from;
    private String to;
    private String cc;
    private String bcc;
    private String subject;
    private String charset;
    private String src;
    private String srcMimeType;
    private String body;
    private List attachmentList;
    private Exception exception = null;

    /**
     * Helper class for attachment data. 
     * @author haul
     * @since 2.1
     */
    private class Attachment {
        private Object obj = null;
        private String type = null;
        private String name = null;
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

    /** Creates a new instance of MailMessageSender
     * @param smtpHost The host name or ip-address of a host to accept
     * the email for delivery.
     */
    public MailMessageSender(String smtpHost) {
        smtpHost = smtpHost.trim();

        Properties properties = new Properties();
        Session session = Session.getDefaultInstance(properties);

        if ("null".equals(smtpHost)) {
            properties.put("mail.smtp.host", "127.0.0.1");
        } else {
            properties.put("mail.smtp.host", smtpHost);
        }

        this.message = new MimeMessage(session);
        this.attachmentList = new ArrayList();
    }

    /** Assemble the message from the defined fields and send it.
     * @throws AddressException when problems with email addresses are found
     * @throws MessagingException when message could not be send.
     */
    public void send(SourceResolver resolver)
        throws AddressException, MessagingException {
        List sourcesList = new ArrayList();

        if (this.from == null) {
            throw new AddressException("no from address");
        } else {
            try {
                this.message.setFrom(new InternetAddress(this.from));
            } catch (AddressException e) {
                throw new AddressException(
                    "invalid from address: " + this.from + ": " + e.getMessage());
            }
        }

        if (this.to == null) {
            throw new AddressException("no to address");
        } else {
            try {
                this.message.setRecipients(
                    RecipientType.TO,
                    InternetAddress.parse(this.to));
            } catch (AddressException e) {
                throw new AddressException(
                    "invalid to address: " + this.to + ": " + e.getMessage());
            }
        }

        if (this.cc != null) {
            try {
                this.message.setRecipients(
                    RecipientType.CC,
                    InternetAddress.parse(this.cc));
            } catch (AddressException e) {
                throw new AddressException(
                    "invalid cc address: " + this.cc + ": " + e.getMessage());
            }
        }

        if (this.bcc != null) {
            try {
                this.message.setRecipients(
                    RecipientType.BCC,
                    InternetAddress.parse(this.bcc));
            } catch (AddressException e) {
                throw new AddressException(
                    "invalid bcc address: " + this.bcc + ": " + e.getMessage());
            }
        }

        if (this.subject != null) {
            this.message.setSubject(this.subject);
        }

        message.setSentDate(new Date());

        Attachment a = null;
        try {

            if (this.attachmentList.isEmpty()) {
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

                    this.message.setDataHandler(new DataHandler(ds));

                } else if (this.body != null) {
                    if (this.charset != null) {
                        this.message.setText(this.body, this.charset);
                    } else {
                        this.message.setText(this.body);
                    }
                }
            } else {
                Multipart multipart = new MimeMultipart();
                BodyPart bodypart = null;

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

                    multipart.addBodyPart(bodypart);

                } else if (this.body != null) {
                    bodypart = new MimeBodyPart();
                    bodypart.setText(this.body);
                    multipart.addBodyPart(bodypart);
                }
                this.message.setContent(multipart);

                for (Iterator i = this.attachmentList.iterator(); i.hasNext();) {
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
                            throw new AddressException(
                                "Not yet supported: " + a.getObject());
                        }
                    }

                    bodypart = new MimeBodyPart();
                    bodypart.setDataHandler(new DataHandler(ds));
                    bodypart.setFileName(ds.getName());
                    multipart.addBodyPart(bodypart);
                }
            }

            Transport.send(this.message);
        } catch (MessagingException me) {
            throw new MessagingException(me.getMessage());
        } catch (MalformedURLException e) {
            throw new AddressException(
                "Malformed attachment URL: "
                    + a.getObject()
                    + " error "
                    + e.getMessage());
        } catch (IOException e) {
            throw new AddressException(
                "IOException accessing attachment URL: "
                    + a.getObject()
                    + " error "
                    + e.getMessage());
        } finally {
            if (sourcesList != null) {
                for (Iterator j = sourcesList.iterator(); j.hasNext();) {
                    resolver.release((Source) j.next());
                }
            }
        }
    }

    /**
     * Invokes the {@link #send(SourceResolver)} method but catches any exception thrown. This 
     * method is intended to be used from the sendmail logicsheet. 
     * @return true when successful
     */
    public boolean sendIt(SourceResolver resolver) {
        boolean success = false;
        try {
            this.exception = null;
            this.send(resolver);
            success = true;
        } catch (Exception e) {
            this.exception = e;
        }
        return success;
    }

    /**
     *  Accesses any Exception caught by {@link #sendIt(SourceResolver)}.
     * @return AddressException or MessagingException
     */
    public Exception getException() {
        return this.exception;
    }

    /** Set the <CODE>from</CODE> address of the message.
     * @param from The address the message appears to be from.
     */
    public void setFrom(String from) {
        if (!("".equals(from) || "null".equals(from))) {
            this.from = from.trim();
        }
    }

    /** Sets the destination address(es) for the message. The address
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

    /** Sets the address(es), which should receive a carbon copy of the
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

    /** Sets the address(es), which should receive a black carbon copy of
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

    /** Sets the character set for encoding the message. This has no effect,
     * if any attachments are send in the message.
     * @param charset the character set to be used for enbcoding the message
     */
    public void setCharset(String charset) {
        if (!("".equals(charset) || "null".equals(charset))) {
            this.charset = charset.trim();
        }
    }

    /** Sets the subject line of the message.
     * @param subject the subject line of the message
     */
    public void setSubject(String subject) {
        if (!("".equals(subject) || "null".equals(subject))) {
            this.subject = subject;
        }
    }

    /** Sets the body text of the email message.
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

    /** Sets the body source URL of the email message.
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

    /** Sets the optional body source Mime Type of the email message.
     * @param srcMimeType The optional body source Mime Type of the email message
     */
    public void setBodyFromSrcMimeType(String srcMimeType) {
        if (!("".equals(srcMimeType) || "null".equals(srcMimeType))) {
            this.srcMimeType = srcMimeType;
        }
    }

    /** Add an attachement to the message to be send. The attachment can
     * be of type <CODE>org.apache.excalibur.source.Source</CODE> or
     * {@link org.apache.cocoon.servlet.multipart.Part} or its
     * subclasses.
     * @param attachment to be send with the message
     * @see org.apache.excalibur.source.Source
     */
    public void addAttachment(Object attachment) {
        if (attachment != null) {
            attachmentList.add(new Attachment(attachment));
        }
    }

    /** Add an attachement to the message to be send. The attachment can
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
            attachmentList.add(new Attachment(attachment, type, name));
        }
    }

    /** Add an attachement to the message to be send. The attachment can
     * be of type <CODE>org.apache.excalibur.source.Source</CODE> or
     * {@link org.apache.cocoon.servlet.multipart.Part} or its
     * subclasses.
     * @param url URL to attach to the message
     * @see org.apache.excalibur.source.Source
     */
    public void addAttachmentURL(String url) {
        if (url != null) {
            attachmentList.add(new Attachment(url, null, null, true));
        }
    }

    /** Add an attachement to the message to be send. The attachment can
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
            attachmentList.add(new Attachment(url, type, name, true));
        }
    }

}
