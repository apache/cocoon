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
package org.apache.cocoon.woody.binding;

import org.apache.cocoon.woody.formmodel.Widget;
import org.apache.commons.jxpath.AbstractFactory;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
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
 */
public class InsertNodeJXPathBinding extends JXPathBindingBase {

    private final DocumentFragment template;
    
    /**
     * Constructs InsertNodeJXPathBinding
     */
    public InsertNodeJXPathBinding(JXpathBindingBuilderBase.CommonAttributes commonAtts, DocumentFragment domTemplate) {
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

        jxpc.setFactory(new AbstractFactory() {
            public boolean createObject(JXPathContext context, Pointer pointer,
                Object parent, String name, int index) {

                Node parentNode = (Node) parent;
                Document targetDoc = parentNode.getOwnerDocument();
                Node toInsert = targetDoc.importNode(InsertNodeJXPathBinding.this.template, true);
                parentNode.appendChild(toInsert);

                if (getLogger().isDebugEnabled())
                    getLogger().debug("InsertNode jxpath factory executed for index." + index);
                return true;
            }
        });

        if (getLogger().isDebugEnabled())
            getLogger().debug("done registered factory for inserting node -- " + toString());
    }
    
    public String toString() {
        return "InsertNodeJXPathBinding [for nested template]";
    }

}
