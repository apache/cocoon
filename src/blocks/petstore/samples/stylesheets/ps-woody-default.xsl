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
<xsl:stylesheet
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:wi="http://apache.org/cocoon/woody/instance/1.0">

    <xsl:template match="wi:field">

        <xsl:choose>
            <xsl:when test="wi:selection-list">
                <xsl:call-template name="field-with-selection-list">
                    <xsl:with-param name="fieldelement" select="."/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="field">
                    <xsl:with-param name="fieldelement" select="."/>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>

        <xsl:if test="wi:validation-message">
            <xsl:call-template name="validation-message">
                <xsl:with-param name="message" select="wi:validation-message"/>
            </xsl:call-template>
        </xsl:if>
        <xsl:if test="@required='true'">
            &#160;<b>*</b>
        </xsl:if>
    </xsl:template>

    <xsl:template name="field">
        <xsl:param name="fieldelement"/>
        <input name="{$fieldelement/@id}" value="{$fieldelement/wi:value}">
            <xsl:if test="wi:styling">
                <xsl:copy-of select="wi:styling/@*"/>
            </xsl:if>
        </input>
    </xsl:template>

    <xsl:template name="field-with-selection-list">
        <xsl:param name="fieldelement"/>

        <xsl:variable name="value" select="$fieldelement/wi:value"/>
        <xsl:variable name="liststyle" select="$fieldelement/wi:styling/list-style/text()"/>

        <xsl:choose>
            <xsl:when test="$liststyle='radio'">
                <xsl:for-each select="$fieldelement/wi:selection-list/wi:item">
                    <input type="radio" name="{$fieldelement/@id}" value="{@value}">
                        <xsl:if test="@value = $value">
                            <xsl:attribute name="checked">true</xsl:attribute>
                        </xsl:if>
                    </input>
                    <xsl:copy-of select="wi:label/node()"/><br/>
                </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>
                <!-- default: dropdown box -->
                <select name="{$fieldelement/@id}">
                    <xsl:if test="$liststyle='listbox'">
                        <xsl:attribute name="size">
                            <xsl:choose>
                                <xsl:when test="$fieldelement/wi:styling/listbox-size">
                                    <xsl:value-of select="$fieldelement/wi:styling/listbox-size"/>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:text>5</xsl:text>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:attribute>
                    </xsl:if>
                    <xsl:for-each select="$fieldelement/wi:selection-list/wi:item">
                        <option value="{@value}">
                            <xsl:if test="@value = $value">
                                <xsl:attribute name="selected">selected</xsl:attribute>
                            </xsl:if>
                            <xsl:copy-of select="wi:label/node()"/>
                        </option>
                    </xsl:for-each>
                </select>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="validation-message">
        <xsl:param name="message"/>
        &#160;<a href="#" style="color:RED; font-weight: bold; font-size: 24px;" onclick="alert('{normalize-space($message)}')">!</a>
    </xsl:template>

    <xsl:template match="wi:output">
        <xsl:copy-of select="wi:value/node()"/>
    </xsl:template>

    <xsl:template match="wi:booleanfield">
        <input type="checkbox" value="true" name="{@id}">
            <xsl:if test="wi:value/text() = 'true'">
                <xsl:attribute name="checked">true</xsl:attribute>
            </xsl:if>
        </input>
    </xsl:template>

    <xsl:template match="wi:action">
        <input type="submit" name="{@id}">
            <xsl:attribute name="value"><xsl:value-of select="wi:label/node()"/></xsl:attribute>
        </input>
    </xsl:template>

    <xsl:template match="wi:continuation-id">
        <xsl:choose>
            <xsl:when test="@name">
                <input name="{@name}" type="hidden" value="{.}"/>
            </xsl:when>
            <xsl:otherwise>
                <input name="continuation-id" type="hidden" value="{.}"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="wi:multivaluefield">
        <xsl:if test="wi:validation-message">
            <xsl:call-template name="validation-message">
                <xsl:with-param name="message" select="wi:validation-message"/>
            </xsl:call-template>
        </xsl:if>
        <xsl:variable name="id" select="@id"/>
        <xsl:variable name="values" select="wi:values/wi:value/text()"/>
        <xsl:for-each select="wi:selection-list/wi:item">
            <xsl:variable name="value" select="@value"/>
            <input type="checkbox" value="{@value}" name="{$id}">
                <xsl:if test="$values[.=$value]">
                    <xsl:attribute name="checked">true</xsl:attribute>
                </xsl:if>
            </input>
            <xsl:copy-of select="wi:label/node()"/>
            <br/>
        </xsl:for-each>
    </xsl:template>

    <xsl:template match="wi:repeater">
        <input type="hidden" name="{@id}.size" value="{@size}"/>
        <table border="1">
            <tr>
                <xsl:for-each select="wi:headings/wi:heading">
                    <th><xsl:value-of select="."/></th>
                </xsl:for-each>
            </tr>
            <xsl:apply-templates select="wi:repeater-row"/>
        </table>
    </xsl:template>

    <xsl:template match="wi:repeater-row">
        <tr>
            <xsl:for-each select="*">
                <td>
                    <xsl:apply-templates select="."/>
                </td>
            </xsl:for-each>
        </tr>
    </xsl:template>

    <xsl:template match="wi:repeater-size">
        <input type="hidden" name="{@id}.size" value="{@size}"/>
    </xsl:template>

    <xsl:template match="wi:form-template">
        <form>
            <xsl:apply-templates select="@*|node()"/>
        </form>
    </xsl:template>
    
    <xsl:template match="wi:form[@layout='grey']">
        <table cellpadding="10" cellspacing="0" align="center" border="1" bgcolor="#dddddd">
            <tr>
                <td>
                    <xsl:for-each select="wi:group">
                        <font color="darkgreen"><h3><xsl:value-of select="wi:label"/></h3></font>
                        <table bgcolor="#008800" border="0" cellpadding="3" cellspacing="1">
                            <xsl:call-template name="processWidgets" />
                        </table>
                    </xsl:for-each>
                </td>
            </tr>
        </table>
    </xsl:template>

    <xsl:template match="wi:form[@layout='yellow']">
        <table bgcolor="#008800" border="0" cellpadding="3" cellspacing="1">
            <xsl:for-each select="wi:group">
                <tr bgcolor="#FFFF88">
                    <td colspan="2"><font color="GREEN" size="4"><b><xsl:value-of select="wi:label"/></b></font></td>
                </tr>
                <xsl:call-template name="processWidgets" />
            </xsl:for-each>
        </table>
    </xsl:template>



    <xsl:template name="processWidgets">
            <xsl:for-each select="wi:widgets/*">
                    <xsl:choose>
                        <xsl:when test="local-name(.) = 'repeater'">
                            <tr bgcolor="#FFFF88">
                                <td colspan="2"><xsl:apply-templates select="."/></td>
                            </tr>
                        </xsl:when>
                        <xsl:when test="local-name(.) = 'booleanfield'">
                            <tr bgcolor="#FFFF88">
                                <td colspan="2">
                                    <xsl:apply-templates select="."/>
                                    <xsl:text> </xsl:text>
                                    <xsl:copy-of select="wi:label"/>
                                </td>
                            </tr>
                        </xsl:when>
                        <xsl:when test="wi:styling/@class = 'output'">
                            <tr bgcolor="#FFFF88">
                                <td>
                                    <xsl:value-of select="wi:label"/>
                                </td>
                                <td>
                                    <xsl:value-of select="wi:value"/><input type="hidden" name="{@id}" value="{wi:value}" />
                                </td>
                            </tr>
                        </xsl:when>
                        <xsl:when test="wi:styling/@class = 'warnoutput'">
                            <xsl:variable name="tmp">
                                <xsl:value-of select="wi:value"/>
                            </xsl:variable> 
                            <xsl:if test="boolean(normalize-space($tmp))"> 
                                <tr bgcolor="#FFFF88">
                                    <td>
                                        <xsl:value-of select="wi:label"/>
                                    </td>
                                    <td>
                                        <b><font color="RED"><xsl:value-of select="wi:value"/></font></b>
                                    </td>
                                </tr>
                            </xsl:if> 
                        </xsl:when>
                        <xsl:otherwise>
                            <tr bgcolor="#FFFF88">
                                <td>
                                    <xsl:value-of select="wi:label"/>
                                </td>
                                <td>
                                    <xsl:apply-templates select="."/>
                                </td>
                            </tr>
                        </xsl:otherwise>
                    </xsl:choose>
                    </xsl:for-each>
    </xsl:template>



    <xsl:template match="wi:aggregatefield">
        <input name="{@id}" value="{wi:value}"/>

        <xsl:if test="wi:validation-message">
            <xsl:call-template name="validation-message">
                <xsl:with-param name="message" select="wi:validation-message"/>
            </xsl:call-template>
        </xsl:if>
        <xsl:if test="@required='true'">
            <b>*</b>
        </xsl:if>
    </xsl:template>
    <xsl:template match="@*|node()" priority="-1">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>
