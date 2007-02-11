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

import java.util.Iterator;

import org.apache.cocoon.precept.Constraint;
import org.apache.cocoon.precept.Preceptor;

/**
 * @author Torsten Curdt <tcurdt@dff.st>
 * @since Feb 22, 2002
 * @version CVS $Id: AttributePreceptorNode.java,v 1.3 2004/03/05 13:02:19 bdelacretaz Exp $
 */
public class AttributePreceptorNode extends AbstractPreceptorNode {

    private boolean required;
    //private StringBuffer valueObject;

    public AttributePreceptorNode(Preceptor preceptor, ElementPreceptorNode parent, String name, boolean required) {
        this.name = name;
        this.required = required;
        this.parent = parent;
        this.preceptor = preceptor;
    }

   public boolean isRequired() {
        return (required);
    }

   public void toStringBuffer(StringBuffer sb, int depth) {
        sb.append(" ").append(name).append("=");

       if (constraints != null) {
            for (Iterator it = constraints.iterator(); it.hasNext();) {
                Constraint constraint = (Constraint) it.next();
                sb.append("{").append(constraint.getType()).append("}");
            }
        }

       sb.append("[").append((required) ? "required" : "optional").append("]");
    }

/*
  public StringBuffer setValue( String value ) {
    if (valueObject == null){
      valueObject = new StringBuffer(value);
    }
    else {
      valueObject.setLength(0);
      valueObject.append(value);
    }
    return(valueObject);
  }

  public StringBuffer getValue() {
    return(valueObject);
  }
  */
}
