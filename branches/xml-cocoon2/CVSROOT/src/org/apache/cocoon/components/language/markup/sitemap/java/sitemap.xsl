<?xml version="1.0"?>
<!--
 *****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * _________________________________________________________________________ *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************
-->

<!--
 * @author &lt;a href="mailto:Giacomo.Pati@pwr.ch"&gt;Giacomo Pati&lt;/a&gt;
 * @version CVS $Revision: 1.1.2.41 $ $Date: 2000-10-01 00:19:53 $
-->

<!-- Sitemap Core logicsheet for the Java language -->
<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:map="http://apache.org/cocoon/sitemap/1.0"
    xmlns:java="http://xml.apache.org/xslt/java" exclude-result-prefixes="java"
    version="1.0">

  <xsl:output method="text"/>

  <xsl:variable name="prefix">map</xsl:variable>
  <xsl:variable name="factory-loader" select="java:org.apache.cocoon.sitemap.XSLTFactoryLoader.new()"/>

  <xsl:template match="/">
    <code xml:space="preserve">
      <xsl:apply-templates/>
    </code>
  </xsl:template>

  <xsl:template match="map:sitemap">
    package <xsl:value-of select="translate(@file-path, '/', '.')"/>;

    import java.io.OutputStream;
    import java.io.IOException;

    import java.util.List;
    import java.util.ArrayList;
    import java.util.Map;

    import org.apache.avalon.Configurable;
    import org.apache.avalon.Configuration;
    import org.apache.avalon.ConfigurationException;
    import org.apache.avalon.SAXConfigurationBuilder;
    import org.apache.avalon.utils.Parameters;

    import org.apache.cocoon.Cocoon;
    import org.apache.cocoon.ProcessingException;
    import org.apache.cocoon.environment.Environment;
    import org.apache.cocoon.generation.Generator;
    import org.apache.cocoon.matching.Matcher;
    import org.apache.cocoon.reading.Reader;
    import org.apache.cocoon.selection.Selector;
    //import org.apache.cocoon.acting.Action;
    import org.apache.cocoon.serialization.Serializer;
    import org.apache.cocoon.sitemap.AbstractSitemap;
    import org.apache.cocoon.sitemap.ResourcePipeline;
    import org.apache.cocoon.sitemap.Sitemap;
    import org.apache.cocoon.sitemap.ErrorNotifier;
    import org.apache.cocoon.sitemap.Manager;
    import org.apache.cocoon.transformation.Transformer;

    import org.xml.sax.SAXException;
    import org.xml.sax.helpers.AttributesImpl;

public class <xsl:value-of select="@file-name"/> extends AbstractSitemap {
    static {
        dateCreated = <xsl:value-of select="@creation-date"/>L;
    }

    private Parameters emptyParam = new Parameters();
    private Generator generator_error_handler = null;
    private Configuration generator_config_error_handler = null;
    private Transformer transformer_link_translator = null;
    private Configuration transformer_config_link_translator = null;

    <!-- generate variables for all components -->
    /** The generators */
    <xsl:for-each select="/map:sitemap/map:components/map:generators/map:generator">
      private Generator generator_<xsl:value-of select="translate(./@name, '- ', '__')"/> = null;
      private Configuration generator_config_<xsl:value-of select="translate(./@name, '- ', '__')"/> = null;
    </xsl:for-each>

    /** The transformers */
    <xsl:for-each select="/map:sitemap/map:components/map:transformers/map:transformer">
      private Transformer transformer_<xsl:value-of select="translate(./@name, '- ', '__')"/> = null;
      private Configuration transformer_config_<xsl:value-of select="translate(./@name, '- ', '__')"/> = null;
    </xsl:for-each>

    /** The readers */
    <xsl:for-each select="/map:sitemap/map:components/map:readers/map:reader">
      private Reader reader_<xsl:value-of select="translate(./@name, '- ', '__')"/> = null;
      private Configuration reader_config_<xsl:value-of select="translate(./@name, '- ', '__')"/> = null;
    </xsl:for-each>

    /** The serializers */
    <xsl:for-each select="/map:sitemap/map:components/map:serializers/map:serializer">
      private Serializer serializer_<xsl:value-of select="translate(./@name, '- ', '__')"/> = null;
      private Configuration serializer_config_<xsl:value-of select="translate(./@name, '- ', '__')"/> = null;
    </xsl:for-each>

    /** The matchers */
    <xsl:for-each select="/map:sitemap/map:components/map:matchers/map:matcher[@src]">
      private Matcher matcher_<xsl:value-of select="translate(./@name, '- ', '__')"/> = null;
      private Configuration matcher_config_<xsl:value-of select="translate(./@name, '- ', '__')"/> = null;
    </xsl:for-each>

    /** The selectors */
    <xsl:for-each select="/map:sitemap/map:components/map:selectors/map:selector[@src]">
      private Selector selector_<xsl:value-of select="translate(./@name, '- ', '__')"/> = null;
      private Configuration selector_config_<xsl:value-of select="translate(./@name, '- ', '__')"/> = null;
    </xsl:for-each>

