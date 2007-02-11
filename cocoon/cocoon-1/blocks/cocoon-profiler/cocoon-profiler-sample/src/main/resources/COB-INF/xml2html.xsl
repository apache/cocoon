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
<!--
 |
 | XSLT REC Compliant Version of IE5 Default Stylesheet
 |
 | Original version by Jonathan Marsh (jmarsh@microsoft.com)
 | http://msdn.microsoft.com/xml/samples/defaultss/defaultss.xsl
 |
 | Conversion to XSLT 1.0 REC Syntax by Steve Muench (smuench@oracle.com)
 | Added script support by Andrew Timberlake (andrew@timberlake.co.za)
 |
 +-->
<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

   <xsl:template match="processing-instruction()" mode="xml2html">
      <DIV class="e">
         <SPAN class="b">
         		<xsl:call-template name="nbsp-ref"/>
         </SPAN>
         <SPAN class="m">
            <xsl:text>&lt;?</xsl:text>
         </SPAN>
         <SPAN class="pi">
            <xsl:value-of select="name(.)"/>
            <xsl:value-of select="."/>
         </SPAN>
         <SPAN class="m">
            <xsl:text>?></xsl:text>
         </SPAN>
      </DIV>
   </xsl:template>

   <xsl:template match="processing-instruction('xml')" mode="xml2html">
      <DIV class="e">
         <SPAN class="b">
            <xsl:call-template name="nbsp-ref"/>
         </SPAN>
         <SPAN class="m">
            <xsl:text>&lt;?</xsl:text>
         </SPAN>
         <SPAN class="pi">
            <xsl:text>xml </xsl:text>
            <xsl:for-each select="@*">
               <xsl:value-of select="name(.)"/>
               <xsl:text>="</xsl:text>
               <xsl:value-of select="."/>
               <xsl:text>" </xsl:text>
            </xsl:for-each>
         </SPAN>
         <SPAN class="m">
            <xsl:text>?></xsl:text>
         </SPAN>
      </DIV>
   </xsl:template>

   <xsl:template match="@*" mode="xml2html">
      <SPAN>
         <xsl:attribute name="class">
            <xsl:if test="xsl:*/@*">
              <xsl:text>x</xsl:text>
            </xsl:if>
            <xsl:text>t</xsl:text>
         </xsl:attribute>
         <xsl:value-of select="name(.)"/>
      </SPAN>
      <SPAN class="m">="</SPAN>
      <B>
         <xsl:value-of select="."/>
      </B>
      <SPAN class="m">"</SPAN>
      <xsl:if test="position()!=last()">
         <xsl:text> </xsl:text>
      </xsl:if>
   </xsl:template>

   <xsl:template match="text()" mode="xml2html">
      <DIV class="e">
         <SPAN class="b"> </SPAN>
         <SPAN class="tx">
            <xsl:value-of select="."/>
         </SPAN>
      </DIV>
   </xsl:template>

   <xsl:template match="comment()" mode="xml2html">
      <DIV class="k">
         <SPAN>
            <A STYLE="visibility:hidden" class="b" onclick="return false" onfocus="h()">-</A>
            <SPAN class="m">
               <xsl:text>&lt;!--</xsl:text>
            </SPAN>
         </SPAN>
         <SPAN class="ci" id="clean">
            <PRE>
               <xsl:value-of select="."/>
            </PRE>
         </SPAN>
         <SPAN class="b">
            <xsl:call-template name="nbsp-ref"/>
         </SPAN>
         <SPAN class="m">
            <xsl:text>--></xsl:text>
         </SPAN>
         <SCRIPT>if(document.all)f(clean);</SCRIPT>
      </DIV>
   </xsl:template>

   <xsl:template match="*" mode="xml2html">
      <DIV class="e">
         <DIV STYLE="margin-left:1em;text-indent:-2em">
            <SPAN class="b">
            		<xsl:call-template name="nbsp-ref"/>
            </SPAN>
            <SPAN class="m">&lt;</SPAN>
            <SPAN>
               <xsl:attribute name="class">
                  <xsl:if test="xsl:*">
                     <xsl:text>x</xsl:text>
                  </xsl:if>
                  <xsl:text>t</xsl:text>
               </xsl:attribute>
               <xsl:value-of select="name(.)"/>
               <xsl:if test="@*">
                  <xsl:text> </xsl:text>
               </xsl:if>
            </SPAN>
            <xsl:apply-templates select="@*" mode="xml2html"/>
            <SPAN class="m">
               <xsl:text>/></xsl:text>
            </SPAN>
         </DIV>
      </DIV>
   </xsl:template>

   <xsl:template match="*[node()]" mode="xml2html">
      <DIV class="e">
         <DIV class="c">
            <A class="b" href="#" onclick="return false" onfocus="h()">-</A>
            <SPAN class="m">&lt;</SPAN>
            <SPAN>
               <xsl:attribute name="class">
                  <xsl:if test="xsl:*">
                     <xsl:text>x</xsl:text>
                  </xsl:if>
                  <xsl:text>t</xsl:text>
               </xsl:attribute>
               <xsl:value-of select="name(.)"/>
               <xsl:if test="@*">
                  <xsl:text> </xsl:text>
               </xsl:if>
            </SPAN>
            <xsl:apply-templates select="@*" mode="xml2html"/>
            <SPAN class="m">
               <xsl:text>></xsl:text>
            </SPAN>
         </DIV>
         <DIV>
            <xsl:apply-templates mode="xml2html"/>
            <DIV>
               <SPAN class="b">
            			<xsl:call-template name="nbsp-ref"/>
               </SPAN>
               <SPAN class="m">
                  <xsl:text>&lt;/</xsl:text>
               </SPAN>
               <SPAN>
                  <xsl:attribute name="class">
                     <xsl:if test="xsl:*">
                        <xsl:text>x</xsl:text>
                     </xsl:if>
                     <xsl:text>t</xsl:text>
                  </xsl:attribute>
                  <xsl:value-of select="name(.)"/>
               </SPAN>
               <SPAN class="m">
                  <xsl:text>></xsl:text>
               </SPAN>
            </DIV>
         </DIV>
      </DIV>
   </xsl:template>

   <xsl:template match="*[text() and not (comment() or processing-instruction())]" mode="xml2html">
      <DIV class="e">
         <DIV STYLE="margin-left:1em;text-indent:-2em">
            <SPAN class="b">
            		<xsl:call-template name="nbsp-ref"/>
            </SPAN>
            <SPAN class="m">
               <xsl:text>&lt;</xsl:text>
            </SPAN>
            <SPAN>
               <xsl:attribute name="class">
                  <xsl:if test="xsl:*">
                     <xsl:text>x</xsl:text>
                  </xsl:if>
                  <xsl:text>t</xsl:text>
               </xsl:attribute>
               <xsl:value-of select="name(.)"/>
               <xsl:if test="@*">
                  <xsl:text> </xsl:text>
               </xsl:if>
            </SPAN>
            <xsl:apply-templates select="@*" mode="xml2html"/>
            <SPAN class="m">
               <xsl:text>></xsl:text>
            </SPAN>
            <SPAN class="tx">
               <xsl:value-of select="."/>
            </SPAN>
            <SPAN class="m">&lt;/</SPAN>
            <SPAN>
               <xsl:attribute name="class">
                  <xsl:if test="xsl:*">
                     <xsl:text>x</xsl:text>
                  </xsl:if>
                  <xsl:text>t</xsl:text>
               </xsl:attribute>
               <xsl:value-of select="name(.)"/>
            </SPAN>
            <SPAN class="m">
               <xsl:text>></xsl:text>
            </SPAN>
         </DIV>
      </DIV>
   </xsl:template>

   <xsl:template match="*[*]" priority="20" mode="xml2html">
      <DIV class="e">
         <DIV STYLE="margin-left:1em;text-indent:-2em" class="c">
            <A class="b" href="#" onclick="return false" onfocus="h()">-</A>
            <SPAN class="m">&lt;</SPAN>
            <SPAN>
               <xsl:attribute name="class">
                  <xsl:if test="xsl:*">
                     <xsl:text>x</xsl:text>
                  </xsl:if>
                  <xsl:text>t</xsl:text>
               </xsl:attribute>
               <xsl:value-of select="name(.)"/>
               <xsl:if test="@*">
                  <xsl:text> </xsl:text>
               </xsl:if>
            </SPAN>
            <xsl:apply-templates select="@*" mode="xml2html"/>
            <SPAN class="m">
               <xsl:text>></xsl:text>
            </SPAN>
         </DIV>
         <DIV>
            <xsl:apply-templates mode="xml2html"/>
            <DIV>
               <SPAN class="b">
            			<xsl:call-template name="nbsp-ref"/>
               </SPAN>
               <SPAN class="m">
                  <xsl:text>&lt;/</xsl:text>
               </SPAN>
               <SPAN>
                  <xsl:attribute name="class">
                     <xsl:if test="xsl:*">
                        <xsl:text>x</xsl:text>
                     </xsl:if>
                     <xsl:text>t</xsl:text>
                  </xsl:attribute>
                  <xsl:value-of select="name(.)"/>
               </SPAN>
               <SPAN class="m">
                  <xsl:text>></xsl:text>
               </SPAN>
            </DIV>
         </DIV>
      </DIV>
   </xsl:template>

   <xsl:template name="nbsp-ref">
      <xsl:text>&#160;</xsl:text>
   </xsl:template>

</xsl:stylesheet>
