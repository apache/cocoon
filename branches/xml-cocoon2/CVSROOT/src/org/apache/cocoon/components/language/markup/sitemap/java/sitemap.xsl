<?xml version="1.0"?>
<!-- Sitemap Core logicsheet for the Java language -->

<xsl:stylesheet version="1.0"
  xmlns:map="http://apache.org/cocoon/sitemap/1.0" 
  xmlns:java="http://xml.apache.org/xslt/java"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>

  <xsl:output method="text"/>

  <xsl:variable name="prefix">map</xsl:variable>
  <xsl:variable name="matcher-factory-loader" select="java:org.apache.cocoon.sitemap.XSLTMatcherFactoryLoader.new()"/>
<!--
  <xsl:variable name="selector-factory-loader" select="java:org.apache.cocoon.sitemap.XSLTSelectorFactoryLoader.new()"/>
-->

  <xsl:template match="/">
    <code xml:space="preserve">
      <xsl:apply-templates/>
    </code>
  </xsl:template>
  
  <xsl:template match="map:sitemap"> 
    package <xsl:value-of select="translate(@file-path, '/', '.')"/>;

    import java.util.Map; 
    import java.util.Stack; 
    import java.util.Vector; 

    import org.apache.avalon.Configuration;
    import org.apache.avalon.ConfigurationException;
    import org.apache.avalon.SitemapConfigurationBuilder;
    import org.apache.avalon.utils.Parameters; 
    import org.apache.cocoon.Request;
    import org.apache.cocoon.Response;
    import org.apache.cocoon.selection.Selector;
    import org.apache.cocoon.transformation.Transformer;
    import org.apache.cocoon.generation.Generator;
    import org.apache.cocoon.matching.Matcher;
    import org.apache.cocoon.serialization.Serializer;
    import org.apache.cocoon.sitemap.AbstractSitemapProcessor;
    import org.apache.cocoon.sitemap.ResourcePipeline;

    import org.xml.sax.helpers.AttributesImpl;

/**
 *
 * @author &lt;a href="mailto:Giacomo.Pati@pwr.ch"&gt;Giacomo Pati&lt;/a&gt;
 * @version CVS $Revision: 1.1.2.6 $ $Date: 2000-07-11 23:46:36 $
 */
public class <xsl:value-of select="@file-name"/> extends AbstractSitemapProcessor {
    
    SitemapConfigurationBuilder confBuilder = new SitemapConfigurationBuilder ();
    Parameters emptyParam = new Parameters(); 

    <!-- generate variables for all components -->
    /** The generators */
    <xsl:for-each select="/map:sitemap/map:components/map:generators/map:generator">
      private Generator generator_<xsl:value-of select="translate(./@type, '- ', '__')"/> = null;
    </xsl:for-each>
    
    /** The transformers */
    <xsl:for-each select="/map:sitemap/map:components/map:transformers/map:transformer">
      private Transformer transformer_<xsl:value-of select="translate(./@type, '- ', '__')"/> = null;
    </xsl:for-each>
    
    /** The serializers */
    <xsl:for-each select="/map:sitemap/map:components/map:serializers/map:serializer">
      private Serializer serializer_<xsl:value-of select="translate(./@type, '- ', '__')"/> = null;
    </xsl:for-each>
    
    /** The matchers */
    <xsl:for-each select="/map:sitemap/map:components/map:matchers/map:matcher[@src]">
      private Matcher matcher_<xsl:value-of select="translate(./@type, '- ', '__')"/> = null;
    </xsl:for-each>
      
    /** The selectors */  
    <xsl:for-each select="/map:sitemap/map:components/map:selctors/map:selector[@src]">
      private Selector selector_<xsl:value-of select="translate(./@type, '- ', '__')"/> = null;
    </xsl:for-each>
      
    /** The sub sitemaps */  
    <xsl:for-each select="/map:sitemap/map:pipelines//map:mount">  
      SitemapProcessor sitemap_<xsl:value-of select="translate(@src, ':@./-{}#', '_____')"/> = null;
    </xsl:for-each>

