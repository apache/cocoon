<?xml version="1.0"?>
<!-- Sitemap Core logicsheet for the Java language -->

<xsl:stylesheet version="1.0"
  xmlns:map="http://apache.org/cocoon/sitemap/1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>

  <xsl:output method="text"/>

  <xsl:variable name="prefix">map</xsl:variable>

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

    import org.apache.arch.config.Configuration;
    import org.apache.arch.config.ConfigurationException;
    import org.apache.arch.config.SitemapConfigurationBuilder;
    import org.apache.cocoon.Parameters; 
    import org.apache.cocoon.Request;
    import org.apache.cocoon.Response;
    import org.apache.cocoon.choosers.Chooser;
    import org.apache.cocoon.filters.Filter;
    import org.apache.cocoon.generators.Generator;
    import org.apache.cocoon.matchers.Matcher;
    import org.apache.cocoon.serializers.Serializer;
    import org.apache.cocoon.sitemap.AbstractSitemapProcessor;
    import org.apache.cocoon.sitemap.ResourcePipeline;

/**
 *
 * @author &lt;a href="mailto:Giacomo.Pati@pwr.ch"&gt;Giacomo Pati&lt;/a&gt;
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2000-07-06 04:57:43 $
 */
public class <xsl:value-of select="@file-name"/> extends AbstractSitemapProcessor {
    
    SitemapConfigurationBuilder confBuilder = new SitemapConfigurationBuilder ();

    <!-- generate variables for all components -->
    /** The generators */
    <xsl:call-template name="define-components">
      <xsl:with-param name="name-prefix">generator</xsl:with-param>
      <xsl:with-param name="interface">Generator</xsl:with-param>
      <xsl:with-param name="components" select="/map:sitemap/map:components/map:generators/map:generator"/>
    </xsl:call-template>
    
    /** The filters */
    <xsl:call-template name="define-components">
      <xsl:with-param name="name-prefix">transformer</xsl:with-param>
      <xsl:with-param name="interface">Transformer</xsl:with-param>
      <xsl:with-param name="components" select="/map:sitemap/map:components/map:transformerss/map:transformer"/>
    </xsl:call-template>
    
    /** The serializers */
    <xsl:call-template name="define-components">
      <xsl:with-param name="name-prefix">serializer</xsl:with-param>
      <xsl:with-param name="interface">Serializer</xsl:with-param>
      <xsl:with-param name="components" select="/map:sitemap/map:components/map:serializers/map:serializer"/>
    </xsl:call-template>
    
    /** The matchers */
    <xsl:call-template name="define-components">
      <xsl:with-param name="name-prefix">matcher</xsl:with-param>
      <xsl:with-param name="interface">Matcher</xsl:with-param>
      <xsl:with-param name="components" select="/map:sitemap/map:components/map:matchers/map:matcher"/>
    </xsl:call-template>
    
    /** The choosers */
    <xsl:call-template name="define-components">
      <xsl:with-param name="name-prefix">chooser</xsl:with-param>
      <xsl:with-param name="interface">Chooser</xsl:with-param>
      <xsl:with-param name="components" select="/map:sitemap/map:components/map:choosers/map:chooser"/>
    </xsl:call-template>

    /**
     * Pass a &lt;code&gt;Configuration&lt;/code&gt; instance to this
     * &lt;code&gt;Configurable&lt;/code&gt; class.
     */
    public void setConfiguration(Configuration xconf) {
//    throws ConfigurationException {

    <!-- configure all components -->
    /* Configure generators */
    <xsl:call-template name="config-components">
      <xsl:with-param name="name">generator</xsl:with-param>
      <xsl:with-param name="interface">Generator</xsl:with-param>
      <xsl:with-param name="components" 
          select="/map:sitemap/map:components/map:generators/map:generator"/>
      <xsl:with-param name="default" 
          select="/map:sitemap/map:components/map:generators/@default"/>
    </xsl:call-template>    

    /* Configure filters */
    <xsl:call-template name="config-components">
      <xsl:with-param name="name">transformer</xsl:with-param>
      <xsl:with-param name="interface">Transformer</xsl:with-param>
      <xsl:with-param name="components" 
          select="/map:sitemap/map:components/map:transformers/map:transformer"/>
      <xsl:with-param name="default" 
          select="/map:sitemap/map:components/map:transformers/@default"/>
    </xsl:call-template>    
    
    /* Configure serializers */
    <xsl:call-template name="config-components">
      <xsl:with-param name="name">serializer</xsl:with-param>
      <xsl:with-param name="interface">Serializer</xsl:with-param>
      <xsl:with-param name="components" 
          select="/map:sitemap/map:components/map:serializers/map:serializer"/>
      <xsl:with-param name="default" 
          select="/map:sitemap/map:components/map:serializers/@default"/>
    </xsl:call-template>    

    /* Configure matchers */
    <xsl:call-template name="config-components">
      <xsl:with-param name="name">matcher</xsl:with-param>
      <xsl:with-param name="interface">Matcher</xsl:with-param>
      <xsl:with-param name="components" 
          select="/map:sitemap/map:components/map:matchers/map:matcher"/>
      <xsl:with-param name="default" 
          select="/map:sitemap/map:components/map:matchers/@default"/>
    </xsl:call-template>
    
    /* Configure choosers */
    <xsl:call-template name="config-components">
      <xsl:with-param name="name">chooser</xsl:with-param>
      <xsl:with-param name="interface">Chooser</xsl:with-param>
      <xsl:with-param name="components" 
          select="/map:sitemap/map:components/map:choosers/map:chooser"/>
      <xsl:with-param name="default" 
          select="/map:sitemap/map:components/map:choosers/@default"/>
    </xsl:call-template>
    }
 
