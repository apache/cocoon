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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimePart;

/**
 *  Description of the Class
 *
 * @author Bernhard Huber
 * @since 26. Oktober 2002
 * @version CVS $Id: MimeMessageUtil.java,v 1.4 2004/03/01 03:50:57 antonio Exp $
 */
public class MimeMessageUtil {
    /**
     *  Description of the Field
     */
    public final static String SENDER_NOT_AVAILABLE = "-not available-";
    /**
     *  Description of the Field
     */
    public final static String NO_SUBJECT = "-none-";


    /**
     *  Gets the sender attribute of the MimeMessageUtil class
     *
     *@param  msg  Description of the Parameter
     *@return      The sender value
     */
    public static String getSender(MimeMessage msg) {
        String sender = null;
        try {
            InternetAddress[] from = (InternetAddress[]) msg.getFrom();
            if (from != null && from.length > 0) {
                sender = from[0].getPersonal();
                if (sender == null) {
                    sender = from[0].getAddress();
                }
            }
            if (sender == null) {
                sender = SENDER_NOT_AVAILABLE;
            }
        } catch (AddressException e) {
            sender = SENDER_NOT_AVAILABLE;
        } catch (MessagingException e) {
            sender = SENDER_NOT_AVAILABLE;
        }
        if (sender == null || sender.trim().equals("")) {
            sender = SENDER_NOT_AVAILABLE;
        }
        return sender;
    }


    /**
     *  Gets the senderEmail attribute of the MimeMessageUtil class
     *
     *@param  msg  Description of the Parameter
     *@return      The senderEmail value
     */
    public static String getSenderEmail(MimeMessage msg) {
        String senderEmail = null;
        try {
            InternetAddress[] from = (InternetAddress[]) msg.getFrom();
            if (from != null && from.length > 0) {
                senderEmail = from[0].getAddress();
            }
        } catch (AddressException e) {
            senderEmail = SENDER_NOT_AVAILABLE;
        } catch (MessagingException e) {
            senderEmail = SENDER_NOT_AVAILABLE;
        }
        if (senderEmail == null || senderEmail.trim().equals("")) {
            senderEmail = SENDER_NOT_AVAILABLE;
        }
        return senderEmail;
    }


    /**
     *  Gets the subject attribute of the MimeMessageUtil class
     *
     *@param  msg  Description of the Parameter
     *@return      The subject value
     */
    public static String getSubject(MimeMessage msg) {
        String subject = null;
        try {
            subject = msg.getSubject();
        } catch (MessagingException e) {
            subject = NO_SUBJECT;
        }
        if (subject == null || subject.trim().equals("")) {
            subject = NO_SUBJECT;
        }
        return subject;
    }


    /**
     *  Gets the date attribute of the MimeMessageUtil class
     *
     *@param  msg  Description of the Parameter
     *@return      The date value
     */
    public static Date getDate(MimeMessage msg) {
        Date date = null;
        try {
            date = msg.getReceivedDate();
        } catch (MessagingException messagingexception) {
            /*
             *  empty
             */
        }
        if (date == null) {
            try {
                date = msg.getSentDate();
            } catch (MessagingException messagingexception) {
                /*
                 *  empty
                 */
            }
        }
        return date;
    }


    /**
     *  Gets the iD attribute of the MimeMessageUtil class
     *
     *@param  msg  Description of the Parameter
     *@return      The iD value
     */
    public static String getID(MimeMessage msg) {
        String id = null;
        try {
            id = msg.getMessageID();
        } catch (MessagingException messagingexception) {
            /*
             *  empty
             */
        }
        return id;
    }


    /**
     *
     *@param  part
     *@param  ctPref
     *@param  l
     */
    private static void flattenMessageHelper
            (MimePart part, ContentTypePreference ctPref, List l) {
        try {
            if (part.isMimeType("multipart/alternative")) {
                MimeMultipart mp = (MimeMultipart) part.getContent();
                MimePart bestPart = null;
                int ctMax = 0;
                for (int i = 0; i < mp.getCount(); i++) {
                    MimePart p = (MimePart) mp.getBodyPart(i);
                    int ctPrefN = ctPref.preference(part);
                    if (ctPrefN > ctMax) {
                        ctMax = ctPrefN;
                        bestPart = p;
                    }
                }
                if (bestPart != null) {
                    l.add(bestPart);
                }
            } else if (part.isMimeType("multipart/*")) {
                MimeMultipart mp = (MimeMultipart) part.getContent();
                for (int i = 0; i < mp.getCount(); i++) {
                    flattenMessageHelper((MimePart) mp.getBodyPart(i),
                            ctPref, l);
                }
            } else if (part.isMimeType("message/rfc822")) {
                flattenMessageHelper((MimePart) part.getContent(), ctPref,
                        l);
            } else if (ctPref.preference(part) > 0) {
                l.add(part);
            }
        } catch (MessagingException e) {
            /*
             *  empty
             */
        } catch (IOException ioexception) {
            /*
             *  empty
             */
        }
    }


    /**
     *  Description of the Method
     *
     *@param  message  Description of the Parameter
     *@param  ctPref   Description of the Parameter
     *@return          Description of the Return Value
     */
    public static MimePart[] flattenMessage(MimeMessage message,
            ContentTypePreference ctPref) {
        List parts = new ArrayList();
        flattenMessageHelper(message, ctPref, parts);
        return (MimePart[]) parts.toArray(new MimePart[0]);
    }
}

