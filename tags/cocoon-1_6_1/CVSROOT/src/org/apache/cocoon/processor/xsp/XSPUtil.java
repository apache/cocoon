/*-- $Id: XSPUtil.java,v 1.10 2000-01-15 11:19:25 ricardo Exp $ -- 

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
 
 package org.apache.cocoon.processor.xsp;

import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;
import org.w3c.dom.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.cocoon.Utils;

/**
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version $Revision: 1.10 $ $Date: 2000-01-15 11:19:25 $
 */
public class XSPUtil {
  public static String pathComponent(String filename) {
    int i = filename.lastIndexOf(File.separator);
    return (i >= 0) ? filename.substring(0, i) : filename;
  }

  public static String fileComponent(String filename) {
    int i = filename.lastIndexOf(File.separator);
    return (i >= 0) ? filename.substring(i + 1) : filename;
  }

  public static String baseName(String filename) {
    return baseName(filename, ".");
  }

  public static String baseName(String filename, String suffix) {
    int lastDot = filename.lastIndexOf(suffix);

    if (lastDot >= 0) {
      filename = filename.substring(0, lastDot);
    }

    return filename;
  }

  public static String normalizedBaseName(String filename) {
    filename = baseName(filename);
    String[] path = split(filename, File.separator);
    int start = (path[0].length() == 0) ? 1 : 0;

    StringBuffer buffer = new StringBuffer();
    for (int i = start; i < path.length; i++) {
      if (i > start) {
        buffer.append(File.separator);
      }

      buffer.append('_');
      char[] chars = path[i].toCharArray();

      for (int j = 0; j < chars.length; j++) {
        if (isAlphaNumeric(chars[j])) {
          buffer.append(chars[j]);
        } else {
          buffer.append('_');
        }
      }
    }

    return buffer.toString();
  }

  public static String relativeFilename(
    String filename,
    HttpServletRequest request,
    ServletContext context
  ) throws IOException
  {
    File file = new File(filename);

    if (file.isAbsolute()) {
      return filename;
    }

    return
      (
        new File(
          new File(
            Utils.getBasename(request, context)
          ).getParent(),
          filename
        )
      ).getCanonicalPath();
  }

  public static String relativeFilename(
    String filename,
    HttpServletRequest request
  ) throws IOException
  {
    return relativeFilename(filename, request, null);
  }

  public static boolean isAlphaNumeric(char c) {
    return c == '_' ||
           (c >= 'a' && c <= 'z') ||
           (c >= 'A' && c <= 'Z') ||
           (c >= '0' && c <= '9');
  }

  public static String[] split(String line) {
    return split(line, " \t\r\n");
  }

  public static String[] split(String line, String delimiter) {
    StringTokenizer tokenizer = new StringTokenizer(line, delimiter);
    int tokenCount = tokenizer.countTokens();
    String[] result = new String[tokenCount];

    for (int i = 0; i < tokenCount; i++)
    {
      result[i] = tokenizer.nextToken();
    }

    return result;
  }

  public static Node cloneNode(Node node, Document factory) {
    switch (node.getNodeType()) {
      case Node.CDATA_SECTION_NODE: {
        return factory.createCDATASection(node.getNodeValue());
      }
      case Node.COMMENT_NODE: {
      }
      case Node.ENTITY_REFERENCE_NODE: {
        return factory.createEntityReference(node.getNodeValue());
      }
      case Node.ELEMENT_NODE: {
        Element input = (Element) node;
        Element output = factory.createElement(input.getTagName());

        NamedNodeMap attributes = input.getAttributes();
        int attrCount = attributes.getLength();
        for (int i = 0; i < attrCount; i++) {
          Attr attr = (Attr) attributes.item(i);
          output.setAttribute(attr.getName(), attr.getValue());
        }

        NodeList nodeList = input.getChildNodes();
        int childCount = nodeList.getLength();
        for (int i = 0; i < childCount; i++) {
          output.appendChild(cloneNode(nodeList.item(i), factory));
        }

        return output;
      }
      case Node.PROCESSING_INSTRUCTION_NODE: {
        ProcessingInstruction pi = (ProcessingInstruction) node;
        return factory.createProcessingInstruction(pi.getTarget(), pi.getData());
      }
      case Node.TEXT_NODE: {
        return factory.createTextNode(node.getNodeValue());
      }
      default:
        return null;
    }
  }

  public static String toMarkup(Node node) {
    StringBuffer buffer = new StringBuffer();
    doMarkup(node, buffer);
    return buffer.toString();
  }

