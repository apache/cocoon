<?xml version="1.0"?>

<!-- $Id: sendmail.xsl,v 1.2 2003/07/01 22:22:03 haul Exp $-->
<!--

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
 * This is a stylesheet to send mail via the java mail API.
 *
 * @author Donald A. Ball Jr.
 * @author Christian Haul
 * @author Frank Riddersbusch
 * @version CVS $Revision: 1.2 $ $Date: 2003/07/01 22:22:03 $
-->
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xsp="http://apache.org/xsp"
  xmlns:sendmail="http://apache.org/cocoon/sendmail/1.0"
>

  <xsl:variable name="namespace-uri">http://apache.org/cocoon/sendmail/1.0</xsl:variable>
  <xsl:variable name="prefix">sendmail</xsl:variable>

  <xsl:include href="logicsheet-util.xsl"/>
  

  <!--
       Sends an email message. Email parameters need to be set using nested tags
       like <sendmail:to>users@cocoon.apache.org</sendmail:to>. Special tags are
       <sendmail:attachment/> for attachments and <sendmail:on-error/> resp. 
       <sendmail:on-success/> tags.
  -->
  <xsl:template match="sendmail:send-mail">
    <xsl:variable name="from">
      <xsl:call-template name="get-nested-string">
        <xsl:with-param name="content" select="sendmail:from"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="to">
      <xsl:call-template name="get-nested-string">
        <xsl:with-param name="content" select="sendmail:to"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="subject">
      <xsl:call-template name="get-nested-string">
        <xsl:with-param name="content" select="sendmail:subject"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="body">
      <xsl:call-template name="get-nested-string">
        <xsl:with-param name="content" select="sendmail:body"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="smtphost">
      <xsl:call-template name="get-nested-string">
        <xsl:with-param name="content" select="sendmail:smtphost"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="cc">
      <xsl:call-template name="get-nested-string">
        <xsl:with-param name="content" select="sendmail:cc"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="bcc">
      <xsl:call-template name="get-nested-string">
        <xsl:with-param name="content" select="sendmail:bcc"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="charset">
      <xsl:call-template name="get-nested-string">
        <xsl:with-param name="content" select="sendmail:charset"/>
      </xsl:call-template>
    </xsl:variable>
    <xsp:logic>
       { // sendmail
         org.apache.cocoon.mail.MailMessageSender _sendmail_mms =
           new org.apache.cocoon.mail.MailMessageSender(String.valueOf(<xsl:copy-of select="$smtphost"/>));

         _sendmail_mms.setTo(String.valueOf(<xsl:copy-of select="$to"/>));
         _sendmail_mms.setSubject(String.valueOf(<xsl:copy-of select="$subject"/>));
         _sendmail_mms.setFrom(String.valueOf(<xsl:copy-of select="$from"/>));
      <xsl:if test="sendmail:cc">
         _sendmail_mms.setCc(String.valueOf(<xsl:copy-of select="$cc"/>));
      </xsl:if>
      <xsl:if test="sendmail:bcc">
         _sendmail_mms.setBcc(String.valueOf(<xsl:copy-of select="$bcc"/>));
      </xsl:if>
         _sendmail_mms.setCharset(String.valueOf(<xsl:copy-of select="$charset"/>));
         _sendmail_mms.setBody(String.valueOf(<xsl:copy-of select="$body"/>));

      <xsl:apply-templates select="sendmail:attachment"/>

         if(_sendmail_mms.sendIt(resolver)){
            <xsl:apply-templates select="sendmail:on-success"/>
         } else {
            <xsl:choose>
              <xsl:when test="sendmail:on-error">
                 <xsl:apply-templates select="sendmail:on-error"/>
              </xsl:when>
              <xsl:otherwise>
              if (_sendmail_mms.getException() instanceof 
                  javax.mail.internet.AddressException) {
                  <error type="user">One of the given email address(es) is invalid.</error>
              } else if (_sendmail_mms.getException() instanceof
                  javax.mail.MessagingException) {
                  <error type="server">An error occured while sending email.</error>
              }
              </xsl:otherwise>
            </xsl:choose>
         } 
       }// sendmail
    </xsp:logic>
  </xsl:template>



  <!-- 
       Add an attachment to the message. Attachements could be uploads or URLs 
       e.g. using the cocoon:-protocol. Attachment name and mime-type can optionally
       be specified through attributes or <sendmail:param/> elements
       and override the detected values.
       Objects like uploads need to be added using a nested <sendmail:param/> tag.
       
       -->
  <xsl:template match="sendmail:send-mail//sendmail:attachment">
    <xsl:variable name="url">
      <xsl:call-template name="get-ls-parameter">
        <xsl:with-param name="name">url</xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="object">
      <xsl:call-template name="get-ls-parameter">
        <xsl:with-param name="name">object</xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="name">
      <xsl:call-template name="get-ls-parameter">
        <xsl:with-param name="name">name</xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="type">
      <xsl:call-template name="get-ls-parameter">
        <xsl:with-param name="name">mime-type</xsl:with-param>
      </xsl:call-template>
    </xsl:variable>

    <xsl:choose>
      <xsl:when test="$url='null'">
        _sendmail_mms.addAttachment(
        <xsl:copy-of select="$object"/>
      </xsl:when>
      <xsl:otherwise>
        _sendmail_mms.addAttachmentURL(
        <xsl:copy-of select="$url"/>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:if test="$type!='null' or $name!='null'">
          ,<xsl:copy-of select="$type"/>
          ,<xsl:copy-of select="$name"/>
    </xsl:if>
        );
  </xsl:template>

  <!-- 
       Branch of the page to go into when an exception occurred. If not present, 
       an <error/> tag is included with some details about the problem.
       -->
  <xsl:template match="sendmail:send-mail//sendmail:on-error">
    <xsl:apply-templates/>
  </xsl:template>

  <!--
       Returns the message included in the exception thrown.
       -->
  <xsl:template match="sendmail:send-mail//sendmail:on-error//sendmail:error-message">
    <xsp:expr>_sendmail_mms.getException().getMessage()</xsp:expr>
  </xsl:template>


  <!--
       Branch of the page to go into on successful sending mail.
       -->
  <xsl:template match="sendmail:send-mail//sendmail:on-success">
    <xsl:apply-templates/>
  </xsl:template>


  <xsl:template match="@*|node()" priority="-1">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
