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

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output indent="yes" method="xml"/>

<!-- specify here the language of the catalogue you would like to create -->
<xsl:param name="lang">hy</xsl:param>

<xsl:template match="translations">
	<catalogue xml:lang="{$lang}">
		<xsl:apply-templates select="entry"/>
	</catalogue>
</xsl:template>

<xsl:template match="entry">
	<message key="{key}">
		<xsl:value-of select="translation[@lang=$lang]"/>
	</message>
</xsl:template>

</xsl:stylesheet>
