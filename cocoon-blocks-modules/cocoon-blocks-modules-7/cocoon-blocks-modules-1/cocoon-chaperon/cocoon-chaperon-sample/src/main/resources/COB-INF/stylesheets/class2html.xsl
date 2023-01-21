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
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:st="http://chaperon.sourceforge.net/schema/syntaxtree/2.0">

 <xsl:output indent="no"/>

 <xsl:param name="package"/>
 <xsl:param name="class"/>

 <xsl:param name="prefix">cocoon/samples/chaperon/</xsl:param>

 <xsl:template match="/st:output">
    <document>
      <header>
        <title>Java2HTML example</title>
        <style href="java.css"/>
      </header>
      <body>
        <row>
         <column title="Source file: {$class}">

       <xsl:call-template name="header"/>
   
       <xsl:call-template name="declaration"/>

         </column>
                                                                                                                                                             
        </row>
      </body>
    </document>

 </xsl:template>

 <xsl:template match="package">
  <a href="/{$prefix}{translate(@full,'.','/')}/index.html"><xsl:value-of select="@full"/></a><br/>
 </xsl:template>

 <xsl:template match="class">
  <a href="/{$prefix}{translate(@full,'.','/')}.class.html"><xsl:value-of select="@name"/></a><br/>
 </xsl:template>

 <xsl:template name="header">
  <p>
   <table width="100%" cellpadding="3" cellspacing="0" class="view">
    <tbody>
     <tr>
      <td class="headerTitle" colspan="4"><b>&#160;Summary&#160;</b></td>
     </tr>
     <tr>
      <td class="headerLeft"><b><nobr>&#160;Package&#160;</nobr></b></td>
      <td class="header"><b>&#160;Type&#160;</b></td>
      <td class="header"><b>&#160;Name&#160;</b></td>
      <td class="headerRight"><b>&#160;Description&#160;</b></td>
     </tr>
     <tr>
      <td class="itemLeft"><xsl:value-of select="st:CompilationUnit/st:ProgramFile/
                  st:PackageStatement/st:QualifiedName"/></td>
      <td class="item"><xsl:value-of select="st:CompilationUnit/st:ProgramFile/
                                            st:TypeDeclarations/st:TypeDeclarationOptSemi/
                                        st:TypeDeclaration/st:ClassHeader/st:ClassWord"/></td>
      <td class="item"><xsl:value-of select="st:CompilationUnit/st:ProgramFile/
                                            st:TypeDeclarations/st:TypeDeclarationOptSemi/
                                        st:TypeDeclaration/st:ClassHeader/st:IDENTIFIER"/></td>
      <td class="itemRight"><xsl:apply-templates select="st:CompilationUnit/st:ProgramFile/
                                            st:TypeDeclarations/st:TypeDeclarationOptSemi/
                                        st:TypeDeclaration/st:output/st:JavaDoc" mode="short"/>&#160;</td>
     </tr>
    </tbody>
   </table>
  </p>
 </xsl:template>

 <xsl:template name="declaration">
  <xsl:apply-templates select="st:CompilationUnit/st:ProgramFile/st:TypeDeclarations/
                               st:TypeDeclarationOptSemi/st:TypeDeclaration/st:FieldDeclarations"/>
 </xsl:template>

 

 <xsl:template match="st:FieldDeclarations">
  <p>
   <table width="100%" cellpadding="3" cellspacing="0" class="view">
    <tbody>
     <tr>
      <td class="headerTitle" colspan="4"><b>&#160;Field Summary&#160;</b></td>
     </tr>
     <tr>
      <td class="headerLeft"><b><nobr>&#160;Modifier&#160;</nobr></b></td>
      <td class="header"><b>&#160;Type&#160;</b></td>
      <td class="header"><b>&#160;Name&#160;</b></td>
      <td class="headerRight"><b>&#160;Description&#160;</b></td>
     </tr>
     <xsl:apply-templates select="st:FieldDeclarationOptSemi/st:FieldDeclaration/st:FieldVariableDeclaration"/>
    </tbody>
   </table>
  </p>

  <p>
   <table width="100%" cellpadding="3" cellspacing="0" class="view">
    <tbody>
     <tr>
      <td class="headerTitle" colspan="3"><b>&#160;Constructor Summary&#160;</b></td>
     </tr>
     <tr>
      <td class="headerLeft"><b><nobr>&#160;Modifier&#160;</nobr></b></td>
      <td class="header"><b>&#160;Name&#160;</b></td>
      <td class="headerRight"><b>&#160;Description&#160;</b></td>
     </tr>
     <xsl:apply-templates select="st:FieldDeclarationOptSemi/st:FieldDeclaration/st:ConstructorDeclaration"/>
    </tbody>
   </table>
  </p>

  <p>
   <table width="100%" cellpadding="3" cellspacing="0" class="view">
    <tbody>
     <tr>
      <td class="headerTitle" colspan="4"><b>&#160;Method Summary&#160;</b></td>
     </tr>
     <tr>
      <td class="headerLeft"><b><nobr>&#160;Modifier&#160;</nobr></b></td>
      <td class="header"><b>&#160;Type&#160;</b></td>
      <td class="header"><b>&#160;Name&#160;</b></td>
      <td class="headerRight"><b>&#160;Description&#160;</b></td>
     </tr>
     <xsl:apply-templates select="st:FieldDeclarationOptSemi/st:FieldDeclaration/st:MethodDeclaration"/>
    </tbody>
   </table>
  </p>

  <xsl:apply-templates select="st:FieldDeclarationOptSemi/st:FieldDeclaration/st:MethodDeclaration" mode="detail"/>

 </xsl:template>

 <xsl:template match="st:FieldVariableDeclaration">
  <xsl:if test="not(contains(st:Modifiers/st:Modifier,'private'))">
  <tr>
   <td class="itemLeft">
    <nobr>
     <xsl:for-each select="st:Modifiers/st:Modifier">
      <xsl:value-of select="."/>&#160;
     </xsl:for-each>
    </nobr>
   </td>
   <td class="item">&#160;<xsl:value-of select="st:TypeSpecifier"/></td>
   <td class="item">
    <xsl:value-of select="st:VariableDeclarators/st:VariableDeclarator/st:DeclaratorName"/>
   </td>
   <td class="itemRight">
    <xsl:apply-templates select="st:output/st:JavaDoc" mode="short"/>&#160;
   </td>
  </tr>
  </xsl:if>
 </xsl:template>

 <xsl:template match="st:FieldVariableDeclaration" mode="detail">
 </xsl:template>

 <xsl:template match="st:ConstructorDeclaration">
  <tr>
   <td class="itemLeft">
    <nobr>
     <xsl:for-each select="st:Modifiers/st:Modifier">
      <xsl:value-of select="."/>&#160;
     </xsl:for-each>
    </nobr>
   </td>
   <td class="item">
    <nobr>
     <xsl:value-of select="st:ConstructorDeclarator/st:IDENTIFIER"/>
     (<xsl:for-each select="st:ConstructorDeclarator/st:ParameterList/st:Parameter">
      <xsl:if test="position()!=1">&#160;,</xsl:if>
       <xsl:value-of select="st:TypeSpecifier/st:TypeName"/>&#160;<xsl:value-of select="st:DeclaratorName"/>
     </xsl:for-each>)
    </nobr>
   </td>
   <td class="itemRight">
    <xsl:apply-templates select="st:output/st:JavaDoc" mode="short"/>&#160;
   </td>
  </tr>
 </xsl:template>

 <xsl:template match="st:ConstructorDeclaration" mode="detail">
 </xsl:template>

 <xsl:template match="st:MethodDeclaration">
  <tr>
   <td class="itemLeft">
    <nobr>
     <xsl:for-each select="st:Modifiers/st:Modifier">
      <xsl:value-of select="."/>&#160;
     </xsl:for-each>
    </nobr>
   </td>
   <td class="item"><xsl:value-of select="st:TypeSpecifier"/></td>
   <td class="item">
    <nobr>
     <xsl:value-of select="st:MethodDeclarator/st:DeclaratorName"/>
     (<xsl:for-each select="st:MethodDeclarator/st:ParameterList/st:Parameter">
      <xsl:if test="position()!=1">&#160;,</xsl:if>
       <xsl:value-of select="st:TypeSpecifier/st:TypeName"/>&#160;<xsl:value-of select="st:DeclaratorName"/>
     </xsl:for-each>)
    </nobr>
   </td>
   <td class="itemRight">
    <xsl:apply-templates select="st:output/st:JavaDoc" mode="short"/>&#160;
   </td>
  </tr>
 </xsl:template>

 <xsl:template match="st:MethodDeclaration" mode="detail">
  <p>
   <table width="100%" cellpadding="3" cellspacing="0" class="view">
    <tr>
     <td class="headerTitle" colspan="4">
      <b>
      <nobr>
       <xsl:for-each select="st:Modifiers/st:Modifier">
        <xsl:value-of select="."/>&#160;
       </xsl:for-each>
       <xsl:value-of select="st:TypeSpecifier"/>&#160;
       <xsl:value-of select="st:MethodDeclarator/st:DeclaratorName"/>
       (<xsl:for-each select="st:MethodDeclarator/st:ParameterList/st:Parameter">
        <xsl:if test="position()!=1">&#160;,</xsl:if>
         <xsl:value-of select="st:TypeSpecifier/st:TypeName"/>&#160;<xsl:value-of select="st:DeclaratorName"/>
       </xsl:for-each>)
      </nobr>
      </b>
     </td>
    </tr>
    <tr>
     <td class="itemRight">
 
      <dl>
       <dd><xsl:apply-templates select="st:output/st:JavaDoc"/></dd>

       <dt><b>Parameters:</b></dt>

       <xsl:for-each select="st:MethodDeclarator/st:ParameterList/st:Parameter">
        <dd>
         <code><xsl:value-of select="st:DeclaratorName"/></code><xsl:text> - </xsl:text>
         <xsl:variable name="name"><xsl:value-of select="st:DeclaratorName"/></xsl:variable> 
         <xsl:for-each select="../../../st:output/st:JavaDoc/st:Properties/
              st:Property[substring-after(st:PROPERTYNAME,'@')='param']/st:Description">
          <xsl:if test="starts-with(.,$name)">
           <xsl:value-of select="substring(.,string-length($name)+1,string-length(.)-string-length($name))"/>
          </xsl:if>
         </xsl:for-each>
        </dd>
       </xsl:for-each>

       <dt><b>Returns:</b></dt>
       <dd>
        <code><xsl:value-of select="st:TypeSpecifier"/></code>
        <xsl:if test="st:output/st:JavaDoc/st:Properties/st:Property[substring-after(st:PROPERTYNAME,'@')='return']/st:Description">
         <xsl:text> - </xsl:text>
        </xsl:if>
        <xsl:value-of select="st:output/st:JavaDoc/st:Properties/st:Property[substring-after(st:PROPERTYNAME,'@')='return']/st:Description"/>
       </dd>
      </dl>

     </td>
    </tr>
   </table>
  </p>
 </xsl:template>

 <xsl:template match="st:JavaDoc">
  <xsl:apply-templates select="st:Description"/>
 </xsl:template>

 <xsl:template match="st:JavaDoc" mode="short">
  <xsl:choose>
   <xsl:when test="string-length(substring-before(st:Description,'.'))>0">
    <xsl:value-of select="substring-before(st:Description,'.')"/>
   </xsl:when>
   <xsl:otherwise>
    <xsl:value-of select="st:Description"/>
   </xsl:otherwise>
  </xsl:choose>
 </xsl:template>

 <xsl:template match="st:Description">
  <xsl:apply-templates select="st:TEXT|st:TAG"/>
 </xsl:template>

 <xsl:template match="st:TAG">
  <xsl:value-of select="." disable-output-escaping="yes"/>
 </xsl:template>

</xsl:stylesheet>
