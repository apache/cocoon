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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.woody.datatype.convertor.Convertor;
import org.apache.cocoon.woody.formmodel.Widget;
import org.apache.cocoon.woody.formmodel.Repeater;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;

/**
 * RepeaterJXPathBinding provides an implementation of a {@link Binding}
 * that allows for bidirectional binding of a repeater-widget to/from
 * repeating structures in the back-end object model.
 *
 * @version CVS $Id: RepeaterJXPathBinding.java,v 1.23 2004/03/03 19:47:35 joerg Exp $
 */
public class RepeaterJXPathBinding extends JXPathBindingBase {

    private final String repeaterId;
    private final String repeaterPath;
    private final String rowPath;
    private final String rowPathForInsert;
    private final JXPathBindingBase rowBinding;
    private final JXPathBindingBase insertRowBinding;
    private final JXPathBindingBase deleteRowBinding;
    private final List uniqueRowBinding;

    /**
     * Constructs RepeaterJXPathBinding
     */
    public RepeaterJXPathBinding(
            JXPathBindingBuilderBase.CommonAttributes commonAtts,
            String repeaterId, String repeaterPath, String rowPath,
            String rowPathForInsert, String uniqueRowId,
            String uniqueRowPath, JXPathBindingBase[] childBindings,
            JXPathBindingBase insertBinding,
            JXPathBindingBase[] deleteBindings, JXPathBindingBase[] uniqueBindings) {
        this(commonAtts, repeaterId, repeaterPath, rowPath, rowPathForInsert,
                uniqueRowId, uniqueRowPath, null, null, childBindings,
                insertBinding, deleteBindings, uniqueBindings);
    }

    /**
     * Constructs RepeaterJXPathBinding
     */
    public RepeaterJXPathBinding(
            JXPathBindingBuilderBase.CommonAttributes commonAtts,
            String repeaterId, String repeaterPath, String rowPath,
            String rowPathForInsert, String uniqueRowId,
            String uniqueRowPath, Convertor convertor, Locale convertorLocale,
            JXPathBindingBase[] childBindings, JXPathBindingBase insertBinding,
            JXPathBindingBase[] deleteBindings, JXPathBindingBase[] uniqueBindings) {
        super(commonAtts);
        this.repeaterId = repeaterId;
        this.repeaterPath = repeaterPath;
        this.rowPath = rowPath;
        this.rowPathForInsert = rowPathForInsert;
        this.rowBinding = new ComposedJXPathBindingBase(
                JXPathBindingBuilderBase.CommonAttributes.DEFAULT,
                childBindings);
        this.rowBinding.setParent(this);
        this.insertRowBinding = insertBinding;
        if (this.insertRowBinding != null) {
            this.insertRowBinding.setParent(this);
        }
        this.deleteRowBinding = new ComposedJXPathBindingBase(
                JXPathBindingBuilderBase.CommonAttributes.DEFAULT,
                deleteBindings);
        if (this.deleteRowBinding != null) {
            this.deleteRowBinding.setParent(this);
        }
        // New unique key management
        uniqueRowBinding = new ArrayList();
        // Create a UniqueFieldJXPathBining for the unique define in old-style
        if (uniqueRowId != null && uniqueRowPath != null) {
            uniqueRowBinding.add(new UniqueFieldJXPathBinding(
                JXPathBindingBuilderBase.CommonAttributes.DEFAULT,
                uniqueRowId, uniqueRowPath, convertor, convertorLocale));
        }
        if (uniqueBindings != null) {
            for (int i=0; i < uniqueBindings.length; i++) {
                uniqueRowBinding.add(uniqueBindings[i]);
            }
        }
    }

