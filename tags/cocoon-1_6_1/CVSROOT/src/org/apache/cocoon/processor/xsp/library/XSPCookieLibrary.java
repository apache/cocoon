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

import org.w3c.dom.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class XSPCookieLibrary {
  // Cookie
  public static Element getCookie(Cookie cookie, Document document) {
    Element property = null;
    Element element = document.createElement("cookie:cookie");

    String comment = cookie.getComment();
    if (comment != null) {
      property = document.createElement("cookie:comment");
      property.appendChild(document.createTextNode(comment));
      element.appendChild(property);
    }

    property = document.createElement("cookie:domain");
    property.appendChild(document.createTextNode(cookie.getDomain()));
    element.appendChild(property);

    property = document.createElement("cookie:max-age");
    property.appendChild(document.createTextNode(String.valueOf(cookie.getMaxAge())));
    element.appendChild(property);

    property = document.createElement("cookie:name");
    property.appendChild(document.createTextNode(cookie.getName()));
    element.appendChild(property);

    property = document.createElement("cookie:path");
    property.appendChild(document.createTextNode(cookie.getPath()));
    element.appendChild(property);

    property = document.createElement("cookie:secure");
    property.appendChild(document.createTextNode(String.valueOf(cookie.getSecure())));
    element.appendChild(property);

    property = document.createElement("cookie:value");
    property.appendChild(document.createTextNode(cookie.getValue()));
    element.appendChild(property);

    property = document.createElement("cookie:version");
    property.appendChild(document.createTextNode(String.valueOf(cookie.getVersion())));
    element.appendChild(property);


    return element;
  }

  public static Element getComment(Cookie cookie, Document document) {
    Element element = document.createElement("cookie:comment");
    element.appendChild(document.createTextNode(cookie.getComment()));
    return element;
  }

  public static Element getDomain(Cookie cookie, Document document) {
    Element element = document.createElement("cookie:domain");
    element.appendChild(document.createTextNode(cookie.getDomain()));
    return element;
  }

  public static Element getMaxAge(Cookie cookie, Document document) {
    Element element = document.createElement("cookie:max-age");
    element.appendChild(document.createTextNode(String.valueOf(cookie.getMaxAge())));
    return element;
  }

  public static Element getName(Cookie cookie, Document document) {
    Element element = document.createElement("cookie:name");
    element.appendChild(document.createTextNode(cookie.getName()));
    return element;
  }

  public static Element getPath(Cookie cookie, Document document) {
    Element element = document.createElement("cookie:path");
    element.appendChild(document.createTextNode(cookie.getPath()));
    return element;
  }

  public static Element getSecure(Cookie cookie, Document document) {
    Element element = document.createElement("cookie:secure");
    element.appendChild(document.createTextNode(String.valueOf(cookie.getSecure())));
    return element;
  }

  public static Element getValue(Cookie cookie, Document document) {
    Element element = document.createElement("cookie:secure");
    element.appendChild(document.createTextNode(cookie.getValue()));
    return element;
  }

  public static Element getVersion(Cookie cookie, Document document) {
    Element element = document.createElement("cookie:version");
    element.appendChild(document.createTextNode(String.valueOf(cookie.getVersion())));
    return element;
  }
}
