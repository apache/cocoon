<?xml version="1.0"?>

<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:map="http://apache.org/cocoon/sitemap/1.0">
  
  <xsl:template match="map:sitemap">
    <sitemap>
      <xsl:apply-templates />
    </sitemap>
  </xsl:template>
  
  <xsl:template match="map:components/*/*">
    <component id="{@name}-{local-name()}" class="{@src}">
      <xsl:copy-of select="child::node()" />
    </component>
  </xsl:template>
  
  <xsl:template match="map:pipelines">
    <pipelines-node id="default">
      <xsl:for-each select="map:pipeline">
        <pipeline id-ref="{position()}" />
      </xsl:for-each>
    </pipelines-node>
    <xsl:for-each select="map:pipeline">
      <xsl:apply-templates select=".">
        <xsl:with-param name="id" select="position()" />
      </xsl:apply-templates>
    </xsl:for-each>
  </xsl:template>
  
  <xsl:template match="map:pipeline">
    <xsl:param name="id" />
    <pipeline-node id="{$id}">
      <xsl:copy-of select="@*" />
      <xsl:for-each select="map:match|map:select">
        <xsl:element name="{local-name()}">
          <xsl:attribute name="id-ref">
            <xsl:value-of select="$id"/>
            <xsl:value-of select="position()" />
          </xsl:attribute>
        </xsl:element>
      </xsl:for-each>
    </pipeline-node>
    <xsl:for-each select="map:match|map:select">
      <xsl:apply-templates select=".">
        <xsl:with-param name="id">
          <xsl:value-of select="$id" />
          <xsl:value-of select="position()" />
        </xsl:with-param>
      </xsl:apply-templates>
    </xsl:for-each>
  </xsl:template>
  
  <xsl:template match="map:match">
    <xsl:param name="id" />
    <match-node id="{$id}">
      <xsl:apply-templates select="@*|map:parameter" />
      <xsl:if test="not(@type)">
        <xsl:if test="/map:sitemap/map:components/map:matchers/@default">
          <xsl:attribute name="type">
            <xsl:value-of select="/map:sitemap/map:components/map:matchers/@default" />
          </xsl:attribute>
        </xsl:if>
      </xsl:if>
      <xsl:for-each select="map:match|map:select|map:call|map:aggregate|map:generate|map:transform|map:serialize">
        <xsl:element name="{local-name()}">
          <xsl:attribute name="id-ref">
            <xsl:value-of select="$id"/>
            <xsl:value-of select="position()" />
          </xsl:attribute>
        </xsl:element>
      </xsl:for-each>
    </match-node>
    <xsl:for-each select="map:match|map:select|map:call|map:aggregate|map:generate|map:transform|map:serialize">
      <xsl:apply-templates select=".">
        <xsl:with-param name="id">
          <xsl:value-of select="$id"/>
          <xsl:value-of select="position()"/>
        </xsl:with-param>
      </xsl:apply-templates>
    </xsl:for-each>
  </xsl:template>
  
  <xsl:template match="map:select">
    <xsl:param name="id" />
    <select-node id="{$id}">
      <xsl:apply-templates select="@*|map:parameter" />
      <xsl:if test="not(@type)">
        <xsl:if test="/map:sitemap/map:components/map:selectors/@default">
          <xsl:attribute name="type">
            <xsl:value-of select="/map:sitemap/map:components/map:selectors/@default" />
          </xsl:attribute>
        </xsl:if>
      </xsl:if>
      <xsl:for-each select="map:when|map:otherwise">
        <xsl:element name="local-name()">
          <xsl:copy-of select="@test" />
        </xsl:element>
        <xsl:for-each select="map:match|map:select|map:call|map:aggregate|map:generate|map:transform|map:serialize">
          <xsl:element name="{local-name()}">
            <xsl:attribute name="id-ref">
              <xsl:value-of select="$id"/>
              <xsl:value-of select="position()" />
            </xsl:attribute>
          </xsl:element>
        </xsl:for-each>
      </xsl:for-each>
    </select-node>
    <xsl:for-each select="map:when|map:otherwise">
      <xsl:for-each select="map:match|map:select|map:call|map:aggregate|map:generate|map:transform|map:serialize">
        <xsl:apply-templates select=".">
          <xsl:with-param name="id">
            <xsl:value-of select="$id"/>
            <xsl:value-of select="position()"/>
          </xsl:with-param>
        </xsl:apply-templates>
      </xsl:for-each>
    </xsl:for-each>
  </xsl:template>
  
  <xsl:template match="map:generate">
    <xsl:param name="id" />
    <generate-node id="{$id}">
      <xsl:apply-templates select="@*|map:parameters" />
      <xsl:if test="not(@type)">
        <xsl:if test="/map:sitemap/map:components/map:generators/@default">
          <xsl:attribute name="type">
            <xsl:value-of select="/map:sitemap/map:components/map:generators/@default" />
          </xsl:attribute>
        </xsl:if>
      </xsl:if>
    </generate-node>
  </xsl:template>
  
  <xsl:template match="map:transform">
    <xsl:param name="id" />
    <transform-node id="{$id}">
      <xsl:apply-templates select="@*|map:parameters" />
      <xsl:if test="not(@type)">
        <xsl:if test="/map:sitemap/map:components/map:transformers/@default">
          <xsl:attribute name="type">
            <xsl:value-of select="/map:sitemap/map:components/map:transformers/@default" />
          </xsl:attribute>
        </xsl:if>
      </xsl:if>
    </transform-node>
  </xsl:template>
  
  <xsl:template match="map:serialize">
    <xsl:param name="id" />
    <serialize-node id="{$id}">
      <xsl:apply-templates select="@*|map:parameters" />
      <xsl:if test="not(@type)">
        <xsl:if test="/map:sitemap/map:components/map:serializers/@default">
          <xsl:attribute name="type">
            <xsl:value-of select="/map:sitemap/map:components/map:serializers/@default" />
          </xsl:attribute>
        </xsl:if>
      </xsl:if>
    </serialize-node>
  </xsl:template>
  
  <xsl:template match="map:call">
    <xsl:param name="id" />
    <call id="{$id}">
      <xsl:apply-templates select="@*|map:parameter" />
    </call>
  </xsl:template>
  
  <xsl:template match="map:aggregate">
    <xsl:param name="id" />
    <aggregate-node id="{$id}">
      <xsl:apply-templates select="@*|map:part" />
    </aggregate-node>
  </xsl:template>
  
  <xsl:template match="map:part">
    <part>
      <xsl:copy-of select="@*" />
    </part>
  </xsl:template>
  
  <xsl:template match="map:parameter">
    <parameter name="{@name}" value="{@value}" />
  </xsl:template>
  
  <xsl:template match="@*">
    <xsl:copy-of select="." />
  </xsl:template>
  
</xsl:stylesheet>
