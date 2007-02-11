/*
  
 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================
 
 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 
 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:
 
 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.
 
 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.
  
 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.
 
 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.
 
 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.
 
 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.
 
 */
package org.apache.cocoon.generation; 

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpUrlMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.UrlGetMethod;
import org.apache.commons.httpclient.methods.UrlPostMethod;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.xml.sax.SAXParser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 *  The WebServiceProxyGenerator is intended to:
 *
 * 1) Allow easy syndication of dynamic interactive content as a natural extension of the currently popular static content syndication with RSS.
 *
 * 2) Allow transparent routing of web service request through GET, POST, SOAP-RPC and SOAP-DOC binding methods.
 *
 * 3) Allow almost full control through sitemap configuration.
 *
 * 4) Allow use of Cocoon components for content formatting, aggregation and styling through a tight integration with the Cocoon sitemap.
 *
 * 5) Require 0 (zero) lines of Java or other business logic code in most cases.
 *
 * 6) Be generic and flexible enough to allow custom extensions for advanced and non-typical uses.
 *
 * 7) Support sessions, authentication, http 1.1, https,  request manipulation, redirects following, connection pooling, and others.
 *
 * 8) Use the Jakarta HttpClient library which provides many sophisticated features for HTTP connections.
 *
 * 9) (TBD) Use Axis for SOAP-RPC and SOAP-DOC bindings.
 *
 *
 * @author <a href="mailto:ivelin@apache.org">Ivelin Ivanov</a>, June 30, 2002
 * @author <a href="mailto:tc@hist.umn.edu">Tony Collen</a>, December 2, 2002
 * @version CVS $Id: WebServiceProxyGenerator.java,v 1.3 2003/03/16 17:49:15 vgritsenko Exp $
 */
public class WebServiceProxyGenerator extends ComposerGenerator {

