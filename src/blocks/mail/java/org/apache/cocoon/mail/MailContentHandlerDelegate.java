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

import java.io.IOException;
//import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimePart;
import javax.mail.internet.ParameterList;
import javax.mail.internet.ParseException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.excalibur.xml.sax.XMLizable;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Marshal javamail objects
 * <p>
 *   Having one or more javamail objects, like Message, Folder, et al. emit SAX events
 * </p>
 *
 * @author Bernhard Huber
 * @since 24. Oktober 2002
 * @version CVS $Id: MailContentHandlerDelegate.java,v 1.6 2004/03/05 13:02:00 bdelacretaz Exp $
 */

/*
 *  Generated SAX events conforming to following folder sample xml:
 *
 *  <mail:mail xmlns:mail="http://apache.org/cocoon/mail/1.0">
 *
 *  <mail:folder name="INBOX" full-name="INBOX" url-name="imap://user-name@host-name/INBOX"
 *  is-subscribed="yes"
 *  is-direcory="no"
 *  has-new-messages="no"
 *  total-messages="3"
 *  new-messages="0"
 *  deleted-messages="-1"
 *  unread-messages="0"
 *  >
 *  </mail:mail>
 *
 *  Generated SAX events conforming to following message sample xml:
 *
 *  <mail:mail xmlns:mail="http://apache.org/cocoon/mail/1.0">
 *
 *  <mail:message-envelope>
 *  <mail:from email-address="name@a1.net">name@xxx.net</mail:from>
 *  <mail:to email-address="test@xxx.net">test@xxx.net</mail:to>
 *  <mail:reply-to email-address="xxx@test.net">test@xxx.net</mail:reply-to>
 *  <mail:subject>TEST</mail:subject>
 *  <mail:sent-date>Thu Oct 10 14:40:43 CEST 2002</mail:sent-date>
 *  <mail:received-date>Thu Oct 10 14:43:29 CEST 2002</mail:received-date>
 *  <mail:size>4440</mail:size>
 *  <mail:message-number>1</mail:message-number> <mail:flags seen="yes"/>
 *  <mail:header name="Return-path" value="<test@a1.xxx>"/>
 *  <mail:header name="Received" value="from x1"/>
 *  <mail:header name="Received" value="from x2"/>
 *  <mail:header name="Date" value="Thu, 10 Oct 2002 14:40:43 +0200 (CEST)"/>
 *  <mail:header name="From" value="test@xxx.net"/>
 *  <mail:header name="Subject" value="TEST"/>
 *  <mail:header name="To" value="test@xxx.net"/>
 *  <mail:header name="Message-id" value="<4630087.1034253643941.JavaMail.root@test>"/>
 *  <mail:header name="MIME-version" value="1.0"/>
 *  <mail:header name="Content-type" value="multipart/mixed;&#10; boundary="18119425.1034253643876.JavaMail.root@test""/>
 *  </mail:message-envelope>
 *
 *  <mail:part content-type="multipart/MIXED; &#10; boundary="18119425.1034253643876.JavaMail.root@test"">
 *  <mail:content>
 *  <mail:part content-type="TEXT/PLAIN; charset=us-ascii">
 *  <mail:content>TEST CONTENT MESSSAGE TEST.</mail:content>
 *  </mail:part>
 *  <mail:part content-type="MESSAGE/RFC822">
 *  <mail:content>
 *  <mail:part content-type="multipart/MIXED; boundary=--1f735d241edf3a1">
 *  <mail:content>
 *  <mail:part content-type="TEXT/PLAIN; charset=us-ascii">
 *  <mail:content>Test &#13;</mail:content>
 *  </mail:part>
 *  <mail:part content-type="TEXT/X-VCARD; name=xxx.vcf; charset=windows-1252" description="Card for <xxx@a1.xxx>" disposition="ATTACHMENT" file-name="xxx.vcf">
 *  <mail:content>begin:vcard&#13; n:Name;Name&#13; title:Dr&#13; email;internet:xxx@xxx.net&#13; url:www.xxx.net&#13; fn:xXxXx&#13; end:vcard&#13; &#13; </mail:content>
 *  </mail:part>
 *  </mail:content>
 *  </mail:part>
 *  </mail:content>
 *  </mail:part>
 *  </mail:content>
 *  </mail:part>
 *
 *  </mail:mail>
 *
 */
