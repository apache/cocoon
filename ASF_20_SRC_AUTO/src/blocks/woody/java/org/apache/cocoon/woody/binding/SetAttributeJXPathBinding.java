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
package org.apache.cocoon.woody.binding;

import org.apache.cocoon.woody.formmodel.Widget;
import org.apache.commons.jxpath.JXPathContext;

/**
 * SetAttributeJXPathBinding provides an implementation of a {@link Binding}
 * that sets a particular attribute to a fixed value upon save.
 * <p>
 * NOTES: <ol>
 * <li>This Binding does not perform any actions when loading.</li>
 * </ol>
 *
 * @version CVS $Id: SetAttributeJXPathBinding.java,v 1.7 2004/03/05 13:02:27 bdelacretaz Exp $
 */
public class SetAttributeJXPathBinding extends JXPathBindingBase {

    private final String name;
    private final String value;

    /**
     * Constructs SetAttributeJXPathBinding
     */
    public SetAttributeJXPathBinding(JXPathBindingBuilderBase.CommonAttributes commonAtts, String attName, String attValue) {
        super(commonAtts);
        this.name = attName;
        this.value = attValue;
    }

    /**
     * Do-Nothing implementation.
     */
    public void doLoad(Widget frmModel, JXPathContext jxpc) {
        //this does nothing in the loading of things
    }

    /**
     * Sets the attribute value on the passed JXPathContext
     */
    public void doSave(Widget frmModel, JXPathContext jxpc) {
        jxpc.setValue("@" + this.name, this.value);
        if (getLogger().isDebugEnabled())
            getLogger().debug("done saving " + toString());
    }

    public String toString() {
        return "SetAttributeJXPathBinding [attName=" + this.name + ", attValue=" + this.value + "]";
    }
}
