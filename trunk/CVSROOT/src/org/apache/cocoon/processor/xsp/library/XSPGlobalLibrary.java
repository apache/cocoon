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
import org.apache.cocoon.framework.DOMConvertible;

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

    if (value instanceof DOMConvertible) {
      element.appendChild(((DOMConvertible) value).toXML(document));
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
