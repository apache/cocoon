/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================
 
    Copyright (C) 1999 The Apache Software Foundation. All rights reserved.
 
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

public class XSPResponseLibrary {
  // ServletResponse
  public static Element getCharacterEncoding(HttpServletResponse response, Document document) {
    Element element = document.createElement("response:get-character-encoding");
    element.appendChild(document.createTextNode(response.getCharacterEncoding()));
    return element;
  }

  public static Element getLocale(HttpServletResponse response, Document document) {
    Element property = null;
    Locale locale = response.getLocale();
    Element element = document.createElement("response:locale");

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

  // HttpServletResponse
  public static void addDateHeader(HttpServletResponse response, String name, long date) {
    response.addDateHeader(name, date);
  }

  public static void addDateHeader(HttpServletResponse response, String name, Date date) {
    response.addDateHeader(name, date.getTime());
  }

  public static void addDateHeader(HttpServletResponse response, String name, String date) throws ParseException {
    addDateHeader(response, name, date, DateFormat.getDateInstance());
  }

  public static void addDateHeader(HttpServletResponse response, String name, String date, String format) throws ParseException {
    addDateHeader(response, name, date, new SimpleDateFormat(format));
  }

  public static void addDateHeader(HttpServletResponse response, String name, String date, DateFormat format) throws ParseException {
    response.addDateHeader(name, format.parse(date).getTime());
  }

  public static Element containsHeader(HttpServletResponse response, String name, Document document) {
    Element element = document.createElement("response:contains-header");
    element.appendChild(document.createTextNode(String.valueOf(response.containsHeader(name))));
    return element;
  }

  public static Element encodeRedirectURL(HttpServletResponse response, String url, Document document) {
    Element element = document.createElement("response:encode-redirect-url");
    element.appendChild(document.createTextNode(response.encodeRedirectURL(url)));
    return element;
  }

  public static Element encodeURL(HttpServletResponse response, String url, Document document) {
    Element element = document.createElement("response:encode-url");
    element.appendChild(document.createTextNode(response.encodeURL(url)));
    return element;
  }

  public static void setDateHeader(HttpServletResponse response, String name, long date) {
    response.setDateHeader(name, date);
  }

  public static void setDateHeader(HttpServletResponse response, String name, Date date) {
    response.setDateHeader(name, date.getTime());
  }

  public static void setDateHeader(HttpServletResponse response, String name, String date) throws ParseException {
    setDateHeader(response, name, date, DateFormat.getDateInstance());
  }

  public static void setDateHeader(HttpServletResponse response, String name, String date, String format) throws ParseException {
    setDateHeader(response, name, date, new SimpleDateFormat(format));
  }

  public static void setDateHeader(HttpServletResponse response, String name, String date, DateFormat format) throws ParseException {
    response.setDateHeader(name, format.parse(date).getTime());
  }
}
