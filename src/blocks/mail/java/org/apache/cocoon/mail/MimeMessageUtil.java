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
 * @version CVS $Id: MimeMessageUtil.java,v 1.5 2004/03/05 13:02:00 bdelacretaz Exp $
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

