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
 * @author Timothy Larson
 * @version CVS $Id: TempRepeaterJXPathBinding.java,v 1.5 2004/02/03 12:22:08 joerg Exp $
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
    private final boolean virtualRows;

    public TempRepeaterJXPathBinding(
            JXpathBindingBuilderBase.CommonAttributes commonAtts,
            String repeaterId, String repeaterPath,
            String rowPath, String rowPathInsert,
            boolean virtualRows, boolean clearOnLoad, boolean deleteIfEmpty,
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
        this.virtualRows = virtualRows;
        this.clearOnLoad = clearOnLoad;
        this.deleteIfEmpty = deleteIfEmpty;
    }

    public void doLoad(Widget frmModel, JXPathContext jctx) throws BindingException {
        // (There should be a general widget type checker for all the bindings to use,
        // coupled with a general informative exception class to throw if the widget is
        // of the wrong type or null.)
        Repeater repeater = (Repeater) frmModel.getWidget(this.repeaterId);
        if (repeater == null) {
            String fullId = frmModel.getFullyQualifiedId();
            if (fullId == null || fullId.length() == 0) {
                fullId = "";
            } else {
                fullId = fullId + ".";
            }
            throw new RuntimeException(
                "TempRepeaterJXPathBinding: Repeater \"" + fullId + this.repeaterId +
                "\" does not exist (" + frmModel.getLocation() + ")");
        }
 
        // Start by clearing the repeater, if necessary.
        if (this.clearOnLoad) {
            repeater.removeRows();
        }

        // Find the location of the repeater data.
        Pointer repeaterPointer = jctx.getPointer(this.repeaterPath);

        // Check if there is data present.
        //
        // (Otherwise, should we check the leniency config option
        // to decide whether to be silent or throw an exception?) 
        if (repeaterPointer != null) {

            // Narrow to repeater context.
            JXPathContext repeaterContext = jctx.getRelativeContext(repeaterPointer);

            // Build a jxpath iterator for the repeater row pointers.
            Iterator rowPointers = repeaterContext.iteratePointers(this.rowPath);

            // Iterate through the rows of data.
            int rowNum = 0;
            while (rowPointers.hasNext()) {

                // Get or create a row widget.
                Repeater.RepeaterRow thisRow;
                if (repeater.getSize() > rowNum) {
                    thisRow = repeater.getRow(rowNum);
                } else {
                    thisRow = repeater.addRow();
                }
                rowNum++;

                // Narrow to the row context.
                Pointer rowPointer = (Pointer) rowPointers.next();
                JXPathContext rowContext = repeaterContext.getRelativeContext(rowPointer);

                // If virtual rows are requested, place a deep clone of the row data
                // into a temporary node, and narrow the context to this virtual row.
                //
                // (A clone of the data is used to prevent modifying the source document.
                // Otherwise, the appendChild method would remove the data from the source
                // document.  Is this protection worth the penalty of a deep clone?)
                //
                // (This implementation of virtual rows currently only supports DOM
                // bindings, but could easily be extended to support other bindings.)

                if (virtualRows == true) {
                    Node repeaterNode = (Node)repeaterPointer.getNode();
                    Node virtualNode = repeaterNode.getOwnerDocument().createElementNS(null, "virtual");
                    Node clone = ((Node)rowPointer.getNode()).cloneNode(true);
                    virtualNode.appendChild(clone);
                    rowContext = JXPathContext.newContext(repeaterContext, virtualNode);
                }

                // Finally, perform the load row binding.
                this.rowBinding.loadFormFromModel(thisRow, rowContext);
            }
        }

        if (getLogger().isDebugEnabled())
            getLogger().debug("done loading rows " + toString());
    }

    public void doSave(Widget frmModel, JXPathContext jctx) throws BindingException {
        // (See comment in doLoad about type checking and throwing a meaningful exception.)
        Repeater repeater = (Repeater) frmModel.getWidget(this.repeaterId);

        // Perform shortcut binding if the repeater is empty
        // and the deleteIfEmpty config option is selected.
        if (repeater.getSize() == 0 && this.deleteIfEmpty) {
            // Delete all of the old data for this repeater.
            jctx.removeAll(this.repeaterPath);

        // Otherwise perform the normal save binding.
        } else {

            // Narrow to the repeater context, creating the path if it did not exist.
            JXPathContext repeaterContext = jctx.getRelativeContext(jctx.createPath(this.repeaterPath));

            // Start by deleting all of the old row data.
            repeaterContext.removeAll(this.rowPath);

            // Verify that repeater is not empty and has an insert row binding.
            if(repeater.getSize() > 0) {
                if (this.insertRowBinding != null) {

                    //register the factory!
                    //this.insertRowBinding.saveFormToModel(repeater, repeaterContext);

                    // Iterate through the repeater rows.
                    for (int i = 0; i < repeater.getSize(); i++) {

                        // Narrow to the repeater row context.
                        Pointer rowPointer = repeaterContext.getPointer(this.rowPathInsert);
                        JXPathContext rowContext = repeaterContext.getRelativeContext(rowPointer);

                        // Variables used for virtual rows.
                        // They are initialized here just to keep the compiler happy. 
                        Node rowNode = null;
                        Node virtualNode = null;

                        // If virtual rows are requested, create a temporary node and
                        // narrow the context to this initially empty new virtual row.
                        if (virtualRows == true) {
                            rowNode = (Node)rowContext.getContextBean();
                            virtualNode = rowNode.getOwnerDocument().createElementNS(null, "virtual");
                            rowContext = JXPathContext.newContext(repeaterContext, virtualNode);
                        }

                        // Perform the insert row binding
                        this.insertRowBinding.saveFormToModel(repeater, rowContext);

                        // Perform the save row binding.
                        this.rowBinding.saveFormToModel(repeater.getRow(i), rowContext);

                        // If virtual rows are requested, finish by appending the
                        // children of the virtual row to the real context node.
                        if (virtualRows == true) {
                            NodeList list = virtualNode.getChildNodes();
                            int count = list.getLength();
                            for (int j = 0; j < count; j++) {
                                // The list shrinks when a child is appended to the context
                                // node, so we always reference the first child in the list.
                                rowNode.appendChild(list.item(0));
                            }
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
