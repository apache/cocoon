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

public class XSPContextLibrary {
  // ServletContext
  public static Object getAttribute(ServletContext context, String name) {
    if (name == null || name.length() == 0) {
      return null;
    }

    return context.getAttribute(name);
  }

  public static Element getAttribute(ServletContext context, String name, Document document) {
    Object value = getAttribute(context, name);

    if (value == null) {
      return null;
    }

    Element element = document.createElement("context:attribute");

    element.setAttribute("name", name);

    if (value instanceof DOMConvertible) {
      element.appendChild(((DOMConvertible) value).toXML(document));
    } else {
      element.appendChild(document.createTextNode(value.toString()));
    }

    return element;
  }

  public static String[] getAttributeNames(ServletContext context) {
    Vector v = new Vector();
    Enumeration e = context.getAttributeNames();

    while (e.hasMoreElements()) {
      v.addElement(context.getAttribute((String) e.nextElement()));
    }

    String[] attributeNames = new String[v.size()];
    v.copyInto(attributeNames);
    Arrays.sort(attributeNames); // Since Java2

    return attributeNames;
  }


  public static Element getAttributeNames(ServletContext context, Document document) {
    String[] attributeNames = getAttributeNames(context);
    Element element = document.createElement("context:attribute-names");

    for (int i = 0; i < attributeNames.length; i++) {
      Element nameElement = document.createElement("context:attribute-name");
      nameElement.appendChild(document.createTextNode(attributeNames[i]));
      element.appendChild(nameElement);
    }

    return element;
  }

  // Not supported by JSDK versions prior to 2.2
  /*
  public static String getInitParameter(ServletContext context, String name) {
    if (name == null || name.length() == 0) {
      return null;
    }

    return context.getInitParameter(name);
  }

  public static Element getInitParameter(ServletContext context, String name, Document document) {
    String value = getInitParameter(context, name);

    if (value == null) {
      return null;
    }

    Element element = document.createElement("context:init-parameter");

    element.setAttribute("name", name);
    element.appendChild(document.createTextNode(value));

    return element;
  }
  */

  // Not supported by JSDK versions prior to 2.2
  /*
  public static String[] getInitParameterNames(ServletContext context) {
    Vector v = new Vector();
    Enumeration e = context.getInitParameterNames();

    while (e.hasMoreElements()) {
      v.addElement(context.getInitParameter((String) e.nextElement()));
    }

    String[] attributeNames = new String[v.size()];
    v.copyInto(attributeNames);
    Arrays.sort(attributeNames); // Since Java2

    return attributeNames;
  }


  public static Element getInitParameterNames(ServletContext context, Document document) {
    String[] attributeNames = getInitParameterNames(context);
    Element element = document.createElement("context:attribute-names");

    for (int i = 0; i < attributeNames.length; i++) {
      Element nameElement = document.createElement("context:attribute-name");
      nameElement.appendChild(document.createTextNode(attributeNames[i]));
      element.appendChild(nameElement);
    }

    return element;
  }
  */

  public Element getMajorVersion(ServletContext context, Document document) {
    Element element = document.createElement("context:major-version");
    element.appendChild(document.createTextNode(String.valueOf(context.getMajorVersion())));
    return element;
  }

  public Element getMinorVersion(ServletContext context, Document document) {
    Element element = document.createElement("context:minor-version");
    element.appendChild(document.createTextNode(String.valueOf(context.getMinorVersion())));
    return element;
  }

  public Element getMimeType(ServletContext context, String file, Document document) {
    Element element = document.createElement("context:mime-type");
    element.appendChild(document.createTextNode(context.getMimeType(file)));
    return element;
  }

  public Element getRealPath(ServletContext context, String path, Document document) {
    Element element = document.createElement("context:real-path");
    element.appendChild(document.createTextNode(context.getRealPath(path)));
    return element;
  }

  public Element getServerInfo(ServletContext context, Document document) {
    Element element = document.createElement("context:real-server-info");
    element.appendChild(document.createTextNode(context.getServerInfo()));
    return element;
  }
}
