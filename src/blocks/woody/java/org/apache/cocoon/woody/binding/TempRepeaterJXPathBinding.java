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

import java.util.Iterator;

import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.woody.formmodel.Repeater;
import org.apache.cocoon.woody.formmodel.Widget;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Experimental simple binding for repeaters:
 *   on save, first deletes the target data before recreating it from scratch.
 * Based on code from SimpleRepeater.
 * <p>
 * For a smarter binding that avoids deletion and recreation, consider
 * {@link org.apache.cocoon.woody.binding.RepeaterJXPathBinding}
 *
 * CVS $Id: TempRepeaterJXPathBinding.java,v 1.1 2003/12/29 06:14:48 tim Exp $
 * @author Timothy Larson
 */
public class TempRepeaterJXPathBinding extends JXPathBindingBase {

    private final String repeaterId;
    private final String repeaterPath;
    private final String rowPath;
    private final String rowPathInsert;
    private final boolean clearOnLoad;
    private final JXPathBindingBase rowBinding;
    private final JXPathBindingBase insertRowBinding;
    private final boolean deleteIfEmpty;

    public TempRepeaterJXPathBinding(
            JXpathBindingBuilderBase.CommonAttributes commonAtts,
            String repeaterId, String repeaterPath,
            String rowPath, String rowPathInsert,
            boolean clearOnLoad, boolean deleteIfEmpty,
            JXPathBindingBase rowBinding, JXPathBindingBase insertBinding) {
        super(commonAtts);
        this.repeaterId = repeaterId;
        this.repeaterPath = repeaterPath;
        this.rowPath = rowPath;
        this.rowPathInsert = rowPathInsert;
        this.rowBinding = rowBinding;
        this.rowBinding.setParent(this);
        this.insertRowBinding = insertBinding;
        this.insertRowBinding.setParent(this);
        this.clearOnLoad = clearOnLoad;
        this.deleteIfEmpty = deleteIfEmpty;
    }

    public void doLoad(Widget frmModel, JXPathContext jctx) {
        // Find the repeater and clear it
        Repeater repeater = (Repeater) frmModel.getWidget(this.repeaterId);
        
        // TODO: RAD
        if (repeater == null) {
            throw new RuntimeException(
                    "frmModel.getLocation() = " + frmModel.getLocation() +
                    "  repeaterId = " + this.repeaterId);
        }

        if (this.clearOnLoad) {
            repeater.removeRows();
        }

        // Move to repeater context
        Pointer ptr = jctx.getPointer(this.repeaterPath);
        if (ptr.getNode() != null) {
            // There are some nodes to load from
            
            JXPathContext repeaterContext = jctx.getRelativeContext(ptr);
            // build a jxpath iterator for pointers
            Iterator rowPointers = repeaterContext.iteratePointers(this.rowPath);

            //iterate through it
            int rowNum = 0;
            while (rowPointers.hasNext()) {
                // Get a row. It is created if needed (depends on clearOnLoad)
                Repeater.RepeaterRow thisRow;
                if (repeater.getSize() > rowNum) {
                    thisRow = repeater.getRow(rowNum);
                } else {
                    thisRow = repeater.addRow();
                }
                rowNum++;
     
                // make a jxpath sub context on the iterated element
                Pointer jxp = (Pointer) rowPointers.next();
                JXPathContext rowContext = repeaterContext.getRelativeContext(jxp);
    
                this.rowBinding.loadFormFromModel(thisRow, rowContext);
            }
        }

        if (getLogger().isDebugEnabled())
            getLogger().debug("done loading rows " + toString());
    }

    public void doSave(Widget frmModel, JXPathContext jctx) throws BindingException {
        // Find the repeater
        Repeater repeater = (Repeater) frmModel.getWidget(this.repeaterId);

        if (repeater.getSize() == 0 && this.deleteIfEmpty) {
            // Repeater is empty : erase all
            jctx.removeAll(this.repeaterPath);

        } else {
            // Repeater is not empty
            
            // Move to repeater context and create the path if needed
            JXPathContext repeaterContext = jctx.getRelativeContext(jctx.createPath(this.repeaterPath));

            // Delete all that is already present
            repeaterContext.removeAll(this.rowPath);

            if(repeater.getSize() > 0) {
                if (this.insertRowBinding != null) {
                    //register the factory!
                    this.insertRowBinding.saveFormToModel(repeater, repeaterContext);
                    for (int i = 0; i < repeater.getSize(); i++) {
                        java.lang.System.err.println("TempRepeater: Bind row.");
                        //String path = this.rowPathInsert + '[' + (i+1) + ']';
                        // -->  create the path to let the context be created
                        //Pointer rowPtr = repeaterContext.createPath(path);
                        //JXPathContext rowContext = repeaterContext.getRelativeContext(rowPtr);
                        Node node = (Node)repeaterContext.getContextBean();
                        Node virtualNode = node.getOwnerDocument().createElementNS(null, "virtual");
                        //JXPathContext rowContext = JXPathContext.newContext(repeaterContext, virtualNode);
                        JXPathContext rowContext = JXPathContext.newContext(repeaterContext, virtualNode);
                        //    + bind to children for output
                        this.rowBinding.saveFormToModel(repeater.getRow(i), rowContext);
                        // Append children to current path.
                        NodeList list = virtualNode.getChildNodes();
                        java.lang.System.err.println("Node count: " + list.getLength());
                        //node.appendChild(virtualNode);
                        int count = list.getLength();
                        for (int j = 0; j < count; j++) {
                            java.lang.System.err.println("Hello, child not null. j: " + j);
                            node.appendChild(list.item(0));
                            java.lang.System.err.println("After. j: " + j);
                        }
                        getLogger().debug("bound new row");
                    }
                } else {
                    getLogger().warn("TempRepeaterBinding has detected rows to insert, " +
                        "but misses the <on-insert-row> binding to do it.");
                }
            }
        }
    }

    public String toString() {
        return "TempRepeaterJXPathBinding [widget=" + this.repeaterId + ", xpath=" + this.repeaterPath + "]";
    }

    public void enableLogging(Logger logger) {
        super.enableLogging(logger);
        if (this.insertRowBinding != null) {
            this.insertRowBinding.enableLogging(logger);            
        }
        this.rowBinding.enableLogging(logger);
    }
}
