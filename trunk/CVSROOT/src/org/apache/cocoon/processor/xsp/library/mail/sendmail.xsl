<?xml version="1.0"?>
<!--
 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2001 The Apache Software Foundation. All rights reserved.

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

 4. The names "Cocoon" and  "Apache Software Foundation"  must not be used to
    endorse  or promote  products derived  from this  software without  prior
    written permission. For written permission, please contact
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
-->
<!--
 <description>
 This is a stylesheet to send mail via the java mail API.
 </description>

 <author>Donald A. Ball Jr.</author>
 <version>1.0</version>
 <release version="1.1" author="Drasko Kokic"/>
-->
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xsp="http://www.apache.org/1999/XSP/Core"
                xmlns:sendmail="http://apache.org/cocoon/sendmail/v1">

  <xsl:template match="xsp:page">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsp:structure>
        <xsp:include>javax.mail.Message</xsp:include>
        <xsp:include>javax.mail.Transport</xsp:include>
        <xsp:include>javax.mail.Session</xsp:include>
        <xsp:include>javax.mail.MessagingException</xsp:include>
        <xsp:include>javax.mail.internet.InternetAddress</xsp:include>
        <xsp:include>javax.mail.internet.MimeMessage</xsp:include>
        <xsp:include>javax.mail.internet.AddressException</xsp:include>
        <xsp:include>java.util.Date</xsp:include>
        <xsp:include>java.util.Properties</xsp:include>
      </xsp:structure>
      <xsp:logic>
        static Properties _sendmail_properties;
        static
         {
          _sendmail_properties = new Properties();
          _sendmail_properties.put ("mail.smtp.host", "127.0.0.1");
         }
      </xsp:logic>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>


  <xsl:template match="sendmail:send-mail">
    <xsl:variable name="subject"><xsl:call-template name="get-nested-string"><xsl:with-param name="content" select="sendmail:subject"/></xsl:call-template></xsl:variable>
    <xsl:variable name="body"><xsl:call-template name="get-nested-string"><xsl:with-param name="content" select="sendmail:body"/></xsl:call-template></xsl:variable>
    <xsl:variable name="smtphost"><xsl:call-template name="get-nested-string"><xsl:with-param name="content" select="sendmail:smtphost"/></xsl:call-template></xsl:variable>
    <xsp:logic>
      try
       {
        Properties _sendmail_properties = new Properties (this._sendmail_properties);
        if (!"null".equals (String.valueOf (<xsl:copy-of select="$smtphost"/>)))
         {
          _sendmail_properties.put ("mail.smtp.host", String.valueOf (<xsl:copy-of select="$smtphost"/>));
         }
        Session _sendmail_session = Session.getDefaultInstance (_sendmail_properties,null);
        Message _sendmail_message = new MimeMessage (_sendmail_session);
        _sendmail_message.setFrom (new InternetAddress (String.valueOf (<xsl:call-template name="get-nested-string"><xsl:with-param name="content" select="sendmail:from"/></xsl:call-template>)));

        <xsl:if test="sendmail:reply-to">
          InternetAddress[] _sendmail_ias = new InternetAddress[<xsl:value-of select="count(sendmail:reply-to)"/>];
          <xsl:for-each select="sendmail:reply-to">
            _sendmail_ias[<xsl:value-of select="position()-1"/>] = new InternetAddress (String.valueOf (<xsl:call-template name="get-nested-string"><xsl:with-param name="content" select="."/></xsl:call-template>));
          </xsl:for-each>
          _sendmail_message.setReplyTo (_sendmail_ias);
        </xsl:if>

        InternetAddress _sendmail_ia = null;
        <xsl:for-each select="sendmail:to">
          _sendmail_ia = new InternetAddress (String.valueOf (<xsl:call-template name="get-nested-string"><xsl:with-param name="content" select="."/></xsl:call-template>));
          _sendmail_message.addRecipient (Message.RecipientType.TO, _sendmail_ia);
        </xsl:for-each>
        <xsl:for-each select="sendmail:cc">
          _sendmail_ia = new InternetAddress (String.valueOf (<xsl:call-template name="get-nested-string"><xsl:with-param name="content" select="."/></xsl:call-template>));
          _sendmail_message.addRecipient (Message.RecipientType.CC, _sendmail_ia);
        </xsl:for-each>
        <xsl:for-each select="sendmail:bcc">
          _sendmail_ia = new InternetAddress (String.valueOf (<xsl:call-template name="get-nested-string"><xsl:with-param name="content" select="."/></xsl:call-template>));
          _sendmail_message.addRecipient (Message.RecipientType.BCC, _sendmail_ia);
        </xsl:for-each>

        _sendmail_message.setSentDate (new Date());
        _sendmail_message.setSubject (String.valueOf (<xsl:copy-of select="$subject"/>));
        _sendmail_message.setText (String.valueOf (<xsl:copy-of select="$body"/>));

        <xsl:apply-templates select="sendmail:debug"/>
        Transport.send (_sendmail_message);
       }
      catch (AddressException _sendmail_e)
       {
        <sendmail:error type="user">
          <xsp:attribute name="string"><xsp:expr>_sendmail_e.getRef()</xsp:expr></xsp:attribute>
          <xsp:attribute name="position"><xsp:expr>_sendmail_e.getPos()</xsp:expr></xsp:attribute>
          The email address is invalid.
        </sendmail:error>
       }
      catch (MessagingException _sendmail_e)
       {
        <sendmail:error type="server">
          <xsp:expr>_sendmail_e.getMessage()</xsp:expr>
        </sendmail:error>
       }
    </xsp:logic>
  </xsl:template>


  <xsl:template match="sendmail:debug">
    <sendmail:debug sendmail:version="1.1">
      <xsp:logic>
      InternetAddress[] _sendmail_addr = null;
      _sendmail_addr = (InternetAddress[])_sendmail_message.getFrom();
      if (_sendmail_addr != null)
        for (int _sendmail_i=0; _sendmail_i&lt;_sendmail_addr.length; _sendmail_i++)
         {  <sendmail:from><xsp:expr>_sendmail_addr[_sendmail_i].toString()</xsp:expr></sendmail:from>  }
      _sendmail_addr = (InternetAddress[])_sendmail_message.getReplyTo();
      if (_sendmail_addr != null)
        for (int _sendmail_i=0; _sendmail_i&lt;_sendmail_addr.length; _sendmail_i++)
         {  <sendmail:reply-to><xsp:expr>_sendmail_addr[_sendmail_i].toString()</xsp:expr></sendmail:reply-to>  }
      _sendmail_addr = (InternetAddress[])_sendmail_message.getRecipients (Message.RecipientType.TO);
      if (_sendmail_addr != null)
        for (int _sendmail_i=0; _sendmail_i&lt;_sendmail_addr.length; _sendmail_i++)
         {  <sendmail:to><xsp:expr>_sendmail_addr[_sendmail_i].toString()</xsp:expr></sendmail:to>  }
      _sendmail_addr = (InternetAddress[])_sendmail_message.getRecipients (Message.RecipientType.CC);
      if (_sendmail_addr != null)
        for (int _sendmail_i=0; _sendmail_i&lt;_sendmail_addr.length; _sendmail_i++)
         {  <sendmail:cc><xsp:expr>_sendmail_addr[_sendmail_i].toString()</xsp:expr></sendmail:cc> }
      _sendmail_addr = (InternetAddress[])_sendmail_message.getRecipients(Message.RecipientType.BCC);
      if (_sendmail_addr != null)
        for (int _sendmail_i=0; _sendmail_i&lt;_sendmail_addr.length; _sendmail_i++)
         {  <sendmail:bcc><xsp:expr>_sendmail_addr[_sendmail_i].toString()</xsp:expr></sendmail:bcc>  }
        <sendmail:subject><xsp:expr>_sendmail_message.getSubject()</xsp:expr></sendmail:subject>
        <sendmail:content><xsp:expr>(String)_sendmail_message.getContent()</xsp:expr></sendmail:content>
      </xsp:logic>
    </sendmail:debug>
  </xsl:template>


  <xsl:template match="@*|node()" priority="-1">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>


  <xsl:template name="get-nested-string">
    <xsl:param name="content"/>
    <xsl:choose>
      <xsl:when test="$content/*">
        ""
        <xsl:for-each select="$content/node()">
          <xsl:choose>
            <xsl:when test="name(.)"> 
              + <xsl:apply-templates select="."/>
            </xsl:when>
            <xsl:otherwise>
              + "<xsl:value-of select="translate(.,'&#9;&#10;&#13;','   ')"/>"
            </xsl:otherwise>
          </xsl:choose>
        </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>"<xsl:value-of select="normalize-space($content)"/>"</xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
