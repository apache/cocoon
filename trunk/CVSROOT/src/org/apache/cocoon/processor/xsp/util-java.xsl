<?xml version="1.0"?>

<!-- $Id: util-java.xsl,v 1.1 2000-01-03 01:42:50 stefano Exp $

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================
 
    Copyright (C) 1999 The Apache Software Foundation. All rights reserved.
 
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

<!-- written by Ricardo Rocha "ricardo@apache.org" -->

<xsl:stylesheet
  xmlns:xsl="http://www.w3.org/XSL/Transform/1.0"
  xmlns:xsp="http://apache.org/DTD/XSP/Layer1"
  xmlns:util="http://apache.org/tags/util"
>

  <xsl:template match="xsp:page">
    <xsp:page>
      <xsl:copy>
        <xsl:apply-templates select="@*"/>
      </xsl:copy>

      <xsp:structure>
        <xsp:include>java.util.Date</xsp:include>
        <xsp:include>java.text.SimpleDateFormat</xsp:include>
      </xsp:structure>

      <xsp:logic>
	       /* Util Class Level */
        private static int count = 0;
        
        private static synchronized int getCount() {
          return ++count;
        }
        
        private static synchronized int getSessionCount(HttpSession session) {
          Integer integer = (Integer) session.getValue("util.counter");
          if (integer == null) {
            integer = new Integer(0);
          }
          int cnt = integer.intValue() + 1;
          session.putValue("util.counter", new Integer(cnt));
          return cnt;
        }
        
       	private static String formatDate(Date date, String pattern) {
       	  if (pattern == null || pattern.length() == 0) {
       	    pattern = "yyyy/MM/dd hh:mm:ss aa";
       	  }
       	  return (new SimpleDateFormat(pattern)).format(date);
       	}
      </xsp:logic>

      <xsl:apply-templates/>
    </xsp:page>
  </xsl:template>

  <xsl:template match="util:counter">
    <xsl:choose>
      <xsl:when test="@scope = 'session'">
        <xsp:expr>getSessionCount(session)</xsp:expr>
      </xsl:when>
      <xsl:otherwise>
        <xsp:expr>getCount()</xsp:expr>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="util:time">
    <xsp:expr>
      formatDate(new Date(), "<xsl:value-of select="@format"/>")
    </xsp:expr>
  </xsl:template>

  <xsl:template match="util:include">
    <xsp:logic>
      xspCurrentNode.appendChild(
        XSPUtil.cloneNode(
          this.xspParser.parse(
	           new InputSource(
              new FileReader(
                XSPUtil.relativeFilename(
                  "<xsl:value-of select="@file"/>",
                  request
                )
              )
            )
          ).getDocumentElement(),
          document
        )
      );
    </xsp:logic>
  </xsl:template>

  <xsl:template match="@*|node()" priority="-1">
    <xsl:copy><xsl:apply-templates select="@*|node()"/></xsl:copy>
  </xsl:template>

</xsl:stylesheet>
