/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.components.jsp;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.thread.ThreadSafe;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;

/**
 * Allows WLS JSP to be used as a generator. 
 * 
 * This implementation includes via ServletContext.getNamedDispatcher() the
 * jsp-response. This a WLS-specific implementation.
 * This code contain WLS 5.1 specific classes, and uses WLS internal classes.
 *
 * @author <a href="mailto:dims@yahoo.com">Davanum Srinivas</a>
 * @author <a href="mailto:bh22351@i-one.at">Bernhard Huber</a>
 * @version CVS $Id: JSPEngineImplWLS.java,v 1.6 2004/03/05 13:01:57 bdelacretaz Exp $
 */
public class JSPEngineImplWLS extends AbstractLogEnabled
    implements JSPEngine, Parameterizable, ThreadSafe {

    /** The Servlet Include Path */
    public static final String INC_SERVLET_PATH = "javax.servlet.include.servlet_path";

    /** config-parameter name for specifying the jsp servlet-name.
      ie. servlet-name
    */
    public static final String CONFIG_SERVLET_NAME = "servlet-name";
    /** default value of CONFIG_SERVLET_NAME.
      ie. *jsp, this is the WLS JSP servlet default name
    */
    public static final String DEFAULT_SERVLET_NAME = "*.jsp";
    /** the configured name of the jsp servlet
    */
    
    String servletName = DEFAULT_SERVLET_NAME;

    /**
     * parameterize
     * @param params Parameters
     * @exception ParameterException
     */
    public void parameterize(Parameters params) throws ParameterException {
        this.servletName = params.getParameter( CONFIG_SERVLET_NAME, DEFAULT_SERVLET_NAME);
    }

    /**
     * execute the JSP and return the output
     *
     * @param url
     * @param servletRequest
     * @param servletResponse
     * @param servletContext
     * @exception IOException
     * @exception ServletException
     * @exception Exception
     */
    public byte[] executeJSP(String url,
                             HttpServletRequest servletRequest,
                             HttpServletResponse servletResponse,
                             ServletContext servletContext)
        throws IOException, ServletException, Exception {

        byte[] bytes = null;

        HttpServletRequest request = servletRequest;
        String inc_servlet_path_was = (String) servletRequest.getAttribute(INC_SERVLET_PATH);
        request.setAttribute(INC_SERVLET_PATH, url);
        MyWLSResponse response = new MyWLSResponse( servletResponse,
          (weblogic.servlet.internal.ServletContextImpl) servletContext);

        // dispatch to the named servlet
        RequestDispatcher rd = servletContext.getNamedDispatcher(servletName);
        if (rd != null) {
          rd.include(request,response);
          response.flushBuffer();
          
          if (getLogger().isDebugEnabled()) {
              getLogger().debug("JSP response: " + response.getResponseContentAsString());
          }
          
          bytes = response.getResponseContentAsByteArray();

          if (inc_servlet_path_was != null) {
            servletRequest.setAttribute( INC_SERVLET_PATH, inc_servlet_path_was );
          }
        } else {
          getLogger().error( "Specify a correct " + CONFIG_SERVLET_NAME + " " + servletName );
        }

        return bytes;
    }

    /** WLS jsp servlet hack.
      <p>
        Here WLS specific classes are used.
      </p>
      <p>
        The weblogic.servlet.JSPServlet, and
        weblogic.servlet.internal.RequesDispatcherImpl expects
        objects weblogic.servlet.internal.ServletOutputStreamImpl,
        and weblogic.servlet.internal.ServletResponseImpl.
        Thus we have to use <i>exactly</i> these classes!
      </p>
    */
    class MyWLSResponse extends weblogic.servlet.internal.ServletResponseImpl {
      /* the cocoon2 response. Let's use this response to forward headers
      , cookies, etc generated inside the jsp-response
      */
      HttpServletResponse response;

      ByteArrayOutputStream baos;
      weblogic.servlet.internal.ServletOutputStreamImpl wlsOutputStream;

      public MyWLSResponse( HttpServletResponse response,
        weblogic.servlet.internal.ServletContextImpl servlet_context ) {

        super( servlet_context );
        this.response = response;

        baos = new ByteArrayOutputStream();

        wlsOutputStream =
          new weblogic.servlet.internal.ServletOutputStreamImpl( baos );
        this.setOutputStream( wlsOutputStream );
        wlsOutputStream.setImpl( this );
      }

      /** flush response content.
      */
      public void flushBuffer() throws IOException {
        super.flushBuffer();
        baos.flush();
      }

      /** return response as byte array.
        <p>Note: http-headers are skipped. More exactly all chars until first
        '&lt;?xml', or '\r\n\r\n&lt; sequence. This may be a bit heuristic.
        </p>
        <p>Note: we are expecting the xml prolog, without the xml prolog http
        -headers are passed further, and the xml parser will surly complain!
        </p>
      */
      public byte[] getResponseContentAsByteArray() {
        byte[] baos_arr = baos.toByteArray();

        int baos_arr_length = baos_arr.length;
        int i = 0;
        boolean matched = false;
        final int I_MAX = 8192; // check only header

        final byte MATCH_0d = (byte)'\r';
        final byte MATCH_0a = (byte)'\n';

        final byte MATCH_FIRST = (byte)'<';
        final byte MATCH_SECOND = (byte)'?';
        final byte MATCH_THIRD = (byte)'x';
        final byte MATCH_FOURTH = (byte)'m';
        final byte MATCH_FIFTH = (byte)'l';

        final int MATCH_COUNT = 5;

        while (i + MATCH_COUNT < baos_arr_length && i < I_MAX && !matched) {

          matched = (baos_arr[i] == MATCH_FIRST && baos_arr[i+1] == MATCH_SECOND &&
             baos_arr[i+2] == MATCH_THIRD && baos_arr[i+3] == MATCH_FOURTH &&
             baos_arr[i+4] == MATCH_FIFTH);
          if (matched) break;

          matched = (baos_arr[i] == MATCH_0d && baos_arr[i+1] == MATCH_0a &&
            baos_arr[i+2] == MATCH_0d && baos_arr[i+3] == MATCH_0a &&
            baos_arr[i+4] == MATCH_FIRST);
          if (matched) {
            i += 4; // skip leading \r\n\r\n, too
            break;
          }
          i += 2;
        }
        if (matched && i > 0) {
          int baos_arr_new_length = baos_arr_length - i;

          byte []new_baos_arr = new byte[baos_arr_new_length];
          System.arraycopy( baos_arr, i, new_baos_arr, 0, baos_arr_new_length );
          baos_arr = new_baos_arr;
        }
        return baos_arr;
      }

      public String getResponseContentAsString() {
        String s = new String( getResponseContentAsByteArray() );
        return s;
      }

      // following methods forwarding from jsp-repsonse to cocoon2-repsonse

      public String getCharacterEncoding() { return this.response.getCharacterEncoding();}
      public Locale getLocale(){ return this.response.getLocale();}
      public void addCookie(Cookie cookie){ response.addCookie(cookie); }
      public boolean containsHeader(String s){ return response.containsHeader(s); }
      /** @deprecated use encodeURL(String url) instead. */
      public String encodeUrl(String s){ return response.encodeUrl(s); }
      public String encodeURL(String s){ return response.encodeURL(s); }
      /** @deprecated use encodeRedirectURL(String url) instead. */
      public String encodeRedirectUrl(String s){ return response.encodeRedirectUrl(s); }
      public String encodeRedirectURL(String s){ return response.encodeRedirectURL(s); }
      public void sendError(int i, String s)
          throws IOException{response.sendError(i,s); }
      public void sendError(int i)
          throws IOException{response.sendError(i); }
      public void sendRedirect(String s)
          throws IOException{response.sendRedirect(s); }
      public void setDateHeader(String s, long l){response.setDateHeader(s, l); }
      public void addDateHeader(String s, long l){response.addDateHeader(s, l); }
      public void setHeader(String s, String s1){response.setHeader(s, s1); }
      public void addHeader(String s, String s1){response.addHeader(s, s1); }
      public void setIntHeader(String s, int i){response.setIntHeader(s, i); }
      public void addIntHeader(String s, int i){response.addIntHeader(s, i); }
      public void setStatus(int i){response.setStatus(i); }
      /** @deprecated use sendError(int, String) instead */
      public void setStatus(int i, String s){response.setStatus(i, s); }
    }
}
