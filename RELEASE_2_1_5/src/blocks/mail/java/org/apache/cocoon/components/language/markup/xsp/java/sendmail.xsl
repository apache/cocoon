<?xml version="1.0"?>
<!--
  Copyright 1999-2004 The Apache Software Foundation

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

<!--
 * This is a stylesheet to send mail via the java mail API.
 *
 * @author Donald A. Ball Jr.
 * @author Christian Haul
 * @author Frank Riddersbusch
 * @version CVS $Revision: 1.4 $ $Date: 2004/05/09 20:05:59 $
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
         org.apache.cocoon.mail.MailSender _sendmail_mms = null;
       	 try {
	         _sendmail_mms = (org.apache.cocoon.mail.MailSender) this.manager.lookup(org.apache.cocoon.mail.MailSender.ROLE);
         } catch (org.apache.avalon.framework.component.ComponentException e) {
         	throw new ProcessingException("Could not setup mail components.", e);
         }

      <xsl:if test="$smtphost != ''">
         _sendmail_mms.setSmtpHost(String.valueOf(<xsl:copy-of select="$smtphost"/>));
      </xsl:if>

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
         this.manager.release((org.apache.avalon.framework.component.Component) _sendmail_mms);
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
