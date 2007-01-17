<?xml version="1.0"?>
<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fi="http://apache.org/cocoon/forms/1.0#instance"
                xmlns:i18n="http://apache.org/cocoon/i18n/2.1"
                exclude-result-prefixes="fi">
  <!--+
      | This stylesheet is designed to be included by 'forms-advanced-styling.xsl'.
      +-->

  <!--+
      | fi:field or fi:aggregatefield with either
      | - explicit styling @type = 'date' or
      | - implicit if no styling @type is specified,
      |   but datatype/@type = 'date',
      |   selection lists must be excluded here
      +-->
  <xsl:template match="fi:field[fi:styling/@type='date'] |
                       fi:field[fi:datatype[@type='date']][not(fi:styling/@type)][not(fi:selection-list)] |
                       fi:aggregatefield[fi:datatype[@type='date'][fi:convertor/@variant='date']][not(fi:styling/@type)][not(fi:selection-list)]
                       ">
    <xsl:variable name="id" select="concat(@id, ':cal')"/>

    <xsl:variable name="format">
      <xsl:choose>
        <xsl:when test="fi:datatype[@type='date']/fi:convertor/@pattern">
          <xsl:value-of select="fi:datatype[@type='date']/fi:convertor/@pattern"/>
        </xsl:when>
        <xsl:otherwise>yyyy-MM-dd</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="variant">
      <xsl:choose>
        <xsl:when test="fi:datatype[@type='date']/fi:convertor/@variant">
          <xsl:value-of select="fi:datatype[@type='date']/fi:convertor/@variant"/>
        </xsl:when>
        <xsl:otherwise>date</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <span id="{@id}">
      <xsl:choose>
        <xsl:when test="@state = 'output'">
          <xsl:value-of select="fi:value"/>
        </xsl:when>
        <xsl:otherwise>
        <!-- regular input -->
	      <input id="{@id}:input" name="{@id}" value="{fi:value}" title="{normalize-space(fi:hint)}" type="text" dojoType="forms:dropdownDateTimePicker" pattern="{$format}" variant="{$variant}">
	        <xsl:apply-templates select="." mode="styling"/>
	      </input>
        <!-- common stuff -->
	      <xsl:apply-templates select="." mode="common"/>
	    </xsl:otherwise>
	  </xsl:choose>
    </span>
  </xsl:template>

</xsl:stylesheet>
