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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:sql="http://apache.org/cocoon/SQL/2.0">

	<xsl:template match="employee">
		<sql:execute-query>
			<sql:query>
				<xsl:text>UPDATE employee SET name='</xsl:text>
				<xsl:value-of select="name"/>
				<xsl:text>' WHERE id =</xsl:text>
				<xsl:value-of select="@id"/>
			</sql:query>
		</sql:execute-query>
	</xsl:template>

</xsl:stylesheet>
