------------------------------------------------------------------------

The WebServiceProxyGenerator is intended to allow one 
Web Site to Embed Another Web Site's presentation and logic.
It is an extension of the popular RSS based content syndication. 


Requirements:

1) Easy syndication of interactive content.  

2) Transparent routing of web service request through GET, POST, SOAP-RPC and SOAP-DOC binding methods.

3) Near complete control only through sitemap configuration.

4) Reuse of Cocoon components for content formatting, aggregation and styling through a tight integration with the Cocoon sitemap.

5) Require 0 (zero) lines of Java or other business logic code in most cases.

6) Be generic and flexible enough to allow custom extensions for advanced and non-typical uses.

7) Support sessions, authentication, http 1.1, https,  request manipulation, redirects following, connection pooling, and others.

8) Use the Jakarta HttpClient library which provides many sophisticated features for HTTP connections.

9) (TBD) Use Axis for SOAP-RPC and SOAP-DOC bindings.


------------------------------------------------------------------------
Example I: Integration of the Amazon book search service in a Cocoon portal:
------------------------------------------------------------------------

pipeline:
---------

    <map:pipeline>
      <map:match pattern="amazon/searchform">
        <map:generate src="AmazonForm.xml"/>
        <map:transform src="AmazonForm2html.xsl"/>
        <map:transform src="context://stylesheets/xmlform/xmlform2html.xsl"/>
        <map:serialize type="html"/>
      </map:match>  

      <map:match pattern="amazon/search*">
        <map:generate type="wsproxy" src="http://rcm.amazon.com/e/cm{1}">
          <!-- The WebSericeProxy generator takes 3 arguments: -->
          <!-- 1) The URL of the targeted web service -->
          <!-- 2) The binding method: GET, POST, SOAP-RPC or SOAP-DOC -->
          <!-- 3) Optionally, name of the session to the remote service, which allows scoping and grouping between different sitemap sections. -->
          <!-- Only 1) is required. 2) and 3) are optional and should not be used for most applications -->
          <map:parameter name="wsproxy-method" value="GET"/>
          <map:parameter name="wsproxy-session" value="myAmazonSession"/>
        </map:generate>

        <map:transform src="amazonform2html.xsl"/>
        <map:transform src="context://stylesheets/xmlform/xmlform2html.xsl"/>
        <map:serialize type="html"/>
      </map:match>  
    </map:pipeline>


AmazonForm.xml:
---------------

<?xml version="1.0" ?>
<document xmlns:xf="http://apache.org/cocoon/xmlform/1.0">
  <xf:form id="form-amazon" view="search" action="amazon/search?t=myAmazonId-02-20&amp;l=st1&amp;mode=books&amp;p=102&amp;o=1&amp;f=xml">
    <xf:caption>Amazon book search by keyword</xf:caption>    
    <xf:textbox ref="search">
        <xf:caption>Keywords</xf:caption>
    </xf:textbox>       
    <xf:submit id="submit" class="button">
      <xf:caption>Submit</xf:caption>
    </xf:submit>
  </xf:form>
</document>


------------------------------------------------------------------------
Example II: Even more interesting. 
Integration with an interactive web service - the Cocoon FeedBack Wizard. 
Notice that it is actually simpler to integrate with an interactive 
service then a "single call" based one, because WebServiceProxy supports sessions !
------------------------------------------------------------------------

    <map:pipeline>
      <map:match pattern="RemoteWizard/*">
        <map:generate type="wsproxy" src="http://remotehost:8080/cocoon/samples/xmlform/{1}">
            <map:parameter name="wsproxy-method" value="POST"/>
            <map:parameter name="wsproxy-session" value="myFeedbackWizardSession"/>
        </map:generate>
        <map:transform src="myNewStyle-wizard2html.xsl"/>
        <map:transform src="context://stylesheets/xmlform/xmlform2html.xsl"/>
        <map:serialize type="html"/>
      </map:match>  
    </map:pipeline>



------------------------------------------------------------------------
End of file
------------------------------------------------------------------------


-- 

-= Ivelin =-



