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
package org.apache.cocoon.mail.transformation;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.xml.transform.OutputKeys;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.mail.datasource.SourceDataSource;
import org.apache.cocoon.transformation.AbstractSAXTransformer;
import org.apache.excalibur.source.Source;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * The SendMailTransformer send mails with optional attachments using a SMTP
 * server and delivers furthermore a status report of each sent mail.
 *
 * <p>
 *  The SendMailTransformer requires the Sun's JavaMail API implementation. Please
 *  download and copy the following files to the Cocoon lib directory:
 *  <ul>
 *    <li><code>mail.jar</code> from
 *        <a href="http://java.sun.com/products/javamail/">http://java.sun.com/products/javamail/</a>
 *    </li>
 *    <li><code>activation.jar</code> from
 *        <a href="http://java.sun.com/products/javabeans/glasgow/jaf.html">http://java.sun.com/products/javabeans/glasgow/jaf.html</a>
 *    </li>
 * </p>
 *
 * <p>
 *  Use the following sitemap component declaration to define, configure and
 *  parameterize the transformer.
 *  in the <code>map:sitemap/map:components/map:transformers</code>:

 * <pre>
 *  &lt;map:transformer name=&quot;sendmail&quot; src=&quot;org.apache.cocoon.mail.transformation.SendMailTransformer&quot;&gt;
 *     &lt;smtphost&gt;smtp.foo.com&lt;/smtphost&gt;
 *     &lt;from&gt;sender@localhost&lt;/from&gt;
 *  &lt;/map:transformer&gt;
 *  </pre>
 *
 * where
 *  <ul>
 *   <li>
 *     &lt;smtphost&gt; is the SMTP server host name, e.g. smtp.foo.com
 *   </li>
 *   <li>
 *     &lt;from&gt; is the sender e-mail address
 *   </li>
 *  </ul>
 * </p>
 *
 * <p>
 *  Furthermore, these parameters can be defined in the sitemap pipeline section:
 *  <ul>
 *   <li>
 *     <b>smtphost</b>, <b>from</b> - If they are defined, this values overwrite
 *     the values from component section described above.
 *   </li>
 *   <li>
 *     <b>to</b> - email addresses of recipients
 *     e.g.: <code>&lt;map:parameter name="to" value="customer1@target.com,customer2@target.com"/&gt;</code>
 *   </li>
 *   <li>
 *     <b>subject</b> - a string, can also come from an input module
 *     e.g.; &lt;map:parameter name="subject" value="{request-param:subject}"/&gt;
 *   </li>
 *   <li>
 *     <b>body</b> - a string, can also come from an input module
 *     e.g.; &lt;map:parameter name="body" value="{request-param:body}"/&gt;
 *   </li>
 *   <li>
 *     <b>sendpartial</b> - a boolean, define how to send the mails. When mail is being send
 *       to more than one recipient and the parameter is set to false, then all email addresses
 *       will appear concatenated in the address field at the mail client of the recipient.
 *       The default is true.
 *   </li>
 *  </ul>
 * </p>
 *
 * <p>
 *   More configurations can be made in a specific configuration file, which
 *   can be retrieved with a
 *   <a href="http://cocoon.apache.org/2.1/userdocs/generators/generators.html">generator</a> as
 *   the input document. The input document should have the following configuration entities:
 *  <ul>
 *   <li>
 *     <b>&lt;email:smtphost&gt;</b>, <b>&lt;email:from&gt;</b> and
 *     <b>&lt;email:subject&gt;</b> can be set to overwrite values from the
 *     sitemap
 *   </li>
 *   <li>
 *     <b>&lt;email:to&gt;</b> - each entry will be append to the list of
 *     email addresses
 *   </li>
 *   <li>
 *     <b>&lt;email:body&gt;</b> - Overwrites the value from the sitemap.
 *     If there is a <b>src</b> attribute, the transformer will try to retrieve
 *     the file and place it instead of a text-string as the mail body.
 *   </li>
 *   <li>
 *     <b>&lt;email:attachment&gt;</b> - each entry defines a attachment.
 *     The attribute <b>name</b> defines the name of the attachment. The <b>mime-type</b> attribute
 *     defines the content of the attachment.
 *     If there is a nested &lt;email:content&gt; - element, text can be included and the
 *     attachment will then be a plain text-file.
 *     Is there a <b>url</b> attribute, the transformer tries to retrieve the
 *     appropriate file and handle it as an attachment.
 *            To use a file as an attachment, retrieved over a protocol like http or
 *     cocoon, use the <b>src</b> attribute.
 *   </li>
 *  </ul>
 * </p>
 *
 * <p>
 *  Input document sample:
 *  <pre>
 *   &lt;?xml version="1.0" encoding="UTF-8"?&gt;
 *   &lt;document xmlns:email="http://apache.org/cocoon/transformation/sendmail"&gt;
 *     &lt;email:sendmail&gt;
 *       &lt;email:smtphost&gt;hostname.company.com&lt;/email:smtphost&gt;
 *       &lt;email:from&gt;info@company.com&lt;/email:from&gt;
 *       &lt;email:to&gt;customer3@target.com&lt;/email:to&gt;
 *       &lt;email:to&gt;customer4@target.com&lt;/email:to&gt;
 *       &lt;email:to&gt;customer5@target.com&lt;/email:to&gt;
 *       &lt;email:to&gt;customer6@target.com&lt;/email:to&gt;
 *       &lt;email:subject&gt;subject-content&lt;/email:subject&gt;
 *       &lt;email:body src="cocoon:/softwareupdate.html?locale=en&amp;country=UK"/&gt;
 *       &lt;!-- &lt;email:body&gt;some Text&lt;/email:body&gt; --&gt;
 *       &lt;email:attachment name="hello.html" mime-type="text/html"&gt;
 *         &lt;email:content&gt;
 *           Dear Customer, please visit out new Product-Shop.
 *         &lt;/email:content&gt;
 *       &lt;/email:attachment&gt;
 *       &lt;email:attachment name="hello2.html" mime-type="text/html" src="cocoon:/src1"/&gt;
 *       &lt;email:attachment name="hello3.html" mime-type="text/html"
 *          url="C:\path\softwareupdate.html"/&gt;
 *       &lt;email:attachment name="hello.gif" mime-type="image/gif"
 *          url="c:\path\powered.gif"/&gt;
 *     &lt;/email:sendmail&gt;
 *   &lt;/document&gt;
 *   </pre>
 * </p>
 *
 * <p>
 *   After the transformation a report will be generated, where the state for each sent mail can be seen.
 *   In case of an exception, the exception-message and a stacktrace will be reported.
 * </p>
 *
 * <p>
 *   <b style="color: red;">FIXME: Known Issues:</b>
 *   <ul>
 *     <li>Refactor to use MailSender component</li>
 *     <li>No support for <a href="http://www.ietf.org/rfc/rfc2554.txt">RFC 2554:
 *     SMTP Service Extension for Authentication</a></li>
 *     <li>No support for different mail servers, first one will always be used</li>
 *   </ul>
 * </p>
 *
 * @author <a href="mailto:pklassen@s-und-n.de">Peter Klassen</a>
 * @version $Id$
 */
