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
<!--
	Cocoon WebServiceProxyGenerator 
    Feedback Wizard processing and displaying stylesheet.	
  
  This stylesheet merges an XMLForm document into 
  a pre-final HTML document. It includes other presentational
  parts of a page orthogonal to the xmlform.

  author: Ivelin Ivanov, ivelin@apache.org, June 2002

-->

<xsl:stylesheet 
    version="1.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xf="http://apache.org/cocoon/xmlform/1.0"
    exclude-result-prefixes="xalan"
>

	<xsl:template match="document">
		<html>
			<head>
				<title>Embedding One Web Site into another with Cocoon WebServiceProxyGenerator Demo </title>		
				<style type="text/css">
				<![CDATA[
              H1{font-family : sans-serif,Arial,Tahoma;color : white;background-color : #009900;} 
              BODY{font-family : sans-serif,Arial,Tahoma;color : black;background-color : #AAFFAA;} 
              B{color : white;background-color : #5090a0;} 
              HR{color : #0086b2;}
              input { background-color: #FFFFFF; color: #000099; border: 1px solid #0000FF; }		
              table { background-color: #DDDDFF; color: #000099; font-size: x-small; border: 2px solid brown;}
              select { background-color: #FFFFFF; color: #000099 }
             .error { color: #FF0000; }	      
             .invalid { color: #FF0000; border: 2px solid #FF0000; }
             .info { color: #0000FF; border: 1px solid #0000FF; }
             .repeat { background-color: #AAAACC; border: 0px inset #999999;border: 1px inset #999999; width: 100%; }
             .group { background-color: #AAAACC; border: 0px inset #999999;border: 0px inset #999999;  width: 100%; }
             .sub-table { background-color: #AAAACC; border: 1px inset black; }
             .button { background-color: green; color: white; border: 5px solid #AAFFAA; width: 90px; }
             .plaintable { background-color: #AAAACC; border: 1px inset black;border: 1px inset black; width: 100%; }
              ]]>
				</style>			
              </head>
			<body>
                <table border="3">
                  <tr>
                    <td colspan="2" align="center">
                        <br/>
                        Embedding One Web Site into Another 
                        <br/><br/>
                        made easy with Cocoon 
                        <br/><br/>
                    </td>
                  </tr>
                  <tr>
                    <td width="20%" valign="top">
                      Navigation bar of the containing web site
                        <ul>
                          <li>
                              Remote Web Site
                          </li>
                          <li>
                              Local Content
                          </li>
                          <li>
                              All look like ONE.
                          </li>
                        </ul>
                    </td>
                    <td>
                      This is area for an embedded web site.
                      <br/>
                      <xsl:apply-templates />
                    </td>
                  </tr>
                  <tr>
                    <td colspan="2">
                      <p>
                      Remote Web Site Integration is an easy task with Cocoon.
                      This demo shows how the WebServiceProxyGenerator sitemap component, 
                      combined with the XMLForm framework and XSLT, allows 
                      vendors to share interactive content with little effort.
                      </p>
                      <p>
                      The Web Service Proxy takes advantage of the fact that 
                      a Cocoon web application produces XML content
                      which is later translated into multiple presentation formats, like HTML or WML.
                      </p>
                      <p>
                      This demo embeds the Cocoon Feedback Wizard application, which produces an XML
                      view containing both static data and interactive forms.
                      Having a client independent content format, allows this view to be 
                      pulled to the embedding web site (this demo) and styled with XSLT 
                      in the Look &amp; Feel of the site.
                      </p>
                      <p>
                      <i>
                      Ok, styling presentation is easy to understand, but how is a form submitted to the original site?
                      </i>
                      <br/>
                      Since the form markup in the XML content of an embedded page uses relative URL
                      address for the target, once the end user submits, the form data is sent to the 
                      containing site, which captures the form data and the relative URL.
                      The Web Service Proxy then takes this information and re-submits it to the
                      original site. It then reads the XML response and makes it available to the 
                      sitemap for styling again. 
                      </p>
                      <p>
                      <i>
                      Hm, but the Feedback Wizard example maintains a session while going through
                      multiple pages. So, how is the containing site propagating the end user session to the 
                      to the embedded site?
                      </i>
                      <br/>
                      The answer is simple. The Web Service Proxy simply hooks to the end user session,
                      and automatically starts its own session with the remote site.
                      If the remote site does not require authentication, then everything is transparent
                      to the developer of the containing web site. Otherwise the WebServiceProxyGenerator 
                      has to be extended to override the procedure for initating session with the remote site.
                      </p>
                      <p>
                      <i>What transport protocols are supported?</i>
                      <br/>
                      HTTP 1.0, HTTP 1.1, HTTPS.
                      </p>
                      <p>
                      Have more questions? Look at the code, it is really simple.
                      If you need advise, search through the Cocoon mailing lists archives.
                      If you can't find the answer, email your question to the Cocoon mailing lists.
                      Finally, if you need to contact me, send email to Ivelin Ivanov, ivelin@apache.org.
                      </p>
                      <p>
                      If you like this component, please help with documentation.
                      Write an FAQ, HOW-TO or User Guide.
                      </p>
                      <p>
                      </p>
                    </td>
                  </tr>
                </table> 
			</body>
		</html>
	</xsl:template>
	
	<xsl:template match="xf:form">
    <xf:form method="post">
      <xsl:copy-of select="@*" />
        <br/><br/>
        <i><u>... and This is a Personalized HTML Form:</u></i>
        <br/><br/>
        <table align="center" border="0" class="sub-table">
          <tr>
            <td align="center" colspan="3">
                <h1>
                    <xsl:value-of select="xf:caption"/>
                    <hr/>
                </h1>
            </td>
          </tr>
          <xsl:if test="count(error/xf:violation) > 0">
              <tr>
                <td align="left" colspan="3" class="{error/xf:violation[1]/@class}">
                    <p>
                    * There are
                    [<b><xsl:value-of select="count(error/xf:violation)"/></b>]
                    errors. Please fix these errors and submit the form again.
                    </p>
                    <p>
                      <xsl:variable name="localViolations" select=".//xf:*[ child::xf:violation ]"/>
                      <xsl:for-each 
                        select="error/xf:violation">
                        <xsl:variable name="eref" select="./@ref"/>
                        <xsl:if test="count ($localViolations[ @ref=$eref ]) = 0">
                            * 
                            <xsl:value-of select="." />
                            <br/>
                        </xsl:if>
                      </xsl:for-each>
                    </p>
                    <p/>
                </td>
              </tr>
          </xsl:if>

         <xsl:for-each select="*[name() != 'xf:submit']">
          <xsl:choose>
            <xsl:when test="name() = 'error'"/>
            <xsl:when test="xf:*">
                <xsl:apply-templates select="."/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:copy-of select="."/>
            </xsl:otherwise>
           </xsl:choose>
         </xsl:for-each>
        <tr>
          <td align="center" colspan="3">
            <xsl:for-each select="*[name() = 'xf:submit']">
              <xsl:copy-of select="." />
              <xsl:text> </xsl:text>
            </xsl:for-each>
          </td>
        </tr>
      </table>
    </xf:form>
	</xsl:template>
    
	<xsl:template match="xf:repeat">
        <tr  width="100%">
            <td colspan="3" width="100%">
              Repeat elements:
                <table class="repeat">
               	    <xsl:apply-templates select="*"/>
                </table>
            </td>
        </tr>
  	</xsl:template>

	<xsl:template match="xf:group">
        <tr width="100%">
            <td width="100%" colspan="2">
                Group element:
                <table class="group" border="0">
               	    <xsl:apply-templates select="*"/>
                </table>
            </td>
        </tr>
  	</xsl:template>
    
	<xsl:template match="xf:output[@form]">
        <div align="center"> 
            <hr width="30%"/>
            <br/>
            <font size="-1">
              <code>
              [*]
                <xsl:value-of select="xf:caption" /> :
                <xsl:copy-of select="." />
              </code>
            </font>
            <br/>
        </div>
	</xsl:template>
	

    <xsl:template match="xf:*">
      <tr>
          <td align="left">
            <xsl:value-of select="xf:caption" />
          </td>
          <td align="left">
        <table class="plaintable">
         <tr>
          <td align="left">
            <xsl:copy-of select="." />
          </td>
          <xsl:if test="xf:violation">
              <td align="left" class="{xf:violation[1]/@class}" width="100%">
                <xsl:for-each select="xf:violation">
                  * <xsl:value-of select="." />
                  <br/>
                </xsl:for-each>
              </td>
          </xsl:if>
         </tr>
        </table>
          </td>
      </tr>
    </xsl:template>
    
        
	<xsl:template match="*">
	    <xsl:copy-of select="." />
	</xsl:template>
    
</xsl:stylesheet>



