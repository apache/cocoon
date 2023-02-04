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

import org.apache.cocoon.forms.formmodel.Widget;

import org.apache.commons.jxpath.JXPathContext;

/**
 * DeleteNodeJXPathBinding provides an implementation of a {@link Binding}
 * that deletes the current context-bean from the target
 * back-end model upon save.
 * <p>
 * NOTES: <ol>
 * <li>This Binding does not perform any actions when loading.</li>
 * </ol>
 *
 * @version $Id$
 */
public class DeleteNodeJXPathBinding extends JXPathBindingBase {

    public DeleteNodeJXPathBinding(JXPathBindingBuilderBase.CommonAttributes commonAtts) {
        super(commonAtts);
    }

    public void doLoad(Widget frmModel, JXPathContext jxpc) {
        // doesn't do a thing when loading.
    }

    /**
     * Removes the current context-bean from the jxpath context.
     */
    public void doSave(Widget frmModel, JXPathContext jxpc) {
        // get rid of the contextbean
        jxpc.removePath(".");
    }
}
