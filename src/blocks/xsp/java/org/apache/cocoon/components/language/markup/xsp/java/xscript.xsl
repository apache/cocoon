<?xml version="1.0" encoding="utf-8"?>

<!-- $Id: xscript.xsl,v 1.1 2004/03/10 12:58:06 stephan Exp $-->
<!--

 ============================================================================
                   The Apache Software License, Version 1.2
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
 *
 * Date: September 19, 2001
 *
 * @author <a href="mailto:ovidiu@cup.hp.com>Ovidiu Predescu</a>
 * @version CVS $Revision: 1.1 $ $Date: 2004/03/10 12:58:06 $
-->

<xsl:stylesheet
  version="1.0"
  xmlns:xsp="http://apache.org/xsp"
  xmlns:xscript="http://apache.org/xsp/xscript/1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:include href="xscript-lib.xsl"/>

  <xsl:template match="xsp:page">
    <xsp:page>
      <xsl:apply-templates select="@*"/>
      <xsp:structure>
        <xsp:include>org.apache.cocoon.components.xscript.*</xsp:include>
        <xsp:include>org.apache.avalon.framework.parameters.Parameters</xsp:include>
        <xsp:include>org.xml.sax.ContentHandler</xsp:include>
      </xsp:structure>

      <xsp:logic>
        XScriptVariableScope pageScope = new XScriptVariableScope();
        XScriptManager xscriptManager;

        public void compose(ComponentManager manager) throws ComponentException {
          super.compose(manager);
          xscriptManager = (XScriptManager)this.manager.lookup(XScriptManager.ROLE);
        }

        public void dispose() {
          if (xscriptManager != null) {
            manager.release((Component)xscriptManager);
            xscriptManager = null;
          }
          super.dispose();
        }
      </xsp:logic>

      <xsl:apply-templates/>
    </xsp:page>
  </xsl:template>


  <xsl:template match="xscript:variable">
    <xsl:variable name="scope">
      <xsl:call-template name="xscript-get-scope-for-creation">
        <xsl:with-param name="scope" select="@scope"/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:call-template name="xscript-variable">
      <xsl:with-param name="name" select="@name"/>
      <xsl:with-param name="href" select="@href"/>
      <xsl:with-param name="scope" select="$scope"/>
    </xsl:call-template>
  </xsl:template>


  <xsl:template match="xscript:get">
    <xsl:variable name="scope">
      <xsl:call-template name="xscript-get-scope">
        <xsl:with-param name="scope" select="@scope"/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:call-template name="xscript-get">
      <xsl:with-param name="name" select="@name"/>
      <xsl:with-param name="as" select="@as"/>
      <xsl:with-param name="scope" select="$scope"/>
    </xsl:call-template>
  </xsl:template>


  <xsl:template match="xscript:remove">
    <xsl:variable name="scope">
      <xsl:call-template name="xscript-get-scope">
        <xsl:with-param name="scope" select="@scope"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:call-template name="xscript-remove">
      <xsl:with-param name="name" select="@name"/>
      <xsl:with-param name="scope" select="$scope"/>
    </xsl:call-template>
  </xsl:template>


  <xsl:template match="xscript:transform">
    <xsl:variable name="name" select="@name"/>
    <xsl:variable name="scope">
      <xsl:call-template name="xscript-get-scope">
        <xsl:with-param name="scope" select="@scope"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="stylesheet" select="@stylesheet"/>
    <xsl:variable name="stylesheet-scope">
      <xsl:call-template name="xscript-get-scope">
        <xsl:with-param name="stylesheet-scope" select="@stylesheet-scope"/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:variable name="parameters">
      <xsl:copy-of select="xscript:parameter"/>
    </xsl:variable>

    <xsl:call-template name="xscript-transform">
      <xsl:with-param name="name" select="$name"/>
      <xsl:with-param name="scope" select="$scope"/>
      <xsl:with-param name="stylesheet" select="$stylesheet"/>
      <xsl:with-param name="stylesheet-scope" select="$stylesheet-scope"/>
      <xsl:with-param name="parameters" select="$parameters"/>
    </xsl:call-template>
  </xsl:template>


  <xsl:template match="xscript:copy-of | xscript:value-of">
    <xsl:variable name="parameters">
      <xscript:parameter name="xpath">
        <xsl:choose>
          <xsl:when test="string-length(@select) &gt; 0">
            <xsl:value-of select="@select"/>
          </xsl:when>
          <xsl:otherwise>/</xsl:otherwise>
        </xsl:choose>

      </xscript:parameter>
    </xsl:variable>
    <xsl:call-template name="xscript-builtin">
      <xsl:with-param name="builtin" select="name()"/>
      <xsl:with-param name="parameters" select="$parameters"/>
    </xsl:call-template>
  </xsl:template>


  <!-- PRIVATE -->
  <xsl:template name="xscript-builtin">
    <xsl:param name="builtin"/>
    <xsl:param name="parameters"/>
    <xsl:param name="name" select="@name"/>

    <xsl:variable name="scope">
      <xsl:call-template name="xscript-get-scope">
        <xsl:with-param name="scope" select="@scope"/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:call-template name="xscript-transform">
      <xsl:with-param name="parameters" select="$parameters"/>
      <xsl:with-param name="name" select="$name"/>
      <xsl:with-param name="scope" select="$scope"/>
      <xsl:with-param name="stylesheet" select="$builtin"/>
      <xsl:with-param name="stylesheet-scope">
        <xsl:call-template name="xscript-get-scope">
          <xsl:with-param name="scope" select="'global'"/>
        </xsl:call-template>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>


  <xsl:template match="@*|*|text()|processing-instruction()">
    <!-- Catch all template. Just pass along unmodified everything we
         don't handle. -->
    <xsl:copy>
      <xsl:apply-templates select="@*|*|text()|processing-instruction()"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