public class MailContentHandlerDelegate extends AbstractLogEnabled {

    /**
     *  URI of the generated XML elements
     */
    public final static String URI = "http://apache.org/cocoon/mail/1.0";

    /**
     *  PREFIX of the generated XML elements
     */
    public final static String PREFIX = "mail";

    /**
     * Destination content handler receiving SAX events generated by
     * this class
     */
    private ContentHandler contentHandler;

    /**
     *  use this attributes for startElement attributes invocation, reusing
     *  attributes object for all elements
     */
    private AttributesImpl attributes = null;

    private SimpleDateFormat sdf;

    private ContentTypePreference alternativeMailCtPref = new MailCtPref();


    /**
     *  Constructor for the MailContentHandler object
     *
     *@param  contentHandler  Description of Parameter
     */
    public MailContentHandlerDelegate(ContentHandler contentHandler) {
        this.contentHandler = contentHandler;
    }


    /**
     *  Sets the simpleDateFormatter attribute of the MailContentHandlerDelegate object
     *
     *@param  sdf  The new simpleDateFormat value
     */
    public void setSimpleDateFormat(SimpleDateFormat sdf) {
        this.sdf = sdf;
    }


    /**
     *  Gets the simpleDateFormat attribute of the MailContentHandlerDelegate object
     *
     *@return    The simpleDateFormat value
     */
    public SimpleDateFormat getSimpleDateFormat() {
        return sdf;
    }


    /**
     *  Emit starting SAX events sequence, including SAX startDocument event
     *
     *@exception  SAXException  thrown iff generating SAX events fails
     */
    public void startDocument() throws SAXException {
        startDocumentInternal(true);
    }


    /**
     *  Emit starting SAX events sequence, excluding SAX startDocument event
     *
     *@exception  SAXException  thrown iff generating SAX events fails
     */
    public void startDocumentXMLizer() throws SAXException {
        startDocumentInternal(false);
    }


    /**
     *  Emit starting SAX events sequence, including SAX endDocument event
     *
     *@exception  SAXException  thrown iff generating SAX events fails
     */
    public void endDocument() throws SAXException {
        endDocumentInternal(true);
    }


    /**
     *  Emit starting SAX events sequence, excluding SAX endDocument event
     *
     *@exception  SAXException  thrown iff generating SAX events fails
     */
    public void endDocumentXMLizer() throws SAXException {
        endDocumentInternal(false);
    }


    /**
     *  Emit a folder  as a sequence of SAX events
     *
     *@param  folder  emit this folder
     */
    public void marshalFolderToSAX(Folder folder) {
        try {
            folderToSAX(this.contentHandler, folder);
        } catch (Exception e) {
            getLogger().error("Cannot generate SAX events from folder", e);
        }
    }


    /**
     *  Emit folders as a sequence of SAX events
     *
     *@param  folders  emit these folders
     */
    public void marshalFolderToSAX(Folder[] folders) {
        try {
            for (int i = 0; i < folders.length; i++) {
                folderToSAX(this.contentHandler, folders[i]);
            }
        } catch (Exception e) {
            getLogger().error("Cannot generate SAX events from folders", e);
        }
    }


    /**
     *  Emit a message envelope as a sequence of SAX events
     *
     *@param  message  emit envelope of this message
     */
    public void marshalMessageEnvelopeToSAX(Message message) {
        try {
            messageEnvelopeToSAX(this.contentHandler, message);
        } catch (Exception e) {
            getLogger().error("Cannot generate SAX events from message envelope ", e);
        }
    }


    /**
     *  Emit a message envelope, and message content as a sequence of SAX events
     *
     *@param  message  emit envelope, and content of this message
     */
    public void marshalMessageToSAX(Message message) {
        try {
            messageEnvelopeToSAX(this.contentHandler, message);
            partToSAX(this.contentHandler, message, 0);
        } catch (Exception e) {
            getLogger().error("Cannot generate SAX events from message ", e);
        }
    }


    /**
     *  Emit a message content as a sequence of SAX events
     *
     *@param  part  Description of the Parameter
     */
    public void marshalPartToSAX(Part part) {
        try {
            partToSAX(this.contentHandler, part, 0);
        } catch (Exception e) {
            getLogger().error("Cannot generate SAX events part", e);
        }
    }