public class SendMailTransformer extends AbstractSAXTransformer {

    /*
     * constants, related to elements in configuration-file
     */
    public static final String NAMESPACE                  = "http://apache.org/cocoon/transformation/sendmail";
    public static final String ELEMENT_SENDMAIL           = "sendmail";
    public static final String ELEMENT_SMTPHOST           = "smtphost";
    public static final String ELEMENT_SMTPPORT           = "smtpport";
    public static final String ELEMENT_MAILFROM           = "from";
    public static final String ELEMENT_MAILTO             = "to";
    public static final String ELEMENT_REPLYTO            = "reply-to";
    public static final String ELEMENT_MAILSUBJECT        = "subject";
    public static final String ELEMENT_MAILBODY           = "body";
    public static final String ELEMENT_ATTACHMENT         = "attachment";
    public static final String ELEMENT_ATTACHMENT_CONTENT = "content";
    public static final String ELEMENT_EMAIL_PREFIX       = "email";
    public static final String ELEMENT_ERROR              = "error";
    public static final String ELEMENT_SUCCESS            = "success";
    public static final String ELEMENT_FAILURE            = "failure";
    public static final String ELEMENT_RESULT             = "result";

    public static final String DEFAULT_BODY_MIMETYPE      = "text/html";

    /*
     * mode-constants
     */
    protected static final int MODE_NONE               = 0;
    protected static final int MODE_SMTPHOST           = 1;
    protected static final int MODE_FROM               = 2;
    protected static final int MODE_TO                 = 3;
    protected static final int MODE_SUBJECT            = 4;
    protected static final int MODE_BODY               = 5;
    protected static final int MODE_ATTACHMENT         = 6;
    protected static final int MODE_ATTACHMENT_CONTENT = 7;
    protected static final int MODE_REPLY_TO           = 8;
    protected static final int MODE_SMTPPORT           = 9;