  protected static void doMarkup(Node node, StringBuffer buffer) {
    switch (node.getNodeType()) {
      case Node.CDATA_SECTION_NODE:
        buffer.append("<![CDATA[\n");
        buffer.append(node.getNodeValue());
        buffer.append("]]>\n");
        break;
      case Node.DOCUMENT_NODE:
      case Node.DOCUMENT_FRAGMENT_NODE: {
        NodeList nodeList = node.getChildNodes();
        int childCount = nodeList.getLength();

        for (int i = 0; i < childCount; i++) {
          doMarkup(nodeList.item(i), buffer);
        }

        break;
      }
      case Node.ELEMENT_NODE: {
        Element element = (Element) node;

        buffer.append("<" + element.getTagName());

        NamedNodeMap attributes = element.getAttributes();
        int attributeCount = attributes.getLength();

        for (int i = 0; i < attributeCount; i++) {
          Attr attribute = (Attr) attributes.item(i);

          buffer.append(" ");
          buffer.append(attribute.getName());
          buffer.append("=\"");
          buffer.append(attribute.getValue());
          buffer.append("\"");
        }

        NodeList nodeList = element.getChildNodes();
        int childCount = nodeList.getLength();

        if (childCount == 0) {
          buffer.append("/>\n");
        } else {
          buffer.append(">");
          for (int i = 0; i < childCount; i++) {
            doMarkup(nodeList.item(i), buffer);
          }

          buffer.append("</");
          buffer.append(element.getTagName());
          buffer.append(">");
        }

        break;
      }
      case Node.COMMENT_NODE:
        buffer.append("<!-- ");
        buffer.append(node.getNodeValue());
        buffer.append(" -->\n");
        break;
      case Node.PROCESSING_INSTRUCTION_NODE:
        ProcessingInstruction pi = (ProcessingInstruction) node;

        buffer.append("<?");
        buffer.append(pi.getTarget());
        buffer.append(" ");
        buffer.append(pi.getData());
        buffer.append("?>\n");
        break;
      case Node.TEXT_NODE:
        buffer.append(encodeMarkup(node.getNodeValue()));
        // buffer.append("\n");
        break;
      default:
        break;
    }
  }

  public static String encodeMarkup(String string) {
    char[] array = string.toCharArray();
    StringBuffer buffer = new StringBuffer();

    for (int i = 0; i < array.length; i++) {
      switch (array[i]) {
        case '<':
          buffer.append("&lt;");
          break;
        case'>':
          buffer.append("&gt;");
          break;
        case '&':
          buffer.append("&amp;");
          break;
        default:
          buffer.append(array[i]);
          break;
      }
    }

    return buffer.toString();
  }

  public static String formEncode(String text) throws Exception {
    char[] c = text.toCharArray();
    StringBuffer buffer = new StringBuffer();

    for (int i = 0; i < c.length; i++) {
      if (isAlphaNumeric(c[i])) {
        buffer.append(c[i]);
      } else if (c[i] == ' ') {
        buffer.append('+');
      } else {
        buffer.append('%');
        String hex = Integer.toHexString((byte) c[i]).toUpperCase();

        if (hex.length() < 2) {
          buffer.append('0');
        }

        buffer.append(hex);
      }
    }

    return buffer.toString();
  }

  // Shameless, ain't it?
  public static String formDecode(String s) throws Exception {
    StringBuffer sb = new StringBuffer();
    for(int i=0; i<s.length(); i++) {
      char c = s.charAt(i);
      switch (c) {
        case '+':
          sb.append(' ');
          break;
        case '%':
          try {
            sb.append((char)Integer.parseInt(
            s.substring(i+1,i+3),16));
          }
          catch (NumberFormatException e) {
            throw new IllegalArgumentException();
          }
          i += 2;
          break;
        default:
          sb.append(c);
          break;
      }
    }
    // Undo conversion to external encoding
    String result = sb.toString();
    byte[] inputBytes = result.getBytes("8859_1");
    return new String(inputBytes);
  }

  /* Library Utility Methods */
  // Inclusion
  public static String getURLContents(String url) throws IOException {
    return getContents(new URL(url).openStream());
  }

  public static String getFileContents(String filename) throws IOException {
    return getContents(new FileReader(filename));
  }

  public static String getContents(InputStream in) throws IOException {
    return getContents(new InputStreamReader(in));
  }

  public static String getContents(Reader reader) throws IOException {
    int len;
    char[] chr = new char[4096];
    StringBuffer buffer = new StringBuffer();
    while ((len = reader.read(chr)) > 0) {
      buffer.append(chr, 0, len);
    }
    return buffer.toString();
  }

  // Date
  public static String formatDate(Date date, String pattern) {
    if (pattern == null || pattern.length() == 0) {
      pattern = "yyyy/MM/dd hh:mm:ss aa";
    }
    try {
      return (new SimpleDateFormat(pattern)).format(date);
    } catch (Exception e) {
      return date.toString();
    }
  }

  // Counters
  private static int count = 0;

  public static synchronized int getCount() {
    return ++count;
  }

  public static synchronized int getSessionCount(HttpSession session) {
    Integer integer = (Integer) session.getValue("util.counter");
    if (integer == null) {
      integer = new Integer(0);
    }
    int cnt = integer.intValue() + 1;
    session.putValue("util.counter", new Integer(cnt));
    return cnt;
  }
}
