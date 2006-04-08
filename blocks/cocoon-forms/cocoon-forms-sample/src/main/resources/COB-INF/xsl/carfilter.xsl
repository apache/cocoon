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
  xmlns:fd="http://apache.org/cocoon/forms/1.0#definition">

  <xsl:param name="list"/>
  <xsl:param name="make"/>
  <xsl:param name="type"/>

  <xsl:template match="/">
    <xsl:choose>
      <xsl:when test="$list = 'makes'">
        <xsl:call-template name="makes-list"/>
      </xsl:when>
      <xsl:when test="$list = 'models'">
        <xsl:call-template name="models-list"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="types-list"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="makes-list">
    <fd:selection-list>
      <fd:item value="">
        <fd:label>-- Choose maker --</fd:label>
      </fd:item>
      <xsl:for-each select="cars/make">
        <fd:item value="{@name}"/>
      </xsl:for-each>
    </fd:selection-list>
  </xsl:template>

  <xsl:template name="types-list">
    <fd:selection-list>
      <fd:item value="">
        <fd:label>-- Choose type --</fd:label>
      </fd:item>
      <xsl:for-each select="cars/make[@name=$make]/type">
        <fd:item value="{@name}"/>
      </xsl:for-each>
    </fd:selection-list>
  </xsl:template>

  <xsl:template name="models-list">
    <fd:selection-list>
      <fd:item value="">
        <fd:label>-- Choose model --</fd:label>
      </fd:item>
      <xsl:choose>
        <xsl:when test="cars/make[@name=$make]/type[@name=$type]/model">
          <xsl:for-each select="cars/make[@name=$make]/type[@name=$type]/model">
            <fd:item value="{@name}"/>
          </xsl:for-each>
        </xsl:when>
        <xsl:otherwise>
          <!-- dummy list -->
          <fd:item value="{$type} model 1"/>
          <fd:item value="{$type} model 2"/>
          <fd:item value="{$type} model 3"/>
          <fd:item value="{$type} model 4"/>
          <fd:item value="{$type} model 5"/>
        </xsl:otherwise>
      </xsl:choose>
    </fd:selection-list>
  </xsl:template>

</xsl:stylesheet>