    /**
     *  Emit start document sequence
     *
     *@param  emitStartDocument  flag controlling invocation of SAX startDocument
     *@exception  SAXException   thrown iff generating SAX events fails
     */
    protected void startDocumentInternal(boolean emitStartDocument) throws SAXException {
        if (emitStartDocument) {
            this.contentHandler.startDocument();
        }
        this.contentHandler.startPrefixMapping(PREFIX, URI);

        attributes = new AttributesImpl();
        attributes.clear();
        attributes.addAttribute("", PREFIX, "xmlns:" + PREFIX, "CDATA", URI);
        startElement("mail", attributes);
    }


    /**
     *  Emit end document sequence
     *
     *@param  emitEndDocument   flag controlling invocation of SAX endDocument
     *@exception  SAXException  thrown iff generating SAX events fails
     */
    protected void endDocumentInternal(boolean emitEndDocument) throws SAXException {
        endElement("mail");

        this.contentHandler.endPrefixMapping(PREFIX);
        if (emitEndDocument) {
            this.contentHandler.endDocument();
        }
    }


    /**
     *  Emit folder as sequence of SAX events
     *
     *@param  folder                  emit this folder
     *@param  contentHandler          specifies sink of SAX events
     *@exception  MessagingException  thrown iff accessing javamail data fails
     *@exception  SAXException        thrown iff generating SAX events fails
     */
    protected void folderToSAX(ContentHandler contentHandler, Folder folder) throws MessagingException, SAXException {
        attributes.clear();
        addAttribute("name", folder.getName());
        addAttribute("full-name", folder.getFullName());
        addAttribute("url-name", folder.getURLName().toString());
        addAttribute("is-subscribed", folder.isSubscribed() ? "yes" : "no");
        addAttribute("is-directory", (folder.getType() & Folder.HOLDS_FOLDERS) != 0 ? "yes" : "no");

        if ((folder.getType() & Folder.HOLDS_MESSAGES) != 0) {
            addAttribute("holds-messages", "yes");
            addAttribute("has-new-messages", folder.hasNewMessages() ? "yes" : "no");
            addAttribute("total-messages", String.valueOf(folder.getMessageCount()));
            addAttribute("new-messages", String.valueOf(folder.getNewMessageCount()));
            addAttribute("deleted-messages", String.valueOf(folder.getDeletedMessageCount()));
            addAttribute("unread-messages", String.valueOf(folder.getUnreadMessageCount()));
        }

        startElement("folder", attributes);
        endElement("folder");
    }


