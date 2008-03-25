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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.cocoon.forms.datatype.convertor.ConversionResult;
import org.apache.cocoon.forms.formmodel.Repeater;
import org.apache.cocoon.forms.formmodel.Widget;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;

/**
 * RepeaterJXPathBinding provides an implementation of a {@link Binding}
 * that allows for bidirectional binding of a repeater-widget to/from
 * repeating structures in the back-end object model.
 *
 * @version $Id$
 */
public class RepeaterJXPathBinding extends JXPathBindingBase {

    private final String repeaterId;
    private final String repeaterPath;
    private final String rowPath;
    private final String rowPathForInsert;
    private final JXPathBindingBase rowBinding;
    private final JXPathBindingBase insertRowBinding;
    private final JXPathBindingBase deleteRowBinding;
    private final ComposedJXPathBindingBase identityBinding;

    /**
     * Constructs RepeaterJXPathBinding
     */
    public RepeaterJXPathBinding(JXPathBindingBuilderBase.CommonAttributes commonAtts,
                                 String repeaterId,
                                 String repeaterPath,
                                 String rowPath,
                                 String rowPathForInsert,
                                 JXPathBindingBase[] childBindings,
                                 JXPathBindingBase insertBinding,
                                 JXPathBindingBase[] deleteBindings,
                                 JXPathBindingBase[] identityBindings) {
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

        if (deleteBindings != null) {
            this.deleteRowBinding = new ComposedJXPathBindingBase(
                    JXPathBindingBuilderBase.CommonAttributes.DEFAULT,
                    deleteBindings);
            this.deleteRowBinding.setParent(this);
        } else {
            this.deleteRowBinding = null;
        }


        if (identityBindings != null) {

            this.identityBinding = new ComposedJXPathBindingBase(
                    JXPathBindingBuilderBase.CommonAttributes.DEFAULT,
                    identityBindings);
            this.identityBinding.setParent(this);
        }
        else
            this.identityBinding = null;
    }

    public String getId() { return repeaterId; }
    public String getRepeaterPath() { return repeaterPath; }
    public String getRowPath() { return rowPath; }
    public String getInsertRowPath() { return rowPathForInsert; }
    public ComposedJXPathBindingBase getRowBinding() { return (ComposedJXPathBindingBase)rowBinding; }
    public ComposedJXPathBindingBase getDeleteRowBinding() { return (ComposedJXPathBindingBase)deleteRowBinding; }
    public ComposedJXPathBindingBase getIdentityBinding() { return identityBinding; }
    public JXPathBindingBase getInsertRowBinding() { return insertRowBinding; }

