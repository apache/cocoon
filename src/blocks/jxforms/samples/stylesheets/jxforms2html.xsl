<?xml version="1.0" encoding="iso-8859-1" ?>
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

  Basic XMLForm processing stylesheet.  
  Converts XMLForm tags to HTML tags.
  
  Syntax is borrowed from the XForms standard.
  http://www.w3.org/TR/2002/WD-xforms-20020118/
  
  This stylesheet is usually applied at the end of a 
  transformation process after laying out the jxform
  tags on the page is complete. At this stage jxform tags 
  are rendered in device specific format.
  
  Different widgets are broken into templates 
  to allow customization in importing stylesheets

  author: Ivelin Ivanov, ivelin@apache.org, June 2002
  author: Andrew Timberlake <andrew@timberlake.co.za>, June 2002
  author: Michael Ratliff, mratliff@collegenet.com <mratliff@collegenet.com>, May 2002
  author: Torsten Curdt, tcurdt@dff.st, March 2002
  author: Simon Price <price@bristol.ac.uk>, September 2002
  author: Konstantin Piroumian <kpiroumian@protek.com>, September 2002
  author: Robert Ellis Parrott <parrott@fas.harvard.edu>, October 2002
-->

<xsl:stylesheet version="1.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:xf="http://apache.org/cocoon/jxforms/1.0">

   <xsl:output method = "xml" omit-xml-declaration = "no"  /> 
  

   <xsl:template match="/">
     <xsl:apply-templates />
   </xsl:template>


   <xsl:template match="xf:form">
      <form>
         <xsl:copy-of select="@*"/>

         <!-- the xf:form/@view attributed is sent back to the server as a hidden field -->
         <input type="hidden" name="cocoon-xmlform-view" value="{@view}"/>
         
         <!-- render the child form controls -->
         <xsl:apply-templates />
         
      </form>
   </xsl:template>


   <xsl:template match="xf:output">
      [<xsl:value-of select="xf:value/text()"/>]
   </xsl:template>


   <xsl:template match="xf:input">
      <!-- the ref attribute is assigned to html:name, which is how it is linked to the model -->
      <input name="{@ref}" type="text" value="{xf:value/text()}">
        <!-- copy all attributes from the original markup, except for "ref" -->
        <xsl:copy-of select="@*[not(name()='ref')]"/>
        <xsl:apply-templates select="xf:hint"/>
      </input>
   </xsl:template>


   <xsl:template match="xf:textarea">
      <textarea name="{@ref}" >
        <xsl:copy-of select="@*[not(name()='ref')]"/>
        <xsl:value-of select="xf:value/text()"/>
        <xsl:apply-templates select="xf:hint"/>
      </textarea>
   </xsl:template>

	<xsl:template match="xf:repeat">
		<tr width="100%">
			<td colspan="3" width="100%">
				<table class="repeat">
					<xsl:apply-templates select="*"/>
				</table>
			</td>
		</tr>
	</xsl:template>
	<xsl:template match="xf:group">
		<tr width="100%">
			<td width="100%" colspan="2">
				<table class="group" border="0">
					<tr>
						<td align="left">
							<xsl:value-of select="xf:label" />
						</td>
					</tr>
					<xsl:apply-templates select="*"/>
				</table>
			</td>
		</tr>
	</xsl:template>

   <xsl:template match="xf:secret">
      <input name="{@ref}" type="password" value="{xf:value/text()}">
        <xsl:copy-of select="@*[not(name()='ref')]"/>
        <xsl:apply-templates select="xf:hint"/>
      </input>
   </xsl:template>


   <xsl:template match="xf:hidden">
      <input name="{@ref}" type="hidden" value="{xf:value/text()}">
        <xsl:copy-of select="@*[not(name()='ref')]"/>
      </input>
   </xsl:template>


   <xsl:template match="xf:select1 | xf:select1[@appearance='compact']">
     <select name="{@ref}">
     <xsl:copy-of select="@*[not(name()='ref')]"/>
     <!-- all currently selected nodes are listed as value elements -->
       <xsl:variable name="selected" select="xf:value"/>
       <xsl:for-each select="xf:item">
         <option value="{xf:value}">
           <!-- If the current item value matches one of the selected values -->
           <!-- mark it as selected in the listbox -->
           <xsl:if test="$selected = xf:value">
             <xsl:attribute name="selected"/>
           </xsl:if>
           <xsl:value-of select="xf:label"/>
         </option>
       </xsl:for-each>
     </select>
   </xsl:template>

   
   <xsl:template match="xf:select1[@appearance='full']">
        <xsl:variable name="selected" select="xf:value"/>
        <xsl:variable name="ref" select="@ref"/>
        <xsl:for-each select="xf:item">
            <input name="{$ref}" type="radio" value="{xf:value}">
                <xsl:copy-of select="@*[not(name()='ref')]"/>
                <xsl:if test="xf:value = $selected">
                    <xsl:attribute name="checked"/>
                </xsl:if>
            </input>
            <xsl:value-of select="xf:label"/>
            <br/>
        </xsl:for-each>
   </xsl:template>

   
   <xsl:template match="xf:select | xf:select[@appearance='compact']">
     <xsl:variable name="selected" select="xf:value"/>
     <select name="{@ref}">
       <xsl:copy-of select="@*[not(name()='ref')]"/>
       <xsl:attribute name="multiple"/>
       <xsl:for-each select="xf:item">
         <option value="{xf:value}">
           <xsl:if test="xf:value = $selected">
             <xsl:attribute name="selected"/>
           </xsl:if>
           <xsl:value-of select="xf:label"/>
         </option>
       </xsl:for-each>
     </select>  
   </xsl:template>

   
   <xsl:template match="xf:select[@appearance='full']">
        <xsl:variable name="selected" select="xf:value"/>
        <xsl:variable name="ref" select="@ref"/>
        <xsl:for-each select="xf:item">
            <input name="{$ref}" type="checkbox" value="{xf:value}">
                <xsl:copy-of select="@*[not(name()='ref')]"/>
                <xsl:if test="xf:value = $selected">
                  <xsl:attribute name="checked"/>
                </xsl:if>
            </input>
            <xsl:value-of select="xf:label"/>
            <br/>
        </xsl:for-each>
   </xsl:template>

   
   
   <xsl:template match="xf:submit">
       <!-- the id attribute of the submit control is sent to the server -->
       <!-- as a conventional Cocoon Action parameter of the form cocoon-action-* -->
      <xsl:choose>
          <xsl:when test="@src">
              <input name="cocoon-action-{@id}" type="image" value="{xf:label/text()}">
                  <xsl:copy-of select="@*[not(name()='id')]"/>
                  <xsl:apply-templates select="xf:hint"/>
              </input>
          </xsl:when>
          <xsl:otherwise>
              <input name="cocoon-action-{@id}" type="submit" value="{xf:label/text()}">
                  <xsl:copy-of select="@*[not(name()='id')]"/>
                  <xsl:apply-templates select="xf:hint"/>
              </input>
          </xsl:otherwise>
      </xsl:choose>
   </xsl:template>
   
   <xsl:template match="xf:hint">
          <xsl:attribute name="title"><xsl:value-of select="."/></xsl:attribute>
   </xsl:template>


   <!-- copy all the rest of the markup which is not recognized above -->
   <xsl:template match="*">
      <xsl:copy><xsl:copy-of select="@*" /><xsl:apply-templates /></xsl:copy>
   </xsl:template>

   <xsl:template match="text()">
      <xsl:value-of select="." />
   </xsl:template>


</xsl:stylesheet>