    /** The actions */
    <xsl:for-each select="/map:sitemap/map:components/map:actions/map:action[@src]">
      private Action action_<xsl:value-of select="translate(./@name, '- ', '__')"/> = null;
      private Configuration action_config_<xsl:value-of select="translate(./@name, '- ', '__')"/> = null;
    </xsl:for-each>

    /** The generated matchers */
    <xsl:for-each select="/map:sitemap/map:components/map:matchers/map:matcher[@factory]">
      <xsl:variable name="factory" select="@factory"/>
      <xsl:variable name="type" select="@name"/>
      <xsl:variable name="default"><xsl:if test="$type = ../@default">true</xsl:if></xsl:variable>
      <xsl:variable name="config"><xsl:copy-of select="."/></xsl:variable>
      <xsl:for-each select="/map:sitemap/map:pipelines/map:pipeline/descendant-or-self::map:match[@type=$type or (not(@type) and $default!='')]">
        <xsl:variable name="matcher-name">
          <xsl:call-template name="mangle-name">
            <xsl:with-param name="prefix">matcher_</xsl:with-param>
            <xsl:with-param name="value" select="@pattern"/>
          </xsl:call-template>
        </xsl:variable>
        <xsl:value-of select="java:getClassSource($factory-loader,string($factory),string($matcher-name),string(@pattern),$config)"/>
        private List <xsl:value-of select="$matcher-name"/> (String pattern, Environment environment) {
          <xsl:value-of select="java:getMethodSource($factory-loader,string($factory),string($matcher-name),string(@pattern),$config)"/>
        }
      </xsl:for-each>
    </xsl:for-each>

    /** The generated selectors */
    <xsl:for-each select="/map:sitemap/map:components/map:selectors/map:selector[@factory]">
      <xsl:variable name="factory" select="@factory"/>
      <xsl:variable name="type" select="@name"/>
      <xsl:variable name="default"><xsl:if test="$type = ../@default">true</xsl:if></xsl:variable>
      <xsl:variable name="config"><xsl:copy-of select="."/></xsl:variable>
      <xsl:for-each select="/map:sitemap/map:pipelines/map:pipeline/descendant-or-self::map:when[../map:select/@type=$type or (not(../map:select/@type) and $default!='')]">
        <xsl:variable name="selector-name">
          <xsl:call-template name="mangle-name">
            <xsl:with-param name="prefix">selector_</xsl:with-param>
            <xsl:with-param name="value" select="@test"/>
          </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="this-test">
          <xsl:value-of select="@test"/>
        </xsl:variable>
        <xsl:if test="not(preceding::map:when[@test = $this-test])">
          <xsl:value-of select="java:getClassSource($factory-loader,string($factory),string(@test),string($selector-name),$config)"/>
          private boolean <xsl:value-of select="$selector-name"/> (String pattern, Environment environment) {
            <xsl:value-of select="java:getMethodSource($factory-loader,string($factory),string(@test),string($selector-name),$config)"/>
          }
        </xsl:if>
      </xsl:for-each>
    </xsl:for-each>

    /**
     * Pass a &lt;code&gt;Configuration&lt;/code&gt; instance to this
     * &lt;code&gt;Configurable&lt;/code&gt; class.
     */
    public void setConfiguration(Configuration conf)
    throws ConfigurationException /*, SAXException, ClassNotFoundException,
           InstantiationException, IllegalAccessException */ {
      SAXConfigurationBuilder confBuilder = new SAXConfigurationBuilder ();
      Configuration cconf = null;
      AttributesImpl attr = new AttributesImpl();

      this.sitemapManager = new Manager();
      this.sitemapManager.setComponentManager(this.manager);
      this.sitemapManager.setConfiguration(conf);
      try {
      <!-- configure all components -->
      /* Configure special ErrorNotifier */
      confBuilder.startDocument ();
      confBuilder.endDocument ();
      Configuration cconf2 = confBuilder.getConfiguration();
      generator_config_error_handler = cconf2;
      generator_error_handler = 
        (Generator) load_component ("org.apache.cocoon.sitemap.ErrorNotifier", cconf2);
      transformer_config_link_translator = cconf2;
      transformer_link_translator = 
        (Transformer) load_component ("org.apache.cocoon.sitemap.LinkTranslator", cconf2);

      /* Configure generators */
      <xsl:call-template name="config-components">
        <xsl:with-param name="name">generator</xsl:with-param>
        <xsl:with-param name="interface">Generator</xsl:with-param>
        <xsl:with-param name="components"
            select="/map:sitemap/map:components/map:generators/map:generator"/>
      </xsl:call-template>

      /* Configure transformers */
      <xsl:call-template name="config-components">
        <xsl:with-param name="name">transformer</xsl:with-param>
        <xsl:with-param name="interface">Transformer</xsl:with-param>
        <xsl:with-param name="components"
            select="/map:sitemap/map:components/map:transformers/map:transformer"/>
      </xsl:call-template>

      /* Configure readers */
      <xsl:call-template name="config-components">
        <xsl:with-param name="name">reader</xsl:with-param>
        <xsl:with-param name="interface">Reader</xsl:with-param>
        <xsl:with-param name="components"
            select="/map:sitemap/map:components/map:readers/map:reader"/>
      </xsl:call-template>

      /* Configure serializers */
      <xsl:call-template name="config-components">
        <xsl:with-param name="name">serializer</xsl:with-param>
        <xsl:with-param name="interface">Serializer</xsl:with-param>
        <xsl:with-param name="components"
            select="/map:sitemap/map:components/map:serializers/map:serializer"/>
      </xsl:call-template>

      /* Configure matchers */
      <xsl:call-template name="config-components">
        <xsl:with-param name="name">matcher</xsl:with-param>
        <xsl:with-param name="interface">Matcher</xsl:with-param>
        <xsl:with-param name="components"
            select="/map:sitemap/map:components/map:matchers/map:matcher[@src]"/>
      </xsl:call-template>

      /* Configure selectors */
      <xsl:call-template name="config-components">
        <xsl:with-param name="name">selector</xsl:with-param>
        <xsl:with-param name="interface">Selector</xsl:with-param>
        <xsl:with-param name="components"
            select="/map:sitemap/map:components/map:selectors/map:selector[@src]"/>
      </xsl:call-template>

      /* Configure actions */
      <xsl:call-template name="config-components">
        <xsl:with-param name="name">action</xsl:with-param>
        <xsl:with-param name="interface">Action</xsl:with-param>
        <xsl:with-param name="components"
            select="/map:sitemap/map:components/map:actions/map:action[@src]"/>
      </xsl:call-template>
      } catch (Exception e) {
        throw new ConfigurationException (e.toString(), cconf);
      }
    }

