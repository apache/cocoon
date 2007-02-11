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

<!--
  Author: Ovidiu Predescu "ovidiu@cup.hp.com"

  Date: October 9, 2001
 -->

<xsl:stylesheet
  version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
  xmlns:n="urn:xmethods-delayed-quotes"
  exclude-result-prefixes="soap n"
  >

  <xsl:template match="soap:Envelope">
    <b><xsl:value-of select="soap:Body/n:getQuoteResponse/Result"/></b>
  </xsl:template>

  <xsl:template match="@*|*|text()|processing-instruction()">
    <!-- Catch all template. Just pass along unmodified everything we
         don't handle. -->
    <xsl:copy>
      <xsl:apply-templates select="@*|*|text()|processing-instruction()"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
