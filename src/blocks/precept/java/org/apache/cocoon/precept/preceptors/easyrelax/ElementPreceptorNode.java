/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

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

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
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
package org.apache.cocoon.precept.preceptors.easyrelax;

import org.apache.cocoon.precept.Preceptor;

import org.apache.cocoon.precept.Constraint;


import java.util.*;

/**
 * @author Torsten Curdt <tcurdt@dff.st>
 * @since Feb 22, 2002
 * @version CVS $Id: ElementPreceptorNode.java,v 1.2 2003/03/16 17:49:05 vgritsenko Exp $
 */
public class ElementPreceptorNode extends AbstractPreceptorNode {

    public final static int UNBOUND = -1;


    private Map childs;
    private Map attributes;
//  private List values;
    private int minOcc;
    private int maxOcc;

    public ElementPreceptorNode(Preceptor preceptor, ElementPreceptorNode parent, String name, int minOcc, int maxOcc) {
        this.name = name;
        this.minOcc = minOcc;
        this.maxOcc = maxOcc;
        this.parent = parent;
        this.preceptor = preceptor;
    }


    public AttributePreceptorNode addAttribute(String name, boolean required, List constraints) {
        if (attributes == null) attributes = new HashMap();
        AttributePreceptorNode a = new AttributePreceptorNode(preceptor, this, name, required);
        a.addConstraints(constraints);
        attributes.put(name, a);
        return (a);
    }


    public ElementPreceptorNode addElement(String name, int min, int max, List constraints) {
        if (childs == null) childs = new HashMap();
        ElementPreceptorNode e = new ElementPreceptorNode(preceptor, this, name, min, max);
        e.addConstraints(constraints);
        childs.put(name, e);
        return (e);
    }


    public ElementPreceptorNode getChild(String name) {
        if (childs != null) {
            return ((ElementPreceptorNode) childs.get(name));
        }
        else {
            return (null);
        }
    }


    public Collection getChildElements() {
        if (childs != null) {
            return (childs.values());
        }
        else {
            return (null);
        }
    }


    public AttributePreceptorNode getAttribute(String name) {
        if (attributes != null) {
            return ((AttributePreceptorNode) attributes.get(name));
        }
        else {
            return (null);
        }
    }


    public Collection getAttributes() {
        if (attributes != null) {
            return (attributes.values());
        }
        else {
            return (null);
        }
    }

    public int getMinOcc() {
        return (this.minOcc);
    }

    public int getMaxOcc() {
        return (this.maxOcc);
    }

/*

  public StringBuffer setValue(int i, String value) throws PreceptorViolationException {
    if (values == null) values = new ArrayList(1);

    if (i <= values.size()) {
      // already there
      StringBuffer valueObject = (StringBuffer) values.get(i-1);
      valueObject.setLength(0);
      valueObject.append(value);
      return(valueObject);
    }
    else {
      // create a slot
      if (i > maxOcc) {
        // restricted
        throw new PreceptorViolationException( String.valueOf(name) + " is out of bound");
      }
      else {
        StringBuffer valueObject = new StringBuffer(value);
        values.add(valueObject);
        return(valueObject);
      }
    }
  }

  public StringBuffer getValue(int i) {
    if (values != null && i <= values.size()) {
      // is there
      return((StringBuffer)values.get(i-1));
    }
    else {
      return(null);
    }
  }

  public int valueCount() {
    if (values != null) {
      return(values.size());
    }
    else {
      return(0);
    }
  }
  */

    public void toStringBuffer(StringBuffer sb, ElementPreceptorNode e, int depth) {
        StringBuffer ident = new StringBuffer();
        for (int i = 0; i < depth * 3; i++) ident.append(" ");

        sb.append("\n").append(ident).append("<").append(e.getName());
        sb.append("[").append(e.getMinOcc()).append(",").append(e.getMaxOcc()).append("]");

        Collection attributes = e.getAttributes();
        if (attributes != null) {
            for (Iterator it = attributes.iterator(); it.hasNext();) {
                AttributePreceptorNode attr = (AttributePreceptorNode) it.next();
                attr.toStringBuffer(sb, depth);
            }
        }

        sb.append(">");

        if (e.getConstraints() != null) {
            for (Iterator it = e.getConstraints().iterator(); it.hasNext();) {
                Constraint constraint = (Constraint) it.next();
                sb.append("{").append(constraint.getType()).append("}");
            }
        }
        else {
            sb.append("{*}");
        }

        Collection childs = e.getChildElements();
        if (childs != null) {
            for (Iterator it = childs.iterator(); it.hasNext();) {
                ElementPreceptorNode child = (ElementPreceptorNode) it.next();
                toStringBuffer(sb, child, depth + 1);
            }
        }
        sb.append("</").append(e.getName()).append(">");
    }
}