    /**
     * Binds the unique-id of the repeated rows, and narrows the context on
     * objectModelContext and Repeater to the repeated rows before handing
     * over to the actual binding-children.
     */
    public void doLoad(Widget frmModel, JXPathContext jxpc)
    throws BindingException {
        // Find the repeater
        Repeater repeater = (Repeater) selectWidget(frmModel, this.repeaterId);
        if (repeater == null) {
            throw new BindingException("The repeater with the ID [" + this.repeaterId
                    + "] referenced in the binding does not exist in the form definition.");
        }

        repeater.clear();

        Pointer ptr = jxpc.getPointer(this.repeaterPath);
        if (ptr.getNode() != null) {
            // There are some nodes to load from
            final int initialSize = repeater.getSize();

            // build a jxpath iterator for pointers
            JXPathContext repeaterContext = jxpc.getRelativeContext(ptr);
            Iterator rowPointers = repeaterContext.iteratePointers(this.rowPath);
            //iterate through it
            int currentRow = 0;
            while (rowPointers.hasNext()) {
                // create a new row, take that as the frmModelSubContext
                Repeater.RepeaterRow thisRow;
                if (currentRow < initialSize) {
                    thisRow = repeater.getRow(currentRow++);
                } else {
                    thisRow = repeater.addRow();
                }
                // make a jxpath ObjectModelSubcontext on the iterated element
                Pointer jxp = (Pointer)rowPointers.next();
                JXPathContext rowContext = repeaterContext.getRelativeContext(jxp);
                // hand it over to children
                if (this.identityBinding != null) {
                    this.identityBinding.loadFormFromModel(thisRow, rowContext);
                }
                this.rowBinding.loadFormFromModel(thisRow, rowContext);
            }
        }

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("done loading rows " + this);
        }
    }

    /**
     * Uses the mapped identity of each row to detect if rows have been
     * updated, inserted or removed.  Depending on what happened the appropriate
     * child-bindings are allowed to visit the narrowed contexts.
     */
    public void doSave(Widget frmModel, JXPathContext jxpc)
    throws BindingException {
        // Find the repeater
        Repeater repeater = (Repeater) selectWidget(frmModel, this.repeaterId);

        // and his context, creating the path if needed
        JXPathContext repeaterContext =
            jxpc.getRelativeContext(jxpc.createPath(this.repeaterPath));

        // create set of updatedRowIds
        Set updatedRows = new HashSet();
        //create list of rows to insert at end
        List rowsToInsert = new ArrayList();

        // iterate rows in the form model...
        int formRowCount = repeater.getSize();
        for (int i = 0; i < formRowCount; i++) {
            Repeater.RepeaterRow thisRow = repeater.getRow(i);

            // Get the identity
            List identity = getIdentity(thisRow);

            if (hasNonNullElements(identity)) {
                // iterate nodes to find match
                Iterator rowPointers = repeaterContext.iteratePointers(this.rowPath);
                boolean found = false;
                while (rowPointers.hasNext()) {
                    Pointer jxp = (Pointer) rowPointers.next();
                    JXPathContext rowContext = repeaterContext.getRelativeContext(jxp);
                    List contextIdentity = getIdentity(rowContext);
                    if (ListUtils.isEqualList(identity, contextIdentity)) {
                        // match! --> bind to children
                        this.rowBinding.saveFormToModel(thisRow, rowContext);
                        //        --> store rowIdValue in list of updatedRowIds
                        updatedRows.add(identity);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    // this is a new row
                    rowsToInsert.add(thisRow);
                    // also add it to the updated row id's so that this row doesn't get deleted
                    updatedRows.add(identity);
                }
            } else {
                // if there is no value to determine the identity --> this is a new row
                rowsToInsert.add(thisRow);
            }
        }
        // Iterate again nodes for deletion
        Iterator rowPointers = repeaterContext.iteratePointers(this.rowPath);
        List rowsToDelete = new ArrayList();
        while (rowPointers.hasNext()) {
            Pointer jxp = (Pointer)rowPointers.next();
            JXPathContext rowContext = repeaterContext.getRelativeContext((Pointer)jxp.clone());
            List contextIdentity = getIdentity(rowContext);
            // check if the identity of the rowContext is in the updated rows
            //     if not --> bind for delete
            if (!isIdentityInUpdatedRows(updatedRows, contextIdentity)) {
                rowsToDelete.add(rowContext);
            }
        }
        if (rowsToDelete.size() > 0) {
            // run backwards through the list, so that we don't get into
            // trouble by shifting indexes
            for (int i = rowsToDelete.size() - 1; i >= 0; i--) {
                if (this.deleteRowBinding != null) {
                    this.deleteRowBinding.saveFormToModel(frmModel,
                        rowsToDelete.get(i));
                } else {
                    // Simply remove the corresponding path
                    ((JXPathContext)rowsToDelete.get(i)).removePath(".");
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
                Iterator rowIterator = rowsToInsert.iterator();
                //register the factory!
                while (rowIterator.hasNext()) {
                    Repeater.RepeaterRow thisRow = (Repeater.RepeaterRow)rowIterator.next();
                    // Perform the insert row binding.
                    if (this.insertRowBinding != null) {
                        this.insertRowBinding.saveFormToModel(repeater, repeaterContext);
                    }
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
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("bound new row");
                    }
                    indexCount++;
                }
//            } else {
//                if (getLogger().isWarnEnabled()) {
//                    getLogger().warn("RepeaterBinding has detected rows to insert, but misses "
//                            + "the <on-insert-row> binding to do it.");
//                }
//            }
        }

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("done saving rows " + this);
        }
    }

    /**
     * Tests if an identity is already contained in a Set of identities.
     * @param identitySet the Set of identities.
     * @param identity the identity that is tested if it is already in the Set.
     * @return true if the Set contains the identity, false otherwise.
     */
    private boolean isIdentityInUpdatedRows(Set identitySet, List identity) {
        Iterator iter = identitySet.iterator();
        while (iter.hasNext()) {
            List identityFromSet = (List)iter.next();
            if (ListUtils.isEqualList(identityFromSet, identity)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tests if any of the elements in a List is not null.
     * @param list
     */
    private boolean hasNonNullElements(List list) {
        Iterator iter = list.iterator();
        while (iter.hasNext()) {
            if (iter.next() != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the identity of the given row context. That's infact a list of all
     * the values of the fields in the bean or XML that constitute the identity.
     * @param rowContext
     * @return List the identity of the row context
     */
    private List getIdentity(JXPathContext rowContext) {
        if (this.identityBinding == null) {
            return Collections.EMPTY_LIST;
        }

        List identity = new ArrayList();

        JXPathBindingBase[] childBindings = this.identityBinding.getChildBindings();
        if (childBindings != null) {
            int size = childBindings.length;
            for (int i = 0; i < size; i++) {
                ValueJXPathBinding vBinding = (ValueJXPathBinding)childBindings[i];
                Object value = rowContext.getValue(vBinding.getXPath());
                if (value != null && vBinding.getConvertor() != null) {
                    if (value instanceof String) {
                        ConversionResult conversionResult = vBinding.getConvertor().convertFromString(
                                (String)value, vBinding.getConvertorLocale(), null);
                        if (conversionResult.isSuccessful())
                            value = conversionResult.getResult();
                        else
                            value = null;
                    } else {
                        if (getLogger().isWarnEnabled()) {
                            getLogger().warn("Convertor ignored on backend-value " +
                                             "which isn't of type String.");
                        }
                    }
                }
                identity.add(value);
            }
        }
        return identity;
    }

    /**
     * Get the identity of the given row. That's in fact a list of all the values
     * of the fields in the form model that constitute the identity.
     * @param row
     * @return List the identity of the row
     */
    private List getIdentity(Repeater.RepeaterRow row) {
        // quit if we don't have an identity binding
        if (this.identityBinding == null) {
            return Collections.EMPTY_LIST;
        }

        List identity = new ArrayList();

        JXPathBindingBase[] childBindings = this.identityBinding.getChildBindings();
        if (childBindings != null) {
            int size = childBindings.length;
            for (int i = 0; i < size; i++) {
                String fieldId = ((ValueJXPathBinding) childBindings[i]).getFieldId();
                Widget widget = row.lookupWidget(fieldId);
                Object value = widget.getValue();
                identity.add(value);
            }
        }
        return identity;
    }

    public String toString() {
        return "RepeaterJXPathBinding [widget=" + this.repeaterId +
                ", xpath=" + this.repeaterPath + "]";
    }
}
