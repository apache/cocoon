<?xml version="1.0"?>
<!--
 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) @year@ The Apache Software Foundation. All rights reserved.

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
-->
<xsl:stylesheet
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:xsp="http://www.apache.org/1999/XSP/Core"
 xmlns:sendmail="http://apache.org/cocoon/sendmail/v1"
 version="1.0"
>

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
      static Properties _properties;
      static {
        _properties = new Properties();
        _properties.put("mail.smtp.host","127.0.0.1");
      }
    </xsp:logic>
    <xsl:apply-templates/>
  </xsl:copy>
</xsl:template>

<xsl:template match="sendmail:send-mail">
  <xsl:variable name="from"><xsl:call-template name="get-nested-string"><xsl:with-param name="content" select="sendmail:from"/></xsl:call-template></xsl:variable>
  <xsl:variable name="to"><xsl:call-template name="get-nested-string"><xsl:with-param name="content" select="sendmail:to"/></xsl:call-template></xsl:variable>
  <xsl:variable name="subject"><xsl:call-template name="get-nested-string"><xsl:with-param name="content" select="sendmail:subject"/></xsl:call-template></xsl:variable>
  <xsl:variable name="body"><xsl:call-template name="get-nested-string"><xsl:with-param name="content" select="sendmail:body"/></xsl:call-template></xsl:variable>
  <xsl:variable name="smtphost"><xsl:call-template name="get-nested-string"><xsl:with-param name="content" select="sendmail:smtphost"/></xsl:call-template></xsl:variable>
  <xsp:logic>
    try {
      Properties _properties = new Properties(this._properties);
      if (!"null".equals(String.valueOf(<xsl:copy-of select="$smtphost"/>))) {
        _properties.put("mail.smtp.host",String.valueOf(<xsl:copy-of select="$smtphost"/>));
      }
      Session _sendmail_session = Session.getDefaultInstance(_properties,null);
      Message _message = new MimeMessage(_sendmail_session);
      InternetAddress _from = new InternetAddress(String.valueOf(<xsl:copy-of select="$from"/>));
      _message.setFrom(_from);
      InternetAddress _to = new InternetAddress(String.valueOf(<xsl:copy-of select="$to"/>));
      _message.setRecipient(Message.RecipientType.TO,_to);
      _message.setSentDate(new Date());
      _message.setSubject(String.valueOf(<xsl:copy-of select="$subject"/>));
      _message.setText(String.valueOf(<xsl:copy-of select="$body"/>));
      Transport.send(_message);
    } catch (AddressException e) {
      <error type="user">Your email address is invalid.</error>
    } catch (MessagingException e) {
      <error type="server">An error occured while sending email.</error>
    }
  </xsp:logic>
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
    <xsl:otherwise>
      "<xsl:value-of select="normalize-space($content)"/>"
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

</xsl:stylesheet>
