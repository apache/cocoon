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
import org.apache.cocoon.framework.XObject;

public class XSPSessionLibrary {
  // HttpSession
  // JSDK 2.1 version: deprecated "name"
  public static Object getAttribute(HttpSession session, String name) {
    if (name == null || name.length() == 0) {
      return null;
    }

    return session.getValue(name);
  }

  // JSDK 2.2 version: "attribute" instead of 'name"
  /*
  public static Object getAttribute(HttpSession session, String name) {
    if (name == null || name.length() == 0) {
      return null;
    }

    return session.getAttribute(name);
  }
  */

  public static Element getAttribute(HttpSession session, String name, Document document) {
    Object value = getAttribute(session, name);

    if (value == null) {
      return null;
    }

    Element element = document.createElement("session:attribute");

    element.setAttribute("name", name);

    if (value instanceof XObject) {
      DocumentFragment fragment = document.createDocumentFragment();
      ((XObject) value).toDOM(fragment);
      element.appendChild(fragment);
    } else {
      element.appendChild(document.createTextNode(value.toString()));
    }

    return element;
  }

  // JSDK 2.1 version: deprecated "name"
  public static String[] getAttributeNames(HttpSession session) {
    String[] attributeNames = session.getValueNames();
    Arrays.sort(attributeNames); // Since Java2

    return attributeNames;
  }

  // JSDK 2.2 version: "attribute" instead of "name"
  /*
  public static String[] getAttributeNames(HttpSession session) {
    Vector v = new Vector();
    Enumeration e = session.getAttributeNames();

    while (e.hasMoreElements()) {
      v.addElement(session.getAttribute((String) e.nextElement()));
    }

    String[] attributeNames = new String[v.size()];
    v.copyInto(attributeNames);
    Arrays.sort(attributeNames); // Since Java2

    return attributeNames;
  }
  */


  public static Element getAttributeNames(HttpSession session, Document document) {
    String[] attributeNames = getAttributeNames(session);
    Element element = document.createElement("session:attribute-names");

    for (int i = 0; i < attributeNames.length; i++) {
      Element nameElement = document.createElement("session:attribute-name");
      nameElement.appendChild(document.createTextNode(attributeNames[i]));
      element.appendChild(nameElement);
    }

    return element;
  }

  public Element getCreationTime(HttpSession session, Document document) {
    Element element = document.createElement("session:creation-time");
    element.appendChild(document.createTextNode(String.valueOf(session.getCreationTime())));
    return element;
  }

  public Element getId(HttpSession session, Document document) {
    Element element = document.createElement("session:id");
    element.appendChild(document.createTextNode(session.getId()));
    return element;
  }

  public Element getLastAccessedTime(HttpSession session, Document document) {
    Element element = document.createElement("session:last-accessed-time");
    element.appendChild(document.createTextNode(String.valueOf(session.getLastAccessedTime())));
    return element;
  }

  public Element getMaxInactiveInterval(HttpSession session, Document document) {
    Element element = document.createElement("session:max-inactive-interval");
    element.appendChild(document.createTextNode(String.valueOf(session.getMaxInactiveInterval())));
    return element;
  }

  public Element isNew(HttpSession session, Document document) {
    Element element = document.createElement("session:is-new");
    element.appendChild(document.createTextNode(String.valueOf(session.isNew())));
    return element;
  }
}