    /*
     * constants, related to parameter from request
     */
    public final static String PARAM_SMTPHOST    = "smtphost";
    public final static String PARAM_SMTPPORT    = "smtpport";
    public final static String PARAM_FROM        = "from";
    public final static String PARAM_TO          = "to";
    public final static String PARAM_REPLY_TO    = "reply-to";
    public final static String PARAM_SUBJECT     = "subject";
    public final static String PARAM_BODY        = "body";
    public final static String PARAM_SENDPARTIAL = "sendpartial";
    protected int              mode;

    /*
     * communication parameters, which will be used to send mails
     */
    protected List                 toAddresses;
    protected List                 replyToAddresses;
    protected List                 defaultToAddresses;
    protected List                 defaultReplyToAddresses;
    protected List                 attachments;
    protected String               subject;
    protected String               body;
    protected String               bodyURI;
    protected String               bodyMimeType;
    protected String               mailHost;
    protected int                  mailPort;
    protected String               fromAddress;
    protected AttachmentDescriptor attachmentDescriptor;
    protected int                  port;
    protected String               contextPath;
    protected boolean              sendPartial;
    protected Message              smtpMessage;

    protected String defaultSmtpHost;
    protected int defaultSmtpPort;
    protected String defaultFromAddress;

    protected List    usedSources = new ArrayList();

    /**
     * create a new Transformer
     */
    public SendMailTransformer() {
        this.defaultNamespaceURI = NAMESPACE;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration configuration)
    throws ConfigurationException {
        super.configure(configuration);
        this.defaultSmtpHost = configuration.getChild("smtphost").getValue("");
        this.defaultSmtpPort = configuration.getChild("smtpport").getValueAsInteger(25);
        this.defaultFromAddress = configuration.getChild("from").getValue("");
    }

