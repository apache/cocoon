/*-- $Id: DOMWriter.java,v 1.1 2000-09-16 16:04:30 greenrd Exp $ --

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

package org.apache.tools;

import org.w3c.dom.Document;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Utility class for creating a DOM tree.
 *
 * Use getCurrent() to get the result at the end (make sure to pop() the right
 * number of times!)
 *
 * @author <a href="mailto:greenrd@hotmail.com">Robin Green</a>
 * @version $Revision: 1.1 $ $Date: 2000-09-16 16:04:30 $
 */

public class DOMWriter {

  protected final Document doc;
  protected Node current;

  public DOMWriter (Document doc) {
    this.doc = doc;
    current = doc.createDocumentFragment ();
  }

  public DOMWriter (Document doc, String initElement) {
    this.doc = doc;
    current = doc.createElement (initElement);
  }

  public Element add (String name) throws DOMException {
    Element e = doc.createElement (name);
    appendChild (e);
    return e;
  }

  public Element add (String namespaceURI, String name) throws DOMException {
    Element e = doc.createElementNS (namespaceURI, name);
    appendChild (e);
    return e;
  }

  public void appendChild (Node n) throws DOMException {
    current.appendChild (n);
  }

  public void setAttribute (String name, String value) throws DOMException {
    ((Element) current).setAttribute (name, value);
  }

  public Document getDocument () {
    return doc;
  }

  public void addText (String text) throws DOMException {
    current.appendChild (doc.createTextNode (text));
  }

  public Element push (String elemName) throws DOMException {
    return (Element) (current = add (elemName));
  }

  public Element push (String namespaceURI, String elemName) throws DOMException {
    return (Element) (current = add (namespaceURI, elemName));
  }

  public Node pop () throws DOMException {
    Node old = current;
    current = old.getParentNode ();
    return old;
  }

  public Node pop (String elemName) throws DOMException {
    Element old = (Element) pop ();
    String oldName = old.getTagName ();
    if (!oldName.equals (elemName))
      throw new DOMException
        (DOMException.HIERARCHY_REQUEST_ERR,
         "Expected '" + elemName + "', but node to be popped is '" +
         oldName);
    return old;
  }

  public Element addQuick (String name, String contents) throws DOMException {
    Element e = push (name);
    addText (contents);
    pop ();
    return e;
  }

  public Element addQuick (String namespaceURI, String name, String contents)
  throws DOMException {
    Element e = push (namespaceURI, name);
    addText (contents);
    pop ();
    return e;
  }

  public Node getCurrent () {
    return current;
  }

  public void setCurrent (Node n) {
    current = n;
  }
}