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

  public Element getComment(Cookie cookie, Document document) {
    Element element = document.createElement("cookie:comment");
    element.appendChild(document.createTextNode(cookie.getComment()));
    return element;
  }

  public Element getDomain(Cookie cookie, Document document) {
    Element element = document.createElement("cookie:domain");
    element.appendChild(document.createTextNode(cookie.getDomain()));
    return element;
  }

  public Element getMaxAge(Cookie cookie, Document document) {
    Element element = document.createElement("cookie:max-age");
    element.appendChild(document.createTextNode(String.valueOf(cookie.getMaxAge())));
    return element;
  }

  public Element getName(Cookie cookie, Document document) {
    Element element = document.createElement("cookie:name");
    element.appendChild(document.createTextNode(cookie.getName()));
    return element;
  }

  public Element getPath(Cookie cookie, Document document) {
    Element element = document.createElement("cookie:path");
    element.appendChild(document.createTextNode(cookie.getPath()));
    return element;
  }

  public Element getSecure(Cookie cookie, Document document) {
    Element element = document.createElement("cookie:secure");
    element.appendChild(document.createTextNode(String.valueOf(cookie.getSecure())));
    return element;
  }

  public Element getValue(Cookie cookie, Document document) {
    Element element = document.createElement("cookie:secure");
    element.appendChild(document.createTextNode(cookie.getValue()));
    return element;
  }

  public Element getVersion(Cookie cookie, Document document) {
    Element element = document.createElement("cookie:version");
    element.appendChild(document.createTextNode(String.valueOf(cookie.getVersion())));
    return element;
  }
}
