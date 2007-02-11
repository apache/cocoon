<?xml version="1.0"?>
<xsl:stylesheet
  version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  
  <xsl:include href="woody-page-layout.xsl"/>
  
  <!-- head and body stuff required to use the calendar popup -->
  <xsl:template match="head">
    <xsl:copy>
      <xsl:apply-templates/>
      <xsl:call-template name="woody-layout-head"/>
      <style type="text/css">
        .woody-tab {
            background-color: #ffffff;
            border: 1px solid #000000;
            border-bottom-width: 0px;
            padding: 2px 1em 2px 1em;
            margin-right: 5px;
            position: relative;
            text-decoration: none;
            top: -1px;
            z-index: 100;
            cursor: pointer;
        }
              
        .woody-tab.woody-activeTab {
            z-index: 102;
            font-weight: bold;
            padding-top: 5px;
            cursor: default;
        }
        
        .woody-tabContent {
            background-color: #ffffff;
            border: 1px solid #000000;
            padding: 1em;
            position: relative;
            z-index: 101;
        }
      </style>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="body">
    <xsl:copy>
      <xsl:apply-templates/>
      <xsl:call-template name="woody-layout-body"/>
    </xsl:copy>
  </xsl:template>
  

</xsl:stylesheet>
