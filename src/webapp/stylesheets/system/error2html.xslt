<?xml version="1.0"?>

<!-- CVS $Id: error2html.xslt,v 1.16 2004/02/18 17:40:19 joerg Exp $ -->

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:error="http://apache.org/cocoon/error/2.1">

  <xsl:param name="contextPath"/>

  <!-- let sitemap override default page title -->
  <xsl:param name="pageTitle" select="//error:notify/error:title"/>

  <xsl:template match="error:notify">
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
        </style>
        <script src="{$contextPath}/scripts/main.js" type="text/javascript"/>
      </head>
      <body>
        <xsl:apply-templates select="." mode="onload"/>
        <h1><xsl:value-of select="$pageTitle"/></h1>

        <p class="message">
          <xsl:value-of select="error:message"/>
        </p>

        <p class="description">
          <xsl:value-of select="error:description"/>
        </p>

        <xsl:apply-templates select="error:extra"/>

        <p class="topped">
          If you need help and this information is not enough, you
          are invited to read the
          <a href="http://cocoon.apache.org/2.1/faq/">Cocoon FAQ</a>.<br/>
          If you still don't find the answers you need,
          can send a mail to the
          <a href="http://cocoon.apache.org/community/mail-lists.html">
          Cocoon mailing lists</a>,
          remembering to:
        </p>

        <ul>
          <li>specify the version of Cocoon you're using, or we'll assume that you
              are talking about the latest released version;</li>
          <li>specify the platform-operating system-version-servlet container version;</li>
          <li>send any pertinent error message;</li>
          <li>send pertinent log snippets;</li>
          <li>send pertinent sitemap snippets;</li>
          <li>send pertinent parts of the page that give you problems.</li>
        </ul>

        <p>
          For more detailed technical information, take a look at the log
          files in the log directory of Cocoon, which is placed by default in
          the <code>WEB-INF/logs/</code> folder of your cocoon webapp context.<br/>
          If the logs don't give you enough information, you might want to increase the
          log level by changing the Logging configuration which is by default the
          <code>WEB-INF/logkit.xconf</code> file.
        </p>

        <p>
          If you think you found a bug, please report it to
          <a href="http://nagoya.apache.org/bugzilla/">Apache's Bugzilla</a>;
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

  <xsl:template match="error:notify" mode="onload">
    <xsl:attribute name="onload">
      <xsl:for-each select="error:extra[contains(@error:description,'stacktrace')]">
        <xsl:text>toggle('</xsl:text>
        <xsl:value-of select="@error:description"/>
        <xsl:text>');</xsl:text>
      </xsl:for-each>
    </xsl:attribute>
  </xsl:template>

  <xsl:template match="error:extra">
    <xsl:choose>
     <xsl:when test="contains(@error:description,'stacktrace')">
      <p class="stacktrace">
       <span class="description"><xsl:value-of select="@error:description"/></span>
       <span class="switch" id="{@error:description}-switch" onclick="toggle('{@error:description}')">[hide]</span>
       <pre id="{@error:description}">
         <xsl:value-of select="translate(.,'&#13;','')"/>
       </pre>
      </p>
     </xsl:when>
     <xsl:otherwise>
      <p class="extra">
       <span class="description"><xsl:value-of select="@error:description"/>:&#160;</span>
       <xsl:value-of select="."/>
      </p>
     </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
