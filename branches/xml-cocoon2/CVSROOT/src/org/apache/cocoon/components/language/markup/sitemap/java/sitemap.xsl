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

<!-- Sitemap Core logicsheet for the Java language -->
<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:map="http://apache.org/cocoon/sitemap/1.0"
    xmlns:java="http://xml.apache.org/xslt/java" exclude-result-prefixes="java"
    version="1.0">

  <xsl:output method="text"/>

  <xsl:variable name="prefix">map</xsl:variable>

  <!-- this variable holds the factory loader used to get at the code matcher/selector factories
       are producing -->
  <xsl:variable name="factory-loader" select="java:org.apache.cocoon.sitemap.XSLTFactoryLoader.new()"/>

  <xsl:template match="/">
    <code xml:space="preserve">
      <xsl:apply-templates/>
    </code>
  </xsl:template>

  <xsl:template match="map:sitemap">
    /*****************************************************************************/
    /* Copyright (C) The Apache Software Foundation. All rights reserved.        */
    /* _________________________________________________________________________ */
    /* This software is published under the terms of the Apache Software License */
    /* version 1.1, a copy of which has been included  with this distribution in */
    /* the LICENSE file.                                                         */
    /*****************************************************************************/
    package <xsl:value-of select="translate(@file-path, '/', '.')"/>;

    import java.io.OutputStream;
    import java.io.IOException;

    import java.util.List;
    import java.util.ArrayList;
    import java.util.Map;
    import java.util.Stack;

    import javax.servlet.http.HttpServletRequest;

    import org.apache.avalon.Configurable;
    import org.apache.avalon.Configuration;
    import org.apache.avalon.ConfigurationException;
    import org.apache.avalon.DefaultConfiguration;
    import org.apache.avalon.Parameters;

    import org.apache.regexp.RE;
    import org.apache.regexp.REProgram;

    import org.apache.cocoon.Cocoon;
    import org.apache.cocoon.ProcessingException;
    import org.apache.cocoon.acting.Action;
    import org.apache.cocoon.environment.Environment;
    import org.apache.cocoon.matching.Matcher;
    import org.apache.cocoon.selection.Selector;
    import org.apache.cocoon.sitemap.AbstractSitemap;
    import org.apache.cocoon.sitemap.ComponentHolder;
    import org.apache.cocoon.sitemap.ResourcePipeline;
    import org.apache.cocoon.sitemap.Sitemap;
    import org.apache.cocoon.sitemap.ErrorNotifier;
    import org.apache.cocoon.sitemap.Manager;

    /**
     * This is the automatically generated class from the sitemap definitions
     *
     * @author &lt;a href="mailto:Giacomo.Pati@pwr.ch"&gt;Giacomo Pati&lt;/a&gt;
     * @author &lt;a href="mailto:bloritsch@apache.org"&gt;Berin Loiritsch&lt;/a&gt;
     * @version CVS $Revision: 1.1.2.62 $ $Date: 2000-11-15 19:29:34 $
     */
    public class <xsl:value-of select="@file-name"/> extends AbstractSitemap {
      static {
        dateCreated = <xsl:value-of select="@creation-date"/>L;
      }

      /** An empty &lt;code&gt;Parameter&lt;/code&gt; used to pass to the sitemap components */
      private Parameters emptyParam = new Parameters();

      <!-- Generate matchers which implements CodeFactory -->
      <xsl:for-each select="/map:sitemap/map:components/map:matchers/map:matcher[@src]">
        <xsl:if test="java:isFactory($factory-loader, string(@src))">
          <xsl:variable name="src" select="@src"/>
          <xsl:variable name="type" select="translate(@name, '- ', '__')"/>
          <xsl:variable name="default" select="$type = ../@default"/>
          <xsl:variable name="config"><xsl:copy-of select="."/></xsl:variable>
          private List <xsl:value-of select="$type"/>Match (<xsl:value-of select="java:getParameterSource($factory-loader, string($src),$config)"/> pattern, Map objectModel) {
            <xsl:value-of select="java:getMethodSource($factory-loader, string($src),$config)"/>
          }
          <!-- process all map:match elements with a type attribute refering to the current matcher factory iteration -->
          <xsl:for-each select="/map:sitemap/map:pipelines/map:pipeline/descendant-or-self::map:match[@type=$type or (not(@type) and $default)]">
            <xsl:variable name="matcher-name">
              <xsl:call-template name="generate-name">
                <xsl:with-param name="prefix">matcher_</xsl:with-param>
                <xsl:with-param name="value" select="@pattern"/>
                <xsl:with-param name="suffix"><xsl:value-of select="$type"/>_<xsl:value-of select="generate-id(.)"/></xsl:with-param>
              </xsl:call-template>
            </xsl:variable>
            /** The generated matcher for a pattern of "<xsl:value-of select="@pattern"/>" */
            <xsl:value-of select="java:getClassSource($factory-loader,string($src),string($matcher-name),string(@pattern),$config)"/>
          </xsl:for-each>
        </xsl:if>
      </xsl:for-each>

      /** The generated matchers (for backward compatability. Should be removed in the future) */
      <xsl:for-each select="/map:sitemap/map:components/map:matchers/map:matcher[@factory]">
        <xsl:variable name="factory" select="@factory"/>
        <xsl:variable name="type" select="translate(@name, '- ', '__')"/>
        <xsl:variable name="default" select="$type = ../@default"/>
        <xsl:variable name="config"><xsl:copy-of select="."/></xsl:variable>
       private List <xsl:value-of select="$type"/>Match (<xsl:value-of select="java:getParameterSource($factory-loader, string($factory),$config)"/> pattern, Map objectModel) {
           <xsl:value-of select="java:getMethodSource($factory-loader, string($factory),$config)"/>
       }
        <xsl:for-each select="/map:sitemap/map:pipelines/map:pipeline/descendant-or-self::map:match[@type=$type or (not(@type) and $default)]">
          <xsl:variable name="matcher-name">
            <xsl:call-template name="generate-name">
              <xsl:with-param name="prefix">matcher_</xsl:with-param>
              <xsl:with-param name="value" select="@pattern"/>
              <xsl:with-param name="suffix"><xsl:value-of select="$type"/>_<xsl:value-of select="generate-id(.)"/></xsl:with-param>
            </xsl:call-template>
          </xsl:variable>
          <xsl:value-of select="java:getClassSource($factory-loader,string($factory),string($matcher-name),string(@pattern),$config)"/>
        </xsl:for-each>
      </xsl:for-each>

      <!-- Generate selectors which implements CodeFactory -->
      <xsl:for-each select="/map:sitemap/map:components/map:selectors/map:selector[@src]">
        <xsl:if test="java:isFactory($factory-loader, string(@src))">
          <xsl:variable name="factory" select="@src"/>
          <xsl:variable name="name" select="@name"/>
          <xsl:variable name="type" select="translate(@name, '- ', '__')"/>
          <xsl:variable name="default" select="@name = ../@default"/>
          <xsl:variable name="config" select="descendant-or-self::*"/>
          private boolean <xsl:value-of select="$name"/>Select (<xsl:value-of select="java:getParameterSource($factory-loader, string($factory),$config)"/> pattern, Map objectModel) {
            <xsl:value-of select="java:getMethodSource($factory-loader, string($factory),$config)"/>
          }
          <xsl:for-each select="/map:sitemap/map:pipelines/map:pipeline/descendant::map:select[@type=$name or (not(@type) and $default)]/map:when">
            <xsl:variable name="selector-name">
              <xsl:call-template name="generate-name">
                <xsl:with-param name="prefix">selector_</xsl:with-param>
                <xsl:with-param name="value" select="@test"/>
                <xsl:with-param name="suffix"><xsl:value-of select="$type"/>_<xsl:value-of select="generate-id(..)"/></xsl:with-param>
              </xsl:call-template>
            </xsl:variable>
            <xsl:variable name="this-test">
              <xsl:value-of select="@test"/>
            </xsl:variable>
            <!-- produce a definition for this test string -->
            <xsl:value-of select="java:getClassSource($factory-loader,string($factory),string($selector-name),string(@test),$config)"/>
          </xsl:for-each>
        </xsl:if>
      </xsl:for-each>

      /** The generated selectors (for backward compatability. Should be removed in the future) */
      <xsl:for-each select="/map:sitemap/map:components/map:selectors/map:selector[@factory]">
        <xsl:variable name="factory" select="@factory"/>
        <xsl:variable name="name" select="@name"/>
        <xsl:variable name="type" select="translate(@name, '- ', '__')"/>
        <xsl:variable name="default" select="@name = ../@default"/>
        <xsl:variable name="config" select="descendant-or-self::*"/>
        <xsl:variable name="pos"><xsl:value-of select="string(position())"/></xsl:variable>
        private boolean <xsl:value-of select="$name"/>Select (<xsl:value-of select="java:getParameterSource($factory-loader, string($factory),$config)"/> pattern, Map objectModel) {
          <xsl:value-of select="java:getMethodSource($factory-loader, string($factory),$config)"/>
        }
        <xsl:for-each select="/map:sitemap/map:pipelines/map:pipeline/descendant::map:select[@type=$name or (not(@type) and $default)]/map:when">
          <xsl:variable name="selector-name">
            <xsl:call-template name="generate-name">
              <xsl:with-param name="prefix">selector_</xsl:with-param>
              <xsl:with-param name="value" select="@test"/>
              <xsl:with-param name="suffix"><xsl:value-of select="$type"/>_<xsl:value-of select="generate-id(..)"/></xsl:with-param>
            </xsl:call-template>
          </xsl:variable>
          <xsl:variable name="this-test">
            <xsl:value-of select="@test"/>
          </xsl:variable>
          <!-- produce a definition for this test string -->
          <xsl:value-of select="java:getClassSource($factory-loader,string($factory),string($selector-name),string(@test),$config)"/>
        </xsl:for-each>
      </xsl:for-each>

      /**
       * Pass a &lt;code&gt;Configuration&lt;/code&gt; instance to this
       * &lt;code&gt;Configurable&lt;/code&gt; class.
       */
      public void configure(Configuration conf) throws ConfigurationException {
        this.sitemapManager = new Manager(super.sitemapComponentManager);
        this.sitemapManager.compose(this.manager);
        this.sitemapManager.configure(conf);
        try {
          <!-- configure all components -->
          load_component ("!generator:error-notifier!", "org.apache.cocoon.sitemap.ErrorNotifier", new DefaultConfiguration(""), null);
          load_component ("!transformer:link-translator!", "org.apache.cocoon.sitemap.LinkTranslator", new DefaultConfiguration(""), null);

          <!-- Configure generators -->
      <xsl:call-template name="config-components">
            <xsl:with-param name="name">generator</xsl:with-param>
            <xsl:with-param name="components"
                select="/map:sitemap/map:components/map:generators/map:generator"/>
          </xsl:call-template>

          <!-- Configure transformers -->
          <xsl:call-template name="config-components">
            <xsl:with-param name="name">transformer</xsl:with-param>
            <xsl:with-param name="components"
                select="/map:sitemap/map:components/map:transformers/map:transformer"/>
          </xsl:call-template>

          <!-- Configure readers -->
          <xsl:call-template name="config-components">
            <xsl:with-param name="name">reader</xsl:with-param>
            <xsl:with-param name="components"
                select="/map:sitemap/map:components/map:readers/map:reader"/>
          </xsl:call-template>

          <!-- Configure serializers -->
          <xsl:call-template name="config-components">
            <xsl:with-param name="name">serializer</xsl:with-param>
            <xsl:with-param name="components"
                select="/map:sitemap/map:components/map:serializers/map:serializer"/>
          </xsl:call-template>

          <!-- Configure matchers -->
          <xsl:call-template name="config-components">
            <xsl:with-param name="name">matcher</xsl:with-param>
            <xsl:with-param name="components"
                select="/map:sitemap/map:components/map:matchers/map:matcher"/>
          </xsl:call-template>

          <!-- Configure selectors -->
          <xsl:call-template name="config-components">
            <xsl:with-param name="name">selector</xsl:with-param>
            <xsl:with-param name="components"
                select="/map:sitemap/map:components/map:selectors/map:selector"/>
          </xsl:call-template>

          <!-- Configure actions -->
          <xsl:call-template name="config-components">
            <xsl:with-param name="name">action</xsl:with-param>
            <xsl:with-param name="components"
                select="/map:sitemap/map:components/map:actions/map:action"/>
          </xsl:call-template>

        /* catch any exception thrown by a component during configuration */
        } catch (Exception e) {
          throw new ConfigurationException (e.toString()/*, cconf*/);
        }
      }

      <!-- generate methods for every map:resource element -->
      <xsl:for-each select="/map:sitemap/map:resources/map:resource">
        /**
         * This is the internal resource named "<xsl:value-of select="@name"/>"
         * @param pipeline A &lt;code&gt;ResourcePipeline&lt;/code&gt; holding the sitemap component collected so far
         * @param listOfLists A &lt;code&gt;List&lt;/code&gt; holding replacement values for src attributes
         * @param environment The &lt;code&gt;Environment&lt;/code&gt; requesting a resource
         * @param cocoon_view The view of the resource requested
         * @return Wether the request has been processed or not
         * @exception Exception If an error occurs during request evaluation and production
         */
        private boolean resource_<xsl:value-of select="translate(@name, '- ', '__')"/> (ResourcePipeline pipeline,
            List listOfLists, Environment environment, String cocoon_view)
        throws Exception {
          List list = null;
          Parameters param = null;
          <xsl:apply-templates select="./*"/>
          return false;
        }
      </xsl:for-each>

      <!-- generate methods for every map:view element -->
      <xsl:for-each select="/map:sitemap/map:views/map:view">
        /**
         * This is the method to produce the "<xsl:value-of select="@name"/>" view of the requested resource
         * @param pipeline A &lt;code&gt;ResourcePipeline&lt;/code&gt; holding the sitemap component collected so far
         * @param listOfLists A &lt;code&gt;List&lt;/code&gt; holding replacement values for src attributes
         * @param environment The &lt;code&gt;Environment&lt;/code&gt; requesting a resource
         * @return Wether the request has been processed or not
         * @exception Exception If an error occurs during request evaluation and production
         */
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
       * Process to producing the output to the specified &lt;code&gt;OutputStream&lt;/code&gt;.
       */
      public boolean process(Environment environment)
      throws Exception {
        /* the &lt;code&gt;ResourcePipeline&lt;/code&gt; used to collect the sitemap
           components to produce the requested resource */
        ResourcePipeline pipeline = new ResourcePipeline (super.sitemapComponentManager);
        /* the &lt;code&gt;List&lt;/code&gt; objects to hold the replacement values
           delivered from matchers and selectors to replace occurences of
           XPath kind expressions in values of src attribute used with
           generate and transform elements */
        List listOfLists = (List) new ArrayList();
        List list;
        Parameters param;
        Map objectModel = environment.getObjectModel();
        String cocoon_view = environment.getView();

        <!-- process the pipelines -->
        <!-- for each pipeline element generate a try/catch block -->
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

      <!-- generate methods for every map:handle-errors elements in all map:pipeline elements -->
      <xsl:for-each select="/map:sitemap/map:pipelines/map:pipeline">
        <xsl:variable name="pipeline-position" select="position()"/>
        <xsl:if test="(./map:handle-errors)">
          private boolean error_process_<xsl:value-of select="$pipeline-position"/> (Environment environment, Map objectModel, Exception e)
          throws Exception {
            ResourcePipeline pipeline = new ResourcePipeline (super.sitemapComponentManager);
            List listOfLists = (List)(new ArrayList());
            List list;
            Parameters param;
            pipeline.setGenerator ("!generator:error-notifier!", e.getMessage(), emptyParam, e);
            <xsl:apply-templates select="./map:handle-errors/*"/>
            return false;
          }
        </xsl:if>
      </xsl:for-each>
    }
  </xsl:template> <!-- match="map:sitemap" -->

  <!-- a match element calls a match method on a matcher component (or a inlined
       matcher method produced by a CodeFactory -->
  <xsl:template match="map:match">

    <!-- get the type of matcher used -->
    <xsl:variable name="matcher-type">
      <xsl:call-template name="get-parameter">
        <xsl:with-param name="parname">type</xsl:with-param>
        <xsl:with-param name="default"><xsl:value-of select="/map:sitemap/map:components/map:matchers/@default"/></xsl:with-param>
      </xsl:call-template>
    </xsl:variable>

    <!-- get the pattern used -->
    <xsl:variable name="pattern-value">
      <xsl:call-template name="get-parameter">
        <xsl:with-param name="parname">pattern</xsl:with-param>
        <xsl:with-param name="required">true</xsl:with-param>
      </xsl:call-template>
    </xsl:variable>

    <!-- remove all invalid chars from the pattern. The result is used to form the name of the generated method
         in case this matcher is produced by a CodeFactory -->
    <xsl:variable name="matcher-name2">
      <xsl:call-template name="generate-name">
        <xsl:with-param name="prefix">matcher_</xsl:with-param>
        <xsl:with-param name="value" select="@pattern"/>
        <xsl:with-param name="suffix"><xsl:value-of select="$matcher-type"/>_<xsl:value-of select="generate-id(.)"/></xsl:with-param>
      </xsl:call-template>
    </xsl:variable>

    <!-- check if this matcher is a factory ? -->
    <xsl:variable name="is-factory">
      <xsl:choose>
        <xsl:when test="/map:sitemap/map:components/map:matchers/map:matcher[@name=$matcher-type]">
          <xsl:value-of select="false()"/>
        </xsl:when>
        <xsl:when test="/map:sitemap/map:components/map:matchers/map:matcher[@name=$matcher-type]/@factory">
          <xsl:value-of select="true()"/>
        </xsl:when>
        <xsl:when test="/map:sitemap/map:components/map:matchers/map:matcher[@name=$matcher-type]/@src">
          <xsl:value-of select="java:isFactory($factory-loader, string(/map:sitemap/map:components/map:matchers/map:matcher[@name=$matcher-type]/@src))"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="false()"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>


    <!-- gets the string how the matcher is to be invoced in java code -->
    <xsl:variable name="matcher-name">
      <!-- check if we have a matcher definition in this sitemap otherwise get it from the parent -->
      <xsl:choose>
        <xsl:when test="$is-factory">
          <xsl:value-of select="$matcher-name2"/>
        </xsl:when>
        <xsl:otherwise>
          ((Matcher)((ComponentHolder)super.sitemapComponentManager.lookup("matcher:<xsl:value-of select="$matcher-type"/>")).get()).match
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <!-- this is the actual code produced -->
    if ((list = <xsl:value-of select="translate($matcher-type, '- ', '__')"/>Match(<xsl:value-of select="$matcher-name"/>_expr,
          objectModel)) != null) {
      listOfLists.add (list);
      <xsl:apply-templates/>
      listOfLists.remove (list);
    }
  </xsl:template> <!-- match="map:match" -->

  <!-- a select element introduces a multi branch case by calls to a select method on a selector component (or a inlined
       selector method produced by a CodeFactory -->
  <xsl:template match="map:select">

    <!-- get the type of selector used -->
    <xsl:variable name="selector-type">
      <xsl:call-template name="get-parameter">
        <xsl:with-param name="parname">type</xsl:with-param>
        <xsl:with-param name="default"><xsl:value-of select="/map:sitemap/map:components/map:selectors/@default"/></xsl:with-param>
      </xsl:call-template>
    </xsl:variable>

    <!-- loop through all the when cases -->
    <xsl:for-each select="./map:when">

      <!-- get the pattern used -->
      <xsl:variable name="test-value">
        <xsl:call-template name="get-parameter">
          <xsl:with-param name="parname">test</xsl:with-param>
          <xsl:with-param name="required">true</xsl:with-param>
        </xsl:call-template>
      </xsl:variable>

      <!-- remove all invalid chars from the test expression. The result is used to form the name of the generated method
           in case this selector is produced by a CodeFactory -->
      <xsl:variable name="selector-name2">
        <xsl:call-template name="generate-name">
          <xsl:with-param name="prefix">selector_</xsl:with-param>
          <xsl:with-param name="value" select="@test"/>
          <xsl:with-param name="suffix"><xsl:value-of select="$selector-type"/>_<xsl:value-of select="generate-id(..)"/></xsl:with-param>
        </xsl:call-template>
      </xsl:variable>

      <!-- check if this selector is a factory ? -->
      <xsl:variable name="is-factory">
        <xsl:choose>
          <xsl:when test="/map:sitemap/map:components/map:selectors/map:selector[@name=$selector-type]">
            <xsl:value-of select="false()"/>
          </xsl:when>
          <xsl:when test="/map:sitemap/map:components/map:selectors/map:selector[@name=$selector-type]/@factory">
            <xsl:value-of select="true()"/>
          </xsl:when>
          <xsl:when test="/map:sitemap/map:components/map:selectors/map:selector[@name=$selector-type]/@src">
            <xsl:value-of select="java:isFactory($factory-loader, string(/map:sitemap/map:components/map:selectors/map:selector[@name=$selector-type]/@src))"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="false()"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>

      <!-- gets the string how the selector is to be invoced in java code -->
      <xsl:variable name="selector-name">
        <!-- check if we have a selector definition in this sitemap otherwise get it from the parent -->
        <xsl:choose>
          <xsl:when test="$is-factory">
            <xsl:value-of select="$selector-name2"/>
          </xsl:when>
          <xsl:otherwise>
            ((Selector)((ComponentHolder)super.sitemapComponentManager.lookup("selector:<xsl:value-of select="$selector-type"/>")).get()).select
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>

      <!-- this is the actual code produced on the when elements -->
      <xsl:if test="position() > 1">
        else
      </xsl:if>
      if (<xsl:value-of select="translate($selector-type, '- ', '__')"/>Select (<xsl:value-of select="$selector-name"/>_expr, objectModel)) {
       <xsl:apply-templates/>
      }
    </xsl:for-each>

    <!-- this is the actual code produced on the otherwise element -->
    <xsl:for-each select="./map:otherwise">
      else {
      <xsl:apply-templates/>
      }
    </xsl:for-each>
  </xsl:template> <!-- match="map:select" -->

  <!-- processing of an act element -->
  <xsl:template match="map:act">

    <!-- get the type of action used -->
    <xsl:variable name="action-type">
      <xsl:call-template name="get-parameter">
        <xsl:with-param name="parname">type</xsl:with-param>
        <xsl:with-param name="default"><xsl:value-of select="/map:sitemap/map:components/map:actions/@default"/></xsl:with-param>
      </xsl:call-template>
    </xsl:variable>

    <!-- get the source parameter for the Action -->
    <xsl:variable name="action-source">
      <xsl:call-template name="get-parameter-as-string">
        <xsl:with-param name="parname">src</xsl:with-param>
        <xsl:with-param name="default">null</xsl:with-param>
      </xsl:call-template>
    </xsl:variable>

    <!-- gets the string how the action is to be invoced in java code -->
    <xsl:variable name="action-name">
      ((Action)((ComponentHolder)super.sitemapComponentManager.lookup("action:<xsl:value-of select="$action-type"/>")).get()).act
    </xsl:variable>

    <!-- test if we have to define parameters for this action -->
    <xsl:if test="count(parameter)>0">
      param = new Parameters ();
    </xsl:if>

    <!-- generate the value used for the parameter argument in the invocation of the act method of this action -->
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

    <!-- collect the parameters -->
    <xsl:apply-templates select="parameter"/>

    <!-- generate the invocation of the act method of the action component -->
    <xsl:choose>
      <xsl:when test="./*">
        if ((list = <xsl:value-of select="$action-name"/> (environment, objectModel, substitute(listOfLists,<xsl:value-of select="$action-source"/>), <xsl:value-of select="$component-param"/>)) != null) {
          listOfLists.add (list);
          <xsl:apply-templates/>
          listOfLists.remove(list);
        }
      </xsl:when>
      <xsl:otherwise>
        list = <xsl:value-of select="$action-name"/> (environment, objectModel, substitute(listOfLists,<xsl:value-of select="$action-source"/>), <xsl:value-of select="$component-param"/>);
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template> <!-- match="map:act" -->

  <!-- generate the code to invoke a generator -->
  <xsl:template match="map:generate">
    <xsl:call-template name="setup-component">
      <xsl:with-param name="default-component" select="/map:sitemap/map:components/map:generators/@default"/>
      <xsl:with-param name="method">setGenerator</xsl:with-param>
      <xsl:with-param name="prefix">generator</xsl:with-param>
    </xsl:call-template>
  </xsl:template> <!-- match="map:generate" -->

  <!-- generate the code to invoke a transformer -->
  <xsl:template match="map:transform">
    <xsl:call-template name="setup-component">
      <xsl:with-param name="default-component" select="/map:sitemap/map:components/map:transformers/@default"/>
      <xsl:with-param name="method">addTransformer</xsl:with-param>
      <xsl:with-param name="prefix">transformer</xsl:with-param>
    </xsl:call-template>
  </xsl:template> <!-- match="map:transformer" -->

  <!-- generate the code to invoke a serializer -->
  <xsl:template match="map:serialize">
    <xsl:call-template name="setup-component">
      <xsl:with-param name="default-component" select="/map:sitemap/map:components/map:serializers/@default"/>
      <xsl:with-param name="method">setSerializer</xsl:with-param>
      <xsl:with-param name="prefix">serializer</xsl:with-param>
      <xsl:with-param name="mime-type" select="@mime-type"/>
    </xsl:call-template>

    <!-- if there is a status-code attribute tell it to the environment -->
    <xsl:if test="@status-code">
      environment.setStatus(<xsl:value-of select="@status-code"/>);
    </xsl:if>

    <!-- the "if(true)" is needed to prevent "statement not reachable" error messages during compile -->
    if(true)return pipeline.process (environment);
  </xsl:template> <!-- match="map:serialize" -->

  <!-- generate the code to invoke a reader -->
  <xsl:template match="map:read">
    <xsl:call-template name="setup-component">
      <xsl:with-param name="default-component" select="/map:sitemap/map:components/map:readers/@default"/>
      <xsl:with-param name="method">setReader</xsl:with-param>
      <xsl:with-param name="prefix">reader</xsl:with-param>
      <xsl:with-param name="mime-type" select="@mime-type"/>
    </xsl:call-template>

    <!-- the "if(true)" is needed to prevent "statement not reachable" error messages during compile -->
    if(true)return pipeline.process (environment);
  </xsl:template> <!-- match="map:read" -->

  <!-- generate the code to invoke a sub sitemap  -->
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

    <!-- generate the code to invoke the sitemapManager which handles delegation of control to sub sitemaps -->
    <!-- here we make sure the uri-prefix ends with a slash -->
    <xsl:choose>
      <xsl:when test="substring(@uri-prefix,string-length(@uri-prefix))='/'">
        if(true)return sitemapManager.invoke (environment, substitute(listOfLists,"<xsl:value-of select="@uri-prefix"/>"), substitute(listOfLists,"<xsl:value-of select="@src"/>"), <xsl:value-of select="$check-reload"/>);
      </xsl:when>
      <xsl:when test="substring(@uri-prefix,string-length(@uri-prefix))='}'">
        String uri_prefix<xsl:value-of select="count(.)"/>=substitute(listOfLists,"<xsl:value-of select="@uri-prefix"/>");
        if (uri_prefix<xsl:value-of select="count(.)"/>.charAt(uri_prefix<xsl:value-of select="count(.)"/>.length()-1)=='/'){
          if(true)return sitemapManager.invoke (environment, uri_prefix<xsl:value-of select="count(.)"/>, substitute(listOfLists,"<xsl:value-of select="@src"/>"), <xsl:value-of select="$check-reload"/>);
        } else {
          return sitemapManager.invoke (environment, uri_prefix<xsl:value-of select="count(.)"/>+"/", substitute(listOfLists,"<xsl:value-of select="@src"/>"), <xsl:value-of select="$check-reload"/>);
        }
      </xsl:when>
      <xsl:otherwise>
        if(true)return sitemapManager.invoke (environment, substitute(listOfLists,"<xsl:value-of select="@uri-prefix"/>/"), substitute(listOfLists,"<xsl:value-of select="@src"/>"), <xsl:value-of select="$check-reload"/>);
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template> <!-- match="map:mount" -->

  <!-- generate the code to redirect a request -->
  <xsl:template match="map:redirect-to">
    <xsl:choose>

      <!-- redirect to a internal resource definition -->
      <xsl:when test="@resource">
        if(true)return resource_<xsl:value-of select="translate(@resource, '- ', '__')"/>(pipeline, listOfLists, environment, cocoon_view);
      </xsl:when>

      <!-- redirect to a external resource definition. Let the environment do the redirect -->
      <xsl:when test="@uri">
        log.debug("Redirecting to '<xsl:value-of select="@uri"/>'");
        environment.redirect (substitute(listOfLists, "<xsl:value-of select="@uri"/>"));
        if(true)return true;
      </xsl:when>

      <!-- any other combination generates an error message -->
      <xsl:otherwise>
        <xsl:call-template name="error">
          <xsl:with-param name="message">Missing attribute uri= or resource= to element redirect-to</xsl:with-param>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template> <!-- match="map:redirect-to" -->

  <!-- generate the code to match a label definition -->
  <xsl:template match="map:label">
    <xsl:apply-templates/>
    if ("<xsl:value-of select="@name"/>".equals(cocoon_view))
      return view_<xsl:value-of select="translate(@name, '- ', '__')"/> (pipeline, listOfLists, environment);
  </xsl:template> <!-- match="map:label" -->

  <!-- collect parameter definitions -->
  <xsl:template match="map:pipeline//parameter">
    param.setParameter ("<xsl:value-of select="@name"/>", "<xsl:value-of select="@value"/>");
  </xsl:template>

  <!-- FIXME:(GP) is this still valid? -->
  <xsl:template match="map:param">
    param.setParameter ("<xsl:value-of select="@name"/>", "<xsl:value-of select="@map:value"/>");
  </xsl:template>

  <!-- Sitemap Utility templates -->

  <!-- this template generates the code to configure a specific sitemap component -->
  <xsl:template name="config-components">
    <xsl:param name="name"/>
    <xsl:param name="components"/>

    <xsl:variable name="qname">
      <xsl:value-of select="concat($prefix, ':value')"/>
    </xsl:variable>

    <xsl:variable name="ns" select="namespace-uri(.)"/>
    <xsl:for-each select="$components">
      <xsl:variable name="is-factory-component"
        select="(@factory and ($name = 'matcher' or $name = 'selector')) or (@src and ($name = 'matcher' or $name = 'selector') and java:isFactory($factory-loader, string(@src)))"/>
      <xsl:if test="$is-factory-component=false()">
      {
        DefaultConfiguration cconf1 = new DefaultConfiguration("<xsl:value-of select="translate(@name, '- ', '__')"/>");
        <xsl:for-each select="attribute::*[name(.)!=$qname]">
          cconf1.addAttribute ("<xsl:value-of select="name(.)"/>",
                                "<xsl:value-of select="."/>");
        </xsl:for-each>

        <!-- get nested configuration definitions -->
        <xsl:call-template name="nested-config-components">
          <xsl:with-param name="name" select="$name"/>
      <xsl:with-param name="level" select="2"/>
          <xsl:with-param name="config-name"><xsl:value-of select="concat(local-name(.),'/',@name)"/></xsl:with-param>
          <xsl:with-param name="components" select="*"/>
          <xsl:with-param name="type" select="@name"/>
          <xsl:with-param name="ns" select="$ns"/>
        </xsl:call-template>

        <xsl:choose>
          <xsl:when test="@mime-type">
            load_component ("<xsl:value-of select="$name"/>:<xsl:value-of select="@name"/>", "<xsl:value-of select="@src"/>", cconf1, "<xsl:value-of select="@mime-type"/>");
          </xsl:when>
          <xsl:otherwise>
            load_component ("<xsl:value-of select="$name"/>:<xsl:value-of select="@name"/>", "<xsl:value-of select="@src"/>", cconf1, null);
          </xsl:otherwise>
        </xsl:choose>
    }
      </xsl:if>
    </xsl:for-each>
  </xsl:template>

  <!-- this template generates the code to collect configurations for a specific sitemap component -->
  <xsl:template name="nested-config-components">
    <xsl:param name="name"/>
    <xsl:param name="config-name"/>
    <xsl:param name="components"/>
    <xsl:param name="type"/>
    <xsl:param name="ns"/>
    <xsl:param name="subname"/>
    <xsl:param name="level"/>

    <xsl:variable name="qname">
      <xsl:value-of select="concat($prefix, ':value')"/>
    </xsl:variable>

    <!-- process content -->
    <xsl:for-each select="$components">
      {
         DefaultConfiguration cconf<xsl:value-of select="$level"/> = new DefaultConfiguration("<xsl:value-of select="name(.)"/>");
      <xsl:for-each select="attribute::*[name(.)!=$qname]">
        cconf<xsl:value-of select="$level"/>.addAttribute ("<xsl:value-of select="name(.)"/>", "<xsl:value-of select="."/>");
      </xsl:for-each>
      <xsl:for-each select="attribute::*[name(.)=$qname]">
        cconf<xsl:value-of select="$level"/>.appendValueData("<xsl:value-of select="."/>");
      </xsl:for-each>
      <xsl:if test="normalize-space(text())">
        cconf<xsl:value-of select="$level"/>.appendValueData("<xsl:value-of select="text()"/>");
      </xsl:if>
        cconf<xsl:value-of select="($level - 1)"/>.addChild(cconf<xsl:value-of select="$level"/>);

     <xsl:variable name="newsubname">
        <xsl:choose>
          <xsl:when test="not($subname)"><xsl:value-of select="position()"/></xsl:when>
          <xsl:otherwise><xsl:value-of select="concat($subname,position())"/></xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <xsl:call-template name="nested-config-components">
        <xsl:with-param name="level"><xsl:value-of select="($level + 1)"/></xsl:with-param>
        <xsl:with-param name="name"><xsl:value-of select="$name"/></xsl:with-param>
        <xsl:with-param name="config-name"><xsl:value-of select="local-name(.)"/></xsl:with-param>
        <xsl:with-param name="components" select="./*"/>
        <xsl:with-param name="type"><xsl:value-of select="$type"/></xsl:with-param>
        <xsl:with-param name="ns"><xsl:value-of select="namespace-uri(.)"/></xsl:with-param>
        <xsl:with-param name="subname"><xsl:value-of select="$newsubname"/></xsl:with-param>
      </xsl:call-template>
      }
    </xsl:for-each>
  </xsl:template>

  <!-- this template is used to setup a individual sitemap component before putting it into a pipeline -->
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
            pipeline.addTransformer ("!transformer:link-translator!", null, emptyParam);
        }
      </xsl:if>
    </xsl:if>

    <!-- get the type of the component -->
    <xsl:variable name="component-type">
      <xsl:call-template name="get-parameter">
        <xsl:with-param name="parname">type</xsl:with-param>
        <xsl:with-param name="default"><xsl:value-of select="$default-component"/></xsl:with-param>
      </xsl:call-template>
    </xsl:variable>

    <!-- get the source attribute of the component -->
    <xsl:variable name="component-source">
      <xsl:call-template name="get-parameter">
        <xsl:with-param name="parname">src</xsl:with-param>
        <xsl:with-param name="default">null</xsl:with-param>
      </xsl:call-template>
    </xsl:variable>

    <!-- test if we have to define parameters for this component -->
    <xsl:if test="count(parameter)>0">
      param = new Parameters ();
    </xsl:if>

    <!-- generate the value used for the parameter argument in the invocation of the act method of this action -->
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

    <!-- collect the parameters -->
    <xsl:apply-templates select="parameter"/>

    <!-- determine the right invokation according to "has a src attribute" and "has a mime-type attribute" -->
    <xsl:choose>
      <xsl:when test="$component-source='null'">
        <xsl:choose>
          <xsl:when test="$mime-type!=''">
            pipeline.<xsl:value-of select="$method"/> ("<xsl:value-of select="$prefix"/>:<xsl:value-of select="$component-type"/>",
              null, <xsl:value-of select="$component-param"/>,"<xsl:value-of select="$mime-type"/>"
            );
          </xsl:when>
          <xsl:otherwise>
            pipeline.<xsl:value-of select="$method"/> ("<xsl:value-of select="$prefix"/>:<xsl:value-of select="$component-type"/>",
              null, <xsl:value-of select="$component-param"/>
            );
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when test="$mime-type!=''">
            pipeline.<xsl:value-of select="$method"/> ("<xsl:value-of select="$prefix"/>:<xsl:value-of select="$component-type"/>",
                substitute(listOfLists,"<xsl:value-of select="$component-source"/>"),
                <xsl:value-of select="$component-param"/>,"<xsl:value-of select="$mime-type"/>");
          </xsl:when>
          <xsl:otherwise>
            pipeline.<xsl:value-of select="$method"/> ("<xsl:value-of select="$prefix"/>:<xsl:value-of select="$component-type"/>",
                substitute(listOfLists,"<xsl:value-of select="$component-source"/>"),
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

  <!-- replace invalid characters with underscores -->
  <xsl:template name="generate-name">
    <xsl:param name="value"/>
    <xsl:param name="prefix"/>
    <xsl:param name="suffix"/>
    <xsl:variable name="value1" select="translate($value,'/- *?@:{}()[].#^\\$|&#33;~\','______________________')"/>
    <xsl:value-of select="$prefix"/><xsl:value-of select='translate($value1,"&#39;","")'/><xsl:value-of select="$suffix"/>
  </xsl:template>

  <!-- These are the usual utility templates for logicsheets -->

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