    /**
     * invoked every time when the transformer is triggered by the pipeline
     */
    public void setup(SourceResolver resolver, Map objectModel, String src,
                      Parameters par)
   throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, src, par);

        this.mailHost    = par.getParameter(PARAM_SMTPHOST, this.defaultSmtpHost);
        this.mailPort    = par.getParameterAsInteger(PARAM_SMTPPORT, this.defaultSmtpPort);
        this.fromAddress = par.getParameter(PARAM_FROM, this.defaultFromAddress);
        this.port        = this.request.getServerPort();
        this.contextPath = this.request.getContextPath();
        this.sendPartial = par.getParameterAsBoolean(PARAM_SENDPARTIAL, true);

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Using host " + mailHost + " on port " + mailPort +  ", from address " + fromAddress);
        }

        this.attachments = new ArrayList();
        this.defaultToAddresses = new ArrayList();
        appendToAddress(this.defaultToAddresses, par.getParameter(PARAM_TO, ""));
        this.defaultReplyToAddresses = new ArrayList();
        appendToAddress(this.defaultReplyToAddresses, par.getParameter(PARAM_REPLY_TO, ""));

  	    this.subject = par.getParameter(PARAM_SUBJECT, null);
   	    this.body = par.getParameter(PARAM_BODY, null);
	}

    /* (non-Javadoc)
     * @see org.apache.cocoon.transformation.AbstractSAXTransformer#startTransformingElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startTransformingElement(String uri, String name, String raw,
                                         Attributes attr)
    throws SAXException {
        if (name.equals(ELEMENT_SENDMAIL)) {
            // Clean from possible previous usage
            this.toAddresses = new ArrayList(this.defaultToAddresses);
            this.replyToAddresses = new ArrayList(this.defaultReplyToAddresses);
            this.attachments.clear();
        } else if (name.equals(ELEMENT_SMTPHOST)) {
            startTextRecording();
            this.mode = MODE_SMTPHOST;
        } else if (name.equals(ELEMENT_SMTPPORT)) {
            this.startTextRecording();
            this.mode = MODE_SMTPPORT;
        } else if (name.equals(ELEMENT_MAILFROM)) {
            startTextRecording();
            this.mode = MODE_FROM;
        } else if (name.equals(ELEMENT_MAILTO)) {
            startTextRecording();
            this.mode = MODE_TO;
        } else if (name.equals(ELEMENT_REPLYTO)) {
            startTextRecording();
            this.mode = MODE_REPLY_TO;
        } else if (name.equals(ELEMENT_MAILSUBJECT)) {
            startTextRecording();
            this.mode = MODE_SUBJECT;
        } else if (name.equals(ELEMENT_MAILBODY)) {
            String strBody = attr.getValue("src");
            if (strBody != null) {
                this.bodyURI = strBody;
            }

            String mType = attr.getValue("mime-type");
            if (mType != null) {
                this.bodyMimeType = mType;
            } else {
                this.bodyMimeType = DEFAULT_BODY_MIMETYPE;
            }

            Properties outputProperties = new Properties();
            if (this.bodyMimeType.equals("text/plain"))
            	outputProperties.put(OutputKeys.METHOD, "text");
            else if (this.bodyMimeType.equals("text/html"))
            	outputProperties.put(OutputKeys.METHOD, "html");
            startSerializedXMLRecording(outputProperties);
            this.mode = MODE_BODY;
        } else if (name.equals(ELEMENT_ATTACHMENT)) {
            this.attachmentDescriptor = new AttachmentDescriptor(attr.getValue("name"),
                                                                 attr.getValue("mime-type"),
                                                                 attr.getValue("src"),
                                                                 attr.getValue("url"));
            this.mode = MODE_ATTACHMENT;
        } else if (name.equals(ELEMENT_ATTACHMENT_CONTENT)) {
            startSerializedXMLRecording(new Properties());
            this.mode = MODE_ATTACHMENT_CONTENT;
        } else {
            throw new SAXException("Unknown element <" + name + ">");
        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.transformation.AbstractSAXTransformer#endTransformingElement(java.lang.String, java.lang.String, java.lang.String)
     */
    public void endTransformingElement(String uri, String name, String raw)
                                throws SAXException, ProcessingException {
        if (name.equals(ELEMENT_SENDMAIL)) {
            if (getLogger().isInfoEnabled()) {
                getLogger().info("Mail Subject: " + this.subject + "\n" +
                                 "Body: " + this.body);
            }
            sendMail();
        } else if (name.equals(ELEMENT_SMTPHOST) ) {
            this.mailHost = endTextRecording();
            this.mode     = MODE_NONE;
        } else if (name.equals(ELEMENT_SMTPPORT) ) {
            this.mailPort = Integer.parseInt(this.endTextRecording());
            this.mode     = MODE_NONE;
        } else if (name.equals(ELEMENT_MAILFROM)) {
            this.fromAddress = endTextRecording();
            this.mode        = MODE_NONE;
        } else if (name.equals(ELEMENT_MAILTO)) {
            this.toAddresses.add(endTextRecording());
            this.mode = MODE_NONE;
        } else if (name.equals(ELEMENT_REPLYTO)) {
            this.replyToAddresses.add(endTextRecording());
            this.mode = MODE_NONE;
        } else if (name.equals(ELEMENT_MAILSUBJECT)) {
            String strSubject = endTextRecording();
            if (strSubject != null) {
                this.subject = strSubject;
            } else {
                getLogger().debug("Mail: No Subject");
            }
            this.mode = MODE_NONE;
        } else if (name.equals(ELEMENT_ATTACHMENT)) {
            this.attachments.add(this.attachmentDescriptor.copy());
            this.attachmentDescriptor = null;
            this.mode                 = MODE_NONE;
        } else if (name.equals(ELEMENT_ATTACHMENT_CONTENT)) {
            this.attachmentDescriptor.setContent(endSerializedXMLRecording());
            this.mode = MODE_NONE;
        } else if (name.equals(ELEMENT_MAILBODY)) {
            String strB = null;
            try {
                strB = endSerializedXMLRecording();
            } catch (Exception e) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Mail: No Body as String in config-file available");
                }
            }
            if (strB != null) {
                this.body = strB;
            }
            this.mode = MODE_NONE;
        } else {
            throw new SAXException("Unknown element <" + name + ">");
        }
    }

    private static void appendToAddress(List addresses, String s) {
        StringTokenizer t = new StringTokenizer(s.trim(), ";");
        while (t.hasMoreElements()) {
            addresses.add(t.nextToken());
        }
    }

    /**
     *
     */
    private void sendMail() throws SAXException {
        try {
        	this.ignoreHooksCount++;
            super.sendStartElementEventNS(ELEMENT_RESULT);
            Properties props = new Properties();
            props.put("mail.smtp.host", this.mailHost);
            props.put("mail.smtp.port", String.valueOf(this.mailPort));

            if (this.subject == null) {
		        this.ignoreHooksCount++;
                super.sendStartElementEventNS(ELEMENT_ERROR);
                super.sendTextEvent("Subject not available - sending mail aborted");
                super.sendEndElementEventNS(ELEMENT_ERROR);
		        this.ignoreHooksCount--;
                return;
            }

            if (this.body == null && this.bodyURI == null) {
		        this.ignoreHooksCount++;
                super.sendStartElementEventNS(ELEMENT_ERROR);
                super.sendTextEvent("Mailbody not available - sending mail aborted");
                super.sendEndElementEventNS(ELEMENT_ERROR);
		        this.ignoreHooksCount--;
                return;
            }

            Session session = Session.getDefaultInstance(props, null);
            Transport trans = session.getTransport("smtp");
            trans.connect();

            this.smtpMessage = setUpMessage(session);

            if (this.sendPartial) {
                for (int i = 0; i < this.toAddresses.size(); i++) {
                    List v = new ArrayList(1);
                    v.add(this.toAddresses.get(i));
                    sendMail(v, trans);
                }
            } else {
                sendMail(this.toAddresses, trans);
            }

            trans.close();
        } catch (SAXException e){
        	throw e;
        } catch (Exception e) {
            getLogger().error("Exception sending mail", e);
            sendExceptionElement(e);
        }
        finally {
        	super.sendEndElementEventNS(ELEMENT_RESULT);
            this.ignoreHooksCount--;
        }
    }

    /**
     * @see <a href="http://java.sun.com/products/javamail/1.3/docs/javadocs/com/sun/mail/smtp/package-summary.html">Sun Javamail Javadoc</a>
     * @throws Exception
     */
    private void sendMail(List newAddresses, Transport trans)
                   throws Exception {
        AddressHandler[] iA = new AddressHandler[newAddresses.size()];

        try {
        	for (int i = 0; i < newAddresses.size(); i++) {
                InternetAddress inA = new InternetAddress((String) newAddresses.get(i));
                iA[i] = new AddressHandler(inA);
            }
        	InternetAddress[] iaArr = SendMailTransformer.getAddresses(iA);
            this.smtpMessage.setRecipients(Message.RecipientType.TO, iaArr);
            trans.sendMessage(this.smtpMessage, iaArr);
        } catch (SendFailedException e) {
            getLogger().error("Exception during sending of mail", e);

            Address[] adr = e.getInvalidAddresses();
            for (int isfEx = 0; isfEx < iA.length; isfEx++) {
                String tmpAddress = iA[isfEx].getAddress().getAddress();
                for (int sei = 0; sei < adr.length; sei++) {
                    if (((InternetAddress) adr[sei]).getAddress()
                             .equalsIgnoreCase(tmpAddress)) {
                        iA[isfEx].setSendMailResult("Invalid address");
                    }
                }
            }

            Address[] ad = e.getValidUnsentAddresses();
            for (int isfEx = 0; isfEx < iA.length; isfEx++) {
                String tmpAddress = iA[isfEx].getAddress().getAddress();
                for (int sei = 0; sei < ad.length; sei++) {
                    if (((InternetAddress) ad[sei]).getAddress()
                             .equalsIgnoreCase(tmpAddress)) {
                        iA[isfEx].setSendMailResult("Recipient not found");
                    }
                }
            }
        } catch (Exception e) {
            getLogger().error("Exception sending mail", e);
            sendExceptionElement(e);
            return;
        }

        generateSAXReportStatements(iA);
    }

    private Message setUpMessage(Session session) throws Exception {
        Message sm = new MimeMessage(session);

        //sm.setAllow8bitMIME(true);
        Address[] replyTo = new Address[this.replyToAddresses.size()];
        for (int i = 0 ; i < this.replyToAddresses.size(); i++) {
            replyTo[i] = new InternetAddress((String) this.replyToAddresses.get(i));
        }
        sm.setReplyTo(replyTo);
        sm.setFrom(new InternetAddress(this.fromAddress));
        sm.setSubject(this.subject);

        // process mail-body
        BodyPart messageBodyPart = new MimeBodyPart();

        // decide, if to take content from source or plain text
        // from variable to build mailbody
        if (this.bodyURI != null) {
            Source      inSrc   = resolver.resolveURI(this.bodyURI);
            this.usedSources.add(inSrc);
            InputStream inStr   = inSrc.getInputStream();
            byte[]      byteArr = new byte[inStr.available()];
            inStr.read(byteArr);

            String mailBody = new String(byteArr);
            messageBodyPart.setContent(mailBody, this.bodyMimeType);
        } else {
            messageBodyPart.setContent(this.body, this.bodyMimeType);
        }

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);

        // process attachments
        Iterator i = this.attachments.iterator();
        while (i.hasNext()) {
            AttachmentDescriptor aD = (AttachmentDescriptor) i.next();
            messageBodyPart = new MimeBodyPart();

            if (!aD.isTextContent()) {
                Source inputSource = resolver.resolveURI(aD.isURLSource() ? aD.strAttrSrc : aD.strAttrFile);
                this.usedSources.add(inputSource);

                DataSource dataSource = new SourceDataSource(inputSource, aD.strAttrMimeType, aD.strAttrName);
                ((SourceDataSource) dataSource).enableLogging(getLogger());

                messageBodyPart.setDataHandler(new DataHandler(dataSource));
            } else {
                messageBodyPart.setContent(aD.strContent, aD.strAttrMimeType);
            }

            messageBodyPart.setFileName(aD.strAttrName);
            multipart.addBodyPart(messageBodyPart);
        }

        sm.setContent(multipart);

        //sm.setReturnOption(SMTPMessage.RETURN_FULL);
        sm.saveChanges();

        return sm;
    }

    private void generateSAXReportStatements(AddressHandler[] addressArr)
                                      throws SAXException {
        AttributesImpl impl = new AttributesImpl();

        for (int i = 0; i < addressArr.length; i++) {
            String tmpAddress = addressArr[i].getAddress().getAddress();

            if (addressArr[i].getSendMailResult() == null) {
                impl.addAttribute("", "to", "to",
                                  "CDATA", tmpAddress);
                super.sendStartElementEventNS(ELEMENT_SUCCESS, impl);
                super.sendTextEvent("Mail sent");
                super.sendEndElementEventNS(ELEMENT_SUCCESS);
            } else {
                impl.addAttribute("", "to", "to",
                                  "CDATA", tmpAddress);
                super.sendStartElementEventNS(ELEMENT_FAILURE, impl);
                super.sendTextEvent(addressArr[i].getSendMailResult());
                super.sendEndElementEventNS(ELEMENT_FAILURE);
            }
        }
    }

    private void sendExceptionElement(Exception ex) {
        try {
            this.ignoreHooksCount++;
            super.sendStartElementEventNS("exception");
            super.sendStartElementEventNS("message");
            super.sendTextEvent(ex.getMessage());
            super.sendEndElementEventNS("message");

            /* only with jdk 1.4
            super.sendStartElementEvent("email:stacktrace");

            for (int i = 0; i < ex.getStackTrace().length; i++) {
                String s = ((StackTraceElement) ex.getStackTrace()[i]).toString();
                super.sendTextEvent(s + "\n");
            }

            super.sendEndElementEvent("email:stacktrace");*/

            super.sendEndElementEventNS("exception");
            this.ignoreHooksCount--;
        } catch (SAXException e) {
            getLogger().error("Error while sending a SAX-Event", e);
        }
    }

    public static InternetAddress[] getAddresses(AddressHandler[] handlerArr) {
        InternetAddress[] iaArr = new InternetAddress[handlerArr.length];

        for (int i = 0; i < handlerArr.length; i++) {
            iaArr[i] = handlerArr[i].getAddress();
        }

        return iaArr;
    }

	/**
	 * @see org.apache.avalon.excalibur.pool.Recyclable#recycle()
	 */
	public void recycle() {
        this.toAddresses = null;
        this.defaultToAddresses = null;
	    this.attachments = null;
	    this.subject = null;
	    this.body = null;
	    this.bodyURI = null;
	    this.mailHost = null;
        this.mailPort = 0;
        this.fromAddress = null;
	    this.attachmentDescriptor = null;
	    this.port = 0;
	    this.contextPath = null;
	    this.sendPartial = true;
	    this.smtpMessage = null;
        final Iterator i = this.usedSources.iterator();
        while ( i.hasNext() ) {
            final Source source = (Source)i.next();
            this.resolver.release(source);
        }
        this.usedSources.clear();
 	    super.recycle();
	}

    static class AttachmentDescriptor {
        String       strAttrName;
        String       strAttrMimeType;
        String       strAttrSrc;
        String       strAttrFile;
        String       strContent;

        protected AttachmentDescriptor(String newAttrName,
                                       String newAttrMimeType,
                                       String newAttrSrc, String newAttrFile) {
            this.strAttrName     = newAttrName;
            this.strAttrMimeType = newAttrMimeType;
            this.strAttrSrc      = newAttrSrc;
            this.strAttrFile     = newAttrFile;
        }

        protected void setContent(String newContent) {
            this.strContent = newContent;
        }

        protected AttachmentDescriptor copy() {
            AttachmentDescriptor aD = new AttachmentDescriptor(this.strAttrName,
                                                               this.strAttrMimeType,
                                                               this.strAttrSrc,
                                                               this.strAttrFile);
            aD.setContent(this.strContent);

            return aD;
        }

        protected boolean isURLSource() {
            return (this.strAttrSrc != null);
        }

        protected boolean isFileSource() {
            return (this.strAttrFile != null);
        }

        protected boolean isTextContent() {
            return (this.strContent != null);
        }
    }

    static class AddressHandler {
        private InternetAddress address;
        private String          sendMailResult;

        protected AddressHandler(InternetAddress newAddress) {
            this.address = newAddress;
        }

        protected void setSendMailResult(String newSendMailResult) {
            this.sendMailResult = newSendMailResult;
        }

        /**
         * @return mail-address
         */
        public InternetAddress getAddress() {
            return address;
        }

        /**
         * @return sendMailResult as String
         */
        public String getSendMailResult() {
            return sendMailResult;
        }
    }
}