    /** The generated matchers */
    <xsl:for-each select="/map:sitemap/map:components/map:matchers/map:matcher[@factory]">
      <xsl:variable name="factory" select="@factory"/>
      <xsl:variable name="type" select="@type"/>
      <xsl:variable name="default"><xsl:if test="$type = ../@default">true</xsl:if></xsl:variable>
      <xsl:for-each select="/map:sitemap/map:pipelines/map:pipeline/descendant-or-self::map:match[@type=$type or (not(@type) and $default!='')]">
        <xsl:variable name="matcher-name1" select="translate(@pattern,'/- *?@:{}()[].#^\\$|&#33;','_')"/>
        <xsl:variable name="matcher-name" select='translate($matcher-name1,"&#39;","")'/>
        private Map _matcher_<xsl:value-of select="$matcher-name"/> (Request request) {
          <xsl:value-of select="java:getSource($matcher-factory-loader, string($factory), string(@pattern))"/>;
        }
      </xsl:for-each>
    </xsl:for-each>

    /** The generated selectors */
    <xsl:for-each select="/map:sitemap/map:components/map:selectors/map:selector[@factory]">
      <xsl:variable name="factory" select="@factory"/>
      <xsl:variable name="type" select="@type"/>
      <xsl:variable name="default"><xsl:if test="$type = ../@default">true</xsl:if></xsl:variable>
      <xsl:for-each select="/map:sitemap/map:pipelines/map:pipeline/descendant-or-self::map:when[../map:selector/@type=$type or (not(../map:selector/@type) and $default!='')]">
        <xsl:variable name="selector-name1" select="translate(@pattern,'/- *?@:{}()[].#^\\$|&#33;','_')"/>
        <xsl:variable name="selector-name" select='translate($selector-name1,"&#39;","")'/>
        private boolean _selector_<xsl:value-of select="$selector-name"/> (Request request) {
<!--
          <xsl:value-of select="java:getSource($selector-factory-loader, string($factory), string(@test))"/>;
-->
        }
      </xsl:for-each>
    </xsl:for-each>

