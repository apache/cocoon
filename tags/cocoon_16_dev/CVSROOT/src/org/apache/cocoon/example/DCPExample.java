package org.apache.cocoon.example;

import java.util.*;
import java.text.*;
import org.w3c.dom.*;
import javax.servlet.http.*;
import org.apache.cocoon.dcp.*;

public class DCPExample extends ServletDCPProcessor {
  private static int count = 0;

  public synchronized int getCount() {
    return ++count;
  } 

  public String getSystemDate(Dictionary parameters) {
    Date now = new Date();
    String formattedDate = now.toString();
    String format = (String) parameters.get("format");

    if (format != null) {
      try {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        formattedDate = dateFormat.format(now);
      } catch (Exception e) { } // Bad format, ignore and return default
    }

    return formattedDate;
  }

  public Element getRequestParameters() {
    Enumeration e = this.request.getParameterNames();

    if (!e.hasMoreElements()) { // No parameters given, remove node from document
      return null;
    }

    Element parameterList = createElement("parameters");

    int count;
    Element parameterValue;
    Element parameterElement;
    for (count = 0; e.hasMoreElements(); count++) {
      String name = (String) e.nextElement();
      String[] values = this.request.getParameterValues(name);

      parameterElement = createElement("parameter");
      parameterElement.setAttribute("name", name);

      for (int i = 0; i < values.length; i++) {
        parameterValue = createElement("parameter-value");
        parameterValue.appendChild(createTextNode(values[i]));

        parameterElement.appendChild(parameterValue);
      }

      parameterList.appendChild(parameterElement);
    }

    return parameterList;
  }
}