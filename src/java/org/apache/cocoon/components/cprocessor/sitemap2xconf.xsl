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
  xmlns:map="http://apache.org/cocoon/sitemap/1.0">
  
  <xsl:strip-space elements="*"/>
  
  <xsl:template match="map:sitemap">
    <sitemap proxy-type="none">
      <xsl:apply-templates select="map:components|map:views|map:flow|map:resources|map:action-sets|map:pipelines"/>
    </sitemap>
  </xsl:template>
  
  <xsl:template match="map:components">
    <meta>
      <xsl:for-each select="*">
        <default type="{local-name()}" hint="{@default}"/>
      </xsl:for-each>
    </meta>
    <xsl:apply-templates select="*/*" />
  </xsl:template>
  
  <xsl:template match="map:generator|map:transformer|map:serializer|map:reader">
    <xsl:element name="{local-name()}-node">
      <xsl:attribute name="id">
        <xsl:value-of select="@name"/>
      </xsl:attribute>
      <xsl:attribute name="logger">
        <xsl:text>sitemap.processor</xsl:text>
      </xsl:attribute>
      <xsl:apply-templates select="@label|@mime-type" mode="copy" />
      <component hint="{@name}" />
    </xsl:element>
    <component id="{@name}" class="{@src}">
      <xsl:apply-templates select="@*|child::node()" mode="copy"/>
    </component>
  </xsl:template>
  
  <xsl:template match="map:matcher|map:selector|map:action|map:pipe">
    <component id="{@name}" class="{@src}">
      <xsl:apply-templates select="@*|child::node()" mode="copy"/>
    </component>
  </xsl:template>
  
  <xsl:template match="map:views">
    <xsl:variable name="id">v</xsl:variable>
    <xsl:apply-templates select="map:view">
      <xsl:with-param name="parent-id" select="$id"/>
    </xsl:apply-templates>
  </xsl:template>
  
  <xsl:template match="map:flow">
    <flow-node id="flow">
      <xsl:apply-templates select="@*|map:script" mode="copy"/>
    </flow-node>
  </xsl:template>
  
  <xsl:template match="map:resources">
    <xsl:variable name="id">r</xsl:variable>
    <xsl:apply-templates select="map:resource">
      <xsl:with-param name="parent-id" select="$id"/>
    </xsl:apply-templates>
  </xsl:template>
  
  <xsl:template match="map:action-sets">
    <xsl:variable name="id">a</xsl:variable>
    <xsl:apply-templates select="map:action-set">
      <xsl:with-param name="parent-id" select="$id"/>
    </xsl:apply-templates>
  </xsl:template>
  
  <xsl:template match="map:pipelines">
    <xsl:variable name="id">p</xsl:variable>
    <pipelines-node id="{$id}" default="true" logger="sitemap.processor">
      <xsl:apply-templates select="map:component-configurations" mode="copy" />
      <xsl:for-each select="map:pipeline|map:handle-errors">
        <xsl:element name="{local-name()}">
          <xsl:attribute name="id-ref">
            <xsl:value-of select="$id"/>-<xsl:value-of select="position()"/>
          </xsl:attribute>
        </xsl:element>
      </xsl:for-each>
    </pipelines-node>
    <xsl:apply-templates select="map:pipeline|map:handle-errors">
      <xsl:with-param name="parent-id">
        <xsl:value-of select="$id"/>
      </xsl:with-param>
    </xsl:apply-templates>
  </xsl:template>
  
  <xsl:template match="map:view">
    <xsl:variable name="id">
      <xsl:text>v-</xsl:text>
      <xsl:value-of select="@name"/>
    </xsl:variable>
    <view-node id="{$id}" logger="sitemap.processor">
      <xsl:apply-templates select="@*" mode="copy" />
      <xsl:for-each select="map:call|map:aggregate|map:generate|map:transform|map:serialize|map:read|map:mount|map:redirect-to">
        <xsl:element name="{local-name()}">
          <xsl:attribute name="id-ref">
            <xsl:value-of select="$id"/>-<xsl:value-of select="position()"/>
          </xsl:attribute>
        </xsl:element>
      </xsl:for-each>
    </view-node>
    <xsl:apply-templates select="map:call|map:aggregate|map:generate|map:transform|map:serialize|map:read|map:mount|map:redirect-to">
      <xsl:with-param name="parent-id">
        <xsl:value-of select="$id"/>
      </xsl:with-param>
    </xsl:apply-templates>
  </xsl:template>
  
  <xsl:template match="map:resource">
    <xsl:param name="parent-id"/>
    <xsl:variable name="id">
      <xsl:value-of select="$parent-id"/>-<xsl:value-of select="position()"/>
    </xsl:variable>
    <named-container-node id="{$id}" logger="sitemap.processor">
      <xsl:apply-templates select="@*" mode="copy" />
      <xsl:for-each select="map:match|map:select|map:call|map:aggregate|map:generate|map:transform|map:serialize|map:read|map:mount|map:redirect-to">
        <xsl:element name="{local-name()}">
          <xsl:attribute name="id">
            <xsl:value-of select="$id"/>-<xsl:value-of select="position()"/>
          </xsl:attribute>
        </xsl:element>
      </xsl:for-each>
    </named-container-node>
    <xsl:apply-templates select="map:match|map:select|map:call|map:aggregate|map:generate|map:transform|map:serialize|map:read|map:mount|map:redirect-to">
      <xsl:with-param name="parent-id">
        <xsl:value-of select="$id"/>
      </xsl:with-param>
    </xsl:apply-templates>
  </xsl:template>
  
  <xsl:template match="map:pipeline">
    <xsl:param name="parent-id"/>
    <xsl:variable name="id">
      <xsl:value-of select="$parent-id"/>-<xsl:value-of select="position()"/>
    </xsl:variable>
    <pipeline-node id="{$id}" logger="sitemap.processor">
      <xsl:apply-templates select="@*|map:parameter" mode="copy" />
      <!-- TODO:
      allow map:call|map:aggregate|map:generate|map:transform|map:serialize|map:read|map:mount|map:redirect-to ?
      -->
      <xsl:for-each select="map:match|map:select|map:act|map:handle-errors">
        <xsl:element name="{local-name()}">
          <xsl:attribute name="id-ref">
            <xsl:value-of select="$id"/>-<xsl:value-of select="position()" />
          </xsl:attribute>
        </xsl:element>
      </xsl:for-each>
    </pipeline-node>
    <xsl:apply-templates select="map:match|map:select|map:act|map:handle-errors">
      <xsl:with-param name="parent-id">
        <xsl:value-of select="$id" />
      </xsl:with-param>
    </xsl:apply-templates>
  </xsl:template>
  
  <xsl:template match="map:handle-errors">
    <xsl:param name="parent-id"/>
    <xsl:variable name="id">
      <xsl:value-of select="$parent-id"/>-<xsl:value-of select="position()"/>
    </xsl:variable>
    <handle-errors-node id="{$id}" logger="sitemap.processor">
      <xsl:apply-templates select="@*|map:parameter" mode="copy" />
      <!-- TODO:
      allow map:call|map:aggregate|map:generate|map:transform|map:serialize|map:read|map:mount|map:redirect-to ?
      -->
      <xsl:for-each select="map:match|map:select|map:act">
        <xsl:element name="{local-name()}">
          <xsl:attribute name="id-ref">
            <xsl:value-of select="$id"/>-<xsl:value-of select="position()" />
          </xsl:attribute>
        </xsl:element>
      </xsl:for-each>
    </handle-errors-node>
    <xsl:apply-templates select="map:match|map:select|map:act">
      <xsl:with-param name="parent-id">
        <xsl:value-of select="$id" />
      </xsl:with-param>
    </xsl:apply-templates>
  </xsl:template>
  
  <xsl:template match="map:match">
    <xsl:param name="parent-id" />
    <xsl:variable name="id">
      <xsl:value-of select="$parent-id"/>-<xsl:value-of select="position()"/>
    </xsl:variable>
    <match-node id="{$id}" logger="sitemap.processor">
      <xsl:apply-templates select="@*" mode="copy" />
      <xsl:for-each select="map:match|map:select|map:act|map:call|map:aggregate|map:generate|map:transform|map:serialize|map:read|map:mount|map:redirect-to">
        <xsl:element name="{local-name()}">
          <xsl:attribute name="id-ref">
            <xsl:value-of select="$id"/>-<xsl:value-of select="position()" />
          </xsl:attribute>
        </xsl:element>
      </xsl:for-each>
    </match-node>
    <xsl:apply-templates select="map:parameter" mode="copy" />
    <xsl:apply-templates select="map:match|map:select|map:act|map:call|map:aggregate|map:generate|map:transform|map:serialize|map:read|map:mount|map:redirect-to">
      <xsl:with-param name="parent-id">
        <xsl:value-of select="$id"/>
      </xsl:with-param>
    </xsl:apply-templates>
  </xsl:template>
  
  <xsl:template match="map:select">
    <xsl:param name="parent-id" />
    <xsl:variable name="id">
      <xsl:value-of select="$parent-id"/>-<xsl:value-of select="position()"/>
    </xsl:variable>
    <select-node id="{$id}" logger="sitemap.processor">
      <xsl:apply-templates select="@*" mode="copy" />
      <xsl:apply-templates select="map:parameter" mode="copy" />
      <xsl:apply-templates select="map:when|map:otherwise" mode="config">
        <xsl:with-param name="parent-id">
          <xsl:value-of select="$id"/>
        </xsl:with-param>
      </xsl:apply-templates>
    </select-node>
    <xsl:apply-templates select="map:when|map:otherwise" mode="component">
      <xsl:with-param name="parent-id">
        <xsl:value-of select="$id"/>
      </xsl:with-param>
    </xsl:apply-templates>
  </xsl:template>
  
  <xsl:template match="map:when|map:otherwise" mode="config">
    <xsl:param name="parent-id" />
    <xsl:variable name="id">
      <xsl:value-of select="$parent-id"/>-<xsl:value-of select="position()"/>
    </xsl:variable>
    <xsl:element name="{local-name()}">
      <xsl:apply-templates select="@*" mode="copy" />
      <xsl:for-each select="map:match|map:select|map:call|map:aggregate|map:generate|map:transform|map:serialize|map:read|map:mount|map:redirect-to">
        <xsl:element name="{local-name()}">
          <xsl:attribute name="id-ref">
            <xsl:value-of select="$id"/>-<xsl:value-of select="position()" />
          </xsl:attribute>
        </xsl:element>
      </xsl:for-each>
    </xsl:element>
  </xsl:template>
  
  <xsl:template match="map:when|map:otherwise" mode="component">
    <xsl:param name="parent-id" />
    <xsl:variable name="id">
      <xsl:value-of select="$parent-id"/>-<xsl:value-of select="position()"/>
    </xsl:variable>
    <xsl:apply-templates select="map:match|map:select|map:call|map:aggregate|map:generate|map:transform|map:serialize|map:read|map:mount|map:redirect-to">
      <xsl:with-param name="parent-id">
        <xsl:value-of select="$id"/>
      </xsl:with-param>
    </xsl:apply-templates>
  </xsl:template>
  
  <xsl:template match="map:act">
    <xsl:param name="parent-id" />
    <xsl:variable name="id">
      <xsl:value-of select="$parent-id"/>-<xsl:value-of select="position()"/>
    </xsl:variable>
    <act-node id="{$id}" logger="sitemap.processor">
      <xsl:apply-templates select="@*" mode="copy" />
      <xsl:for-each select="map:match|map:select|map:act|map:call|map:aggregate|map:generate|map:transform|map:serialize|map:read|map:mount|map:redirect-to">
        <xsl:element name="{local-name()}">
          <xsl:attribute name="id-ref">
            <xsl:value-of select="$id"/>-<xsl:value-of select="position()" />
          </xsl:attribute>
        </xsl:element>
      </xsl:for-each>
      <xsl:apply-templates select="map:parameter" mode="copy" />
    </act-node>
    <xsl:apply-templates select="map:match|map:select|map:act|map:call|map:aggregate|map:generate|map:transform|map:serialize|map:read|map:mount|map:redirect-to">
      <xsl:with-param name="parent-id">
        <xsl:value-of select="$id"/>
      </xsl:with-param>
    </xsl:apply-templates>
  </xsl:template>
  
  <xsl:template match="map:generate">
    <xsl:param name="parent-id" />
    <xsl:variable name="id">
      <xsl:value-of select="$parent-id"/>-<xsl:value-of select="position()"/>
    </xsl:variable>
    <generate-node id="{$id}" logger="sitemap.processor">
      <xsl:apply-templates select="@*|map:parameter" mode="copy" />
    </generate-node>
  </xsl:template>
  
  <xsl:template match="map:transform">
    <xsl:param name="parent-id" />
    <xsl:variable name="id">
      <xsl:value-of select="$parent-id"/>-<xsl:value-of select="position()"/>
    </xsl:variable>
    <transform-node id="{$id}" logger="sitemap.processor">
      <xsl:apply-templates select="@*|map:parameter" mode="copy" />
    </transform-node>
  </xsl:template>
  
  <xsl:template match="map:serialize">
    <xsl:param name="parent-id" />
    <xsl:variable name="id">
      <xsl:value-of select="$parent-id"/>-<xsl:value-of select="position()"/>
    </xsl:variable>
    <serialize-node id="{$id}" logger="sitemap.processor">
      <xsl:apply-templates select="@*|map:parameter" mode="copy" />
    </serialize-node>
  </xsl:template>
  
  <xsl:template match="map:read">
    <xsl:param name="parent-id" />
    <xsl:variable name="id">
      <xsl:value-of select="$parent-id"/>-<xsl:value-of select="position()"/>
    </xsl:variable>
    <read-node id="{$id}" logger="sitemap.processor">
      <xsl:apply-templates select="@*|map:parameter" mode="copy" />
    </read-node>
  </xsl:template>
  
  <xsl:template match="map:mount">
    <xsl:param name="parent-id" />
    <xsl:variable name="id">
      <xsl:value-of select="$parent-id"/>-<xsl:value-of select="position()"/>
    </xsl:variable>
    <mount-node id="{$id}" logger="sitemap.processor">
      <xsl:apply-templates select="@*" mode="copy" />
    </mount-node>
  </xsl:template>
  
  <xsl:template match="map:redirect-to">
    <xsl:param name="parent-id" />
    <xsl:variable name="id">
      <xsl:value-of select="$parent-id"/>-<xsl:value-of select="position()"/>
    </xsl:variable>
    <redirect-node id="{$id}" logger="sitemap.processor">
      <xsl:apply-templates select="@*" mode="copy" />
    </redirect-node>
  </xsl:template>
  
  <xsl:template match="map:call[@function]">
    <xsl:param name="parent-id" />
    <xsl:variable name="id">
      <xsl:value-of select="$parent-id"/>-<xsl:value-of select="position()"/>
    </xsl:variable>
    <call-function id="{$id}" logger="sitemap.processor">
      <xsl:apply-templates select="@*|map:parameter" mode="copy" />
    </call-function>
  </xsl:template>
  
  <xsl:template match="map:call[@resource]">
    <xsl:param name="parent-id" />
    <xsl:variable name="id">
      <xsl:value-of select="$parent-id"/>-<xsl:value-of select="position()"/>
    </xsl:variable>
    <call-resource id="{$id}" logger="sitemap.processor">
      <xsl:apply-templates select="@*|map:parameter" mode="copy" />
    </call-resource>
  </xsl:template>
  
  <xsl:template match="map:aggregate">
    <xsl:param name="parent-id" />
    <xsl:variable name="id">
      <xsl:value-of select="$parent-id"/>-<xsl:value-of select="position()"/>
    </xsl:variable>
    <aggregate-node id="{$id}" logger="sitemap.processor">
      <xsl:apply-templates select="@*|map:part" mode="copy" />
    </aggregate-node>
  </xsl:template>
  
  <xsl:template match="map:part" mode="copy">
    <part>
      <xsl:apply-templates select="@*" mode="copy"/>
    </part>
  </xsl:template>
  
  <xsl:template match="map:script" mode="copy">
    <script>
      <xsl:apply-templates select="@*" mode="copy"/>
    </script>
  </xsl:template>
  
  <xsl:template match="map:parameter" mode="copy">
    <parameter name="{@name}" value="{@value}" />
  </xsl:template>
  
  <xsl:template match="@*|node()" mode="copy">
    <xsl:copy-of select="current()" />
  </xsl:template>
  
</xsl:stylesheet>
