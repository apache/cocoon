/*

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

package org.apache.cocoon.processor.xsp.library;

import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;
import java.security.*;

import org.w3c.dom.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.cocoon.processor.xsp.*;
import org.apache.cocoon.framework.XObject;

public class XSPRequestLibrary {
  // ServletRequest
  public static Element getAttribute(HttpServletRequest request, String name, Document document) {
    Object value = request.getAttribute(name);
    Element element = document.createElement("request:attribute");
    element.setAttribute("name", name);

    if (value != null) {
      if (value instanceof XObject) {
        DocumentFragment fragment = document.createDocumentFragment();
        ((XObject) value).toDOM(fragment);
        element.appendChild(fragment);
      } else {
        element.appendChild(document.createTextNode(value.toString()));
      }
    }

    return element;
  }

  public static String[] getAttributeNames(HttpServletRequest request) {
    Vector v = new Vector();
    Enumeration e = request.getAttributeNames();

    while (e.hasMoreElements()) {
      v.addElement(request.getAttribute((String) e.nextElement()));
    }

    String[] attributeNames = new String[v.size()];
    v.copyInto(attributeNames);

    return attributeNames;
  }


  public static Element getAttributeNames(HttpServletRequest request, Document document) {
    String[] attributeNames = getAttributeNames(request);
    Element element = document.createElement("request:attribute-names");

    for (int i = 0; i < attributeNames.length; i++) {
      Element nameElement = document.createElement("request:attribute-name");
      nameElement.appendChild(document.createTextNode(attributeNames[i]));
      element.appendChild(nameElement);
    }

    return element;
  }

  public static Element getCharacterEncoding(HttpServletRequest request, Document document) {
    Element element = document.createElement("request:character-encoding");
    element.appendChild(document.createTextNode(request.getCharacterEncoding()));
    return element;
  }

  public static Element getContentLength(HttpServletRequest request, Document document) {
    Element element = document.createElement("request:content-length");
    element.appendChild(document.createTextNode(String.valueOf(request.getContentLength())));
    return element;
  }

  public static Element getContentType(HttpServletRequest request, Document document) {
    Element element = document.createElement("request:content-type");
    element.appendChild(document.createTextNode(request.getContentType()));
    return element;
  }

  public static Element getLocale(HttpServletRequest request, Document document) {
    Element property = null;
    Locale locale = request.getLocale();
    Element element = document.createElement("request:locale");

    property = document.createElement("locale:language");
    property.appendChild(document.createTextNode(locale.getLanguage()));
    element.appendChild(property);

    property = document.createElement("locale:country");
    property.appendChild(document.createTextNode(locale.getCountry()));
    element.appendChild(property);

    property = document.createElement("locale:variant");
    property.appendChild(document.createTextNode(locale.getVariant()));
    element.appendChild(property);

    return element;
  }

  public static Locale[] getLocales(HttpServletRequest request) {
    Vector v = new Vector();
    Enumeration e = request.getLocales();

    while (e.hasMoreElements()) {
      v.addElement(e.nextElement());
    }

    Locale[] locales = new Locale[v.size()];
    v.copyInto(locales);

    return locales;
  }

  public static Element getLocales(HttpServletRequest request, Document document) {
    Enumeration e = request.getLocales();
    Element list = document.createElement("request:locales");

    while (e.hasMoreElements()) {
      list.appendChild(getLocale(request, document));
    }

    return list;
  }

  public static String[] getParameterNames(HttpServletRequest request) {
    Vector v = new Vector();
    Enumeration e = request.getParameterNames();

    while (e.hasMoreElements()) {
      v.addElement(e.nextElement());
    }

    String[] attributeNames = new String[v.size()];
    v.copyInto(attributeNames);

    return attributeNames;
  }

  public static Element getParameterNames(HttpServletRequest request, Document document) {
    String[] attributeNames = getParameterNames(request);
    Element element = document.createElement("request:parameter-names");

    for (int i = 0; i < attributeNames.length; i++) {
      Element nameElement = document.createElement("request:parameter-name");
      nameElement.appendChild(document.createTextNode(attributeNames[i]));
      element.appendChild(nameElement);
    }

    return element;
  }

  public static Element getParameter(HttpServletRequest request, String name, String defaultValue, Document document) {
    String value = getParameter(request, name, defaultValue);

    Element element = document.createElement("request:parameter");
    element.setAttribute("name", name);

    if (value != null) {
      element.appendChild(document.createTextNode(value));
    }

    return element;
  }

  public static String getParameter(HttpServletRequest request, String name, String defaultValue) {
    String value = request.getParameter(name);
    if (value == null) {
      value = defaultValue;
    }

    return value;
  }

  public static Element getParameterValues(HttpServletRequest request, String name, Document document) {
    String[] values = request.getParameterValues(name);
    Element element = document.createElement("request:parameter-values");
    element.setAttribute("name", name);

    if (values != null) {
      for (int i = 0; i < values.length; i++) {
        Element valueElement = document.createElement("request:parameter-value");
        valueElement.appendChild(document.createTextNode(values[i]));
        element.appendChild(valueElement);
      }
    }

    return element;
  }

  public static Element getProtocol(HttpServletRequest request, Document document) {
    Element element = document.createElement("request:protocol");
    element.appendChild(document.createTextNode(request.getProtocol()));
    return element;
  }


  public static Element getRemoteAddr(HttpServletRequest request, Document document) {
    Element element = document.createElement("request:remote-address");
    element.appendChild(document.createTextNode(request.getRemoteAddr()));
    return element;
  }

  public static Element getRemoteHost(HttpServletRequest request, Document document) {
    Element element = document.createElement("request:remote-host");
    element.appendChild(document.createTextNode(request.getRemoteHost()));
    return element;
  }

  public static Element getScheme(HttpServletRequest request, Document document) {
    Element element = document.createElement("request:scheme");
    element.appendChild(document.createTextNode(request.getScheme()));
    return element;
  }

  public static Element getServerName(HttpServletRequest request, Document document) {
    Element element = document.createElement("request:server-name");
    element.appendChild(document.createTextNode(String.valueOf(request.getServerName())));
    return element;
  }

  public static Element getServerPort(HttpServletRequest request, Document document) {
    Element element = document.createElement("request:server-port");
    element.appendChild(document.createTextNode(String.valueOf(request.getServerPort())));
    return element;
  }

  public static Element isSecure(HttpServletRequest request, Document document) {
    Element element = document.createElement("request:is-secure");
    element.appendChild(document.createTextNode(String.valueOf(request.isSecure())));
    return element;
  }

  // HttpServletRequest
  public static Element getAuthType(HttpServletRequest request, Document document) {
    Element element = document.createElement("request:auth-type");
    element.appendChild(document.createTextNode(request.getAuthType()));
    return element;
  }

  public static Element getContextPath(HttpServletRequest request, Document document) {
    Element element = document.createElement("request:context-path");
    element.appendChild(document.createTextNode(request.getContextPath()));
    return element;
  }

  public static Element getCookies(HttpServletRequest request, Document document) {
    Cookie[] cookies = request.getCookies();
    Element element = document.createElement("request:cookies");

    if (cookies != null) {
      for (int i = 0; i < cookies.length; i++) {
        element.appendChild(XSPCookieLibrary.getCookie(cookies[i], document));
      }
    }

    return element;
  }

  public static Element getDateHeader(HttpServletRequest request, String name, String format, Document document) {
    if (name == null || name.length() == 0) {
      return null;
    }

    long dateHeader = request.getDateHeader(name);
    Element element = document.createElement("request:date-header");
    element.setAttribute("name", name);

    if (dateHeader != -1) {
      String header = XSPUtil.formatDate(new Date(dateHeader), format.trim());
      element.appendChild(document.createTextNode(header));
    }

    return element;
  }

  public static Element getHeader(HttpServletRequest request, String name, Document document) {
    String value = request.getHeader(name);
    Element element = document.createElement("request:header");
    element.setAttribute("name", name);

    if (value != null) {
      element.appendChild(document.createTextNode(value));
    }

    return element;
  }

  public static String[] getHeaderNames(HttpServletRequest request) {
    Vector v = new Vector();
    Enumeration e = request.getHeaderNames();

    while (e.hasMoreElements()) {
      v.addElement(request.getHeader((String) e.nextElement()));
    }

    String[] headerNames = new String[v.size()];
    v.copyInto(headerNames);

    return headerNames;
  }


  public static Element getHeaderNames(HttpServletRequest request, Document document) {
    String[] headerNames = getHeaderNames(request);
    Element element = document.createElement("request:header-names");

    for (int i = 0; i < headerNames.length; i++) {
      Element nameElement = document.createElement("request:header-name");
      nameElement.appendChild(document.createTextNode(headerNames[i]));
      element.appendChild(nameElement);
    }

    return element;
  }

  public static String[] getHeaders(HttpServletRequest request, String name) {
    Vector v = new Vector();
    Enumeration e = request.getHeaders(name);

    while (e.hasMoreElements()) {
      v.addElement(e.nextElement());
    }

    String[] headers = new String[v.size()];
    v.copyInto(headers);

    return headers;
  }


  public static Element getHeaders(HttpServletRequest request, String name, Document document) {
    String[] headers = getHeaders(request, name);
    Element element = document.createElement("request:headers");

    for (int i = 0; i < headers.length; i++) {
      Element headerElement = document.createElement("request:header");
      headerElement.appendChild(document.createTextNode(headers[i]));
      element.appendChild(headerElement);
    }

    return element;
  }

  public static Element getIntHeader(HttpServletRequest request, String name, Document document) {
    Element element = document.createElement("request:int-header");
    element.appendChild(document.createTextNode(String.valueOf(request.getIntHeader(name))));
    return element;
  }

  public static Element getMethod(HttpServletRequest request, Document document) {
    Element element = document.createElement("request:method");
    element.appendChild(document.createTextNode(request.getMethod()));
    return element;
  }

  public static Element getPathInfo(HttpServletRequest request, Document document) {
    Element element = document.createElement("request:path-info");
    element.appendChild(document.createTextNode(request.getPathInfo()));
    return element;
  }

  public static Element getPathTranslated(HttpServletRequest request, Document document) {
    Element element = document.createElement("request:path-translated");
    element.appendChild(document.createTextNode(request.getPathTranslated()));
    return element;
  }

  public static Element getQueryString(HttpServletRequest request, Document document) {
    Element element = document.createElement("request:query-string");
    element.appendChild(document.createTextNode(request.getQueryString()));
    return element;
  }

  public static Element getRemoteUser(HttpServletRequest request, Document document) {
    Element element = document.createElement("request:remote-user");
    element.appendChild(document.createTextNode(request.getRemoteUser()));
    return element;
  }

  public static Element getRequestedSessionId(HttpServletRequest request, Document document) {
    Element element = document.createElement("request:requested-session-id");
    element.appendChild(document.createTextNode(request.getRequestedSessionId()));
    return element;
  }

  public static Element getRequestURI(HttpServletRequest request, Document document) {
    Element element = document.createElement("request:request-uri");
    element.appendChild(document.createTextNode(request.getRequestURI()));
    return element;
  }

  public static Element getServletPath(HttpServletRequest request, Document document) {
    Element element = document.createElement("request:servlet-path");
    element.appendChild(document.createTextNode(request.getServletPath()));
    return element;
  }

  public static Element getUserPrincipal(HttpServletRequest request, Document document) {
    Principal principal = request.getUserPrincipal();
    Element element = document.createElement("request:user-principal");

    if (principal != null) {
      Element nameElement = document.createElement("principal:name");
      nameElement.appendChild(document.createTextNode(principal.getName()));
      element.appendChild(nameElement);
    }

    return element;
  }

  public static Element isRequestedSessionIdFromCookie(HttpServletRequest request, Document document) {
    Element element = document.createElement("request:is-requested-session-id-from-cookie");
    element.appendChild(document.createTextNode(String.valueOf(request.isRequestedSessionIdFromCookie())));
    return element;
  }

  public static Element isRequestedSessionIdFromURL(HttpServletRequest request, Document document) {
    Element element = document.createElement("request:is-requested-session-id-from-url");
    element.appendChild(document.createTextNode(String.valueOf(request.isRequestedSessionIdFromURL())));
    return element;
  }

  public static Element isRequestedSessionIdValid(HttpServletRequest request, Document document) {
    Element element = document.createElement("request:is-requested-session-id-valid");
    element.appendChild(document.createTextNode(String.valueOf(request.isRequestedSessionIdValid())));
    return element;
  }

  public static Element isUserInRole(HttpServletRequest request, String role, Document document) {
    Element element = document.createElement("request:is-requested-session-id-valid");
    element.appendChild(document.createTextNode(String.valueOf(request.isUserInRole(role))));
    return element;
  }
}
