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
  public Element getCharacterEncoding(HttpServletResponse response, Document document) {
    Element element = document.createElement("response:get-character-encoding");
    element.appendChild(document.createTextNode(response.getCharacterEncoding()));
    return element;
  }

  // Not supported by JSDK versions prior to 2.2
  /*
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
  */

  // HttpServletResponse
  // Not supported by JSDK versions prior to 2.2
  /*
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
  */

  public Element containsHeader(HttpServletResponse response, String name, Document document) {
    Element element = document.createElement("response:contains-header");
    element.appendChild(document.createTextNode(String.valueOf(response.containsHeader(name))));
    return element;
  }

  public Element encodeRedirectURL(HttpServletResponse response, String url, Document document) {
    Element element = document.createElement("response:encode-redirect-url");
    element.appendChild(document.createTextNode(response.encodeRedirectURL(url)));
    return element;
  }

  public Element encodeURL(HttpServletResponse response, String url, Document document) {
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
