/*-- $Id: HttpServletResponseFacade.java,v 1.4 2001-03-01 16:24:22 greenrd Exp $ --
 
 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================
 
 Copyright (C) @year@ The Apache Software Foundation. All rights reserved.
 
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
 
 4. The names "Cocoon" and  "Apache Software Foundation"  must not be used to
    endorse  or promote  products derived  from this  software without  prior
    written permission. For written permission, please contact
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
 
package org.apache.cocoon.response;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * Hack to handle redirects correctly with Tomcat 3.1.
 *
 * @author <a href="mailto:greenrd@hotmail.com">Robin Green</a>
 * @version $Revision: 1.4 $ $Date: 2001-03-01 16:24:22 $
 */

public class HttpServletResponseFacade implements HttpServletResponse {

  public boolean hasRedirectedOrOutputted = false;
  protected final HttpServletResponse r;

  public HttpServletResponseFacade (HttpServletResponse response) {
    r = response;
  }

  public void setContentLength(int len) {
    r.setContentLength (len);
  }

  public void setContentType(String type) {
    r.setContentType (type);
  }

  public ServletOutputStream getOutputStream() throws IOException {
    this.hasRedirectedOrOutputted = true;
    return r.getOutputStream ();
  }

  public PrintWriter getWriter () throws IOException {
    this.hasRedirectedOrOutputted = true;
    return r.getWriter ();
  }

  public String getCharacterEncoding() {
    return r.getCharacterEncoding ();
  }

  public void addCookie(Cookie cookie) {
    r.addCookie (cookie);
  }

  public boolean containsHeader(String name) {
    return r.containsHeader (name);
  }

  public void setStatus(int sc) {
    r.setStatus (sc);
  }

  public void setHeader(String name, String value) {
    r.setHeader (name, value);
  }

  public void setIntHeader(String name, int value) {
    r.setIntHeader (name, value);
  }

  public void setDateHeader(String name, long date) {
    r.setDateHeader (name, date);
  }

  public void sendError(int sc, String msg) throws IOException {
    this.hasRedirectedOrOutputted = true;
    r.sendError (sc, msg);
  }

  public void sendError(int sc) throws IOException {
    this.hasRedirectedOrOutputted = true;
    r.sendError (sc);
  }

  public void sendRedirect(String location) throws IOException {
    r.sendRedirect (location);
    //this breaks too much code
    //throw new RedirectException ();
    this.hasRedirectedOrOutputted = true;
  }

  public String encodeURL (String url) {
    return r.encodeURL (url);
  }

  public String encodeRedirectURL (String url) {
    return r.encodeRedirectURL (url);
  }

  public void setBufferSize(int size) {
    r.setBufferSize (size);
  }

  public int getBufferSize() {
    return r.getBufferSize ();
  }

  public void flushBuffer() throws IOException {
    r.flushBuffer ();
  }

  public boolean isCommitted() {
    return r.isCommitted ();
  }

  public void reset() {
    r.reset ();
  }

  public void setLocale(Locale locale) {
    r.setLocale (locale);
  }

  public Locale getLocale() {
    return r.getLocale ();
  }

  public void addDateHeader(String name, long date) {
    r.addDateHeader (name, date);
  }

  public void addHeader(String name, String value) {
    r.addHeader (name, value);
  }

  public void addIntHeader(String name, int value) {
    r.addIntHeader (name, value);
  }

  /** @deprecated */
  public void setStatus(int sc, String sm) {
    r.setStatus (sc, sm);
  }

  /** @deprecated */
  public String encodeUrl (String url) {
    return r.encodeUrl (url);
  }

  /** @deprecated */
  public String encodeRedirectUrl (String url) {
    return r.encodeRedirectUrl (url);
  }
}
