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

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.apache.cocoon.environment.SourceResolver;

/**
 * A helper component used by the {@link org.apache.cocoon.acting.Sendmail}
 * action and the <code>sendmail.xsl</code> logicsheet for sending email messages.
 *
 * <p>Please note that this component is not (and can not) be
 * {@link org.apache.avalon.framework.thread.ThreadSafe}, so you need to lookup
 * new instance in each processing thread.
 *
 * @since 2.1.5
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
    void setSmtpHost(String hostname, String username, String password);

    //
    // Compose Message
    //

    /**
     * Set the <code>from</code> address of the message.
     *
     * @param from The address the message appears to be from.
     */
    void setFrom(String from);

    /**
     * Sets the destination address(es) for the message. The address
     * is in the format, that
     * {@link javax.mail.internet.InternetAddress#parse(String)} can handle
     * (one or more email addresses separated by a commas).
     *
     * @param to the destination address(es)
     * @see javax.mail.internet.InternetAddress#parse(String)
     */
    void setTo(String to);

    /**
     * Sets the reply-to address(es) for the message. The address
     * is in the format, that
     * {@link javax.mail.internet.InternetAddress#parse(String)} can handle
     * (one or more email addresses separated by a commas).
     *
     * @param replyTo the address(es) that replies should be sent to
     * @see javax.mail.internet.InternetAddress#parse(String)
     */
    void setReplyTo(String replyTo);

    /**
     * Sets the address(es), which should receive a carbon copy of the
     * message. The address is in the format, that
     * {@link javax.mail.internet.InternetAddress#parse(String)} can handle
     * (one or more email addresses separated by a commas).
     *
     * @param cc the address(es), which should receive a carbon copy.
     * @see javax.mail.internet.InternetAddress#parse(String)
     */
    void setCc(String cc);

    /**
     * Sets the address(es), which should receive a black carbon copy of
     * the message. The address is in the format, that
     * {@link javax.mail.internet.InternetAddress#parse(String)} can handle
     * (one or more email addresses separated by a commas).
     *
     * @param bcc the address(es), which should receive a black carbon copy.
     * @see javax.mail.internet.InternetAddress#parse(String)
     */
    void setBcc(String bcc);

    /**
     * Sets the subject line of the message.
     * @param subject the subject line of the message
     */
    void setSubject(String subject);

    /**
     * Sets the subject line of the message.
     * @param subject the subject line of the message
     * @param charset the character set to be used for encoding the subject
     */
    void setSubject(String subject, String charset);

    //
    // Set the Body
    //

    /**
     * Sets the character set for encoding the message. This has effect
     * only on text set via {@link #setBody(String)}.
     *
     * @param charset the character set to be used for encoding the message
     * @deprecated Since 2.1.10. Use {@link #setBody(Object, String)}
     */
    void setCharset(String charset);

    /**
     * Sets the body text of the email message.
     * If both a text body and a body read from a source are set,
     * only the latter will be used.
     *
     * @param body The body text of the email message
     * @deprecated Since 2.1.10. Use {@link #setBody(Object)}
     */
    void setBody(String body);

    /**
     * Sets the body source URL of the email message.
     * If both a text body and a body read from a source are set,
     * only the latter will be used.
     *
     * @param src The body source URL of the email message
     * @deprecated Since 2.1.10. Use {@link #setBodyURL(String)}
     */
    void setBodyFromSrc(String src);

    /**
     * Sets the optional body source Mime Type of the email message.
     *
     * @param srcMimeType The optional body source Mime Type of the email message
     * @deprecated Since 2.1.10. Use {@link #setBodyURL(String, String)}
     */
    void setBodyFromSrcMimeType(String srcMimeType);

    /**
     * Sets the body content of the email message.
     *
     * <p>The body can be any of: {@link org.apache.excalibur.source.Source},
     * {@link org.apache.cocoon.servlet.multipart.Part}, {@link java.io.InputStream},
     * <code>byte[]</code>, {@link String}, or a subclass.
     *
     * @param body The body text of the email message
     */
    void setBody(Object body);

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
    void setBody(Object body, String type);

    /**
     * Sets the body content of the email message.
     *
     * @param url URL to use as message body
     * @see org.apache.excalibur.source.Source
     */
    void setBodyURL(String url);

    /**
     * Sets the body content of the email message.
     *
     * @param url URL to use as message body
     * @param type mime type (optional)
     * @see org.apache.excalibur.source.Source
     */
    void setBodyURL(String url, String type);

    //
    // Add Attachments
    //

    /**
     * Add an attachement to the message to be send.
     *
     * <p>The attachment can be any of: {@link org.apache.excalibur.source.Source},
     * {@link org.apache.cocoon.servlet.multipart.Part}, {@link java.io.InputStream},
     * <code>byte[]</code>, {@link String}, or a subclass.
     *
     * @param attachment to be send with the message
     */
    void addAttachment(Object attachment);

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
    void addAttachment(Object attachment, String type, String name);

    /**
     * Add an attachement to the message to be send.
     *
     * @param url URL to attach to the message
     * @see org.apache.excalibur.source.Source
     */
    void addAttachmentURL(String url);

    /**
     * Add an attachement to the message to be send.
     *
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