    /**
     * Pass a &lt;code&gt;Configuration&lt;/code&gt; instance to this
     * &lt;code&gt;Configurable&lt;/code&gt; class.
     */
    public void setConfiguration(Configuration xconf) {
    throws ConfigurationException {

    <!-- configure all components -->
    /* Configure generators */
    <xsl:call-template name="config-components">
      <xsl:with-param name="name">generator</xsl:with-param>
      <xsl:with-param name="interface">Generator</xsl:with-param>
      <xsl:with-param name="components" 
          select="/map:sitemap/map:components/map:generators/map:generator"/>
    </xsl:call-template>    

    /* Configure filters */
    <xsl:call-template name="config-components">
      <xsl:with-param name="name">transformer</xsl:with-param>
      <xsl:with-param name="interface">Transformer</xsl:with-param>
      <xsl:with-param name="components" 
          select="/map:sitemap/map:components/map:transformers/map:transformer"/>
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
          select="/map:sitemap/map:components/map:matchers/map:matcher"/>
    </xsl:call-template>
    
    /* Configure selectors */
    <xsl:call-template name="config-components">
      <xsl:with-param name="name">selector</xsl:with-param>
      <xsl:with-param name="interface">Selector</xsl:with-param>
      <xsl:with-param name="components" 
          select="/map:sitemap/map:components/map:selectors/map:selector"/>
    </xsl:call-template>

    /* Configure mounted sitemaps */
    Cocoon cocoon = manager.getComponent ("cocoon");
    cocoon.clearRegistration (this);
    <xsl:for-each select="/map:sitemap/map:pipelines//map:mount">  
      sitemap_<xsl:value-of select="translate(@src, ':@./-{}#', '_____')"/> = cocoon.getSitemapInstance (this, "<xsl:value-of select="@src"/>");
      sitemap_<xsl:value-of select="translate(@src, ':@./-{}#', '_____')"/>.setConfiguration (null);
    </xsl:for-each>
    }

    <xsl:for-each select="/map:sitemap/map:resources/map:resource">
      private boolean resource_<xsl:value-of select="translate(@name, '- ', '__')"/> (ResourcePipeline pipeline, 
          Stack mapStack, Request req, Response res, OutputStream out) 
      throws SAXException, IOException, ProcessingException { 
        Map map = null;
        Parameters param = null; 
        <xsl:apply-templates select="./*"/>
      }
    </xsl:for-each>
 
    /** 
     * Process the given <code>Request</code> producing the output to the 
     * specified <code>Response</code> and <code>OutputStream</code>. 
     */ 
    public boolean process(Request req, Response res, OutputStream out)  
    throws SAXException, IOException, ProcessingException { 
      ResourcePipeline pipeline = new ResourcePipeline ();
      Stack mapStack = new Stack();
      Map map = null;
      Parameters param = null; 
      <xsl:for-each select="/map:sitemap/map:pipelines/map:pipeline">
        try {
          <xsl:apply-templates select="./*"/>
        } catch (Exception e) {
          <xsl:choose>
            <xsl:when test="not (./map:handle-errors)">
             throw e;
            </xsl:when>
            <xsl:otherwise>
              pipeline.setGenerator (generator_error_hanler, e.getMessage(), emptyParam);
              <xsl:apply-templates select="./map:handle-error/*"/>
              return pipeline.process (request, response, out);
            </xsl:otherwise>
          </xsl:choose>
        }
      </xsl:for-each>
    }
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
    if ((map = matcher_<xsl:value-of select="translate($matcher-type, '- ', '__')"/>.match ("<xsl:value-of select="$pattern-value"/>", request)) != null) {
       mapStack.push (map);  
       <xsl:apply-templates/>
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
      if (selector_<xsl:value-of select="translate($selector-type, '- ', '__')"/>.select ("<xsl:value-of select="$test-value"/>", request)) {
       <xsl:apply-templates/>
      }
    </xsl:for-each>

    <xsl:for-each select="./map:otherwise">
      else {
      <xsl:apply-templates/>
      }
    </xsl:for-each>
  </xsl:template> <!-- match="/map:sitemap/map:select" -->

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
  </xsl:template> <!-- match="map:transormer" -->

  <xsl:template match="map:serialize">
    <xsl:call-template name="setup-component">
      <xsl:with-param name="default-component" select="/map:sitemap/map:components/map:serializers/@default"/>
      <xsl:with-param name="method">setSerializer</xsl:with-param>
      <xsl:with-param name="prefix">serializer</xsl:with-param>
    </xsl:call-template> 
    return pipeline.process (request, response, out);
  </xsl:template> <!-- match="map:serialize" --> 

  <xsl:template match="map:mount">
    return sitemap_<xsl:value-of select="translate(@src, ':@./-{}#', '_____')"/>.process(request, response, out);
  </xsl:template> <!-- match="map:mount" -->

  <xsl:template match="map:read">
    return read (request, response, out, 
      "<xsl:value-of select="@src"/>"<xsl:if test="(@mime-type)">, "<xsl:value-of select="@mime-type"/>"</xsl:if>);
  </xsl:template> <!-- match="map:read" -->

  <xsl:template match="map:redirect-to">
    <xsl:choose>
      <xsl:when test="@resource">
        return resource_<xsl:value-of select="translate(@resource, '- ', '__')"/>(pipeline, mapStack, request, response, out);
      </xsl:when>
      <xsl:when test="@uri">
        // request.setUri ("<xsl:value-of select="@uri"/>");
        // this.process(request, response, out);
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="error">
          <xsl:with-param name="message">Missing attribute uri= or resource= to element redirect-to</xsl:with-param>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template> <!-- match="map:redirect-to" -->

  <xsl:template match="map:param">
    param.setParameter ("<xsl:value-of select="@name"/>", "<xsl:value-of select="@map:value"/>");
  </xsl:template> <!-- match="map:param" -->

  <!-- Sitemap Utility templates --> 

  <xsl:template name="config-components"> 
    <xsl:param name="name"/>  
    <xsl:param name="interface"/> 
    <xsl:param name="components"/>  
 
    <xsl:variable name="qname"> 
      <xsl:value-of select="concat($prefix, ':value')"/> 
    </xsl:variable> 

    <xsl:for-each select="$components"> 
      <xsl:variable name="confname">
        <xsl:value-of select="$name"/>_<xsl:value-of select="@type"/>_conf
      </xsl:variable>
      <xsl:call-template name="nested-config-components">
        <xsl:with-param name="name" select="$name"/>
        <xsl:with-param name="config-name"><xsl:value-of select="concat(local-name(.),'/',@type)"/></xsl:with-param>
        <xsl:with-param name="interface" select="$interface"/>
        <xsl:with-param name="components" select="*"/>
        <xsl:with-param name="type" select="@type"/>
      </xsl:call-template>
      <xsl:value-of select="$name"/>_<xsl:value-of select="translate(@type, '- ', '__')"/> = 
        (<xsl:value-of select="$interface"/>)load_component ("<xsl:value-of select="@src"/>", <xsl:value-of select="$confname"/>);
    </xsl:for-each> 
  </xsl:template> 

  <xsl:template name="nested-config-components"> 
    <xsl:param name="name"/>  
    <xsl:param name="config-name"/>  
    <xsl:param name="interface"/> 
    <xsl:param name="components"/>  
    <xsl:param name="type"/>  
    <xsl:param name="subname"/>  
 
    <xsl:variable name="qname"> 
      <xsl:value-of select="concat($prefix, ':value')"/> 
    </xsl:variable> 

    <xsl:variable name="confname">
      <xsl:value-of select="$name"/>_<xsl:value-of select="$type"/><xsl:value-of select="$subname"/>_conf
    </xsl:variable>

    Configuration <xsl:value-of select="$confname"/> = confBuilder.newConfiguration ("<xsl:value-of select="$config-name"/>"); 

    <!-- process content -->
    <xsl:for-each select="$components">
      <xsl:choose>
        <xsl:when test="name(.)='map:param'"> 
          <xsl:choose>
            <xsl:when test="not(@value)">
              confBuilder.addAttribute ("<xsl:value-of select="@name"/>", "<xsl:value-of select="."/>", <xsl:value-of select="$confname"/>);
            </xsl:when>
            <xsl:otherwise>
              confBuilder.addAttribute ("<xsl:value-of select="@name"/>", "<xsl:value-of select="@value"/>", <xsl:value-of select="$confname"/>);
            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <xsl:when test="name(@*)=$qname"> 
          confBuilder.addAttribute ("<xsl:value-of select="name(.)"/>", "<xsl:value-of select="@*[name(.)=$qname]"/>", <xsl:value-of select="$confname"/>);
        </xsl:when>
        <xsl:when test="count(./*)=0"> 
          confBuilder.addAttribute ("<xsl:value-of select="name(.)"/>", "<xsl:value-of select="."/>", <xsl:value-of select="$confname"/>); 
        </xsl:when>
        <xsl:when test="count(./*)>0"> 
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
            <xsl:with-param name="subname"><xsl:value-of select="$newsubname"/></xsl:with-param>
          </xsl:call-template> 
          <xsl:variable name="newconfname">
            <xsl:value-of select="$name"/>_<xsl:value-of select="$type"/><xsl:value-of select="$newsubname"/>_conf
          </xsl:variable>
          confBuilder.addConfiguration (<xsl:value-of select="$newconfname"/>, <xsl:value-of select="$confname"/>); 
        </xsl:when>
      </xsl:choose>
    </xsl:for-each> 
  </xsl:template> 

  <xsl:template name="setup-component">
    <xsl:param name="default-component"/>
    <xsl:param name="method"/>
    <xsl:param name="prefix"/>
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
    <xsl:if test="descendant::map:param">
      param = new Parameters (); 
    </xsl:if>
    <xsl:variable name="component-param">
      <xsl:choose>
        <xsl:when test="descendant::map:param">
          param
        </xsl:when>
        <xsl:otherwise>
          emptyParam
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:apply-templates select="./map:param"/> 
    <xsl:choose> 
      <xsl:when test="$component-source='null'"> 
        pipeline.<xsl:value-of select="$method"/> (<xsl:value-of select="$prefix"/>_<xsl:value-of select="$component-type"/>, 
            null, <xsl:value-of select="$component-param"/>); 
      </xsl:when> 
      <xsl:otherwise> 
        pipeline.<xsl:value-of select="$method"/> (<xsl:value-of select="$prefix"/>_<xsl:value-of select="$component-type"/>,  
                               "<xsl:value-of select="$component-source"/>", <xsl:value-of select="$component-param"/>); 
      </xsl:otherwise> 
    </xsl:choose> 
  </xsl:template>

  <!-- Utility templates -->

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
                  <xsl:with-param name="message">[Logicsheet processor]
Parameter '<xsl:value-of select="$parname"/>' missing in dynamic tag &lt;<xsl:value-of select="name(.)"/>&gt;
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