    /**
     *  Emit message envelope as sequence of SAX events
     *
     *@param  message                 emit envelope of this message
     *@param  contentHandler          specifies sink of SAX events
     *@exception  MessagingException  thrown iff accessing javamail data fails
     *@exception  SAXException        thrown iff generating SAX events fails
     */
    protected void messageEnvelopeToSAX(ContentHandler contentHandler, Message message) throws MessagingException, SAXException {
        attributes.clear();
        startElement("message-envelope", attributes);

        Address[] a;
        // FROM
        if ((a = message.getFrom()) != null) {
            for (int j = 0; j < a.length; j++) {
                emitAddress("from", a[j]);
            }
        }

        // TO
        if ((a = message.getRecipients(Message.RecipientType.TO)) != null) {
            for (int j = 0; j < a.length; j++) {
                emitAddress("to", a[j]);
            }
        }

        // CC
        if ((a = message.getRecipients(Message.RecipientType.CC)) != null) {
            for (int j = 0; j < a.length; j++) {
                emitAddress("cc", a[j]);
            }
        }
        // BCC
        if ((a = message.getRecipients(Message.RecipientType.BCC)) != null) {
            for (int j = 0; j < a.length; j++) {
                emitAddress("bcc", a[j]);
            }
        }

        // REPLY-TO
        if ((a = message.getReplyTo()) != null) {
            for (int j = 0; j < a.length; j++) {
                emitAddress("reply-to", a[j]);
            }
        }

        // SUBJECT
        attributes.clear();
        startElement("subject", attributes);
        characters(message.getSubject());
        endElement("subject");

        // SENT-DATE
        Date d;
        d = message.getSentDate();
        emitDate("sent-date", d);

        // RECEIVED-DATE
        d = message.getReceivedDate();
        emitDate("received-date", d);

        // SIZE
        attributes.clear();
        startElement("size", attributes);
        characters(String.valueOf(message.getSize()));
        endElement("size");

        // MESSAGE NUMBER
        attributes.clear();
        startElement("message-number", attributes);
        characters(String.valueOf(message.getMessageNumber()));
        endElement("message-number");

        // FLAGS:
        Flags flags = message.getFlags();
        Flags.Flag[] sf = flags.getSystemFlags();
        // get the system flags

        attributes.clear();
        for (int i = 0; i < sf.length; i++) {
            Flags.Flag flag = sf[i];
            if (flag == Flags.Flag.ANSWERED) {
                addAttribute("answered", "yes");
            } else if (flag == Flags.Flag.DELETED) {
                addAttribute("deleted", "yes");
            } else if (flag == Flags.Flag.DRAFT) {
                addAttribute("draft", "yes");
            } else if (flag == Flags.Flag.FLAGGED) {
                addAttribute("flagged", "yes");
            } else if (flag == Flags.Flag.RECENT) {
                addAttribute("recent", "yes");
            } else if (flag == Flags.Flag.SEEN) {
                addAttribute("seen", "yes");
            }
        }
        startElement("flags", attributes);
        endElement("flags");

        String[] uf = flags.getUserFlags();
        // get the user flag strings
        for (int i = 0; i < uf.length; i++) {
            attributes.clear();
            startElement("user-flags", attributes);
            characters(uf[i]);
            endElement("user-flags");
        }

        // X-MAILER
        //String[] hdrs = message.getHeader("X-Mailer");
        //logger.info("X-Mailer " + (hdrs != null ? hdrs[0] : "NOT available"));

        Enumeration allHeaders = message.getAllHeaders();
        if (allHeaders != null) {
            while (allHeaders.hasMoreElements()) {
                Header header = (Header) allHeaders.nextElement();
                attributes.clear();
                addAttribute("name", header.getName());
                addAttribute("value", header.getValue());
                startElement("header", attributes);

                endElement("header");
            }
        }

        endElement("message-envelope");
    }


    /**
     *  Emit part as sequence of SAX events
     *
     *@param  part                    Description of the Parameter
     *@param  contentHandler          specifies sink of SAX events
     *@param  i                       Description of the Parameter
     *@exception  MessagingException  thrown iff accessing javamail data fails
     *@exception  IOException         thrown iff accessing content fails
     *@exception  SAXException        thrown iff generating SAX events fails
     */
    protected void partToSAX(ContentHandler contentHandler, Part part, int i) throws MessagingException, IOException, SAXException {
        attributes.clear();
        String v;
        if ((v = part.getContentType()) != null) {
            // content type as-is
            addAttribute("content-type", v);
            try {
                ContentType ct = new ContentType(v);
                String s;

                // primary part only
                s = ct.getPrimaryType();
                if (s != null) {
                    addAttribute("primary-type", s.toLowerCase());
                }

                // secondary part only
                s = ct.getSubType();
                if (s != null) {
                    addAttribute("secondary-type", s.toLowerCase());
                }

                // primary part '/' secondary part
                s = ct.getBaseType();
                if (s != null) {
                    addAttribute("base-type", s.toLowerCase());
                }

                // list of parameters : parameter-name parameter-value
                ParameterList pl = ct.getParameterList();
                Enumeration names = pl.getNames();
                while (names.hasMoreElements()) {
                    String key = (String) names.nextElement();
                    String value = pl.get(key);
                    addAttribute(key, value);
                }
            } catch (ParseException pe) {
                String message = "Cannot parse content-type " + String.valueOf(v);
                getLogger().error(message, pe);
            }
        }

        if (i > 0) {
            addAttribute("part-num", String.valueOf(i));

            if (part.getDescription() != null) {
                addAttribute("description", part.getDescription());
            }
            if (part.getDisposition() != null) {
                addAttribute("disposition", part.getDisposition());
                addAttribute("disposition-inline", String.valueOf(part.getDisposition().equals(Part.INLINE)));
            }
            if (part.getFileName() != null) {
                addAttribute("file-name", part.getFileName());
            }
        } else {
            boolean hasAttachments = false;
            if (part.isMimeType("multipart/*")) {
                Multipart mp = (Multipart) part.getContent();
                if (mp.getCount() > 1) {
                    hasAttachments = true;
                    addAttribute("num-parts", String.valueOf(mp.getCount()));
                }
            }
            addAttribute("has-attachments", String.valueOf(hasAttachments));
        }

        startElement("part", attributes);
        contentToSAX(contentHandler, part);
        endElement("part");
    }


