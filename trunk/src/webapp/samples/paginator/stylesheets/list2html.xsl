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

<!-- CVS: $Id: list2html.xsl,v 1.2 2004/03/10 10:23:33 cziegeler Exp $ -->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="/">
   <html>
    <head>
     <title>Complete List</title>
    </head>
    <body bgcolor="white" alink="red" link="blue" vlink="blue">
     <h3>Complete List</h3>
     <xsl:apply-templates/>
    </body>
   </html>
  </xsl:template>

  <xsl:template match="list">
   <ul>
    <xsl:apply-templates/>
   </ul>
  </xsl:template>

  <xsl:template match="item">
   <li><xsl:apply-templates/></li>
  </xsl:template>

</xsl:stylesheet>
