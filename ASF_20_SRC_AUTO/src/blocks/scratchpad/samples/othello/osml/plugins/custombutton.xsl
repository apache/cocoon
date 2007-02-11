<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:osm="http://osmosis.gr/osml/1.0" xmlns:fo="http://www.w3.org/1999/XSL/Format">

  <xsl:template match="osm:custombutton">
    <input type="button" value="{@caption}" onclick="alert('{@msg}')"/>
  </xsl:template>

</xsl:stylesheet>