    /** 
     * Process the given <code>Request</code> producing the output to the 
     * specified <code>Response</code> and <code>OutputStream</code>. 
     */ 
    public boolean process(Request req, Response res, OutputStream out)  
    throws SAXException, IOException, ProcessingException { 
      ResourcePipeline pipeline = constructPipeline (req, res); 
      return pipeline.startPipeline(req, res, out); 
    } 

    protected ResourcePipeline constructPipeline (Request request, Response resposne) {
      ResourcePipeline pipeline = new ResourcePipeline ();
      Stack resultStack = new Stack();
      Map map = null;
      Parameters emptyParam = new Parameters(); 
      Parameters param = null; 
      <xsl:apply-templates select="/map:sitemap/map:pipelines/map:pipeline/map:match"/>
      return pipeline;
    }
}

  </xsl:template> <!-- match="map:sitemap" -->

  <xsl:template match="map:match">
    <xsl:if test="not(ancestor::*[local-name()='pipeline'])">
      <xsl:call-template name="error">
        <xsl:with-param name="message">&lt;match can only appear in a &lt;pipeline&gt;</xsl:with-param>
      </xsl:call-template>
    </xsl:if>
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
       resultStack.push (map);  
       <xsl:apply-templates/>
       <xsl:if test="not(descendant::map:match or descendant::map:choose)">
         return pipeline;
       </xsl:if>
    }
  </xsl:template> <!-- match="map:match" -->

  <xsl:template match="map:choose">
    <xsl:variable name="chooser-type">
      <xsl:call-template name="get-parameter">
        <xsl:with-param name="parname">type</xsl:with-param>
        <xsl:with-param name="default"><xsl:value-of select="/map:sitemap/map:components/map:choosers/@default"/></xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:for-each select="./map:when">
      <xsl:variable name="test-value">
        <xsl:call-template name="get-parameter">
          <xsl:with-param name="parname">test</xsl:with-param>
          <xsl:with-param name="required">true</xsl:with-param>
        </xsl:call-template>
      </xsl:variable>
      if (chooser_<xsl:value-of select="translate($chooser-type, '- ', '__')"/>.choose ("<xsl:value-of select="$test-value"/>", request)) {
       <xsl:apply-templates/>
       <xsl:if test="not(descendant::map:match or descendant::map:choose)">
         return pipeline;
       </xsl:if>
      }
    </xsl:for-each>

    <xsl:for-each select="./map:otherwise">
      else {
      <xsl:apply-templates/>
       <xsl:if test="not(descendant::map:match or descendant::map:choose)">
         return pipeline;
       </xsl:if>
      }
    </xsl:for-each>
  </xsl:template> <!-- match="/map:sitemap/map:choose" -->

  <xsl:template match="map:generate">
    <xsl:call-template name="setup-component">
      <xsl:with-param name="default-component" select="/map:sitemap/map:components/map:generators/@default"/>
      <xsl:with-param name="method">setGenerator</xsl:with-param>
      <xsl:with-param name="prefix">generator</xsl:with-param>
    </xsl:call-template>
  </xsl:template> <!-- match="map:generate" -->

  <xsl:template match="map:filter[ancestor::*!='map:components']">
    <xsl:call-template name="setup-component">
      <xsl:with-param name="default-component" select="/map:sitemap/map:components/map:filters/@default"/>
      <xsl:with-param name="method">addFilter</xsl:with-param>
      <xsl:with-param name="prefix">filter</xsl:with-param>
    </xsl:call-template>
  </xsl:template> <!-- match="map:filter" -->

  <xsl:template match="map:serialize">
    <xsl:call-template name="setup-component">
      <xsl:with-param name="default-component" select="/map:sitemap/map:components/map:serializers/@default"/>
      <xsl:with-param name="method">setSerializer</xsl:with-param>
      <xsl:with-param name="prefix">serializer</xsl:with-param>
    </xsl:call-template> 
  </xsl:template> <!-- match="map:serialize" --> 

  <xsl:template match="map:mount">
  </xsl:template> <!-- match="map:mount" -->

  <xsl:template match="map:param">
    param.setParameter ("<xsl:value-of select="@name"/>", "<xsl:value-of select="@map:value"/>");
  </xsl:template> <!-- match="map:param" -->

  <!-- Sitemap Utility templates --> 
 
  <xsl:template name="define-components">
    <xsl:param name="name-prefix"/>
    <xsl:param name="interface"/>
    <xsl:param name="components"/>
    <xsl:for-each select="$components">
      private <xsl:value-of select="$interface"/><xsl:text> 
       </xsl:text><xsl:value-of select="$name-prefix"/>_<xsl:value-of select="translate(./@type, '- ', '__')"/> = null;
    </xsl:for-each>
      private <xsl:value-of select="$interface"/><xsl:text> </xsl:text><xsl:value-of select="$name-prefix"/>_default = null;
  </xsl:template>

  <xsl:template name="config-components"> 
    <xsl:param name="name"/>  
    <xsl:param name="interface"/> 
    <xsl:param name="components"/>  
    <xsl:param name="default"/>  
 
    <xsl:variable name="qname"> 
      <xsl:value-of select="concat($prefix, ':value')"/> 
    </xsl:variable> 

    <xsl:for-each select="$components"> 
      <xsl:variable name="confname">
        conf_<xsl:value-of select="$name"/>_<xsl:value-of select="@type"/>
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
    <xsl:value-of select="$name"/>_default = <xsl:value-of select="translate($name, '- ', '__')"/>_<xsl:value-of select="translate($default, '- ', '__')"/>; 
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
      conf_<xsl:value-of select="$name"/>_<xsl:value-of select="$type"/><xsl:value-of select="$subname"/>
    </xsl:variable>

    Configuration <xsl:value-of select="$confname"/> = confBuilder.newConfiguration ("<xsl:value-of select="$config-name"/>"); 

    <!-- process content with map:value -->
    <xsl:for-each select="$components">
      <xsl:choose>
        <xsl:when test="name(@*)=$qname"> 
          confBuilder.addAttribute ("<xsl:value-of select="name(.)"/>", "<xsl:value-of select="@*[name(.)=$qname]"/>", <xsl:value-of select="$confname"/>);
        </xsl:when>
        <xsl:when test="count(./*)=0"> 
          confBuilder.addAttribute ("<xsl:value-of select="name(.)"/>", "<xsl:value-of select="."/>", <xsl:value-of select="$confname"/>); 
        </xsl:when>
        <xsl:when test="count(./*)>0"> 
          <xsl:variable name="newsubname">
            <xsl:choose>
              <xsl:when test="not($subname)">1</xsl:when>
              <xsl:otherwise><xsl:value-of select="number($subname)+1"/></xsl:otherwise>
            </xsl:choose>
          </xsl:variable>
          <xsl:call-template name="nested-config-components"> 
            <xsl:with-param name="name"><xsl:value-of select="$name"/></xsl:with-param> 
            <xsl:with-param name="config-name"><xsl:value-of select="local-name(.)"/></xsl:with-param> 
            <xsl:with-param name="interface"><xsl:value-of select="$interface"/></xsl:with-param> 
            <xsl:with-param name="components" select="./*"/> 
            <xsl:with-param name="type"><xsl:value-of select="$type"/></xsl:with-param>
            <xsl:with-param name="subname">_<xsl:value-of select="$newsubname"/></xsl:with-param>
          </xsl:call-template> 
          <xsl:variable name="newconfname">
            conf_<xsl:value-of select="$name"/>_<xsl:value-of select="$type"/><xsl:value-of select="$newsubname"/>
          </xsl:variable>
          confBuilder.addConfiguration (<xsl:value-of select="$newconfname"/>, <xsl:value-of select="$confname"/>); 
        </xsl:when>
        <xsl:otherwise> 
        </xsl:otherwise> 
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
  <xsl:template match="map:logicsheet|map:dependency"/>

</xsl:stylesheet>