    <xsl:for-each select="/map:sitemap/map:resources/map:resource">
      private boolean resource_<xsl:value-of select="translate(@name, '- ', '__')"/> (ResourcePipeline pipeline,
          List listOfLists, Environment environment, String cocoon_view)
      throws Exception {
        List list = null;
        Parameters param = null;
        <xsl:apply-templates select="./*"/>
        return false;
      }
    </xsl:for-each>

    <xsl:for-each select="/map:sitemap/map:views/map:view">
      private boolean view_<xsl:value-of select="translate(@name, '- ', '__')"/> (ResourcePipeline pipeline,
          List listOfLists, Environment environment)
      throws Exception {
        List list = null;
        Parameters param = null;
        <xsl:apply-templates select="./*"/>
        return false;
      }
    </xsl:for-each>

    /**
     * Process to producing the output to the specified &gt;code&lt;OutputStream&gt;/code&lt;.
     */
    public boolean process(Environment environment)
    throws Exception {
      ResourcePipeline pipeline = new ResourcePipeline ();
      pipeline.setComponentManager (this.manager);
      List listOfLists = (List)(new ArrayList());
      List list = null;
      Parameters param = null;
      Map objectModel = environment.getObjectModel();
      String cocoon_view = environment.getView();
      <xsl:for-each select="/map:sitemap/map:pipelines/map:pipeline">
        <xsl:variable name="pipeline-position" select="position()"/>
        try {
          <xsl:apply-templates select="./*"/>
        } catch (Exception e) {
          <xsl:choose>
          <xsl:when test="(./map:handle-errors)">
            try {
              return error_process_<xsl:value-of select="$pipeline-position"/> (environment, objectModel, e);
            } catch (Exception ex) {
              System.out.println (ex.toString());
              ex.printStackTrace(System.out);
            }
          </xsl:when>
          <xsl:otherwise>
            System.out.println (e.toString());
            e.printStackTrace(System.out);
          </xsl:otherwise>
        </xsl:choose>
        }
      </xsl:for-each>
      return false;
    }

