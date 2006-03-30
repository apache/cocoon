<?xml version="1.0"?>
<!--
  Copyright 1999-2005 The Apache Software Foundation

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
<!-- SVN $Id$ -->
<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:param name="user"/>
  <xsl:param name="title"/>
  <xsl:param name="skin"/>
  <xsl:param name="rsrcprefix"/>

  <xsl:template match="/">
    <html>
      <head>
        <title><xsl:value-of select="$title"/></title>
        <link type="text/css" rel="stylesheet" href="{$rsrcprefix}skin/{$skin}/css/page.css"/>
        <link type="text/css" rel="stylesheet" href="{$rsrcprefix}skin/{$skin}/css/wsrp.css"/>
        <link type="text/css" rel="stylesheet" href="{$rsrcprefix}skin/{$skin}/css/portal-page.css"/>
        <script src="{$rsrcprefix}resources/ajax/js/cocoon-ajax.js" type="text/javascript"/>
        <script src="{$rsrcprefix}resources/portal/js/cocoon-portal.js" type="text/javascript"/>
      </head>
      <body>
        <div class="cocoon-portal-header">
          
          <xsl:if test="$user!='anonymous'">
            <div id="cocoon-portal-header-logout">
              <a href="logout"><img src="{$rsrcprefix}skin/{$skin}/images/logout-door.gif" width="18" height="22" border="0"/></a>
              <a href="logout">Logout</a>
              &#160;
              <a href="tools/">Tools</a>
            </div>
          </xsl:if>
          <div id="cocoon-portal-header-logo" >
            <img src="{$rsrcprefix}skin/{$skin}/images/portal-logo.gif" width="250" height="90"/>
	    </div>
	    </div>
	    
        <div class="cocoon-portal-content">
          <xsl:copy-of select="*"/>
	    </div>
        <div class="cocoon-portal-footer">
	    </div>
      </body>
    </html>
  </xsl:template>

</xsl:stylesheet>
