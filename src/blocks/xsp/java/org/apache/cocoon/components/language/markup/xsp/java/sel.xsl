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

<!-- $Id: sel.xsl,v 1.2 2004/03/17 11:28:22 crossley Exp $-->
<!--
 * Sel Logicsheet (?)
 *
 * @author <a href="mailto:tcurdt@dff.st">Torsten Curdt</a>
 * @version CVS $Revision: 1.2 $ $Date: 2004/03/17 11:28:22 $
-->

<xsl:stylesheet version="1.0"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:xsp="http://apache.org/xsp"
        xmlns:sel="http://apache.org/xsp/sel/1.0"
        exclude-result-prefixes="sel"
>

<xsl:template match="xsp:page">
  <xsp:page>
    <xsl:copy-of select="@*"/>
    
    <!-- create an output method for every subpage so that 64K limit doesn't strike back -->
    <xsl:for-each select=".//sel:subpage|.//sel:default-subpage">
      <xsp:logic>
      private void _<xsl:value-of select="translate(parent::sel:subpage-set/@parameter,' -','__')"/>_<xsl:value-of select="translate(@name,' -','__')"/>_case() throws SAXException {
        AttributesImpl xspAttr = new AttributesImpl();
        <xsl:apply-templates/>
      }
      </xsp:logic>
    </xsl:for-each>

    <xsl:apply-templates select="*[not(name()='sel:subpage' or name()='sel:default-subpage')]"/>

  </xsp:page>
</xsl:template>

<!-- choice of subpages. select page to display with sitemap parameter. -->
<xsl:template match="sel:subpage-set">
  <xsp:logic>
    String selection = parameters.getParameter("<xsl:value-of select="translate(@parameter,' -','__')"/>", null);
  </xsp:logic>
  
  <!-- display named subpages -->
  <xsl:for-each select="sel:subpage">
    <xsp:logic>
      <xsl:if test="position() != 1"> else </xsl:if>
      if ("<xsl:value-of select="@name"/>".equals(selection)){
        _<xsl:value-of select="translate(parent::sel:subpage-set/@parameter,' -','__')"/>_<xsl:value-of select="translate(@name,' -','__')"/>_case();
      }
    </xsp:logic>
  </xsl:for-each>

  <!-- display default page -->
  <xsl:for-each select="sel:default-subpage">
    <xsp:logic>
      else {
        _<xsl:value-of select="translate(parent::sel:subpage-set/@parameter,' -','__')"/>_<xsl:value-of select="translate(@name,' -','__')"/>_case();
      }
    </xsp:logic>
  </xsl:for-each>

</xsl:template>

<!--##################################################################################-->

<xsl:template match="@*|node()" priority="-2"><xsl:copy><xsl:apply-templates select="@*|node()"/></xsl:copy></xsl:template>
<xsl:template match="text()" priority="-1"><xsl:value-of select="."/></xsl:template>

</xsl:stylesheet>
