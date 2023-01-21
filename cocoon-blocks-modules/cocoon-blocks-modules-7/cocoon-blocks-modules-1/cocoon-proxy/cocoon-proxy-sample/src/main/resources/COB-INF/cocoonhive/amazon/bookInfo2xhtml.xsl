<?xml version="1.0" encoding="UTF-8"?>
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
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  version="1.0">

  <xsl:output method="xml" version="1.0" 
  	encoding="UTF-8" indent="yes" 
  	omit-xml-declaration="no" 
  	doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN"  
  	doctype-system="DTD/xhtml1-strict.dtd"/>
  	
  <xsl:template match="/">
        <xsl:apply-templates select="ProductInfo/Details"/>
  </xsl:template>
  
  <xsl:template match="Details">
    <table border="0" width="100%" cellpadding="5">
      <tr>
        <td rowspan="4">
          <img src="{ImageUrlMedium}"/>
        </td>
        <td>
          <b><u><font size="+1"><xsl:value-of select="ProductName"/></font></u></b>
        </td>
      </tr>
      <tr>
        <td>
          <b>
            <xsl:text>Published: </xsl:text>
          </b>
          <xsl:value-of select="ReleaseDate"/>
        </td>
      </tr>
      <tr>
        <td>
          <xsl:call-template name="OurPrice">
            <xsl:with-param name="OurPrice" 
              select="substring(OurPrice, 2)"/>
            <xsl:with-param name="ListPrice" 
              select="substring(ListPrice, 2)"/>
          </xsl:call-template>
          <xsl:apply-templates select="UsedPrice | CollectiblePrice | ThirdPartyNewPrice"/> 
          <xsl:apply-templates select="SalesRank"/>
        </td>
      </tr>
      <tr>
        <td>
          <xsl:apply-templates select="Authors"/>
        </td>
      </tr>
      <tr>
        <td>
          <xsl:call-template name="BuyButton">
            <xsl:with-param name="Asin" 
              select="Asin"/>
          </xsl:call-template>
        </td>
      </tr>
      <xsl:apply-templates select="Reviews/AvgCustomerRating"/>
      <xsl:apply-templates select="Reviews/CustomerReview">
        <xsl:sort select="Rating"/>
      </xsl:apply-templates>
    </table>
    <hr/>
  </xsl:template>

  <xsl:template name="OurPrice">
    <xsl:param name="OurPrice"  select="'1.00'"/>
    <xsl:param name="ListPrice" select="'1.00'"/>
    <xsl:param name="UsedPrice"/>
    <xsl:param name="CollectiblePrice"/>
    <xsl:param name="ThirdPartyNewPrice"/>

    <xsl:choose>
      <xsl:when test="$OurPrice = $ListPrice">
        <b>Our price: </b><xsl:text>$</xsl:text>
        <xsl:value-of select="$OurPrice"/>
      </xsl:when>
      <xsl:otherwise>
        <b>Our price: </b><xsl:text>$</xsl:text>
        <xsl:value-of select="$OurPrice"/>
        <br />
        <b>List price: </b><xsl:text>$</xsl:text>
        <xsl:value-of select="$ListPrice"/>
        <xsl:text> (</xsl:text>
        <i>that's </i>
        <b>
          <xsl:value-of select="100 - round(($OurPrice div $ListPrice) * 100)"/>
          <xsl:text>%</xsl:text>
        </b>
        <i> off!</i>
        <xsl:text>)</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="SalesRank">
    <br />
    <b>
      <xsl:text>Sales rank: </xsl:text>
      <xsl:value-of select="."/>
    </b>
  </xsl:template>

  <xsl:template match="UsedPrice">
    <br />
    <b>Used price: </b>
    <xsl:value-of select="."/>
  </xsl:template>

  <xsl:template match="CollectiblePrice">
    <br />
    <b>Collectible price: </b>
    <xsl:value-of select="."/>
  </xsl:template>

  <xsl:template match="ThirdPartyNewPrice">
    <br />
    <b>Buy one from zShops for: </b>
    <xsl:value-of select="."/>
  </xsl:template>

  <xsl:template match="Authors">
    <xsl:choose>
      <xsl:when test="count(Author)&gt; 2">
        <b>Authors: </b>
        <xsl:for-each select="Author">
          <xsl:choose>
            <xsl:when test="not(position() = last())">
              <xsl:value-of select="."/>
              <xsl:text>, </xsl:text>
            </xsl:when>
            <xsl:otherwise>
              <xsl:text>and </xsl:text>
              <xsl:value-of select="."/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:for-each>
      </xsl:when>
      <xsl:when test="count(Author) = 2">
        <b>Authors: </b>
        <xsl:value-of select="Author[1]"/>
        <xsl:text> and </xsl:text>
        <xsl:value-of select="Author[2]"/>
      </xsl:when>
      <xsl:otherwise>
        <b>Author: </b>
        <xsl:value-of select="Author"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="BuyButton">
    <xsl:param name="Asin"/>
    <xsl:element name="a">
      <xsl:attribute name="href" xml:space="default">http://www.amazon.com/o/dt/assoc/handle-buy-box?asin=<xsl:value-of select="$Asin"/>&amp;tag_value=associates_tag&amp;dev-tag-value=D24H52G74BUDRY&amp;submit.add-to-cart=true</xsl:attribute>
      <xsl:attribute name="target" xml:space="default">_blank</xsl:attribute>
      <xsl:text>Buy from Amazon.com</xsl:text>
    </xsl:element>
  </xsl:template>

  <xsl:template match="AvgCustomerRating">
    <tr>
      <td colspan="2">
        <b>Average customer rating: </b>
        <xsl:call-template name="Stars">
          <xsl:with-param name="average" select="."/>
        </xsl:call-template>
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="CustomerReview">
    <tr>
      <td colspan="2">
        <xsl:call-template name="Stars">
          <xsl:with-param name="average" select="Rating"/>
        </xsl:call-template>
        <xsl:text> </xsl:text>
        <b><xsl:value-of select="Summary"/></b>
        <br />
        <p><xsl:value-of select="Comment"/></p>
      </td>
    </tr>
  </xsl:template>

  <xsl:template name="Stars">
    <xsl:param name="average" select="5"/>
    <xsl:choose>
      <xsl:when test="$average &gt; 4.75">
        <img src="http://g-images.amazon.com/images/G/01/detail/stars-5-0.gif"/>
      </xsl:when>
      <xsl:when test="$average &gt; 4.25">
        <img src="http://g-images.amazon.com/images/G/01/detail/stars-4-5.gif"/>
      </xsl:when>
      <xsl:when test="$average &gt; 3.75">
        <img src="http://g-images.amazon.com/images/G/01/detail/stars-4-0.gif"/>
      </xsl:when>
      <xsl:when test="$average &gt; 3.25">
        <img src="http://g-images.amazon.com/images/G/01/detail/stars-3-5.gif"/>
      </xsl:when>
      <xsl:when test="$average &gt; 2.75">
        <img src="http://g-images.amazon.com/images/G/01/detail/stars-3-0.gif"/>
      </xsl:when>
      <xsl:when test="$average &gt; 2.25">
        <img src="http://g-images.amazon.com/images/G/01/detail/stars-2-5.gif"/>
      </xsl:when>
      <xsl:when test="$average &gt; 1.75">
        <img src="http://g-images.amazon.com/images/G/01/detail/stars-2-0.gif"/>
      </xsl:when>
      <xsl:when test="$average &gt; 1.25">
        <img src="http://g-images.amazon.com/images/G/01/detail/stars-1-5.gif"/>
      </xsl:when>
      <xsl:when test="$average &gt; .75">
        <img src="http://g-images.amazon.com/images/G/01/detail/stars-1-0.gif"/>
      </xsl:when>
      <xsl:when test="$average &gt; .25">
        <img src="http://g-images.amazon.com/images/G/01/detail/stars-0-5.gif"/>
      </xsl:when>
      <xsl:otherwise>
        <img src="http://g-images.amazon.com/images/G/01/detail/stars-0-0.gif"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
