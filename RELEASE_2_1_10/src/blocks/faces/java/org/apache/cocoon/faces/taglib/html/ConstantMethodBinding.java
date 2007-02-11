/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.faces.taglib.html;

import javax.faces.component.StateHolder;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;

/**
 * @version CVS $Id$
 */
public class ConstantMethodBinding extends MethodBinding
                                   implements StateHolder {

    private String outcome;
    private boolean isTransient;

    public ConstantMethodBinding() {
    }

    public ConstantMethodBinding(String outcome) {
        this.outcome = outcome;
    }

    public Object invoke(FacesContext context, Object params[]) {
        return this.outcome;
    }

    public Class getType(FacesContext context) {
        return String.class;
    }

    public Object saveState(FacesContext context) {
        return this.outcome;
    }

    public void restoreState(FacesContext context, Object state) {
        this.outcome = (String) state;
    }

    public boolean isTransient() {
        return this.isTransient;
    }

    public void setTransient(boolean isTransient) {
        this.isTransient = isTransient;
    }
}
