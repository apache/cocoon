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
package org.apache.cocoon.forms.binding;

import org.apache.cocoon.forms.formmodel.Repeater.RepeaterRow;

import org.apache.commons.jxpath.JXPathContext;

/**
 * @version $Id$
 */
public class RepeaterItem {

	private Object handle;

    private JXPathContext context;
    private RepeaterRow row;


    public RepeaterItem(Object handle) {
        super();
        this.handle = handle;
    }

    public JXPathContext getContext() {
        return context;
    }

    public void setContext(JXPathContext context) {
        this.context = context;
    }

    public Object getHandle() {
        return handle;
    }

    public void setHandle(Object handle) {
        this.handle = handle;
    }

    public RepeaterRow getRow() {
        return row;
    }

    public void setRow(RepeaterRow attribute) {
        this.row = attribute;
    }

    public boolean equals(Object other) {
        if (!(other instanceof RepeaterItem)) return false;
        return this.handle.equals(((RepeaterItem) other).handle);
    }

    public int hashCode() {
        return this.handle.hashCode();
    }
}
