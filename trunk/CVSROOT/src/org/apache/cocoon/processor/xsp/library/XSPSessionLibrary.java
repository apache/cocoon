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

    if (value instanceof DOMConvertible) {
      element.appendChild(((DOMConvertible) value).toXML(document));
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
