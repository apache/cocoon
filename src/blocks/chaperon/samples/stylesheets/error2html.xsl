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

<!-- CVS $Id: error2html.xsl,v 1.3 2004/06/22 02:41:15 crossley Exp $ -->

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:lex="http://chaperon.sourceforge.net/schema/lexer/2.0">

  <xsl:param name="contextPath"/>

  <!-- let sitemap override default page title -->
  <xsl:param name="pageTitle" select="//parse-exception/message"/>

  <xsl:template match="parse-exception">
    <html>
      <head>
        <title>
          <xsl:value-of select="$pageTitle"/>
        </title>
        <link href="{$contextPath}/styles/main.css" type="text/css" rel="stylesheet"/>
        <style>
          h1 { color: #336699; text-align: left; margin: 0px 0px 30px 0px; padding: 0px; border-width: 0px 0px 1px 0px; border-style: solid; border-color: #336699;}
          p.message { padding: 10px 30px 10px 30px; font-weight: bold; font-size: 130%; border-width: 1px; border-style: dashed; border-color: #336699; }
          p.description { padding: 10px 30px 20px 30px; border-width: 0px 0px 1px 0px; border-style: solid; border-color: #336699;}
          p.topped { padding-top: 10px; border-width: 1px 0px 0px 0px; border-style: solid; border-color: #336699; }
          pre { font-size: 120%; }
          table { margin: 0px; border: 0px; padding: 0px; }
          tr { margin: 0px; border: 0px; padding: 0px; }
          td { margin: 0px; border: 0px; padding: 0px; }
          span.lt { background-color: #e5ffe5; margin: 0px; border: 0px; padding: 0px; }
          span.eq { background-color: #ff0000; margin: 0px; border: 0px; padding: 0px; }
          span.gt { background-color: #ffe5e5; margin: 0px; border: 0px; padding: 0px; }
        </style>
      </head>
      <body>
        <h1>Error</h1>

        <p class="message">
          <xsl:value-of select="message"/>
        </p>

        <p class="topped"/>

        <p class="extra"><span class="description">column&#160;</span><xsl:value-of select="@column-number"/></p>
        <p class="extra"><span class="description">line&#160;</span><xsl:value-of select="@line-number"/></p>
        <xsl:if test="source">
         <p class="extra"><span class="description">source&#160;</span><xsl:value-of select="source/@ref"/></p>
        </xsl:if>

        <xsl:apply-templates select="source"/>

        <p class="topped">
          For more detailed technical information, take a look at the log
          files in the log directory of Cocoon, which is placed by default in
          the <code>WEB-INF/logs/</code> folder of your cocoon webapp context.<br/>
          If the logs don't give you enough information, you might want to increase the
          log level by changing the Logging configuration which is by default the
          <code>WEB-INF/logkit.xconf</code> file.
        </p>

        <p>
          If you think you found a bug, please report it to
          <a href="http://issues.apache.org/bugzilla/">Apache's Bugzilla</a>;
          a message will automatically be sent to the developer mailing list and you'll
          be kept in contact automatically with the further progress on that bug.
        </p>

        <p>
          Thanks, and sorry for the trouble if this is our fault.
        </p>

        <p class="topped">
          The <a href="http://cocoon.apache.org/">Apache Cocoon</a> Project
        </p>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="source">
   <xsl:variable name="line-number" select="number(../@line-number)"/>

   <p class="topped">
    <pre>
     <xsl:apply-templates select="lex:output/lex:lexeme[(number(@line) &lt; $line-number) and (number(@line) &gt; number($line-number - 10))]" mode="lt"/>
     <xsl:apply-templates select="lex:output/lex:lexeme[number(@line) = $line-number]" mode="eq"/>
     <xsl:apply-templates select="lex:output/lex:lexeme[(number(@line) &gt; $line-number) and (number(@line) &lt; number($line-number + 10))]" mode="gt"/>
    </pre>
   </p>

  </xsl:template>

  <xsl:template match="lex:lexeme" mode="lt">
   <xsl:value-of select="@line"/>&#160;:&#160;<span class="lt"><xsl:value-of select="@text"/></span>
  </xsl:template>

  <xsl:template match="lex:lexeme" mode="eq">
   <xsl:variable name="column-number" select="number(../../../@column-number)"/>
   <xsl:value-of select="@line"/>&#160;:&#160;<span class="lt"><xsl:value-of select="substring(@text, 1, $column-number - 1)"/></span>
   <span class="eq"><xsl:value-of select="substring(@text, $column-number, 1)"/></span>
   <span class="gt"><xsl:value-of select="substring(@text, $column-number + 1, string-length(@text) - $column-number)"/></span>
  </xsl:template>

  <xsl:template match="lex:lexeme" mode="gt">
   <xsl:value-of select="@line"/>&#160;:&#160;<span class="gt"><xsl:value-of select="@text"/></span>
  </xsl:template>

 <xsl:template match="@*|*|text()|processing-instruction()" priority="-1">
  <xsl:copy>
   <xsl:apply-templates select="@*|*|text()|processing-instruction()"/>
  </xsl:copy>
 </xsl:template>

</xsl:stylesheet>
