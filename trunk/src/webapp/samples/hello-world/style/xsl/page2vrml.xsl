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

<!-- CVS $Id: page2vrml.xsl,v 1.3 2004/03/10 10:18:52 cziegeler Exp $ -->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="page">
    <!-- 
         due to a DOM limitation, you must wrap your generated VRML
         with a fake tag, here uses <vrml>, that is stripped out by the
         text formatter 
      -->
    <vrml><xsl:text>#VRML V2.0 utf8</xsl:text>
          <xsl:apply-templates select="content"/>
    </vrml>
  </xsl:template>

  <xsl:template match="content">
    <xsl:text>
      Transform {
        translation 0 0 9
        rotation 0 0 1 0.0
        children Shape {
          appearance DEF WHITE Appearance {
            material Material {
              diffuseColor 1 1 1
            }
          }
          geometry Text {
            string [ " </xsl:text><xsl:value-of select="para"/><xsl:text> " ]
            fontStyle DEF MFS FontStyle {
              size 0.1
              family "SERIF"
              style "BOLD"
              justify "MIDDLE"
            }
          }
        }
      }
    </xsl:text>
</xsl:template>

</xsl:stylesheet>
