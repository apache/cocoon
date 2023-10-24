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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://maven.apache.org/POM/4.0.0" xmlns:pom="http://maven.apache.org/POM/4.0.0" exclude-result-prefixes="pom">
	<xsl:param name="file"/>
	<xsl:param name="groupId"/>
	<xsl:param name="artifactId"/>
	<xsl:param name="version"/>

	<xsl:output method="xml" indent="yes" encoding="UTF-8"/>
	
	<xsl:template match="/">
		<!--xsl:message>Processing file '<xsl:value-of select="$file"/>' ...</xsl:message-->
		<xsl:apply-templates/>
	</xsl:template>
	
	<xsl:template match="*">
		<!--xsl:message>  *: <xsl:value-of select="name()"/></xsl:message-->
	   <xsl:copy>
		  <xsl:copy-of select="@*" />
		  <xsl:apply-templates />
	   </xsl:copy>
	</xsl:template>
	
	<xsl:template match="comment()|processing-instruction()">
		<!--xsl:message>  comment/pi</xsl:message-->
	   <xsl:copy />
	</xsl:template>
	
	<xsl:template match="pom:version">
		<xsl:choose>
			<xsl:when test="(../pom:artifactId=$artifactId) and ((../pom:groupId=$groupId) or (not(../pom:groupId) and (../pom:parent/pom:groupId=$groupId)))">
				<!-- Silbling groupId and artifactId match (if sibling groupId is missing then the groupId of the parent POM must match) -->
				<xsl:message>Replaced version '<xsl:value-of select="."/>' with '<xsl:value-of select="$version"/>' for artifact '<xsl:value-of select="$groupId"/>:<xsl:value-of select="$artifactId"/>' in file '<xsl:value-of select="$file"/>'.</xsl:message>
				<version><xsl:value-of select="$version"/></version>
			</xsl:when>
			<xsl:otherwise>
				<!--xsl:message>  otherwise: <xsl:value-of select="../pom:parent/pom:groupId"/>/<xsl:value-of select="../pom:groupId"/>, <xsl:value-of select="../pom:artifactId"/>, <xsl:value-of select="."/></xsl:message-->
				<!-- Keep the version for all other artifacts -->
				<xsl:copy-of select="."/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
</xsl:stylesheet>
