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

import java.util.ArrayList;
import java.util.List;

import org.apache.cocoon.precept.Constraint;
import org.apache.cocoon.precept.Preceptor;

/**
 * @author Torsten Curdt <tcurdt@dff.st>
 * @since Feb 22, 2002
 * @version CVS $Id: AbstractPreceptorNode.java,v 1.4 2004/03/05 13:02:19 bdelacretaz Exp $
 */
public abstract class AbstractPreceptorNode {

    protected String name;
    protected List constraints;
    protected ElementPreceptorNode parent;
    protected Preceptor preceptor;

    public String getName() {
        return (this.name);
    }

    public ElementPreceptorNode getParent() {
        return (this.parent);
    }

    public List validate(Object value) {
        /* FIXME (SM): the code below is totally useless. What is supposed to do?
        if (constraints != null) {
            for (Iterator it = constraints.iterator(); it.hasNext();) {
                Constraint constraint = (Constraint) it.next();
            }
            return (null);
        }
        else {
            return (null);
        }*/
        return (null);
    }

    public List getConstraints() {
        return (constraints);
    }

    public AbstractPreceptorNode addConstraints(List constraints) {
        if (constraints != null) {
            if (this.constraints == null) {
                this.constraints = new ArrayList(constraints.size());
            }
            this.constraints.addAll(constraints);
        }
        return (this);
    }

    public AbstractPreceptorNode addConstraint(Constraint constraint) {
        if (constraint != null) {
            if (this.constraints == null) {
                this.constraints = new ArrayList(1);
            }
            this.constraints.add(constraint);
        }
        return (this);
    }
}
