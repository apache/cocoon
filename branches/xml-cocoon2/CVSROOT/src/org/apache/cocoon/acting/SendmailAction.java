/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.acting;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.Transport;
import javax.mail.Session;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.AddressException;
import javax.servlet.http.HttpServletRequest;
import org.apache.avalon.configuration.Configurable;
import org.apache.avalon.configuration.Configuration;
import org.apache.avalon.configuration.ConfigurationException;
import org.apache.avalon.Loggable;
import org.apache.avalon.configuration.Parameters;
import org.apache.cocoon.Constants;
import org.apache.log.Logger;
import org.xml.sax.EntityResolver;

/**
 * The SendmailAction class sends email. The action needs four parameters:
 *
 * <dl>
 *   <dt>smtphost</dt>
 *   <dd>the smtp server to send the mail through</dd>
 *   <dt>from</dt>
 *   <dd>the email address the mail appears to be from</dd>
 *   <dt>to</dt>
 *   <dd>the email address the mail it sent to</dd>
 *   <dt>subject</dt>
 *   <dd>the subject of the email</dd>
 *   <dt>body</dt>
 *   <dd>the body of the email</dd>
 * </dl>
 *
 * The class attempts to load all of these parameters from the sitemap, but
 * if they do not exist there it will read them from the request. The exception
 * is the smtphost parameter, which is assumed to be localhost if not specified
 * in the sitemap. Note it's strongly recommended that the to address be
 * specified by the sitemap, not the request, to prevent possible abuse of the
 * SendmailAction as a spam source.
 *
 * @author <a href="mailto:balld@apache.org">Donald Ball</a>
 * @version CVS $Revision
 */
public class SendmailAction extends AbstractAction {

  Properties default_properties = null;

  public void configure(Configuration conf) throws ConfigurationException {
    getLogger().debug("SendmailAction: init");
    default_properties = new Properties();
    if (conf.getAttribute("smtphost") != null) {
      default_properties.put("mail.smtp.host",conf.getAttribute("smtphost"));
    } else {
      default_properties.put("mail.smtp.host","127.0.0.1");
    }
    getLogger().debug("SendmailAction: using "+default_properties.get("mail.smtp.host")+" as the smtp server");
  }

  public Map act(EntityResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception {
    HashMap results = new HashMap();
    try {
      getLogger().debug("SendmailAction: act start");
      HttpServletRequest request = (HttpServletRequest)objectModel.get(Constants.REQUEST_OBJECT);
      Properties properties = new Properties(default_properties);
      if (parameters.isParameter("smtphost")) {
        properties.put("mail.smtp.host",parameters.getParameter("smtphost",null));
        getLogger().debug("SendmailAction: overriding default smtp server, using "+properties.get("mail.smtp.host"));
      }
      Session session = Session.getDefaultInstance(properties);
      Message message = new MimeMessage(session);
      String from = null;
      String to = null;
      String subject = null;
      String body = null;
      try {
        if (parameters.isParameter("from")) {
          from = parameters.getParameter("from",null);
        } else if ((from = request.getParameter("from")) == null) {
          throw new SendmailActionException("no from address");
        }
        message.setFrom(new InternetAddress(from));
      } catch (AddressException e) {
        throw new SendmailActionException("invalid from address: "+from+": "+e.getMessage());
      }
      try {
        if (parameters.isParameter("to")) {
          to = parameters.getParameter("to",null);
        } else if ((to = request.getParameter("to")) == null) {
          throw new SendmailActionException("no to address");
        }
        message.setRecipient(Message.RecipientType.TO,new InternetAddress(to));
      } catch (AddressException e) {
        throw new SendmailActionException("invalid to address: "+to+": "+e.getMessage());
      }
      if (parameters.isParameter("subject")) {
        subject = parameters.getParameter("subject",null);
      } else if ((subject = request.getParameter("subject")) == null) {
        throw new SendmailActionException("no subject");
      }
      message.setSubject(subject);
      if (parameters.isParameter("body")) {
        body = parameters.getParameter("body",null);
      } else if ((body = request.getParameter("body")) == null) {
        throw new SendmailActionException("no body");
      }
      message.setText(body);
      message.setSentDate(new Date());
      Transport.send(message);
      getLogger().debug("SendmailAction: act stop");
      return Collections.unmodifiableMap(results);
    } catch (SendmailActionException e) {
      getLogger().error("SendmailAction: "+e.getMessage());
      return Collections.unmodifiableMap(results);
    }
  }

  class SendmailActionException extends Exception {

    public SendmailActionException(String message) {
      super(message);
    }

  }

}
