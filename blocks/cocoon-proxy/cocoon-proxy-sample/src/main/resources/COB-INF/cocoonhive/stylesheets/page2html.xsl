<?xml version="1.0" encoding="UTF-8"?>
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
  xmlns:ch="http://cocoonhive.org/portal/schema/2002"
  >
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
	
	<xsl:template match="ch:page">
  	   <html>
          <head>
            <title>CocoonHive.org Portal</title>
            <link type="text/css" href="stylesheets/page.css" rel="stylesheet"/>
          </head>
          <body>
            <a href="http://www.cocoonhive.org">CocoonHive</a> Portal
            
            <a href="http://cvs.apache.org/viewcvs.cgi/cocoon-2.1/src/webapp/samples/webserviceproxy/">View source (CVS)</a>
            <br/>
            <xsl:apply-templates/>
          </body>
        </html>
	</xsl:template>
	
	
	<xsl:template match="ch:menu">
        <div class="frame warning">
          <div class="label">Menu</div>
            <div class="warning">
              <xsl:apply-templates/>
            </div>
          </div>
        <br/>
	</xsl:template>
	
	<xsl:template match="ch:item">
            <xsl:element name="a">
              <xsl:attribute name="href"><xsl:value-of select="ch:command"/></xsl:attribute>
              <xsl:value-of select="ch:label"/>
            </xsl:element>
            <xsl:text> </xsl:text>
	</xsl:template>
	
	<xsl:template match="ch:frame">
        <div class="frame note">
        <div class="label"><xsl:value-of select="ch:title"/></div>
        <div class="note ">
        	  <xsl:copy-of select="ch:content/*"/>
        </div>
        </div>
	</xsl:template>

	<xsl:template match="*"/>

</xsl:stylesheet>