    <xsl:for-each select="/map:sitemap/map:pipelines/map:pipeline">
      <xsl:variable name="pipeline-position" select="position()"/>
      <xsl:if test="(./map:handle-errors)">
        private boolean error_process_<xsl:value-of select="$pipeline-position"/> (Environment environment, Map objectModel, Exception e)
        throws Exception {
          ResourcePipeline pipeline = new ResourcePipeline ();
          pipeline.setComponentManager (this.manager);
          List listOfLists = (List)(new ArrayList());
          List list = null;
          Parameters param = null;
          pipeline.setGenerator (generator_error_handler, e.getMessage(), null, emptyParam);
          ErrorNotifier eg = (ErrorNotifier) pipeline.getGenerator();
          eg.setException (e);
          <xsl:apply-templates select="./map:handle-errors/*"/>
          return false;
        }
      </xsl:if>
    </xsl:for-each>
}


  </xsl:template> <!-- match="map:sitemap" -->

  <xsl:template match="map:match">
    <xsl:variable name="matcher-type">
      <xsl:call-template name="get-parameter">
        <xsl:with-param name="parname">type</xsl:with-param>
        <xsl:with-param name="default"><xsl:value-of select="/map:sitemap/map:components/map:matchers/@default"/></xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="pattern-value">
      <xsl:call-template name="get-parameter">
        <xsl:with-param name="parname">pattern</xsl:with-param>
        <xsl:with-param name="required">true</xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="matcher-name2">
      <xsl:call-template name="mangle-name">
        <xsl:with-param name="value"><xsl:value-of select="@pattern"/></xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="matcher-name">
      <xsl:for-each select="/map:sitemap/map:components/map:matchers/map:matcher[@name=$matcher-type]">
        <xsl:choose>
          <xsl:when test="(./@src)">
            matcher_<xsl:value-of select="translate($matcher-type, '- ', '__')"/>.match
          </xsl:when>
          <xsl:when test="(./@factory)">
            matcher_<xsl:value-of select="$matcher-name2"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:call-template name="error">
              <xsl:with-param name="message">cannot choose a matcher name <xsl:value-of select="$matcher-type"/></xsl:with-param>
            </xsl:call-template>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each>
    </xsl:variable>
    if ((list = <xsl:value-of select="$matcher-name"/> ("<xsl:value-of select="$pattern-value"/>", environment)) != null) {
      listOfLists.add (list);
      <xsl:apply-templates/>
      listOfLists.remove (list);
    }
  </xsl:template> <!-- match="map:match" -->

  <xsl:template match="map:select">
    <xsl:variable name="selector-type">
      <xsl:call-template name="get-parameter">
        <xsl:with-param name="parname">type</xsl:with-param>
        <xsl:with-param name="default"><xsl:value-of select="/map:sitemap/map:components/map:selectors/@default"/></xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:for-each select="./map:when">
      <xsl:variable name="test-value">
        <xsl:call-template name="get-parameter">
          <xsl:with-param name="parname">test</xsl:with-param>
          <xsl:with-param name="required">true</xsl:with-param>
        </xsl:call-template>
      </xsl:variable>
      <xsl:variable name="selector-name2">
        <xsl:call-template name="mangle-name">
          <xsl:with-param name="value" select="@test"/>
        </xsl:call-template>
      </xsl:variable>
      <xsl:variable name="selector-name">
        <xsl:for-each select="/map:sitemap/map:components/map:selectors/map:selector[@name=$selector-type]">
          <xsl:choose>
            <xsl:when test="(./@src)">
              selector_<xsl:value-of select="translate($selector-type, '- ', '__')"/>.select
            </xsl:when>
            <xsl:when test="(./@factory)">
              selector_<xsl:value-of select="$selector-name2"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:call-template name="error">
                <xsl:with-param name="message">cannot choose a selector name <xsl:value-of select="$selector-type"/></xsl:with-param>
              </xsl:call-template>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:for-each>
      </xsl:variable>
      <xsl:if test="position() > 1">
        else
      </xsl:if>
      if (<xsl:value-of select="$selector-name"/> ("<xsl:value-of select="$test-value"/>", environment)) {
       <xsl:apply-templates/>
      }
    </xsl:for-each>

    <xsl:for-each select="./map:otherwise">
      else {
      <xsl:apply-templates/>
      }
    </xsl:for-each>
  </xsl:template> <!-- match="/map:sitemap/map:select" -->

  <xsl:template match="map:act">
    <xsl:variable name="action-type">
      <xsl:call-template name="get-parameter">
        <xsl:with-param name="parname">type</xsl:with-param>
        <xsl:with-param name="default"><xsl:value-of select="/map:sitemap/map:components/map:actions/@default"/></xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="src-param">
      <xsl:call-template name="get-parameter-as-string">
        <xsl:with-param name="parname">src</xsl:with-param>
        <xsl:with-param name="default">null</xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="action-name">
      <xsl:for-each select="/map:sitemap/map:components/map:actions/map:action[@name=$action-type]">
        <xsl:choose>
          <xsl:when test="(./@src)">
            action_<xsl:value-of select="translate($action-type, '- ', '__')"/>.act
          </xsl:when>
          <xsl:otherwise>
            <xsl:call-template name="error">
              <xsl:with-param name="message">cannot choose an action name <xsl:value-of select="$action-type"/></xsl:with-param>
            </xsl:call-template>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each>
    </xsl:variable>
    <xsl:if test="count(parameter)>0">
      param = new Parameters ();
    </xsl:if>
    <xsl:variable name="component-param">
      <xsl:choose>
        <xsl:when test="count(parameter)>0">
          param
        </xsl:when>
        <xsl:otherwise>
          emptyParam
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:apply-templates select="parameter"/>
    <xsl:choose>
      <xsl:when test="./*">
        if ((list = <xsl:value-of select="$action-name"/> (environment, objectModel, <xsl:value-of select="$src-param"/>, <xsl:value-of select="$component-param"/>)) != null) {
          listOfLists.add (list);
          <xsl:apply-templates/>
          listOfList.remove(list);
        }
      </xsl:when>
      <xsl:otherwise>
        list = <xsl:value-of select="$action-name"/> (environment, objectModel, <xsl:value-of select="$src-param"/>, <xsl:value-of select="$component-param"/>);
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template> <!-- match="map:act" -->

  <xsl:template match="map:generate">
    <xsl:call-template name="setup-component">
      <xsl:with-param name="default-component" select="/map:sitemap/map:components/map:generators/@default"/>
      <xsl:with-param name="method">setGenerator</xsl:with-param>
      <xsl:with-param name="prefix">generator</xsl:with-param>
    </xsl:call-template>
  </xsl:template> <!-- match="map:generate" -->

  <xsl:template match="map:transform">
    <xsl:call-template name="setup-component">
      <xsl:with-param name="default-component" select="/map:sitemap/map:components/map:transformers/@default"/>
      <xsl:with-param name="method">addTransformer</xsl:with-param>
      <xsl:with-param name="prefix">transformer</xsl:with-param>
    </xsl:call-template>
  </xsl:template> <!-- match="map:transformer" -->

  <xsl:template match="map:serialize">
    <xsl:variable name="default-serializer-type">
      <xsl:value-of select="/map:sitemap/map:components/map:serializers/@default"/>
    </xsl:variable>
    <xsl:variable name="this-type">
      <xsl:choose>
        <xsl:when test="@type">
          <xsl:value-of select="@type"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$default-serializer-type"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="default-mime-type">
      <xsl:value-of select="/map:sitemap/map:components/map:serializers/map:serializer[@name=$this-type]/@mime-type"/>
    </xsl:variable>
    <xsl:call-template name="setup-component">
      <xsl:with-param name="default-component" select="$default-serializer-type"/>
      <xsl:with-param name="method">setSerializer</xsl:with-param>
      <xsl:with-param name="prefix">serializer</xsl:with-param>
      <xsl:with-param name="mime-type">
        <xsl:choose>
          <xsl:when test="@mime-type">
            <xsl:value-of select="@mime-type"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$default-mime-type"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:with-param>
    </xsl:call-template>
    <xsl:if test="@status-code">
      environment.setStatus(<xsl:value-of select="@status-code"/>);
    </xsl:if>
    if(true)return pipeline.process (environment);
  </xsl:template> <!-- match="map:serialize" -->

  <xsl:template match="map:read">
    <xsl:variable name="default-reader-type">
      <xsl:value-of select="/map:sitemap/map:components/map:readers/@default"/>
    </xsl:variable>
    <xsl:variable name="this-type">
      <xsl:choose>
        <xsl:when test="@type">
          <xsl:value-of select="@type"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$default-reader-type"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="default-mime-type">
      <xsl:value-of select="/map:sitemap/map:components/map:readers/map:reader[@name=$this-type]/@mime-type"/>
    </xsl:variable>
    <xsl:call-template name="setup-component">
      <xsl:with-param name="default-component" select="/map:sitemap/map:components/map:readers/@default"/>
      <xsl:with-param name="method">setReader</xsl:with-param>
      <xsl:with-param name="prefix">reader</xsl:with-param>
      <xsl:with-param name="mime-type">
        <xsl:choose>
          <xsl:when test="@mime-type">
            <xsl:value-of select="@mime-type"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$default-mime-type"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:with-param>
    </xsl:call-template>
    if(true)return pipeline.process (environment);
  </xsl:template> <!-- match="map:read" -->

  <xsl:template match="map:mount">
    <xsl:variable name="src" select="@src"/>
    <xsl:variable name="check-reload">
      <xsl:choose>
        <xsl:when test="@check-reload='yes'">true</xsl:when>
        <xsl:when test="@check-reload='true'">true</xsl:when>
        <xsl:when test="@check-reload='no'">false</xsl:when>
        <xsl:when test="@check-reload='false'">false</xsl:when>
        <xsl:when test="not(@check-reload)">true</xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="error">
            <xsl:with-param name="message">
              element <xsl:value-of select="name(.)"/> with uri-prefix="<xsl:value-of select="@uri-prefix"/>" has a wrong value in 'check-reload' attribute . Use "yes" or "no" but not "<xsl:value-of select="@check-reload"/>".
            </xsl:with-param>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="substring(@uri-prefix,string-length(@uri-prefix))='/'">
        if(true)return sitemapManager.invoke (environment, substitute(listOfLists,"<xsl:value-of select="@uri-prefix"/>"), substitute(listOfLists,"<xsl:value-of select="@src"/>"), <xsl:value-of select="$check-reload"/>);
      </xsl:when>
      <xsl:when test="substring(@uri-prefix,string-length(@uri-prefix))='}'">
        String uri_prefix<xsl:value-of select="count(.)"/>=substitute(listOfLists,"<xsl:value-of select="@uri-prefix"/>");
        if (uri_prefix<xsl:value-of select="count(.)"/>.charAt(uri_prefix<xsl:value-of select="count(.)"/>.length()-1)=='/'){
          return sitemapManager.invoke (environment, uri_prefix<xsl:value-of select="count(.)"/>, substitute(listOfLists,"<xsl:value-of select="@src"/>"), <xsl:value-of select="$check-reload"/>);
        } else {
          return sitemapManager.invoke (environment, uri_prefix<xsl:value-of select="count(.)"/>+"/", substitute(listOfLists,"<xsl:value-of select="@src"/>"), <xsl:value-of select="$check-reload"/>);
        }
      </xsl:when>
      <xsl:otherwise>
        if(true)return sitemapManager.invoke (environment, substitute(listOfLists,"<xsl:value-of select="@uri-prefix"/>/"), substitute(listOfLists,"<xsl:value-of select="@src"/>"), <xsl:value-of select="$check-reload"/>);
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template> <!-- match="map:mount" -->

  <xsl:template match="map:redirect-to">
    <xsl:choose>
      <xsl:when test="@resource">
        if(true)return resource_<xsl:value-of select="translate(@resource, '- ', '__')"/>(pipeline, listOfLists, environment, cocoon_view);
      </xsl:when>
      <xsl:when test="@uri">
        environment.redirect (substitute(listOfLists, "<xsl:value-of select="@uri"/>"));
        if(true)return true;
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="error">
          <xsl:with-param name="message">Missing attribute uri= or resource= to element redirect-to</xsl:with-param>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template> <!-- match="map:redirect-to" -->

  <xsl:template match="map:label">
    <xsl:apply-templates/>
    if ("<xsl:value-of select="@name"/>".equals(cocoon_view))
      return view_<xsl:value-of select="translate(@name, '- ', '__')"/> (pipeline, listOfLists, environment);
  </xsl:template> <!-- match="map:label" -->

  <xsl:template match="map:pipeline//parameter">
    param.setParameter ("<xsl:value-of select="@name"/>", "<xsl:value-of select="@value"/>");
  </xsl:template>

  <xsl:template match="map:param">
    param.setParameter ("<xsl:value-of select="@name"/>", "<xsl:value-of select="@map:value"/>");
  </xsl:template>

  <!-- Sitemap Utility templates -->

  <xsl:template name="config-components">
    <xsl:param name="name"/>
    <xsl:param name="interface"/>
    <xsl:param name="components"/>

    <xsl:variable name="qname">
      <xsl:value-of select="concat($prefix, ':value')"/>
    </xsl:variable>

    <xsl:variable name="ns" select="namespace-uri(.)"/>
    <xsl:for-each select="$components">
      confBuilder.startDocument ();
      <xsl:if test="$ns">
        confBuilder.startPrefixMapping("","<xsl:value-of select="namespace-uri(.)"/>");
      </xsl:if>
      <!-- Create root configuration -->
      attr.clear();
      <xsl:for-each select="attribute::*[name(.)!=$qname]">
        attr.addAttribute ("", "<xsl:value-of select="local-name(.)"/>",
            "<xsl:value-of select="name(.)"/>", "CDATA",
            "<xsl:value-of select="."/>");
      </xsl:for-each>
      confBuilder.startElement ("", "<xsl:value-of select="translate(@name, '- ', '__')"/>",
                                    "<xsl:value-of select="translate(@name, '- ', '__')"/>", attr);
      <xsl:call-template name="nested-config-components">
        <xsl:with-param name="name" select="$name"/>
        <xsl:with-param name="config-name"><xsl:value-of select="concat(local-name(.),'/',@name)"/></xsl:with-param>
        <xsl:with-param name="interface" select="$interface"/>
        <xsl:with-param name="components" select="*"/>
        <xsl:with-param name="type" select="@name"/>
        <xsl:with-param name="ns" select="$ns"/>
      </xsl:call-template>
      <!-- end root configuration -->
      confBuilder.endElement ("", "<xsl:value-of select="translate(@name, '- ', '__')"/>",
                                  "<xsl:value-of select="translate(@name, '- ', '__')"/>");
      <xsl:if test="$ns">
        confBuilder.endPrefixMapping("");
      </xsl:if>
      confBuilder.endDocument ();
      cconf = confBuilder.getConfiguration();
      <xsl:value-of select="$name"/>_config_<xsl:value-of select="translate(@name, '- ', '__')"/> = cconf;
      <xsl:value-of select="$name"/>_<xsl:value-of select="translate(@name, '- ', '__')"/> =
        (<xsl:value-of select="$interface"/>)load_component ("<xsl:value-of select="@src"/>",
            cconf);
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="nested-config-components">
    <xsl:param name="name"/>
    <xsl:param name="config-name"/>
    <xsl:param name="interface"/>
    <xsl:param name="components"/>
    <xsl:param name="type"/>
    <xsl:param name="ns"/>
    <xsl:param name="subname"/>

    <xsl:variable name="qname">
      <xsl:value-of select="concat($prefix, ':value')"/>
    </xsl:variable>

    <!-- process content -->
    <xsl:for-each select="$components">
      <xsl:if test="$ns!=namespace-uri(.)">
        confBuilder.startPrefixMapping("","<xsl:value-of select="namespace-uri(.)"/>");
      </xsl:if>
      attr.clear();
      <xsl:for-each select="attribute::*[name(.)!=$qname]">
        attr.addAttribute ("", "<xsl:value-of select="local-name(.)"/>", "<xsl:value-of select="name(.)"/>", "CDATA", "<xsl:value-of select="."/>");
      </xsl:for-each>
      confBuilder.startElement("<xsl:value-of select="namespace-uri(.)"/>", "<xsl:value-of select="local-name(.)"/>", "<xsl:value-of select="name(.)"/>", attr);
      <xsl:for-each select="attribute::*[name(.)=$qname]">
        confBuilder.characters("<xsl:value-of select="."/>".toCharArray(), 0, <xsl:value-of select="string-length(.)"/>);
      </xsl:for-each>
      <xsl:if test="normalize-space(text())">
        confBuilder.characters("<xsl:value-of select="text()"/>".toCharArray(), 0, <xsl:value-of select="string-length(text())"/>);
      </xsl:if>
      <xsl:variable name="newsubname">
        <xsl:choose>
          <xsl:when test="not($subname)"><xsl:value-of select="position()"/></xsl:when>
          <xsl:otherwise><xsl:value-of select="concat($subname,position())"/></xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <xsl:call-template name="nested-config-components">
        <xsl:with-param name="name"><xsl:value-of select="$name"/></xsl:with-param>
        <xsl:with-param name="config-name"><xsl:value-of select="local-name(.)"/></xsl:with-param>
        <xsl:with-param name="interface"><xsl:value-of select="$interface"/></xsl:with-param>
        <xsl:with-param name="components" select="./*"/>
        <xsl:with-param name="type"><xsl:value-of select="$type"/></xsl:with-param>
        <xsl:with-param name="ns"><xsl:value-of select="namespace-uri(.)"/></xsl:with-param>
        <xsl:with-param name="subname"><xsl:value-of select="$newsubname"/></xsl:with-param>
      </xsl:call-template>
      confBuilder.endElement("<xsl:value-of select="namespace-uri(.)"/>", "<xsl:value-of select="local-name(.)"/>", "<xsl:value-of select="name(.)"/>");
      <xsl:if test="$ns!=namespace-uri(.)">
        confBuilder.endPrefixMapping("");
      </xsl:if>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="setup-component">
    <xsl:param name="default-component"/>
    <xsl:param name="method"/>
    <xsl:param name="prefix"/>
    <xsl:param name="mime-type"/>
    <!-- view/label 'last' check -->
    <xsl:if test="not(ancestor::map:views) and not(ancestor::map:handle-errors)">
      <xsl:if test="$prefix='serializer'">
        <xsl:for-each select="/map:sitemap/map:views/map:view[@from-position='last']">
          if ("<xsl:value-of select="@name"/>".equals(cocoon_view)) {
            return view_<xsl:value-of select="translate(@name, '- ', '__')"/> (pipeline, listOfLists, environment);
          }
        </xsl:for-each>
        // performing link translation
        if (environment.getObjectModel().containsKey(Cocoon.LINK_OBJECT)) {
            pipeline.addTransformer (transformer_link_translator, null, transformer_config_link_translator, emptyParam);
        }
      </xsl:if>
    </xsl:if>
    <xsl:variable name="component-type">
      <xsl:call-template name="get-parameter">
        <xsl:with-param name="parname">type</xsl:with-param>
        <xsl:with-param name="default"><xsl:value-of select="$default-component"/></xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="component-source">
      <xsl:call-template name="get-parameter">
        <xsl:with-param name="parname">src</xsl:with-param>
        <xsl:with-param name="default">null</xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:if test="count(parameter)>0">
      param = new Parameters ();
    </xsl:if>
    <xsl:variable name="component-param">
      <xsl:choose>
        <xsl:when test="count(parameter)>0">
          param
        </xsl:when>
        <xsl:otherwise>
          emptyParam
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:apply-templates select="parameter"/>
    <xsl:choose>
      <xsl:when test="$component-source='null'">
        <xsl:choose>
          <xsl:when test="$mime-type!=''">
            pipeline.<xsl:value-of select="$method"/> (<xsl:value-of select="$prefix"/>_<xsl:value-of select="$component-type"/>,
                null, <xsl:value-of select="$prefix"/>_config_<xsl:value-of select="$component-type"/>,
                <xsl:value-of select="$component-param"/>,"<xsl:value-of select="$mime-type"/>");
          </xsl:when>
          <xsl:otherwise>
            pipeline.<xsl:value-of select="$method"/> (<xsl:value-of select="$prefix"/>_<xsl:value-of select="$component-type"/>,
                null, <xsl:value-of select="$prefix"/>_config_<xsl:value-of select="$component-type"/>,
                <xsl:value-of select="$component-param"/>);
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when test="$mime-type!=''">
            pipeline.<xsl:value-of select="$method"/> (<xsl:value-of select="$prefix"/>_<xsl:value-of select="$component-type"/>,
                substitute(listOfLists,"<xsl:value-of select="$component-source"/>"),
                <xsl:value-of select="$prefix"/>_config_<xsl:value-of select="$component-type"/>,
                <xsl:value-of select="$component-param"/>,"<xsl:value-of select="$mime-type"/>");
          </xsl:when>
          <xsl:otherwise>
            pipeline.<xsl:value-of select="$method"/> (<xsl:value-of select="$prefix"/>_<xsl:value-of select="$component-type"/>,
                substitute(listOfLists,"<xsl:value-of select="$component-source"/>"),
                <xsl:value-of select="$prefix"/>_config_<xsl:value-of select="$component-type"/>,
                <xsl:value-of select="$component-param"/>);
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
    <!-- view/label check -->
    <xsl:if test="not(ancestor::map:views) and not(ancestor::map:handle-errors)">
      <xsl:variable name="component-label">
        <xsl:if test="$prefix='generator'">
          <xsl:value-of select="/map:sitemap/map:components/map:generators/map:generator[@name=$component-type]/@label"/>
        </xsl:if>
        <xsl:if test="$prefix='transformer'">
          <xsl:value-of select="/map:sitemap/map:components/map:transformers/map:transformer[@name=$component-type]/@label"/>
        </xsl:if>
      </xsl:variable>
      <xsl:if test="$component-label">
        <xsl:for-each select="/map:sitemap/map:views/map:view[@from-label=$component-label]">
          if ("<xsl:value-of select="@name"/>".equals(cocoon_view)) {
            return view_<xsl:value-of select="translate(@name, '- ', '__')"/> (pipeline, listOfLists, environment);
          }
        </xsl:for-each>
      </xsl:if>
      <xsl:if test="$prefix='generator'">
        <xsl:for-each select="/map:sitemap/map:views/map:view[@from-position='first']">
          if ("<xsl:value-of select="@name"/>".equals(cocoon_view)) {
            return view_<xsl:value-of select="translate(@name, '- ', '__')"/> (pipeline, listOfLists, environment);
          }
        </xsl:for-each>
      </xsl:if>
    </xsl:if>
  </xsl:template>

  <xsl:template name="mangle-name">
    <xsl:param name="value"/>
    <xsl:param name="prefix"/>
    <xsl:param name="suffix"/>
    <xsl:variable name="value1" select="translate($value,'/- *?@:{}()[].#^\\$|&#33;~\','______________________')"/>
    <xsl:value-of select="$prefix"/><xsl:value-of select='translate($value1,"&#39;","")'/><xsl:value-of select="$suffix"/>
  </xsl:template>

  <!-- Utility templates -->

  <xsl:template name="get-parameter-as-string">
    <xsl:param name="parname"/>
    <xsl:param name="default"/>
    <xsl:param name="required">false</xsl:param>

    <xsl:variable name="result">
     <xsl:call-template name="get-parameter">
      <xsl:with-param name="parname"  select="$parname"/>
      <xsl:with-param name="default"  select="$default"/>
      <xsl:with-param name="required" select="$required"/>
     </xsl:call-template>
    </xsl:variable>

    <xsl:choose>
     <xsl:when test="$result=$default">
      <xsl:value-of select="$default"/>
     </xsl:when>
     <xsl:otherwise>
      "<xsl:value-of select="$result"/>"
     </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="get-parameter">
    <xsl:param name="parname"/>
    <xsl:param name="default"/>
    <xsl:param name="required">false</xsl:param>

    <xsl:variable name="qname">
      <xsl:value-of select="concat($prefix, ':param')"/>
    </xsl:variable>

    <xsl:choose>
      <xsl:when test="@*[name(.) = $parname]"><xsl:value-of select="@*[name(.) = $parname]"/>	</xsl:when>
      <xsl:when test="(*[name(.) = $qname])[@name = $parname]">
        <xsl:call-template name="get-nested-content">
          <xsl:with-param name="content"
                          select="(*[name(.) = $qname])[@name = $parname]"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when test="string-length($default) = 0">
            <xsl:choose>
              <xsl:when test="$required = 'true'">
                <xsl:call-template name="error">
                  <xsl:with-param name="message">
                    [Logicsheet processor] Parameter '<xsl:value-of select="$parname"/>' missing in dynamic tag &lt;<xsl:value-of select="name(.)"/>&gt;
                  </xsl:with-param>
                </xsl:call-template>
              </xsl:when>
              <xsl:otherwise>""</xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <xsl:otherwise><xsl:copy-of select="$default"/></xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="get-nested-content">
    <xsl:param name="content"/>
    <xsl:choose>
      <xsl:when test="$content/*">
        <xsl:apply-templates select="$content/*"/>
      </xsl:when>
      <xsl:otherwise><xsl:value-of select="$content"/></xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="error">
    <xsl:param name="message"/>
    <xsl:message terminate="yes"><xsl:value-of select="$message"/></xsl:message>
  </xsl:template>

  <!-- Ignored elements -->
  <xsl:template match="map:logicsheet|map:dependency|map:handle-errors"/>

</xsl:stylesheet>
