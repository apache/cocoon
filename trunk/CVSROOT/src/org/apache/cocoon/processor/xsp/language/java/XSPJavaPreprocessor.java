/*-- $Id: XSPJavaPreprocessor.java,v 1.6 2001-01-19 02:46:06 greenrd Exp $ -- 

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================
 
 Copyright (C) @year@ The Apache Software Foundation. All rights reserved.
 
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

package org.apache.cocoon.processor.xsp.language.java;

import java.io.*;
import java.util.*;
import org.w3c.dom.*;

import org.apache.cocoon.processor.xsp.*;
import org.apache.cocoon.processor.xsp.language.*;

/**
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version $Revision: 1.6 $ $Date: 2001-01-19 02:46:06 $
 */
public class XSPJavaPreprocessor implements XSPPreprocessor {
  protected static XSPJavaProcessor javaProcessor = new XSPJavaProcessor();

  public Document preprocess(Document document, Dictionary parameters)
    throws Exception
  {
    Element root = document.getDocumentElement();

    String filename = (String) parameters.get("filename");
    filename = (new File(filename)).getCanonicalPath();
    String packageName = this.javaProcessor.packageName(filename);
    String className = this.javaProcessor.className(filename);
    
    root.setAttribute("name", className);
    root.setAttribute("package", packageName);

    this.process(document);

    return document;
  }

  protected void process(Node node) {
    switch (node.getNodeType()) {
      case Node.PROCESSING_INSTRUCTION_NODE:
        ProcessingInstruction pi = (ProcessingInstruction) node;
        pi.setData(this.javaProcessor.stringEncode(pi.getData()));
        break;
      case Node.TEXT_NODE:
        Element parent = (Element) node.getParentNode();

        String tagName = parent.getTagName();

        if (
          tagName.equals("xsp:expr") ||
          tagName.equals("xsp:logic") ||
          tagName.equals("xsp:structure") ||
          tagName.equals("xsp:include")
        ) {
          return;
        }

        String value = this.javaProcessor.stringEncode(node.getNodeValue());
        Text textNode = node.getOwnerDocument().createTextNode(value);

        Element textElement = node.getOwnerDocument().createElementNS
          ("http://www.apache.org/1999/XSP/Core", "xsp:text");

        textElement.appendChild(textNode);
        parent.replaceChild(textElement, node);

        break;
      case Node.ELEMENT_NODE:
        ((Element) node).normalize();
        // Fall through
      default:
        NodeList childList = node.getChildNodes();
        int childCount = childList.getLength();

        for (int i = 0; i < childCount; i++) {
          this.process(childList.item(i));
        }

        break;
    }
  }
}
