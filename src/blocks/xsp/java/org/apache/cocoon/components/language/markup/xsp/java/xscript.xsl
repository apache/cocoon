<?xml version="1.0" encoding="utf-8"?>
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

<!-- $Id: xscript.xsl,v 1.2 2004/03/17 11:28:22 crossley Exp $-->
<!--
 *
 * Date: September 19, 2001
 *
 * @author <a href="mailto:ovidiu@cup.hp.com>Ovidiu Predescu</a>
 * @version CVS $Revision: 1.2 $ $Date: 2004/03/17 11:28:22 $
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
