<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="block" xmlns:block="http://apache.org/cocoon/blocks/cob/1.0" version="1.0">
  <xsl:template match="/">
    <projectDescription>
    	<name><xsl:value-of select="/block:block/block:name"/></name>
    	<comment></comment>
    	<projects>
    	</projects>
    	<buildSpec>
    		<buildCommand>
    			<name>org.eclipse.jdt.core.javabuilder</name>
    			<arguments>
    			</arguments>
    		</buildCommand>
    	</buildSpec>
    	<natures>
    		<nature>org.eclipse.jdt.core.javanature</nature>
    	</natures>
    </projectDescription>
  </xsl:template>
</xsl:stylesheet>
