<?xml version="1.0"?>
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

<!--

  This stylesheet is used to copy the BUTransformer's output into a data island in html
  so that AJAX-type updates in CForms can use IoFrameTransport in Dojo which allows AJAX-style file uploads etc.
  
-->

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:bu="http://apache.org/cocoon/browser-update/1.0"
                exclude-result-prefixes="bu"
                >

<!-- this transformtion could be built-in to the BUTransformer -->

	<xsl:template match="/">
		<html>
			<head><title>Browser Update Data-Island</title></head>
			<body>
				<xsl:apply-templates/>
			</body>
		</html>
	</xsl:template>

	<xsl:template match="bu:document">
		<form  id="browser-update">
			<xsl:apply-templates select="@*|*"/>
		</form>
	</xsl:template>	

	<!-- wrap the BU Data in a textarea, nasty but widely compatible -->
	<xsl:template match="bu:*">
		<textarea name="{local-name()}" id="{@id}">
			<xsl:apply-templates/>
		</textarea>
	</xsl:template>	

  <xsl:template match="@*|node()" priority="-1">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
