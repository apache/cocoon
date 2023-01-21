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
<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:page="http://apache.org/cocoon/paginate/1.0"
  xmlns:mail="http://apache.org/cocoon/mail/1.0"
  exclude-result-prefixes="xsl page"
>

<xsl:template match="/">
  <!--xsl:copy-of select="."/-->

  <document>
    <header>
      <title>Message</title>
    </header>
    <body>
      <!-- get subject having a max. number of chars
      -->
      <xsl:variable name="subject" select="/root/mail:mail/mail:message-envelope/mail:subject"/>
      <xsl:variable name="subject-title">
        <xsl:choose>
          <xsl:when test="string-length($subject) &gt; 32">
            <xsl:value-of select="substring($subject, 1, 32 )"/>...
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$subject"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      
      <s1>
        <xsl:attribute name="title">Message #<xsl:value-of select="/root/mail:mail/mail:message-envelope/mail:message-number"/> <xsl:text> - </xsl:text><xsl:value-of select="$subject-title"/> </xsl:attribute>
        
        <table class="list" style="margin-left: auto; margin-right: auto;
          width: 95%;
          border-collapse: collapse;
          border-top: 1px dotted;
          border-bottom: 1px dotted;
          " >
          <tr>
            <th align="left">#</th>
            <th align="left">From</th>
            <th align="left">Subject</th>
            <th align="left">Sent</th>
          </tr>
          <xsl:apply-templates select="/root/mail:mail/mail:message-envelope"/>
        </table>
        
        <div class="row">
          <span class="left">
            <xsl:variable name="prev" select="/root/mail:mail/mail:message-envelope/mail:message-number -1"/>
            [<link href="mail.html?id={$prev}">Prev</link>]
          </span>
          <span class="right">
            <xsl:variable name="next" select="/root/mail:mail/mail:message-envelope/mail:message-number +1"/>
            [<link href="mail.html?id={$next}">Next</link>]
          </span>
        </div>
        <s2 title="Body">
          <xsl:apply-templates select="/root/mail:mail/mail:part"/>
        </s2>
        <s2 title="Attachments">
          <ul>
            <xsl:apply-templates select="/root/mail:mail/mail:part" mode="attachment"/>
          </ul>
        </s2>
      </s1>
    </body>
  </document>
</xsl:template>

  <xsl:template match="mail:message-envelope">
    <tr>
      <td><xsl:value-of select="mail:message-number"/></td>
      <td><xsl:value-of select="mail:from/@personal"/> &lt;<xsl:value-of select="mail:from/@email-address"/>&gt; </td>
      <td><xsl:value-of select="mail:subject"/> </td>
      <td><xsl:value-of select="mail:sent-date"/> </td>
    </tr>  
  </xsl:template>

  <xsl:template match="mail:part[@base-type = 'multipart/report']">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="mail:part[@base-type = 'multipart/alternative']">
    <xsl:apply-templates select="mail:content/mail:part"/>
  </xsl:template>
  
  <xsl:template match="mail:part[@base-type = 'multipart/mixed']">
    <xsl:apply-templates select="mail:content/mail:part"/>
  </xsl:template>
  
  <xsl:template match="mail:part[@base-type = 'text/plain']">
    <xsl:apply-templates/>
  </xsl:template>
  
  <xsl:template match="mail:part">
  </xsl:template>
  
  <xsl:template match="mail:content">
    <source>
      <xsl:value-of select="."/>
    </source>
  </xsl:template>

  <!-- attachments -->
  
  <xsl:template match="mail:part[@base-type = 'multipart/mixed']" mode="attachment">
    <xsl:apply-templates select="mail:content/mail:part" mode="attachment"/>
  </xsl:template>
  
  <xsl:template match="mail:part" mode="attachment">
    <li>
      Part: <xsl:value-of select="@part-num"/>, 
      <xsl:value-of select="@name"/>,
      <xsl:value-of select="@file-name"/>,
      <xsl:value-of select="@base-type"/>,
    </li>
  </xsl:template>
  
  <xsl:template match="mail:content" mode="attachment">
  </xsl:template>
  
</xsl:stylesheet>

