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

public class XSPGlobalLibrary {
  // XSPGlobal
  public static Object getAttribute(XSPGlobal global, String name) {
    if (name == null || name.length() == 0) {
      return null;
    }

    return global.getAttribute(name);
  }

  public static Element getAttribute(XSPGlobal global, String name, Document document) {
    Object value = getAttribute(global, name);

    if (value == null) {
      return null;
    }

    Element element = document.createElement("global:attribute");

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

  public static String[] getAttributeNames(XSPGlobal global) {
    Vector v = new Vector();
    Enumeration e = global.getAttributeNames();

    while (e.hasMoreElements()) {
      v.addElement(global.getAttribute((String) e.nextElement()));
    }

    String[] attributeNames = new String[v.size()];
    v.copyInto(attributeNames);
    Arrays.sort(attributeNames); // Since Java2

    return attributeNames;
  }


  public static Element getAttributeNames(XSPGlobal global, Document document) {
    String[] attributeNames = getAttributeNames(global);
    Element element = document.createElement("global:attribute-names");

    for (int i = 0; i < attributeNames.length; i++) {
      Element nameElement = document.createElement("global:attribute-name");
      nameElement.appendChild(document.createTextNode(attributeNames[i]));
      element.appendChild(nameElement);
    }

    return element;
  }
}