  /**
   * Setup the WSP generator.
   *
   */
  public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par)
  throws ProcessingException, SAXException, IOException
  {
    super.setup(resolver, objectModel, src, par);
    
    try {
        Source inputSource = resolver.resolveURI(super.source);
        this.source = inputSource.getURI();
    } catch (SourceException se) {
        throw SourceUtil.handle("Unable to resolve " + super.source, se);
    }    
     
    configuredHttpMethod = par.getParameter("wsproxy-method", METHOD_GET);
     
    httpClient = getHttpClient();
    
  }
  
  
  /**
   * Recycle this component.
   * All instance variables are set to <code>null</code>.
   */
  public void recycle()
  {
    httpClient = null;
    
    super.recycle();
  }
  
  
  /**
   * Generate XML data.
   */
  public void generate()
    throws IOException, SAXException, ProcessingException
  {
    SAXParser parser = null;
    try 
    {
      if (this.getLogger().isDebugEnabled()) {
          this.getLogger().debug("processing Web Service request:  " + this.source );
      }

      // forward request and bring response back
      String remoteResponseXml = fetch();

      InputSource inputSource = new InputSource(new StringReader( remoteResponseXml ));

      if (getLogger().isDebugEnabled()) {
          getLogger().debug( "processing Web Service Response " + remoteResponseXml );
      }

      parser = (SAXParser)this.manager.lookup(SAXParser.ROLE);
      parser.parse(inputSource, super.xmlConsumer);
        
    } 
    catch (ComponentException ex)
    {
      throw new ProcessingException( "WebServiceProxyGenerator.generate() error", ex);
    }
    finally 
    {
        this.manager.release((Component)parser);
    }
    
  } // generate
  
  
  /**
   *
   * Forward the request and return the response
   *
   * Will use an UrlGetMethod to benefit the cacheing mechanism
   * and intermediate proxy servers.
   * It is potentially possible that the size of the request
   * may grow beyond a certain limit for GET and it will require POST instead.
   *
   * @return String XML response
   *
   */
  public String fetch() throws ProcessingException
  {
    try
    {
      

      // TODO: Write a log entry detailing which httpMethod was configured
     
      HttpUrlMethod method = null;
   
      // check which method (GET or POST) to use.
      if ( this.configuredHttpMethod.equalsIgnoreCase( METHOD_POST ) ) 
      {
        method = new UrlPostMethod( this.source );
        ((UrlPostMethod)method).setUseDisk(false);
      } else {
        method = new UrlGetMethod( this.source );
        ((UrlGetMethod)method).setUseDisk(false);
      }

      // this should probably be exposed as a sitemap option
      method.setFollowRedirects( true );

      // copy request parameters and merge with URL parameters
      Request request = ObjectModelHelper.getRequest( objectModel );

      
      ArrayList paramList = new ArrayList();
      Enumeration enum = request.getParameterNames();
      while ( enum.hasMoreElements() )
      {
        String pname = (String)enum.nextElement();
        String[] paramsForName = request.getParameterValues( pname );
        for (int i = 0; i < paramsForName.length; i++)
        {
          NameValuePair pair = 
            new NameValuePair( pname, paramsForName[i] );
          paramList.add( pair );
        }
      }

      if ( paramList.size() > 0 )
      {
        NameValuePair[] allSubmitParams = 
          new NameValuePair[ paramList.size() ];
        paramList.toArray( allSubmitParams );

        String urlQryString = method.getQueryString();

        // use HttpClient encoding routines
        method.setQueryString( allSubmitParams );
        String submitQryString = method.getQueryString();

        // set final web service query string 
        method.setQueryString( urlQryString + "&" + submitQryString );
      } // if there are submit parameters 

      int htcode = httpClient.executeMethod( method );
       
      // meaningful reaction to htcodes different than 200
       // TODO: We should probably be logging this, as well.
       if (htcode >= 400) {
           throw new ProcessingException( "The remote returned error " + htcode + " when attempting to access remote URL:"  + method.getUrl() );
       }

      
      // FIXME: This sleep() is a temporary workaround 
      // to avoid NullPointerException in the next line.
      Thread.sleep( 100 ); 

      String ret = method.getResponseBodyAsString();

      int startOfXML = ret.indexOf("<?xml");
      if (startOfXML == -1)
      { // No xml?!
        throw new ProcessingException("Invalid (non XML) response returned from remote URL: " + method.getUrl() );
      }

      ret.substring(startOfXML);

      return ret;

    } catch (Exception ex)
    {
      throw new ProcessingException("Error invoking remote service: " + ex,
      ex);
    }
  } // fetch
  
 
/**
 * Create once per client session and
 * consequetively return an HttpMultiClient
 *
 */
protected HttpClient getHttpClient()
{
  Request request = ObjectModelHelper.getRequest( objectModel );
  Session session = request.getSession( true );
  HttpClient httpClient = null;
  if (session != null)
  {
    httpClient = (HttpClient) session.getAttribute( HTTP_CLIENT );
  }
  if (httpClient == null)
  {
    httpClient = new HttpClient();
    
    if (System.getProperty("http.proxyHost") != null)
    {
      String proxyHost = System.getProperty("http.proxyHost");
      int proxyPort = Integer.parseInt(System.getProperty("http.proxyPort"));
      HostConfiguration config = httpClient.getHostConfiguration();
      if (config == null) {
        config = new HostConfiguration();
      }
      config.setProxy(proxyHost, proxyPort);
      httpClient.setHostConfiguration(config);
    }
    
    session.setAttribute( HTTP_CLIENT, httpClient );
  }
  return httpClient;
}

// private attributes section
private static String HTTP_CLIENT = "HTTP_CLIENT";
private HttpClient httpClient;

// for GET/POST configurability
private static String METHOD_GET = "GET";
private static String METHOD_POST = "POST";

// default to GET
private String configuredHttpMethod = METHOD_GET;


} // class
