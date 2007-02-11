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
package org.apache.cocoon.mail.transformation;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.URLDataSource;
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

import org.apache.avalon.excalibur.pool.Recyclable;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.transformation.AbstractSAXTransformer;
import org.apache.excalibur.source.Source;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;


/**
 * The SendMailTransformer send mails using SMTP including attachments with
 * the specific parameters read from a configuration-file and delivers
 * furthermore a report, where a state to each sent mail can bee seen.
 *
 * <p>
 *  The SendMailTransformer requires classes of the mail api from sun which comes
 *  with the mail.jar and activation.jar. 
 *  In order to use the SendMailtransformer, you have to download the mail.jar 
 *  from http://java.sun.com/products/javamail/ also like the activation.jar 
 *  from http://java.sun.com/products/javabeans/glasgow/jaf.html 
 *  and install it in the Cocoon lib directory.
 * </p>
 * <p>
 *    These two parameters can be defined already in the components-secion of the sitemap:
 *  <ul>
 *   <li>
 *     smtphost - the name of the host, e.g. "smtphost.company.com"
 *   </li>
 *   <li>
 *     from - email-adress of mail sender
 *   </li>
 *  </ul>
 * </p>
 * <p>
 * Furthermore, these parameters can be defined in the pipeline-section:
 *  <ul>
 *   <li>
 *     smtphost, from - If these values are available in the pipeline-section,
 *     values from the component-section will be overwritten
 *   </li>
 *   <li>
 *     to - email-addresses of recipients
 *     e.g.: name="to" value="customer1@target.com,customer2@target.com"
 *   </li>
 *   <li>
 *     subject - a string-value, can also come from a request-parameter
 *     e.g.; name="subject" value="{request-param:subject}"
 *   </li>
 *   <li>
 *     body - a string-value, can also come from a request-parameter
 *     e.g.; name="body" value="{request-param:body}"
 *   </li>
 *   <li>
 *     sendpartial - define, if to send the mails partial or together; the value can
 *     be true or false. The default is true.
 *     ( Note: if you send your mail to more than one recipient and you configure
 *       this parameter with false, all email-addresses will appear concatenated
 *       in the address-field at the mail-client of the recipient )
 *   </li>
 *  </ul>
 * </p>
 * <p>
 *   More configurations can be made in a specific configuration-file, which
 *   can be retrieved with a file-generator as an input document.
 * </p>
 * <p>
 *  The input document should define the following configuration entities:
 *  <ul>
 *   <li>
 *     email:smtphost, email:from and email:subject can be set to overwrite
 *     values from the sitemap
 *   </li>
 *   <li>
 *     email:to - each entry will be append to the list of email-adresses
 *   </li>
 *   <li>
 *     email:body - will overwrite the value from the sitemap
 *     if there is a "src"-attribute, the transformer will try to retrieve
 *     the file and place it instead of a text-string as the mail-body
 *   </li>
 *   <li>
 *     email:attachment - define the attachement
 *     The attribute name defines the name of the attachement. The mime-type
 *     defines the content of the attachment.
 *     If there is a nested <email:content> - tag, text can be included and the
 *     attachement will then be a plain text-file.
 *     Is there a url-attribute, the SendMailTransformer tries to retrieve the
 *     appropriate file and handle it as an attachment.
 *            To use a file as an attachment, retrieved over a protocol like http or
 *     cocoon, use the src-attribute.
 *   </li>
 *  </ul>
 * </p>
 * <p>
 *  Example:
 *
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
 *       &lt;email:attachment name="hello.gif" mime- type="image/gif"
 *          url="c:\path\powered.gif"/&gt;
 *     &lt;/email:sendmail&gt;
 *   &lt;/document&gt;
 * </p>
 * <p>
 *   After the transformation a report will be generated, where the state for each sent mail can be seen.
 *   In case of an exception, the exception-message and a stacktrace will be reported.
 * </p>
 *
 * @author <a href="mailto:pklassen@s-und-n.de">Peter Klassen</a>
 * @version CVS $Id: SendMailTransformer.java,v 1.10 2004/03/06 05:09:57 joerg Exp $
 *
 */
