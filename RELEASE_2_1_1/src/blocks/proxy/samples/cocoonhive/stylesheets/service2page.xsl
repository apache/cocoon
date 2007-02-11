<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:ch="http://cocoonhive.org/portal/schema/2002"
  >
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
	
	<xsl:param name="title"/>
	
	<xsl:template match="/">

          <ch:page xmlns:ch="http://cocoonhive.org/portal/schema/2002">
            <ch:menu>
              <ch:item>
                <ch:label>Home</ch:label>
                <ch:command>home</ch:command>
              </ch:item>
            </ch:menu>

            <ch:frame>
              <ch:title><xsl:value-of select="$title"/></ch:title>
              <ch:content>
                <xsl:copy-of select="."/>
              </ch:content>      
            </ch:frame>
          
          </ch:page>
	</xsl:template>

	<xsl:template match="*"/>
	
</xsl:stylesheet>
