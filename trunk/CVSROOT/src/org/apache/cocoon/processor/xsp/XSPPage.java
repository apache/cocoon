/*-- $Id: XSPPage.java,v 1.10 2001-01-17 20:45:15 greenrd Exp $ -- 

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

package org.apache.cocoon.processor.xsp;

import java.io.*;
import java.util.*;
import org.w3c.dom.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.cocoon.parser.*;
import org.apache.cocoon.producer.*;
import org.apache.cocoon.framework.*;
import org.apache.cocoon.xml.XMLFragment;

/**
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version $Revision: 1.10 $ $Date: 2001-01-17 20:45:15 $
 */
public abstract class XSPPage extends AbstractProducer implements Cacheable {
  protected Parser xspParser;
  protected ServletContext servletContext;
  protected XSPGlobal global;

  public void init(Director director) {
    super.init(director);
    this.xspParser = (Parser) this.director.getActor("parser");
  }

  public void init(Dictionary parameters) {
    this.init((Director) parameters.get("director"));
    this.global = (XSPGlobal) parameters.get("global");
    this.servletContext = (ServletContext) this.director.getActor("context");
  }

  public final Document getDocument(HttpServletRequest request)
    throws Exception
  {
    return this.getDocument(request, null);
  }

  public Document getDocument(
    HttpServletRequest request,
    HttpServletResponse response
  ) throws Exception
  {
    Document document = this.xspParser.createEmptyDocument();
    this.populateDocument(request, response, document);

    return document;
  }

  public abstract void populateDocument(
    HttpServletRequest request,
    HttpServletResponse response,
    Document document
  ) throws Exception;

  /**
   * For backward repository compatibility when upgrading to Cocoon 1.8.1
   * from an earlier version.
   */
  public boolean isCacheable (HttpServletRequest request) {
    return false;
  }

  // <xsp:expr> methods
  protected Text xspExpr(char v, Document factory) {
    return factory.createTextNode(String.valueOf(v));
  }

  protected Text xspExpr(byte v, Document factory) {
    return factory.createTextNode(String.valueOf(v));
  }

  protected Text xspExpr(boolean v, Document factory) {
    return factory.createTextNode(String.valueOf(v));
  }

  protected Text xspExpr(int v, Document factory) {
    return factory.createTextNode(String.valueOf(v));
  }

  protected Text xspExpr(long v, Document factory) {
    return factory.createTextNode(String.valueOf(v));
  }

  protected Text xspExpr(float v, Document factory) {
    return factory.createTextNode(String.valueOf(v));
  }

  protected Text xspExpr(double v, Document factory) {
    return factory.createTextNode(String.valueOf(v));
  }

  protected Node xspExpr(Object v, Document factory) 
  {
    // Null? blank text node
    if (v == null) {
      return factory.createTextNode("");
    }

    // Already a node? Use it verbatim
    if (v instanceof Node) {
      Node node = (Node) v;
      // Don't bother cloning node unless necessary
      if (node.getOwnerDocument () == factory) {
        return node;
      }
      else {
        return XSPUtil.cloneNode(node, factory);
      }
    }

    // Array: recurse over each element
    if (v.getClass().isArray()) {
      Object[] elements = (Object[]) v;
      DocumentFragment fragment = factory.createDocumentFragment();

      for (int i = 0; i < elements.length; i++) {
        fragment.appendChild(xspExpr(elements[i], factory));
      }

      return fragment;
    }

    // Convertible to DOM
    if (v instanceof XMLFragment) {
      DocumentFragment fragment = factory.createDocumentFragment ();
      try {
        ((XMLFragment) v).toDOM(fragment);
      }
      catch (Exception ex) {
        throw new RuntimeException ("Error building DOM: " + ex);
      }
      return fragment;
    }
 
    // Deprecated version of XMLFragment (see prev)
    if (v instanceof XObject) {
      // XXX: Should use logger instead
      System.err.println ("Warning: XObject is deprecated and WILL NOT WORK in Cocoon 2. Use XMLFragment instead.");
      DocumentFragment fragment = factory.createDocumentFragment ();
      ((XObject) v).toDOM(fragment);
      return fragment;
    }

    // Give up: hope it's a string or has a meaningful string representation
    return factory.createTextNode(String.valueOf(v));
  }

  // Producer methods
  public Reader getStream(HttpServletRequest request) throws IOException {
    return null;
  }

  public String getPath(HttpServletRequest request) {
    return null;
  }
}
