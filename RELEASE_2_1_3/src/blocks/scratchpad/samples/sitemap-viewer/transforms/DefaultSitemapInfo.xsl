<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:map="http://apache.org/cocoon/sitemap/1.0">

<xsl:template match="/">
    <sitemap2schema>
        <match ref="index.xhtml" pattern="*.html"/>
    </sitemap2schema>
</xsl:template>

</xsl:stylesheet >