<?xml version="1.0" encoding="UTF-8"?>
<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

<!-- CVS $Id: welcome.xslt,v 1.11 2004/03/06 02:25:58 antonio Exp $ -->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                              xmlns="http://www.w3.org/1999/xhtml">

  <xsl:param name="contextPath"/>

  <xsl:template match="welcome">
    <html xml:lang="en" lang="en">
      <head>
        <title>Welcome to Apache Cocoon!</title>
        <!-- 
             NOTE (SM): this meta tag reflects the *output* of the pipeline and not
             the encoding of this file. I agree it's sort of an hack and it should
             be the XHTML serializer to add the meta tag to the response, but, for
             now, this fixes encoding problems in those user-agents that don't parse
             the <?xml?> processing instruction to understand the encoding of the
             stream 
         --> 
        <meta http-equiv="Content-Type" content="text/xhtml; charset=UTF-8"/>
        <link href="{$contextPath}/styles/main.css" type="text/css" rel="stylesheet"/>
        <link href="favicon.ico" rel="SHORTCUT ICON" />
      </head>
      <body>
        <h1>Welcome to Apache Cocoon!</h1>
        <xsl:apply-templates/>
        <p class="copyright">
         Copyright © @year@ <a href="http://www.apache.org/">The Apache Software Foundation</a>. All rights reserved.
        </p>
        <p class="block">
          <a href="http://cocoon.apache.org/"><img src="{$contextPath}/images/powered.gif" alt="Powered by Apache Cocoon"/></a>
        </p>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="message">
    <p class="block"><xsl:apply-templates/></p>
  </xsl:template>
  
  <xsl:template match="link">
    <a href="{@href}"><xsl:apply-templates/></a>
  </xsl:template>

</xsl:stylesheet>
