<?xml version="1.0"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

 <xsl:import href="copyover.xsl"/>

  <xsl:template match="faqs">
   <document>
    <header>
     <title><xsl:value-of select="@title"/></title>
    </header>
    <body>
      <s1 title="Questions">
        <xsl:choose>
          <xsl:when test="faqsection">
            <xsl:apply-templates select="faqsection" mode="index"/>
            <xsl:if test="faq">
              <s2 title="Miscellaneous">
                <xsl:call-template name="faqlist"/>
              </s2>
            </xsl:if>
          </xsl:when>
          <xsl:otherwise>
            <xsl:call-template name="faqlist"/>
          </xsl:otherwise>
        </xsl:choose>
      </s1>

      <s1 title="Answers">
        <xsl:apply-templates select="//faq"/>
      </s1>
    </body>
   </document>
  </xsl:template>

  <xsl:template match="faqsection" mode="index">
    <s2 title="{@title}">
      <xsl:call-template name="faqlist"/>
    </s2>
  </xsl:template>

  <xsl:template name="faqlist">
    <ul>
      <xsl:apply-templates select="faq" mode="index"/>
    </ul>
  </xsl:template>

  <xsl:template match="faq" mode="index">

    <!-- How can we modularise this to avoid copy-and-paste? (RDG) -->
    <xsl:variable name="fragid"><xsl:choose>
        <xsl:when test="@id">
          <xsl:value-of select="@id"/>
        </xsl:when>
        <xsl:otherwise>
          faq-<xsl:value-of select="position()"/>
        </xsl:otherwise>
      </xsl:choose></xsl:variable>

    <li>
      <jump anchor="{$fragid}">
        <xsl:value-of select="question"/>
      </jump>
    </li>
  </xsl:template>

  <xsl:template match="faq">
    <!-- How can we modularise this to avoid copy-and-paste? (RDG) -->
    <xsl:variable name="fragid"><xsl:choose>
        <xsl:when test="@id">
          <xsl:value-of select="@id"/>
        </xsl:when>
        <xsl:otherwise>
          faq-<xsl:value-of select="position()"/>
        </xsl:otherwise>
      </xsl:choose></xsl:variable>

    <anchor id="{$fragid}"/>
    <s2 title="{question}">
      <xsl:apply-templates/>
    </s2>
  </xsl:template>

  <xsl:template match="question">
    <!-- ignored since already used -->
  </xsl:template>

  <xsl:template match="answer">
    <xsl:apply-templates/>
  </xsl:template>

</xsl:stylesheet>
