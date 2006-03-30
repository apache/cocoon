/*
 * Copyright 2004 The Apache Software Foundation.
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

import org.apache.cocoon.environment.SourceResolver;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

/**
 * A helper component used by the {@link org.apache.cocoon.acting.Sendmail}
 * and the <code>sendmail.xsl</code> logicsheet for sending an email message.
 *
 * @since 2.1.5
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @version $Id$
 */
public interface MailSender {

    String ROLE = MailSender.class.getName();

    //
    // Configure Component
    //

    /**
     * Set SMTP hostname to use for mail sending.
     */
    void setSmtpHost(String hostname);

    /**
     * Set SMTP hostname, username, and password to use for mail sending.
     */
    public void setSmtpHost(String hostname, String username, String password);

    //
    // Compose Message
    //

    /**
     * Set the <code>from</code> address of the message.
     * @param from The address the message appears to be from.
     */
    void setFrom(String from);

    /**
     * Sets the destination address(es) for the message. The address
     * is in the format, that
     * {@link javax.mail.internet.InternetAddress#parse(String)} can handle
     * (one or more email addresses separated by a commas).
     * @param to the destination address(es)
     * @see javax.mail.internet.InternetAddress#parse(String)
     */
    void setTo(String to);

    /**
     * Sets the reply-to address(es) for the message. The address
     * is in the format, that
     * {@link javax.mail.internet.InternetAddress#parse(String)} can handle
     * (one or more email addresses separated by a commas).
     * @param replyTo the address(es) that replies should be sent to
     * @see javax.mail.internet.InternetAddress#parse(String)
     */
    void setReplyTo(String replyTo);

    /**
     * Sets the address(es), which should receive a carbon copy of the
     * message. The address is in the format, that
     * {@link javax.mail.internet.InternetAddress#parse(String)} can handle
     * (one or more email addresses separated by a commas).
     * @param cc the address(es), which should receive a carbon copy.
     * @see javax.mail.internet.InternetAddress#parse(String)
     */
    void setCc(String cc);

    /**
     * Sets the address(es), which should receive a black carbon copy of
     * the message. The address is in the format, that
     * {@link javax.mail.internet.InternetAddress#parse(String)} can handle
     * (one or more email addresses separated by a commas).
     * @param bcc the address(es), which should receive a black carbon copy.
     * @see javax.mail.internet.InternetAddress#parse(String)
     */
    void setBcc(String bcc);

    /**
     * Sets the character set for encoding the message. This has no effect,
     * if any attachments are send in the message.
     * @param charset the character set to be used for enbcoding the message
     */
    void setCharset(String charset);

    /**
     * Sets the subject line of the message.
     * @param subject the subject line of the message
     */
    void setSubject(String subject);

    /**
     * Sets the body text of the email message.
     * If both a text body and a body read from a source are set,
     * only the latter will be used.
     * @param body The body text of the email message
     */
    void setBody(String body);

    /**
     * Sets the body source URL of the email message.
     * If both a text body and a body read from a source are set,
     * only the latter will be used.
     * @param src The body source URL of the email message
     */
    void setBodyFromSrc(String src);

    /**
     * Sets the optional body source Mime Type of the email message.
     * @param srcMimeType The optional body source Mime Type of the email message
     */
    void setBodyFromSrcMimeType(String srcMimeType);

    /**
     * Add an attachement to the message to be send. The attachment can
     * be of type <CODE>org.apache.excalibur.source.Source</CODE> or
     * {@link org.apache.cocoon.servlet.multipart.Part} or its
     * subclasses.
     * @param attachment to be send with the message
     * @see org.apache.excalibur.source.Source
     */
    void addAttachment(Object attachment);

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
    void addAttachment(Object attachment, String type, String name);

    /**
     * Add an attachement to the message to be send. The attachment can
     * be of type <CODE>org.apache.excalibur.source.Source</CODE> or
     * {@link org.apache.cocoon.servlet.multipart.Part} or its
     * subclasses.
     * @param url URL to attach to the message
     * @see org.apache.excalibur.source.Source
     */
    void addAttachmentURL(String url);

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
    void addAttachmentURL(String url, String type, String name);

    //
    // Send Message
    //

    /**
     * Assemble the message from the defined fields and send it. The source resolver
     * is obtained from the hosting component container.
     * @throws AddressException when problems with email addresses are found
     * @throws MessagingException when message could not be send.
     */
    void send()
    throws AddressException, MessagingException;

    /**
     * Assemble the message from the defined fields and send it.
     * @throws AddressException when problems with email addresses are found
     * @throws MessagingException when message could not be send.
     * @deprecated Since 2.1.5. Use {@link #send()} which doesn't require passing the source resolver
     */
    void send(SourceResolver resolver)
    throws AddressException, MessagingException;

    /**
     * Invokes the {@link #send()} method but catches any exception thrown. This
     * method is intended to be used from the sendmail logicsheet. The source
     * resolver is obtained from the hosting component container.
     * @return true when successful
     */
    boolean sendIt();

    /**
     * Invokes the {@link #send(SourceResolver)} method but catches any exception thrown.
     * This method is intended to be used from the sendmail logicsheet.
     * @return true when successful
     * @deprecated Since 2.1.5. Use {@link #sendIt()} which doesn't require passing the source resolver
     */
    boolean sendIt(SourceResolver resolver);

    /**
     * Accesses any Exception caught by {@link #sendIt(SourceResolver)}.
     * @return AddressException or MessagingException
     */
    Exception getException();
}
