<?xml version="1.0" encoding="utf-8"?>

<!--

     Simple stylesheet to verify that files defined in lib/jars.xml
     actually appear in the lib/ directory, and vice-versa, that files
     that appear in the lib/ directory have an entry with a
     description in the lib/jars.xml file.

     Author: Ovidiu Predescu "ovidiu@cup.hp.com"
     Date: May 22, 2002

-->

<xsl:stylesheet
  version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output indent="yes" method="xml" doctype-public="-//APACHE//DTD Documentation V1.0//EN" doctype-system="../dtd/document-v10.dtd" />

  <xsl:strip-space elements="*" />

  <xsl:param name="current-files"/>

  <xsl:variable name="directory" select="document($current-files)"/>
  <xsl:variable name="jars.xml" select="/"/>

  <xsl:template match="/">
 	<!-- Validate lib/jars.xml prior to checking files -->

 	<!-- Verify that all the entries in lib/jars.xml have appropriate tags -->
    <xsl:apply-templates select="//file" mode="no-tag"/>

    <!-- Verify that all the file entries are unique -->
    <xsl:for-each select="$jars.xml/jars/file/lib">
      <xsl:variable name="this" select="normalize-space(text())"/>
      <xsl:if test="count($jars.xml/jars/file/lib[normalize-space(text()) = $this]) > 1">
        <xsl:message terminate="yes">
  Duplicate entry for file <xsl:value-of select="$this"/>
        </xsl:message>
      </xsl:if>
      <!-- ensure a decent filename -->
      <xsl:if test="not(contains(translate($this, '0123456789', '9999999999'), '9'))">
        <xsl:message terminate="no">
  [WARN] Poor filename for file <xsl:value-of select="$this"/>
  Please add version number or datestamp.
        </xsl:message>
      </xsl:if>
    </xsl:for-each>

    <!-- Verify if files declared in jars.xml appear in the lib/ directory -->
    <xsl:apply-templates select="jars//lib" mode="declared-but-doesnt-appear"/>

    <!-- Verify that files that appear in lib/ are declared in jars.xml -->
    <xsl:apply-templates select="$directory/jars" mode="appears-but-not-declared"/>
    
	<!-- create the documentation -->
	<document>
	 <header>
	  <title>Cocoon JARs</title>
	  <authors>
	   <person name="John Morrison" email="morrijr@apache.org"/>
	  </authors>
	 </header>
	 <body>
	 <s1 title="What, why and when...">
	  <p>This is a list of the available jars, what they are, where they come from,
	   and what they do.</p>
	  <table>
	   <tr>
	     <th>Title</th>
	     <th>Jar (type/name)</th>
	     <th>Description</th>
	     <th>Used by</th>
	   </tr>
	   <xsl:apply-templates select="//file" mode="documentation"/>
	  </table>
     </s1>
    </body>
   </document>
  </xsl:template>

  <!-- Template to verify if files declared in jars.xml appear in the
       lib/ directory
  -->
  <xsl:template match="lib" mode="declared-but-doesnt-appear">
    <xsl:variable name="this" select="normalize-space(text())"/>
    <xsl:if test="count($directory/jars/jar[normalize-space(text()) = $this]) = 0">
      <xsl:message terminate="yes">
  File <xsl:value-of select="$this"/> is declared in lib/jars.xml, but doesn't appear in the lib/ directory.

  If this file was removed, please update the lib/jars.xml file to remove this file entry.</xsl:message>
    </xsl:if>
  </xsl:template>

  <!-- Template to verify that files that appear in lib/ are declared
       in jars.xml
  -->
  <xsl:template match="jar" mode="appears-but-not-declared">
    <xsl:variable name="this" select="normalize-space(text())"/>
    <xsl:if test="count($jars.xml/jars/file/lib[normalize-space(text()) = $this]) = 0">
      <xsl:choose>
        <xsl:when test="starts-with($this, 'local/')">
          <xsl:message>
  [WARN] Using local library <xsl:value-of select="$this"/> : ensure it doesn't conflict with any other library.
          </xsl:message>
        </xsl:when>
        <xsl:otherwise>
          <xsl:message terminate="yes">
  File <xsl:value-of select="$this"/> appears in the lib/ directory, but is not declared in lib/jars.xml.

  Please update the lib/jars.xml file to include the <xsl:value-of select="$this"/> file together with a description.</xsl:message>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
  </xsl:template>

  <!-- Verify if a file element has a "title" tag -->
  <xsl:template match="file[not(title)]" mode="no-tag">
    <xsl:message terminate="yes">
  Entry for file <xsl:value-of select="normalize-space(lib/text())"/> in the lib/jars.xml does not have a "title" tag.

  Please add a title tag before continuing.</xsl:message>
  </xsl:template>

  <!-- Verify if a file element has a "description" tag -->
  <xsl:template match="file[not(description)]" mode="no-tag">
    <xsl:message terminate="yes">
  Entry for file <xsl:value-of select="normalize-space(lib/text())"/> in the lib/jars.xml does not have a "description" tag.

  Please add a description tag before continuing.</xsl:message>
  </xsl:template>

  <!-- Verify if a file element has an "used-by" tag -->
  <xsl:template match="file[not(used-by)]" mode="no-tag">
    <xsl:message terminate="yes">
  Entry for file <xsl:value-of select="normalize-space(lib/text())"/> in the lib/jars.xml does not have a "used-by" tag.

  Please add a used-by tag before continuing.</xsl:message>
  </xsl:template>

  <!-- Verify if a file element has an "lib" tag -->
  <xsl:template match="file[not(lib)]" mode="no-tag">
    <xsl:message terminate="yes">
  Entry for file <xsl:value-of select="normalize-space(title/text())"/> in the lib/jars.xml does not have a "lib" tag.

  Please add a lib tag before continuing.</xsl:message>
  </xsl:template>

  <!-- Verify if a file element has an "url" tag -->
  <xsl:template match="file[not(homepage)]" mode="no-tag">
    <xsl:message terminate="yes">
  Entry for file <xsl:value-of select="normalize-space(lib/text())"/> in the lib/jars.xml does not have a "homepage" tag.

  Please add a homepage tag before continuing.</xsl:message>
  </xsl:template>

  <!-- Format for documentation -->
  <xsl:template match="file" mode="documentation">
	<tr>
		<td><link href="{homepage}"><xsl:value-of select="title"/></link></td>
		<td><xsl:value-of select="lib"/></td>
		<td><xsl:value-of select="description"/></td>
		<td><xsl:value-of select="used-by"/></td>
	</tr>
  </xsl:template>

  <xsl:template match="file" mode="no-tag" priority="-1"/>
  <xsl:template match="file" mode="declared-but-doesnt-appear" priority="-1"/>
  <xsl:template match="file" mode="appears-but-not-declared" priority="-1"/>    

</xsl:stylesheet>