    /**
     * Binds the unique-id of the repeated rows, and narrows the context on
     * objectModelContext and Repeater to the repeated rows before handing
     * over to the actual binding-children.
     */
    public void doLoad(Widget frmModel, JXPathContext jxpc)
            throws BindingException {
        // Find the repeater
        Repeater repeater = (Repeater) frmModel.getWidget(this.repeaterId);
        repeater.removeRows();
        int initialSize = repeater.getSize();

        // build a jxpath iterator for pointers
        JXPathContext repeaterContext =
            jxpc.getRelativeContext(jxpc.getPointer(this.repeaterPath));
        Iterator rowPointers = repeaterContext.iteratePointers(this.rowPath);
        //iterate through it
        while (rowPointers.hasNext()) {
            // create a new row, take that as the frmModelSubContext
            Repeater.RepeaterRow thisRow;
            if (initialSize > 0) {
                thisRow = repeater.getRow(--initialSize);
            } else {
                thisRow = repeater.addRow();
            }
            // make a jxpath ObjectModelSubcontext on the iterated element
            Pointer jxp = (Pointer)rowPointers.next();
            JXPathContext rowContext = repeaterContext.getRelativeContext(jxp);
            // hand it over to children
            Iterator iter = this.uniqueRowBinding.iterator();
            while (iter.hasNext()) {
                ((UniqueFieldJXPathBinding)iter.next()).loadFormFromModel(thisRow, rowContext);
            }
            this.rowBinding.loadFormFromModel(thisRow, rowContext);
        }
        if (getLogger().isDebugEnabled())
            getLogger().debug("done loading rows " + toString());
    }