public class SendMailTransformer extends AbstractSAXTransformer
    implements Parameterizable, Recyclable {
    /*
     * constants, related to elements in configuration-file
     */
    public static final String NAMESPACE                  = "http://apache.org/cocoon/transformation/sendmail";
    public static final String ELEMENT_SENDMAIL           = "sendmail";
    public static final String ELEMENT_SMTPHOST           = "smtphost";
    public static final String ELEMENT_MAILFROM           = "from";
    public static final String ELEMENT_MAILTO             = "to";
    public static final String ELEMENT_MAILSUBJECT        = "subject";
    public static final String ELEMENT_MAILBODY           = "body";
    public static final String ELEMENT_ATTACHMENT         = "attachment";
    public static final String ELEMENT_ATTACHMENT_CONTENT = "content";
    public static final String ELEMENT_EMAIL_PREFIX       = "email";
    public static final String ELEMENT_ERROR              = "error";
    public static final String ELEMENT_SUCCESS            = "success";
    public static final String ELEMENT_FAILURE            = "failure";
    public static final String ELEMENT_RESULT              = "result";

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

    /*
     * constants, related to parameter from request
     */
    public final static String PARAM_SMTPHOST    = "smtphost";
    public final static String PARAM_FROM        = "from";
    public final static String PARAM_TO          = "to";
    public final static String PARAM_SUBJECT     = "subject";
    public final static String PARAM_BODY        = "body";
    public final static String PARAM_SENDPARTIAL = "sendpartial";
    protected int              mode;

    /*
     * communication parameters, which will be used to send mails
     */
    protected Vector               toAddresses;
    protected Vector               attachments;
    protected StringBuffer         subject;
    protected StringBuffer         body;
    protected String               bodyURI;
    protected String               mailHost;
    protected String               fromAddress;
    protected AttachmentDescriptor attachmentDescriptor;
    protected int                  port;
    protected String               contextPath;
    protected boolean              sendPartial;
    protected Message              smtpMessage = null;

    /**
     * create a new Transformer
     */
    public SendMailTransformer() {
        this.defaultNamespaceURI = NAMESPACE;
    }

    /**
     * invoked every time when the transformer is triggered by the pipeline
     */
    public void setup(SourceResolver resolver, Map objectModel, String src,
                      Parameters par)
               throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, src, par);
        this.mailHost    = par.getParameter(PARAM_SMTPHOST, "");
        this.fromAddress = par.getParameter(PARAM_FROM, "");
        this.port        = this.request.getServerPort();
        this.contextPath = this.request.getContextPath();
        this.sendPartial = par.getParameterAsBoolean(PARAM_SENDPARTIAL, true);

        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("overwritten by Transformer-Parameters in Pipeline");
            this.getLogger().debug("overwritten Parameters=" + mailHost + ":" +
                                   fromAddress);
        }

        String s = par.getParameter(PARAM_TO, "");
        this.toAddresses = new Vector();
        this.attachments = new Vector();
        this.appendToAddress(s, ";");

	try {
	    this.subject = new StringBuffer(par.getParameter(PARAM_SUBJECT));
	} catch (ParameterException pe) {
	    this.getLogger().debug("Parameter <subject> not set."); 
	} try {
	    this.body    = new StringBuffer(par.getParameter(PARAM_BODY));
	} catch (ParameterException pe) {
	    this.getLogger().debug("Parameter <body> not set."); 
	}				    

        this.defaultNamespaceURI = NAMESPACE;
    }

    /**
     * invoked initially
     */
    public void parameterize(Parameters parameters) throws ParameterException {
        // nothing to do by now
    }

    /**
     * overwritten method of AbstractSAXTransformer
     */
    public void startTransformingElement(String uri, String name, String raw,
                                         Attributes attr)
                                  throws SAXException {
        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("BEGIN startElement uri=" + uri + ", name=" +
                                   name + ", raw=" + raw + ", attr=" + attr);
        }

        if (name.equals(ELEMENT_SENDMAIL) == true) {
            // Clean from possible previous usage
            this.toAddresses.clear();
            this.attachments.clear();
        } else if (name.equals(ELEMENT_SMTPHOST) == true) {
            this.startTextRecording();
            this.mode = MODE_SMTPHOST;
        } else if (name.equals(ELEMENT_MAILFROM) == true) {
            //this.fromAddress = attr.getValue(ELEMENT_MAILFROM);
            this.startTextRecording();
            this.mode = MODE_FROM;
        } else if (name.equals(ELEMENT_MAILTO) == true) {
            this.startTextRecording();
            this.mode = MODE_TO;
        } else if (name.equals(ELEMENT_MAILSUBJECT) == true) {
            this.startTextRecording();
            this.mode = MODE_SUBJECT;
        } else if (name.equals(ELEMENT_MAILBODY) == true) {
            String strBody = attr.getValue("src");

            if (strBody != null) {
                this.bodyURI = new String(strBody);
            }

            this.startTextRecording();
            this.mode = MODE_BODY;
        } else if (name.equals(ELEMENT_ATTACHMENT) == true) {
            this.attachmentDescriptor = new AttachmentDescriptor(attr.getValue("name"),
                                                                 attr.getValue("mime-type"),
                                                                 attr.getValue("src"),
                                                                 attr.getValue("url"));
            this.mode = MODE_ATTACHMENT;
        } else if (name.equals(ELEMENT_ATTACHMENT_CONTENT) == true) {
            this.startSerializedXMLRecording(new Properties());
            this.mode = MODE_ATTACHMENT_CONTENT;
        } else {
            throw new SAXException("Unknown element " + name);
        }

        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("END startElement");
        }
    }

    /**
     * overwritten method of AbstractSAXTransformer
     */
    public void endTransformingElement(String uri, String name, String raw)
                                throws SAXException, ProcessingException {
        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("BEGIN endTransformingElement uri=" + uri +
                                   ", name=" + name + ", raw=" + raw);
        }

        if (name.equals(ELEMENT_SENDMAIL) == true) {
            if (this.getLogger().isInfoEnabled() == true) {
                this.getLogger().info("Mail contents- Subject: " +
                                      this.subject + "\n" + "Body: " +
                                      this.body);
            }

            sendMail();
        } else if (name.equals(ELEMENT_SMTPHOST) == true) {
            this.mailHost = this.endTextRecording();
            this.mode     = MODE_NONE;
        } else if (name.equals(ELEMENT_MAILFROM) == true) {
            this.fromAddress = this.endTextRecording();
            this.mode        = MODE_NONE;
        } else if (name.equals(ELEMENT_MAILTO) == true) {
            this.toAddresses.add(this.endTextRecording());
            this.mode = MODE_NONE;
        } else if (name.equals(ELEMENT_MAILSUBJECT) == true) {
            String strSubject = this.endTextRecording();

            if (strSubject != null) {
                this.subject = new StringBuffer(strSubject);
            } else {
                this.getLogger().debug("Mail-Subject not available");
            }

            this.mode = MODE_NONE;
        } else if (name.equals(ELEMENT_ATTACHMENT) == true) {
            this.attachments.add(this.attachmentDescriptor.copy());
            this.attachmentDescriptor = null;
            this.mode                 = MODE_NONE;
        } else if (name.equals(ELEMENT_ATTACHMENT_CONTENT) == true) {
            this.attachmentDescriptor.setContent(new StringBuffer(this.endSerializedXMLRecording()));
            this.mode = MODE_NONE;
        } else if (name.equals(ELEMENT_MAILBODY) == true) {
            String strB = null;

            try {
                strB = new String(this.endTextRecording());
            } catch (Exception e) {
                if (this.getLogger().isDebugEnabled()) {
                    this.getLogger().debug("No Body as String in config-file available");
                }
            }

            if (strB != null) {
                this.body = new StringBuffer(strB);
            }

            this.mode = MODE_NONE;
        } else {
            throw new SAXException("Unknown element " + name);
        }

        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("END endElement");
        }
    }

    private void appendToAddress(String s, String delim) {
        StringTokenizer t = null;
        t = new StringTokenizer(s.trim(), delim);

        while (t.hasMoreElements()) {
            this.toAddresses.add(t.nextToken());
        }
    }

    /**
     *
     * @throws SAXException
     */
    private void sendMail() throws SAXException {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", this.mailHost);

            if (this.subject == null) {
		        this.ignoreHooksCount++;
                super.sendStartElementEventNS(ELEMENT_ERROR);
                super.sendTextEvent("Subject not available - sending mail aborted");
                super.sendEndElementEventNS(ELEMENT_ERROR);
		        this.ignoreHooksCount--;
                return;
            }

            if ((this.body == null) && (this.bodyURI == null)) {
		        this.ignoreHooksCount++;
                super.sendStartElementEventNS(ELEMENT_ERROR);
                super.sendTextEvent("Mailbody not available - sending mail aborted");
                super.sendEndElementEventNS(ELEMENT_ERROR);
		        this.ignoreHooksCount--;
                return;
            }

            Session   session = Session.getDefaultInstance(props, null);
            Transport trans = null;

            trans = session.getTransport("smtp");
            trans.connect();

            this.smtpMessage = setUpMessage(session);

	        this.ignoreHooksCount++;
            super.sendStartElementEventNS(ELEMENT_RESULT);

            if (this.sendPartial == true) {
                for (int i = 0; i < this.toAddresses.size(); i++) {
                    Vector v = new Vector(1);
                    v.add(this.toAddresses.elementAt(i));
                    sendMail(v, trans);
                }
            } else {
                sendMail(this.toAddresses, trans);
            }

            trans.close();
            super.sendEndElementEventNS(ELEMENT_RESULT);
	    this.ignoreHooksCount--;
        } catch (Exception sE) {
            this.getLogger().error("sendMail-Error", sE);
            this.sendExceptionElement(sE);
        }
    }

    /**
     * @link http://java.sun.com/products/javamail/1.3/docs/javadocs/com/sun/mail/smtp/package-summary.html
     * @throws Exception
     */
    private void sendMail(Vector newAddresses, Transport trans)
                   throws Exception {
        AddressHandler[] iA = new AddressHandler[newAddresses.size()];

        for (int i = 0; i < newAddresses.size(); i++) {
            InternetAddress inA = new InternetAddress((String) newAddresses.elementAt(i));
            iA[i] = new AddressHandler(inA);
        }

        try {
            InternetAddress[] iaArr = SendMailTransformer.getAddresses(iA);
            this.smtpMessage.setRecipients(Message.RecipientType.TO, iaArr);
            trans.sendMessage(this.smtpMessage, iaArr);
        } catch (SendFailedException sfEx) {
            this.getLogger().error("Exception during sending of mail", sfEx);

            Address[] adr = sfEx.getInvalidAddresses();

            for (int isfEx = 0; isfEx < iA.length; isfEx++) {
                String tmpAddress = iA[isfEx].getAddress().getAddress();

                for (int sei = 0; sei < adr.length; sei++) {
                    if (((InternetAddress) adr[sei]).getAddress()
                             .equalsIgnoreCase(tmpAddress)) {
                        iA[isfEx].setSendMailResult("Invalid address");
                    }
                }
            }

            Address[] ad = sfEx.getValidUnsentAddresses();

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
            this.getLogger().error("Exception during sending of mail", e);
            this.sendExceptionElement(e);

            return;
        }

        generateSAXReportStatements(iA);
    }

    private Message setUpMessage(Session session) throws Exception {
        Message sm = new MimeMessage(session);

        //sm.setAllow8bitMIME(true);
        sm.setFrom(new InternetAddress(this.fromAddress));
        sm.setSubject(this.subject.toString());

        // process mail-body 					
        BodyPart messageBodyPart = new MimeBodyPart();

        // decide, if to take content from source or plain text 
        // from variable to build mailbody
        if (this.bodyURI != null) {
            Source      inSrc   = resolver.resolveURI(this.bodyURI);
            InputStream inStr   = inSrc.getInputStream();
            byte[]      byteArr = new byte[inStr.available()];
            inStr.read(byteArr);

            String mailBody = new String(byteArr);
            messageBodyPart.setContent(mailBody, "text/html");
        } else {
            messageBodyPart.setContent(this.body.toString(), "text/html");
        }

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);

        // process attachments				
        Enumeration enumAtt = this.attachments.elements();

        while (enumAtt.hasMoreElements()) {
            AttachmentDescriptor aD = (AttachmentDescriptor) enumAtt.nextElement();
            messageBodyPart = new MimeBodyPart();

            if (!aD.isTextContent()) {
                Source     inputSource = null;
                DataSource dataSource = null;

                if (aD.isURLSource()) {
                    inputSource = resolver.resolveURI(aD.strAttrSrc);

                    String iSS = new String(inputSource.getURI());

                    if (iSS.startsWith("cocoon:")) {
                        iSS = iSS.substring(7, iSS.length());

                        if (this.contextPath != null) {
                            iSS = "http://localhost:" + this.port +
                                  this.contextPath + iSS;
                        } else {
                            iSS = "http://localhost:" + this.port + iSS;
                        }

                        if (this.getLogger().isDebugEnabled() == true) {
                            this.getLogger().debug("cocoon-URI changed to " +
                                                   iSS);
                        }

                        dataSource = new URLDataSource(new URL(iSS));
                    } else {
                        dataSource = new URLDataSource(new URL(inputSource.getURI()));
                    }

                    messageBodyPart.setDataHandler(new DataHandler(dataSource));
                } else if (aD.isFileSource()) {
                    inputSource = resolver.resolveURI(aD.strAttrFile);
                    dataSource  = new URLDataSource(new URL(inputSource.getURI()));
                    messageBodyPart.setDataHandler(new DataHandler(dataSource));
                }
            } else {
                messageBodyPart.setContent(aD.strBufContent.toString(),
                                           aD.strAttrMimeType);
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
            this.getLogger().error("Error while sending a SAX-Event", e);
        }
    }

    public static InternetAddress[] getAddresses(AddressHandler[] handlerArr) {
        InternetAddress[] iaArr = new InternetAddress[handlerArr.length];

        for (int i = 0; i < handlerArr.length; i++) {
            iaArr[i] = handlerArr[i].getAddress();
        }

        return iaArr;
    }

	public void recycle() { 
        this.toAddresses = null;
	    this.attachments = null;
	    this.subject = null;
	    this.body = null;
	    this.bodyURI = null;
	    this.mailHost = null;
	    this.fromAddress = null;
	    this.attachmentDescriptor = null;
	    this.port = 0;
	    this.contextPath = null;
	    this.sendPartial = true;
	    this.smtpMessage = null;
	    super.recycle();
	}
	
    class AttachmentDescriptor {
        String       strAttrName     = null;
        String       strAttrMimeType = null;
        String       strAttrSrc      = null;
        String       strAttrFile     = null;
        StringBuffer strBufContent   = null;

        protected AttachmentDescriptor(String newAttrName,
                                       String newAttrMimeType,
                                       String newAttrSrc, String newAttrFile) {
            this.strAttrName     = newAttrName;
            this.strAttrMimeType = newAttrMimeType;
            this.strAttrSrc      = newAttrSrc;
            this.strAttrFile     = newAttrFile;
        }

        protected void setContent(StringBuffer newContent) {
            strBufContent = newContent;
        }

        protected AttachmentDescriptor copy() {
            AttachmentDescriptor aD = new AttachmentDescriptor(this.strAttrName,
                                                               this.strAttrMimeType,
                                                               this.strAttrSrc,
                                                               this.strAttrFile);
            aD.setContent(this.strBufContent);

            return aD;
        }

        protected boolean isURLSource() {
            return (this.strAttrSrc != null) ? true : false;
        }

        protected boolean isFileSource() {
            return (this.strAttrFile != null) ? true : false;
        }

        protected boolean isTextContent() {
            return (this.strBufContent != null) ? true : false;
        }
    }

    class AddressHandler {
        private InternetAddress address        = null;
        private String          sendMailResult = null;

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
