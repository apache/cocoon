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
package org.apache.cocoon.forms.binding;

import org.apache.cocoon.forms.formmodel.Widget;
import org.apache.commons.jxpath.JXPathContext;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;

/**
 * InsertNodeJXPathBinding provides an implementation of a {@link Binding}
 * that inserts a clone of some 'template document-fragment' into the target
 * back-end model upon save.
 * <p>
 * NOTES: <ol>
 * <li>This Binding does not perform any actions when loading.</li>
 * <li>This expects the back-end model to be an XML file.</li>
 * </ol>
 *
 * @version CVS $Id: InsertNodeJXPathBinding.java,v 1.1 2004/03/09 10:33:55 reinhard Exp $
 */
public class InsertNodeJXPathBinding extends JXPathBindingBase {

    private final DocumentFragment template;

    /**
     * Constructs InsertNodeJXPathBinding
     */
    public InsertNodeJXPathBinding(JXPathBindingBuilderBase.CommonAttributes commonAtts, DocumentFragment domTemplate) {
        super(commonAtts);
        this.template = domTemplate;
    }

    /**
     * Do-nothing implementation of the interface.
     */
    public void doLoad(Widget frmModel, JXPathContext jxpc) {
        // doesn't do a thing when loading.
    }

    /**
     * Registers a JXPath Factory on the JXPath Context.
     * <p>
     * The factory will inserts a clone of the 'template' DocumentFragment
     * inside this object into the target objectmodel.
     */
    public void doSave(Widget frmModel, JXPathContext jxpc) {

        Node parentNode = (Node)jxpc.getContextBean();
        Document targetDoc = parentNode.getOwnerDocument();
        Node toInsert = targetDoc.importNode(this.template, true);
        parentNode.appendChild(toInsert);

        if (getLogger().isDebugEnabled())
            getLogger().debug("InsertNode executed.");

        // jxpc.setFactory(new AbstractFactory() {
        //     public boolean createObject(JXPathContext context, Pointer pointer,
        //         Object parent, String name, int index) {
        //
        //         Node parentNode = (Node) parent;
        //         Document targetDoc = parentNode.getOwnerDocument();
        //         Node toInsert = targetDoc.importNode(InsertNodeJXPathBinding.this.template, true);
        //         parentNode.appendChild(toInsert);
        //
        //         if (getLogger().isDebugEnabled())
        //             getLogger().debug("InsertNode jxpath factory executed for index." + index);
        //         return true;
        //     }
        // });
        //
        // if (getLogger().isDebugEnabled())
        //     getLogger().debug("done registered factory for inserting node -- " + toString());
    }

    public String toString() {
        return "InsertNodeJXPathBinding [for nested template]";
    }
}