    /**
     *  Emit content of message part as sequence of SAX events
     *
     *@param  contentHandler          specifies sink of SAX events
     *@param  part                    emit this part as sequence of SAX events
     *@exception  SAXException        thrown iff generating SAX events fails
     *@exception  MessagingException  thrown iff accessing javamail data fails
     *@exception  IOException         thrown iff accessing content fails
     */
    protected void contentToSAX(ContentHandler contentHandler, Part part) throws MessagingException, IOException, SAXException {
        attributes.clear();
        startElement("content", attributes);

        /*
         *  Using isMimeType to determine the content type avoids
         *  fetching the actual content data until we need it.
         *
         *  todo: recheck this code for all circumstances.........
         */
        if (part.getContent() instanceof String && (part.isMimeType("text/plain"))) {
            characters((String) part.getContent());
        } else if (part.isMimeType("multipart/alternative")) {
            MimeMultipart mp = (MimeMultipart) part.getContent();
            MimePart bestPart = null;
            int ctMax = 0;
            for (int i = 0; i < mp.getCount(); i++) {
                MimePart p = (MimePart) mp.getBodyPart(i);
                int ctPrefN = alternativeMailCtPref.preference(p);
                if (ctPrefN > ctMax) {
                    ctMax = ctPrefN;
                    bestPart = p;
                }
            }
            if (bestPart != null) {
                partToSAX(contentHandler, bestPart, 0);
            }
        } else if (part.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) part.getContent();

            int count = mp.getCount();
            for (int i = 0; i < count; i++) {
                partToSAX(contentHandler, mp.getBodyPart(i), i);
            }
        } else if (part.isMimeType("message/rfc822")) {
            partToSAX(contentHandler, (Part) part.getContent(), 0);
        } else {
            /*
             *  If we actually want to see the data, and it's not a
             *  MIME type we know, fetch it and check its Java type.
            Object o = part.getContent();
            if (o instanceof String) {
                characters((String) o);
            } else if (o instanceof InputStream) {
                encodeInputStreamForXML((InputStream) o);
            } else {
                // unknown type
                InputStream is = part.getInputStream();
                encodeInputStreamForXML(is);
            }
            */
        }
        endElement("content");
    }


    /**
     *  Helper method emitting SAX events representing an internet address
     *
     *@param  nodeName          emitted element node name
     *@param  address           emitted address data
     *@exception  SAXException  thrown iff generating SAX events fails
     */
    protected void emitAddress(String nodeName, Address address) throws SAXException {

        attributes.clear();

        if (address instanceof InternetAddress) {
            InternetAddress internetAddress = (InternetAddress) address;
            String personal = internetAddress.getPersonal();
            if (personal != null) {
                addAttribute("personal", personal);
            }
            String emailAddress = internetAddress.getAddress();
            if (emailAddress != null) {
                addAttribute("email-address", emailAddress);
            }
        }

        startElement(nodeName, attributes);
        String addressAsString = address.toString();
        characters(addressAsString);
        endElement(nodeName);
    }


    /**
     *  Helper method emitting SAX events representing a date
     *
     *@param  nodeName          emitted element node name
     *@param  d                 Description of the Parameter
     *@exception  SAXException  thrown iff generating SAX events fails
     */
    protected void emitDate(String nodeName, Date d) throws SAXException {
        attributes.clear();
        startElement(nodeName, attributes);
        if (d != null) {
            if (sdf != null) {
                String formattedDate = sdf.format(d);
                characters(formattedDate);
            } else {
                characters(d.toString());
            }
        }
        endElement(nodeName);
    }


    /**
     *  Helper method emitting SAX startElement event
     *
     *@param  nodeName          Description of the Parameter
     *@param  attributes        Description of the Parameter
     *@exception  SAXException  thrown iff generating SAX events fails
     */
    private void startElement(String nodeName, Attributes attributes) throws SAXException {
        this.contentHandler.startElement(URI, nodeName, PREFIX + ":" + nodeName, attributes);
    }


    /**
     *  Helper method emitting SAX characters event
     *
     *@param  s                 Description of the Parameter
     *@exception  SAXException  thrown iff generating SAX events fails
     */
    private void characters(String s) throws SAXException {
        if (s != null) {
            // replace 0d0a by 0a
            // any better idea ?
            StringBuffer sb = new StringBuffer();
            char[] stringCharacters = s.toCharArray();
            for (int i = 0; i < stringCharacters.length; i++) {
                if (stringCharacters[i] != 0x0d) {
                    sb.append(stringCharacters[i]);
                }
            }
            stringCharacters = sb.toString().toCharArray();

            this.contentHandler.characters(stringCharacters, 0, stringCharacters.length);
        }
    }


    /**
     *  Helper method emitting SAX endElement event
     *
     *@param  nodeName          Description of the Parameter
     *@exception  SAXException  thrown iff generating SAX events fails
     */
    private void endElement(String nodeName) throws SAXException {
        this.contentHandler.endElement(URI, nodeName, PREFIX + ":" + nodeName);
    }


    /**
     *  Helper method adding an attribute name-value pair
     *
     *@param  nodeName   The feature to be added to the Attribute attribute
     *@param  nodeValue  The feature to be added to the Attribute attribute
     */
    private void addAttribute(String nodeName, String nodeValue) {
        attributes.addAttribute("", nodeName, nodeName, "CDATA", nodeValue);
    }


    /*
     *  Description of the Method
     *
     *@param  is                Description of Parameter
     *@exception  IOException   Description of Exception
     *@exception  SAXException  thrown iff generating SAX events fails
     */
    /* FIXME (SM) This method doesn't appear to be used
     private void encodeInputStreamForXML(InputStream is) throws IOException, SAXException {
        int contentLength = is.available();
        if (contentLength < 16) {
            contentLength = 2048;
        }
        attributes.clear();
        addAttribute("type", "hex");
        startElement("encoding", attributes);
        byte content[] = new byte[contentLength];
        int readLength;
        while ((readLength = is.read(content, 0, content.length)) != -1) {
            String strContent = encodeBytes(content, 0, readLength);
            characters(strContent);
        }
        endElement("encoding");
    } */


    /*
     *  A simple byte as hex encodeing
     *
     *@param  bytes   Description of Parameter
     *@param  offset  Description of Parameter
     *@param  length  Description of Parameter
     *@return         Description of the Returned Value
     */
    /* FIXME (SM) This method doesn't appear to be used
    private String encodeBytes(final byte[] bytes, final int offset, final int length) {
        StringBuffer sb = new StringBuffer();
        final String ENCODE_TABLE[] = {
                "0", "1", "2", "3", "4", "5", "6", "7", "8",
                "9", "a", "b", "c", "d", "e", "f"};
        final int l = offset + length;
        for (int i = offset; i < l; i++) {
            byte b = bytes[i];
            int upperNibble = ((b >> 4) & 0x0f);
            int lowerNibble = (b & 0x0f);

            sb.append(ENCODE_TABLE[upperNibble]);
            sb.append(ENCODE_TABLE[lowerNibble]);
            sb.append(" ");
        }
        return sb.toString();
    } */


    /**
     *  XMLizable Wrapper for one or more folders, saxing folders.
     *
     *@author     Bernhard Huber
     *@created    30. Dezember 2002
     *@version    CVS Version: $Id: MailContentHandlerDelegate.java,v 1.6 2004/03/05 13:02:00 bdelacretaz Exp $
     */
    static class FolderXMLizer extends AbstractLogEnabled
             implements XMLizable {
        //private Folder folder;
        private Folder[] folders;


        /**
         *Constructor for the FolderSAX object
         *
         *@param  folder  Description of the Parameter
         */
        FolderXMLizer(Folder folder) {
            this.folders = new Folder[]{folder};
        }


        /**
         *Constructor for the FolderXMLizer object
         *
         *@param  folders  Description of the Parameter
         */
        FolderXMLizer(Folder[] folders) {
            this.folders = folders;
        }


        /**
         *  Generate SAX events from one or more folders
         *
         *@param  handler           Description of the Parameter
         *@exception  SAXException  thrown iff generating SAX events fails
         */
        public void toSAX(ContentHandler handler) throws SAXException {
            MailContentHandlerDelegate mailContentHandlerDelegate = new MailContentHandlerDelegate(handler);
            mailContentHandlerDelegate.enableLogging(getLogger());
            mailContentHandlerDelegate.startDocumentXMLizer();
            for (int i = 0; i < folders.length; i++) {
                mailContentHandlerDelegate.marshalFolderToSAX(folders[i]);
            }
            mailContentHandlerDelegate.endDocumentXMLizer();
        }
    }


    /**
     *  XMLizable Wrapper for one or more messages, saxing envelope only of messages.
     *
     *@author     Bernhard Huber
     *@created    30. Dezember 2002
     *@version    CVS Version: $Id: MailContentHandlerDelegate.java,v 1.6 2004/03/05 13:02:00 bdelacretaz Exp $
     */
    static class MessageEnvelopeXMLizer extends AbstractLogEnabled
             implements XMLizable {
        private Message[] messages;

        private SimpleDateFormat sdf;


        /**
         *Constructor for the MessageEnvelopeXMLizer object
         *
         *@param  message  Description of the Parameter
         */
        public MessageEnvelopeXMLizer(Message message) {
            this.messages = new Message[1];
            this.messages[0] = message;
        }


        /**
         *Constructor for the MessageEnvelopeXMLize object
         *
         *@param  messages  Description of the Parameter
         */
        public MessageEnvelopeXMLizer(Message[] messages) {
            this.messages = messages;
        }


        /**
         *  Sets the simpleDateFormat attribute of the MessageEnvelopeXMLizer object
         *
         *@param  sdf  The new simpleDateFormat value
         */
        public void setSimpleDateFormat(SimpleDateFormat sdf) {
            this.sdf = sdf;
        }


        /**
         *  Gets the simpleDateFormat attribute of the MessageEnvelopeXMLizer object
         *
         *@param  sdf  Description of the Parameter
         */
        public void getSimpleDateFormat(SimpleDateFormat sdf) {
            this.sdf = sdf;
        }


        /**
         *  Generate SAX events from one or more messages
         *
         *@param  handler           Description of the Parameter
         *@exception  SAXException  thrown iff generating SAX events fails
         */
        public void toSAX(ContentHandler handler) throws SAXException {
            MailContentHandlerDelegate mailContentHandlerDelegate = new MailContentHandlerDelegate(handler);
            mailContentHandlerDelegate.enableLogging(getLogger());
            mailContentHandlerDelegate.setSimpleDateFormat(sdf);
            mailContentHandlerDelegate.startDocumentXMLizer();

            for (int i = 0; i < messages.length; i++) {
                mailContentHandlerDelegate.marshalMessageEnvelopeToSAX(messages[i]);
            }

            mailContentHandlerDelegate.endDocumentXMLizer();
        }
    }


    /**
     *  XMLizable Wrapper for a message, saxing a message envelope, plus content.
     *
     *@author     Bernhard Huber
     *@created    30. Dezember 2002
     *@version    CVS Version: $Id: MailContentHandlerDelegate.java,v 1.6 2004/03/05 13:02:00 bdelacretaz Exp $
     */
    static class MessageXMLizer extends AbstractLogEnabled
             implements XMLizable {
        private Message message;

        private SimpleDateFormat sdf;


        /**
         *Constructor for the MessageXMLizer object
         *
         *@param  message  Description of the Parameter
         */
        public MessageXMLizer(Message message) {
            this.message = message;
        }


        /**
         *  Sets the simpleDateFormat attribute of the MessageXMLizer object
         *
         *@param  sdf  The new simpleDateFormat value
         */
        public void setSimpleDateFormat(SimpleDateFormat sdf) {
            this.sdf = sdf;
        }


        /**
         *  Gets the simpleDateFormat attribute of the MessageXMLizer object
         *
         *@param  sdf  Description of the Parameter
         */
        public void getSimpleDateFormat(SimpleDateFormat sdf) {
            this.sdf = sdf;
        }


        /**
         *  Generate SAX events from a message
         *
         *@param  handler           Description of the Parameter
         *@exception  SAXException  thrown iff generating SAX events fails
         */
        public void toSAX(ContentHandler handler) throws SAXException {
            MailContentHandlerDelegate mailContentHandlerDelegate = new MailContentHandlerDelegate(handler);
            mailContentHandlerDelegate.enableLogging(getLogger());
            mailContentHandlerDelegate.setSimpleDateFormat(sdf);
            mailContentHandlerDelegate.startDocumentXMLizer();
            mailContentHandlerDelegate.marshalMessageToSAX(message);
            mailContentHandlerDelegate.endDocumentXMLizer();
        }
    }
}

