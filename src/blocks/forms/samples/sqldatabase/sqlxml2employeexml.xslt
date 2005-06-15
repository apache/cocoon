<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:sql="http://apache.org/cocoon/SQL/2.0">
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
	<xsl:template match="sql:rowset[@sql:name='employees']">
		<employees>
			<xsl:apply-templates/>
		</employees>
	</xsl:template>
	<xsl:template match="sql:row">
		<employee id="{sql:id}">
			<name>
				<xsl:value-of select="sql:name"/>
			</name>
		</employee>
	</xsl:template>
</xsl:stylesheet>