    /**
     * Uses the mapped unique-id of each row to detect if rows have been
     * updated, inserted or removed.  Depending on what happened the appropriate
     * child-bindings are alowed to visit the narrowed contexts.
     */
    public void doSave(Widget frmModel, JXPathContext jxpc)
            throws BindingException {
        // Find the repeater
        Repeater repeater = (Repeater) frmModel.getWidget(this.repeaterId);
        // and his context
        JXPathContext repeaterContext =
            jxpc.getRelativeContext(jxpc.getPointer(this.repeaterPath));

        // create set of updatedRowIds
        Set updatedRowIds = new HashSet();
        //create list of rows to insert at end
        List rowsToInsert = new ArrayList();

        // iterate rows in the form model...
        int formRowCount = repeater.getSize();
        for (int i = 0; i < formRowCount; i++) {
            Repeater.RepeaterRow thisRow = repeater.getRow(i);

            // Get the key values
            List rowIdValues = getUniqueRowValues(thisRow);

            if (isAnyListElementNotNull(rowIdValues)) {
                // iterate nodes to find match
                Iterator rowPointers = repeaterContext.iteratePointers(this.rowPath);
                boolean found = false;
                while (rowPointers.hasNext()) {
                    Pointer jxp = (Pointer) rowPointers.next();
                    JXPathContext rowContext = repeaterContext.getRelativeContext(jxp);
                    List matchIds = getMatchIds(rowContext);
                    if (ListUtils.isEqualList(rowIdValues, matchIds)) {
                        // match! --> bind to children
                        this.rowBinding.saveFormToModel(thisRow, rowContext);
                        //        --> store rowIdValue in list of updatedRowIds
                        updatedRowIds.add(rowIdValues);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    // this is a new row
                    rowsToInsert.add(thisRow);
                    // also add it to the updated row id's so that this row doesn't get deleted
                    updatedRowIds.add(rowIdValues);
                }
            } else {
                // if all rowIdValues == null --> this is a new row
                rowsToInsert.add(thisRow);
            }
        }
        // Iterate again nodes for deletion
        Iterator rowPointers = repeaterContext.iteratePointers(this.rowPath);
        List rowsToDelete = new ArrayList();
        while (rowPointers.hasNext()) {
            Pointer jxp = (Pointer)rowPointers.next();
            JXPathContext rowContext = repeaterContext.getRelativeContext((Pointer)jxp.clone());
            List matchIds = getMatchIds(rowContext);
            // check if matchPath was in list of updates, if not --> bind for delete
            if (!isListInSet(updatedRowIds, matchIds)) {
                rowsToDelete.add(rowContext);
            }
        }
        if (rowsToDelete.size() > 0) {
            if (this.deleteRowBinding != null) {
                // run backwards through the list, so that we don't get into
                // trouble by shifting indexes
                for (int i = rowsToDelete.size() - 1; i >= 0; i--) {
                    this.deleteRowBinding.saveFormToModel(frmModel,
                            rowsToDelete.get(i));
                }
            } else {
                if (getLogger().isWarnEnabled()) {
                    getLogger().warn(
                            "RepeaterBinding has detected rows to delete, " +
                            "but misses the <on-delete-row> binding to do it."
                            );
                }
            }
        }
        // count how many we have now
        int indexCount = 1;
        rowPointers = repeaterContext.iteratePointers(this.rowPathForInsert);
        while (rowPointers.hasNext()) {
            rowPointers.next();
            indexCount++;
        }
        // end with rows to insert (to make sure they don't get deleted!)
        if (rowsToInsert.size() > 0) {
            if (this.insertRowBinding != null) {
                Iterator rowIterator = rowsToInsert.iterator();
                //register the factory!
                while (rowIterator.hasNext()) {
                    Repeater.RepeaterRow thisRow = (Repeater.RepeaterRow)rowIterator.next();
                    // Perform the insert row binding.
                    this.insertRowBinding.saveFormToModel(repeater, repeaterContext);
                    // -->  create the path to let the context be created
                    Pointer newRowContextPointer = repeaterContext.createPath(
                            this.rowPathForInsert + "[" + indexCount + "]");
                    JXPathContext newRowContext =
                            repeaterContext.getRelativeContext(newRowContextPointer);
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("inserted row at " + newRowContextPointer.asPath());
                    }
                    //    + rebind to children for update
                    this.rowBinding.saveFormToModel(thisRow, newRowContext);
                    getLogger().debug("bound new row");
                    indexCount++;
                }
            } else {
                if (getLogger().isWarnEnabled()) {
                    getLogger().warn("RepeaterBinding has detected rows to insert, but misses " +
                            "the <on-insert-row> binding to do it.");
                }
            }
        }
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("done saving rows " + toString());
        }
    }

    /**
     * Tests if a List is already contained in a Set of Lists.
     * @param set the Set of Lists.
     * @param list the list that is tested if it is already in the Set.
     * @return true if the Set contains the List, false otherwise.
     */
    private boolean isListInSet(Set set, List list) {
        Iterator iter = set.iterator();
        while (iter.hasNext()) {
            List listFromSet = (List)iter.next();
            if (ListUtils.isEqualList(listFromSet, list)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tests if any of the elements in a List is not null.
     * @param list
     * @return
     */
    private boolean isAnyListElementNotNull(List list) {
        Iterator iter = list.iterator();
        while (iter.hasNext()) {
            if (iter.next() != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * 
     * @param rowContext
     * @return
     */
    private List getMatchIds(JXPathContext rowContext) {
        List matchIds = new ArrayList();
        Iterator iter = this.uniqueRowBinding.iterator();
        while (iter.hasNext()) {
            UniqueFieldJXPathBinding key = (UniqueFieldJXPathBinding)iter.next();
            Object matchId = rowContext.getValue(key.getXpath());
            if (matchId != null && key.getConvertor() != null) {
                if (matchId instanceof String) {
                    matchId = key.getConvertor().convertFromString(
                            (String)matchId, key.getConvertorLocale(), null);
                } else {
                    if (getLogger().isWarnEnabled()) {
                        getLogger().warn("Convertor ignored on backend-value " +
                                "which isn't of type String.");
                    }
                }
            }   
            matchIds.add(matchId);
        }
        return matchIds;
    }

    /**
     * Get the values of the unique-fields of the given row in the formModel 
     * @param thisRow
     * @return List
     */
    private List getUniqueRowValues(Repeater.RepeaterRow thisRow) {
        List values = new ArrayList();
        Iterator iter = this.uniqueRowBinding.iterator();
        while (iter.hasNext()) {
            UniqueFieldJXPathBinding key = (UniqueFieldJXPathBinding)iter.next();
            Widget rowIdWidget = thisRow.getWidget(key.getFieldId());
            Object rowIdValue = rowIdWidget.getValue();
            values.add(rowIdValue);
        }
        return values;
    }

    public String toString() {
        return "RepeaterJXPathBinding [widget=" + this.repeaterId +
               ", xpath=" + this.repeaterPath + "]";
    }

    public void enableLogging(Logger logger) {
        super.enableLogging(logger);
        if (this.deleteRowBinding != null) {
            this.deleteRowBinding.enableLogging(logger);
        }
        if (this.insertRowBinding != null) {
            this.insertRowBinding.enableLogging(logger);
        }
        this.rowBinding.enableLogging(logger);
        Iterator iter = this.uniqueRowBinding.iterator();
        while (iter.hasNext()) {
            ((UniqueFieldJXPathBinding)iter.next()).enableLogging(logger);
        }
    }
}
