<?xml version="1.0"?>

<!-- $Id: sendmail.xsl,v 1.1 2003/03/09 00:08:57 pier Exp $-->
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
 * @version CVS $Revision: 1.1 $ $Date: 2003/03/09 00:08:57 $
-->
<xsl:stylesheet
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xsp="http://apache.org/xsp"
  xmlns:sendmail="http://apache.org/cocoon/sendmail/1.0"
  version="1.0"
  >
  
  <xsl:param name="XSP-ENVIRONMENT"/>
  <xsl:param name="XSP-VERSION"/>
  <xsl:param name="filename"/>
  <xsl:param name="language"/>
  
  <xsl:variable name="cocoon1-environment">Cocoon 1</xsl:variable>
  <xsl:variable name="cocoon2-environment">Cocoon 2</xsl:variable>
  
  <xsl:variable name="cocoon1-xsp-namespace-uri">http://www.apache.org/1999/XSP/Core</xsl:variable>
  <xsl:variable name="cocoon2-xsp-namespace-uri">http://apache.org/xsp</xsl:variable>
  <xsl:variable name="prefix">sendmail</xsl:variable>

  <xsl:variable name="environment">
    <xsl:choose>
      <xsl:when test="starts-with($XSP-ENVIRONMENT,$cocoon1-environment)">
        <xsl:text>Cocoon1</xsl:text>
      </xsl:when>
      <xsl:when test="starts-with($XSP-ENVIRONMENT,$cocoon2-environment)">
        <xsl:text>Cocoon2</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>Cocoon2</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:variable name="xsp-namespace-uri">
    <xsl:choose>
      <xsl:when test="$environment = 'Cocoon1'">
        <xsl:value-of select="$cocoon1-xsp-namespace-uri"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$cocoon2-xsp-namespace-uri"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:template name="get-nested-content">
    <xsl:param name="content"/>
    <xsl:choose>
      <xsl:when test="$content/xsp:text">"<xsl:value-of select="$content"/>"</xsl:when>
      <xsl:when test="$content/*">
        <xsl:apply-templates select="$content/*"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$content"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="get-nested-string">
    <xsl:param name="content"/>
    <xsl:choose>
      <xsl:when test="$environment = 'Cocoon1'">
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
      </xsl:when>
      <xsl:otherwise>
        <xsl:choose>
        <!-- if $content has sub-elements, concatenate them -->
          <xsl:when test="$content/*"> 
            "" 
            <xsl:for-each select="$content/node()">
              <xsl:choose>
                <xsl:when test="name(.)">
                <!-- element -->
                  <xsl:choose>
                  <!-- xsp:text element -->
                    <xsl:when test="namespace-uri(.)='http://apache.org/xsp' and local-name(.)='text'"> 
                      + "<xsl:value-of select="translate(.,'&#9;&#10;&#13;','   ')"/>"
                  </xsl:when>
                  <!-- other elements -->
                    <xsl:otherwise>
                      + <xsl:apply-templates select="."/>
                    </xsl:otherwise>
                  </xsl:choose>
                </xsl:when>
                <!-- text node -->
                <xsl:otherwise> 
                  + "<xsl:value-of select="translate(.,'&#9;&#10;&#13;','   ')"/>"
                </xsl:otherwise>
              </xsl:choose>
            </xsl:for-each>
          </xsl:when>
          <!-- else return the text value of $content -->
          <xsl:otherwise>"<xsl:value-of select="normalize-space($content)"/>"</xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  


  <xsl:template match="xsp:page">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsp:structure>
        <xsp:include>javax.mail.Message</xsp:include>
        <xsp:include>javax.mail.Transport</xsp:include>
        <!-- xsp:include>javax.mail.Session</xsp:include -->
        <xsp:include>javax.mail.MessagingException</xsp:include>
        <xsp:include>javax.mail.internet.InternetAddress</xsp:include>
        <xsp:include>javax.mail.internet.MimeMessage</xsp:include>
        <xsp:include>javax.mail.internet.AddressException</xsp:include>
        <xsp:include>java.util.Date</xsp:include>
        <xsp:include>java.util.Properties</xsp:include>
      </xsp:structure>
      <xsp:logic>
        static Properties _sendmail_properties;
        static {
           _sendmail_properties = new Properties();
           _sendmail_properties.put("mail.smtp.host","127.0.0.1");
        }
      </xsp:logic>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>


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
       try {
         Properties _sendmail_properties = new Properties(this._sendmail_properties);
      <xsl:if test="sendmail:smtphost">
         if (!"null".equals(String.valueOf(<xsl:copy-of select="$smtphost"/>))) {
            _sendmail_properties.put("mail.smtp.host",String.valueOf(<xsl:copy-of select="$smtphost"/>));
         }
      </xsl:if>
         javax.mail.Session _sendmail_session =  javax.mail.Session.getDefaultInstance(_sendmail_properties,null);
         MimeMessage _sendmail_message = new MimeMessage(_sendmail_session);

         InternetAddress _sendmail_from = new InternetAddress(String.valueOf(<xsl:copy-of select="$from"/>));
         _sendmail_message.setFrom(_sendmail_from);

      <xsl:if test="sendmail:cc">
         if (!"null".equals(String.valueOf(<xsl:copy-of select="$cc"/>))){
            InternetAddress [] _sendmail_cc = InternetAddress.parse(String.valueOf(<xsl:copy-of select="$cc"/>));
            _sendmail_message.setRecipients(Message.RecipientType.CC,_sendmail_cc);
         }
      </xsl:if>
      <xsl:if test="sendmail:bcc">
         if (!"null".equals(String.valueOf(<xsl:copy-of select="$bcc"/>))){
            InternetAddress _sendmail_bcc = new InternetAddress(String.valueOf(<xsl:copy-of select="$bcc"/>));
            _sendmail_message.setRecipient(Message.RecipientType.BCC,_sendmail_bcc);
         }
      </xsl:if>
         InternetAddress _sendmail_to = new InternetAddress(String.valueOf(<xsl:copy-of select="$to"/>));
         _sendmail_message.setRecipient(Message.RecipientType.TO,_sendmail_to);
         _sendmail_message.setSentDate(new Date());
         _sendmail_message.setSubject(String.valueOf(<xsl:copy-of select="$subject"/>));
      <xsl:choose>
        <xsl:when test="sendmail:charset">
         if (!"null".equals(String.valueOf(<xsl:copy-of select="$charset"/>))) {
            _sendmail_message.setText(String.valueOf(<xsl:copy-of select="$body"/>),String.valueOf(<xsl:copy-of select="$charset"/>));
         } else {
            _sendmail_message.setText(String.valueOf(<xsl:copy-of select="$body"/>));
         }
      </xsl:when>
      <xsl:otherwise>
         _sendmail_message.setText(String.valueOf(<xsl:copy-of select="$body"/>));
      </xsl:otherwise>
    </xsl:choose>
         Transport.send(_sendmail_message);
       } catch (AddressException _sendmail_exception) {
      <error type="user">Your email address is invalid.</error>
       } catch (MessagingException _sendmail_exception) {
         <error type="server">An error occured while sending email.</error>
       }
    </xsp:logic>
  </xsl:template>
  


  <xsl:template match="@*|node()" priority="-1">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>
  
</xsl:stylesheet>
