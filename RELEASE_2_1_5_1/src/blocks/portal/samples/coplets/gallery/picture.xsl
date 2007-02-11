<?xml version="1.0"?>
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
<!-- $Id: picture.xsl,v 1.3 2004/03/06 02:25:57 antonio Exp $ 

-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!-- The current picture to display -->
<xsl:param name="pic"/>

<xsl:template match="pictures" xmlns:cl="http://apache.org/cocoon/portal/coplet/1.0">
    <xsl:choose>
        <xsl:when test="$pic=''">
            <p>Please choose a picture in the gallery.</p>
        </xsl:when>
        <xsl:otherwise>
            <img src="{$pic}"/>
        </xsl:otherwise>
    </xsl:choose>
</xsl:template>

</xsl:stylesheet>
