<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:template match="/*">
        <test-result>
            This text comes from service!
            <original-content>
                <xsl:copy-of select="."/>
            </original-content>
        </test-result>
    </xsl:template>
</xsl:stylesheet>