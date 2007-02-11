<?xml version="1.0" encoding="UTF-8"?>
<!--
  Build the Eclipse .classpath file from a list of path items
  (see "eclipse-project" target in build.xml)
  
  @author Sylvain Wallez
  @version CVS $Id: make-classpath.xsl,v 1.2 2003/03/10 16:38:58 stefano Exp $
-->

<xsl:stylesheet
  version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output indent="yes" method="xml"/>

  <xsl:strip-space elements="*"/>

  <xsl:template match="/data">
    <classpath>

      <!-- 1. mock classes -->
      <xsl:for-each select="mock-dirs/item">
        <classpathentry kind="src" path="{.}"/>
      </xsl:for-each>

      <!-- 2. source dirs -->
      <xsl:for-each select="src-dirs/item">
        <classpathentry kind="src" path="{.}"/>
      </xsl:for-each>
          
      <!-- 3. libraries -->
      <xsl:for-each select="libs/item">
        <classpathentry kind="lib" path="{.}"/>
      </xsl:for-each>     

      <!-- 4. JRE runtime -->
      <classpathentry kind="var" path="JRE_LIB" rootpath="JRE_SRCROOT" sourcepath="JRE_SRC"/>
     
      <!-- output directory. Build in a separate dir since Eclipse is confused by classes
           compiled externally by Sun's Javac -->
      <classpathentry kind="output" path="{output}"/>

    </classpath>
  </xsl:template>

</xsl:stylesheet>
