/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.precept.preceptors.easyrelax;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.cocoon.precept.Constraint;
import org.apache.cocoon.precept.Preceptor;

/**
 * @author Torsten Curdt <tcurdt@dff.st>
 * @since Feb 22, 2002
 * @version CVS $Id: ElementPreceptorNode.java,v 1.4 2004/03/05 13:02:19 bdelacretaz Exp $
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
