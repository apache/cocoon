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
package org.apache.cocoon.forms.formmodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.cocoon.environment.Request;
import org.apache.cocoon.forms.FormContext;
import org.apache.cocoon.forms.FormsConstants;
import org.apache.cocoon.forms.FormsRuntimeException;
import org.apache.cocoon.forms.event.RepeaterEvent;
import org.apache.cocoon.forms.event.RepeaterEventAction;
import org.apache.cocoon.forms.event.RepeaterListener;
import org.apache.cocoon.forms.event.WidgetEvent;
import org.apache.cocoon.forms.event.WidgetEventMulticaster;
import org.apache.cocoon.forms.util.I18nMessage;
import org.apache.cocoon.forms.validation.ValidationError;
import org.apache.cocoon.forms.validation.ValidationErrorAware;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * A repeater is a widget that repeats a number of other widgets.
 *
 * <p>Technically, the Repeater widget is a ContainerWidget whose children are
 * {@link RepeaterRow}s, and the RepeaterRows in turn are ContainerWidgets
 * containing the actual repeated widgets. However, in practice, you won't need
 * to use the RepeaterRow widget directly.
 *
 * <p>Using the methods {@link #getSize()} and {@link #getWidget(int, java.lang.String)}
 * you can access all of the repeated widget instances.
 *
 * @version $Id$
 */
public class Repeater extends AbstractWidget
                      implements ValidationErrorAware {

    private static final String REPEATER_EL = "repeater";
    private static final String HEADINGS_EL = "headings";
    private static final String HEADING_EL = "heading";
    private static final String LABEL_EL = "label";
    private static final String REPEATER_SIZE_EL = "repeater-size";

    protected final RepeaterDefinition definition;
    protected final List rows = new ArrayList();
    protected ValidationError validationError;
    private boolean orderable = false;
    private RepeaterListener listener;

    public Repeater(RepeaterDefinition repeaterDefinition) {
        super(repeaterDefinition);
        this.definition = repeaterDefinition;
        // Setup initial size. Do not call addRow() as it will call initialize()
        // on the newly created rows, which is not what we want here.
        for (int i = 0; i < this.definition.getInitialSize(); i++) {
            rows.add(new RepeaterRow(definition));
        }

        this.orderable = this.definition.getOrderable();
        this.listener = this.definition.getRepeaterListener();
    }

    public WidgetDefinition getDefinition() {
        return definition;
    }

    public void initialize() {
        for (int i = 0; i < this.rows.size(); i++) {
            ((RepeaterRow)rows.get(i)).initialize();
            // TODO(SG) Is this safe !?
            broadcastEvent(new RepeaterEvent(this, RepeaterEventAction.ROW_ADDED, i));
        }
        super.initialize();
    }

    public int getSize() {
        return rows.size();
    }

    public int getMinSize() {
        return this.definition.getMinSize();
    }

    public int getMaxSize() {
        return this.definition.getMaxSize();
    }

    public boolean getOrderable() {
        return this.orderable;
    }

    public List getAcceptTypes() {
        return this.definition.getAcceptTypes();
    }

    public List getRowTypes() {
        return this.definition.getRowTypes();
    }

    public List getAllowed() {
        return this.definition.getAllowed();
    }

    public RepeaterRow addRow() {
        RepeaterRow repeaterRow = new RepeaterRow(definition);
        rows.add(repeaterRow);
        repeaterRow.initialize();
        getForm().addWidgetUpdate(this);
        broadcastEvent(new RepeaterEvent(this, RepeaterEventAction.ROW_ADDED, rows.size() - 1));
        return repeaterRow;
    }

    public RepeaterRow addRow(int index) {
        RepeaterRow repeaterRow = new RepeaterRow(definition);
        if (index >= this.rows.size()) {
            rows.add(repeaterRow);
            index = rows.size() - 1;
        } else {
            rows.add(index, repeaterRow);
        }
        repeaterRow.initialize();
        getForm().addWidgetUpdate(this);
        broadcastEvent(new RepeaterEvent(this, RepeaterEventAction.ROW_ADDED, index));
        return repeaterRow;
    }

    public RepeaterRow getRow(int index) {
        return (RepeaterRow)rows.get(index);
    }

    /**
     * Overrides {@link AbstractWidget#getChild(String)} to return the
     * repeater-row indicated by the index in 'id'
     *
     * @param id index of the row as a string-id
     * @return the repeater-row at the specified index
     */
    public Widget getChild(String id) {
        int rowIndex;
        try {
            rowIndex = Integer.parseInt(id);
        } catch (NumberFormatException nfe) {
            // Not a number
            return null;
        }

        if (rowIndex < 0 || rowIndex >= getSize()) {
            return null;
        }

        return getRow(rowIndex);
    }

    /**
     * Crawls up the parents of a widget up to finding a repeater row.
     *
     * @param widget the widget whose row is to be found
     * @return the repeater row
     */
    public static RepeaterRow getParentRow(Widget widget) {
        Widget result = widget;
        while(result != null && ! (result instanceof Repeater.RepeaterRow)) {
            result = result.getParent();
        }

        if (result == null) {
            throw new RuntimeException("Could not find a parent row for widget " + widget);
        }
        return (Repeater.RepeaterRow)result;
    }

    /**
     * Get the position of a row in this repeater.
     * @param row the row which we search the index for
     * @return the row position or -1 if this row is not in this repeater
     */
    public int indexOf(RepeaterRow row) {
        return this.rows.indexOf(row);
    }

    /**
     * @throws IndexOutOfBoundsException if the the index is outside the range of existing rows.
     */
    public void removeRow(int index) {
        broadcastEvent(new RepeaterEvent(this, RepeaterEventAction.ROW_DELETING, index));
        rows.remove(index);
        getForm().addWidgetUpdate(this);
        broadcastEvent(new RepeaterEvent(this, RepeaterEventAction.ROW_DELETED, index));
    }

    /**
     * Move a row from one place to another
     * @param from the existing row position
     * @param to the target position. The "from" item will be moved before that position.
     */
    public void moveRow(int from, int to) {
        int size = this.rows.size();

        if (from < 0 || from >= size || to < 0 || to > size) {
            throw new IllegalArgumentException("Cannot move from " + from + " to " + to +
                                               " on repeater with " + size + " rows");
        }

        if (from == to) {
            return;
        }

        Object fromRow = this.rows.remove(from);
        if (to == size) {
            // Move at the end
            this.rows.add(fromRow);

        } else if (to > from) {
            // Index of "to" was moved by removing
            this.rows.add(to - 1, fromRow);

        } else {
            this.rows.add(to, fromRow);
        }

        getForm().addWidgetUpdate(this);
        broadcastEvent(new RepeaterEvent(this, RepeaterEventAction.ROWS_REARRANGED));
    }

    /**
     * Move a row from one place to another. In contrast to {@link #moveRow}, this
     * method treats the to-index as the exact row-index where you want to have the
     * row moved to.
     *
     * @param from the existing row position
     * @param to the target position. The "from" item will be moved before that position.
     */
    public void moveRow2(int from, int to) {
        int size = this.rows.size();

        if (from < 0 || from >= size || to < 0 || to >= size) {
            throw new IllegalArgumentException("Cannot move from " + from + " to " + to +
                                               " on repeater with " + size + " rows");
        }

        if (from == to) {
            return;
        }

        Object fromRow = this.rows.remove(from);
        this.rows.add(to, fromRow);

        getForm().addWidgetUpdate(this);
        broadcastEvent(new RepeaterEvent(this, RepeaterEventAction.ROWS_REARRANGED));
    }

    public void moveRowLeft(int index) {
        if (index == 0 || index >= this.rows.size()) {
            // do nothing
        } else {
            Object temp = this.rows.get(index-1);
            this.rows.set(index-1, this.rows.get(index));
            this.rows.set(index, temp);
        }
        getForm().addWidgetUpdate(this);
        broadcastEvent(new RepeaterEvent(this, RepeaterEventAction.ROWS_REARRANGED));
    }

    public void moveRowRight(int index) {
        if (index < 0 || index >= this.rows.size() - 1) {
            // do nothing
        } else {
            Object temp = this.rows.get(index+1);
            this.rows.set(index+1, this.rows.get(index));
            this.rows.set(index, temp);
        }
        getForm().addWidgetUpdate(this);
        broadcastEvent(new RepeaterEvent(this, RepeaterEventAction.ROWS_REARRANGED));
    }

    /**
     * @deprecated See {@link #clear()}
     *
     */
    public void removeRows() {
        clear();
    }

    /**
     * Clears all rows from the repeater and go back to the initial size
     */
    public void clear() {
        broadcastEvent(new RepeaterEvent(this, RepeaterEventAction.ROWS_CLEARING));
        rows.clear();
        broadcastEvent(new RepeaterEvent(this, RepeaterEventAction.ROWS_CLEARED));

        // and reset to initial size
        for (int i = 0; i < this.definition.getInitialSize(); i++) {
            addRow();
        }
        getForm().addWidgetUpdate(this);
    }

    public void addRepeaterListener(RepeaterListener listener) {
        this.listener = WidgetEventMulticaster.add(this.listener, listener);
    }

    public void removeRepeaterListener(RepeaterListener listener) {
        this.listener = WidgetEventMulticaster.remove(this.listener, listener);
    }

    public boolean hasRepeaterListeners() {
        return this.listener != null;
    }

    public void broadcastEvent(WidgetEvent event) {
        if (event instanceof RepeaterEvent) {
            if (this.listener != null) {
                this.listener.repeaterModified((RepeaterEvent)event);
            }
        } else {
            // Other kinds of events
            super.broadcastEvent(event);
        }
    }


    /**
     * Gets a widget on a certain row.
     * @param rowIndex startin from 0
     * @param id a widget id
     * @return null if there's no such widget
     */
    public Widget getWidget(int rowIndex, String id) {
        RepeaterRow row = (RepeaterRow)rows.get(rowIndex);
        return row.getChild(id);
    }

    public void readFromRequest(FormContext formContext) {
        if (!getCombinedState().isAcceptingInputs()) {
            return;
        }

        // read number of rows from request, and make an according number of rows
        Request req = formContext.getRequest();
        String paramName = getRequestParameterName();

        String sizeParameter = req.getParameter(paramName + ".size");
        if (sizeParameter != null) {
            int size = 0;
            try {
                size = Integer.parseInt(sizeParameter);
            } catch (NumberFormatException exc) {
                // do nothing
            }

            // some protection against people who might try to exhaust the server by supplying very large
            // size parameters
            if (size > 500) {
                throw new RuntimeException("Client is not allowed to specify a repeater size larger than 500.");
            }

            int currentSize = getSize();
            if (currentSize < size) {
                for (int i = currentSize; i < size; i++) {
                    addRow();
                }
            } else if (currentSize > size) {
                for (int i = currentSize - 1; i >= size; i--) {
                    removeRow(i);
                }
            }
        }

        // let the rows read their data from the request
        Iterator rowIt = rows.iterator();
        while (rowIt.hasNext()) {
            RepeaterRow row = (RepeaterRow)rowIt.next();
            row.readFromRequest(formContext);
        }

        // Handle repeater-level actions
        String action = req.getParameter(paramName + ".action");
        if (action == null) {
            return;
        }

        // Handle row(s) move. It's important for this to happen *after* row.readFromRequest,
        // as reordering rows changes their IDs and therefore their child widget's ID too.
        // NB. this get's called on the target of the dnd operation
        if ("move".equals(action) || "copy".equals(action)) {
            String sourceId = req.getParameter(paramName + ".sourceRepeaterId");
            if (sourceId == null) { // do a local copy or move
              this.checkAllowedState(this, action); // test if copy/move is allowed by the model
              if (!this.orderable) throw new FormsRuntimeException("Widget " + this + " is not orderable", getLocation());
              int beforeIdx = Integer.parseInt(req.getParameter(paramName + ".before"));
              String[] froms = req.getParameterValues(paramName + ".from");
              int[] rowz = new int[froms.length];
              for (int i = 0; i < froms.length; i++) rowz[i] = Integer.parseInt(froms[i]);// make a sortable copy
              Arrays.sort(rowz); // move/copy in ascending order
              boolean up = (beforeIdx < rowz[0]);
              if ("move".equals(action)) {
                for (int i = 0; i < rowz.length; i++) {
                    int fromIdx = rowz[i] - (up ? 0 : i); // track previous location changes
                    RepeaterRow row = (RepeaterRow)this.rows.get(fromIdx);
                    this.rows.add(beforeIdx, row);// Add at the new location
                    if (up) { // moving up
                        fromIdx++;
                        beforeIdx++;
                    }
                    this.rows.remove(fromIdx); // Remove original row, taking into account location changes
                }
              } else { // it's a copy
                for (int i = 0; i < rowz.length; i++) {
                    int fromIdx = rowz[i] + (up ? i : 0); // track previous location changes
                    RepeaterRow row = (RepeaterRow)this.rows.get(fromIdx);
                    RepeaterRow newrow = this.addRow(beforeIdx + i);
                    this.copyRow(row, newrow);// copy values
                }
              }        
            } else { // copy or move between Repeaters
              String formId = this.getForm().getId();
              if ("" != formId) sourceId = sourceId.substring(formId.length()+1);
              Repeater source = (Repeater)this.getForm().lookupWidget(sourceId.replace('.', '/'));
              if (source != null) {
                this.checkAllowedState(source, action); // test if copy/move is allowed by the model
                Iterator rowtypes = source.getRowTypes().iterator();
                boolean acceptable = false;
                while(rowtypes.hasNext() && !acceptable) {
                    String type = (String)rowtypes.next();
                    if (!"".equals(type) && this.getAcceptTypes().contains(type)) acceptable = true;
                }
                if (!acceptable) throw new FormsRuntimeException("Rows in Widget " + source + " are not accepted by Widget " + this, getLocation());
                int targetIdx = Integer.parseInt(req.getParameter(paramName + ".before"));
                String[] sourceIdxs = req.getParameterValues(paramName + ".sourceRepeaterIndex");
                int[] rowz = new int[sourceIdxs.length];
                for (int i = 0; i < sourceIdxs.length; i++) rowz[i] = Integer.parseInt(sourceIdxs[i]);// make a sortable copy
                Arrays.sort(rowz); // move in ascending order
                for (int i = 0; i < rowz.length; i++) { // add the new rows
                    RepeaterRow row = (RepeaterRow)source.rows.get(rowz[i]);
                    RepeaterRow newrow = this.addRow(targetIdx + i);
                    this.copyRow(row, newrow);// copy values
                }
                if ("move".equals(action)) { // the old rows need deleting
                    for (int i = rowz.length -1; i > -1; i--) source.removeRow(rowz[i]); // remove in reverse order
                }
              } else {
                  throw new FormsRuntimeException("Source Widget " + this + " was not found", getLocation());
              }
            }
            getForm().addWidgetUpdate(this);
        } else {
            throw new FormsRuntimeException("Unknown action " + action + " for " + this, getLocation());
        }
    }

    private void checkAllowedState(Repeater source, String action) throws FormsRuntimeException {
      List allows = source.getAllowed();
      if (allows.size() == 0) return;
      boolean isCopy = action.equals("copy");
      String allow = (String)allows.get(0);
      if (this != source) allow = allows.size() > 1 ? (String)allows.get(1) : (String)allows.get(0);
      if (isCopy && "move".equals(allow)) 
          throw new FormsRuntimeException("Widget " + source + " is not allowed to copy rows", getLocation());
      if (!isCopy && "copy".equals(allow)) 
          throw new FormsRuntimeException("Widget " + source + " is not allowed to move rows", getLocation());
    }
    
    
		// recursively copy values from widgets in one row to another
		private void copyRow(ContainerWidget source, ContainerWidget target) {
				Iterator sourceChildren = source.getChildren(); // get the Repeater's child Widgets
				while (sourceChildren.hasNext()) { // for each child widget
						Widget child = ((Widget) sourceChildren.next()); // get the child
						Widget newchild = target.getChild(child.getName()); // look for the same-named child in the target repeater
						if (newchild != null) { // if it exists
							try {
									if (child instanceof ContainerWidget) { // recurse if the child is a Repeater
											this.copyRow((ContainerWidget)child, (ContainerWidget)newchild);
									} else if (!(child instanceof Action)) { // you cannot set values on Actions
											newchild.setValue(child.getValue()); // copy the value
									} 
							} catch (UnsupportedOperationException e) { /* some widgets may throw this */ }
						}
				}
		}

    /**
     * @see org.apache.cocoon.forms.formmodel.Widget#validate()
     */
    public boolean validate() {
        if (!getCombinedState().isValidatingValues()) {
            this.wasValid = true;
            return true;
        }

        boolean valid = true;
        Iterator rowIt = rows.iterator();
        while (rowIt.hasNext()) {
            RepeaterRow row = (RepeaterRow)rowIt.next();
            valid = valid & row.validate();
        }

        if (rows.size() > getMaxSize() || rows.size() < getMinSize()) {
            String [] boundaries = new String[2];
            boundaries[0] = String.valueOf(getMinSize());
            boundaries[1] = String.valueOf(getMaxSize());
            this.validationError = new ValidationError(new I18nMessage("repeater.cardinality", boundaries, FormsConstants.I18N_CATALOGUE));
            valid=false;
        }


        if (valid) {
            valid = super.validate();
        }

        this.wasValid = valid && this.validationError == null;
        return this.wasValid;
    }



    /**
     * @return "repeater"
     */
    public String getXMLElementName() {
        return REPEATER_EL;
    }



    /**
     * Adds attributes to XML output
     */
    public AttributesImpl getXMLElementAttributes() {
        AttributesImpl attrs = super.getXMLElementAttributes();
        attrs.addCDATAAttribute("size", String.valueOf(getSize()));
        attrs.addCDATAAttribute("orderable", Boolean.toString(getOrderable()));
        
        // Generate the min and max sizes if they don't have the default value
        int size = getMinSize();
        if (size > 0) {
            attrs.addCDATAAttribute("min-size", String.valueOf(size));
        }
        size = getMaxSize();
        if (size != Integer.MAX_VALUE) {
            attrs.addCDATAAttribute("max-size", String.valueOf(size));
        }
        return attrs;
    }


    public void generateDisplayData(ContentHandler contentHandler)
    throws SAXException {
        // the repeater's label
        contentHandler.startElement(FormsConstants.INSTANCE_NS, LABEL_EL, FormsConstants.INSTANCE_PREFIX_COLON + LABEL_EL, XMLUtils.EMPTY_ATTRIBUTES);
        generateLabel(contentHandler);
        contentHandler.endElement(FormsConstants.INSTANCE_NS, LABEL_EL, FormsConstants.INSTANCE_PREFIX_COLON + LABEL_EL);

        // heading element -- currently contains the labels of each widget in the repeater
        contentHandler.startElement(FormsConstants.INSTANCE_NS, HEADINGS_EL, FormsConstants.INSTANCE_PREFIX_COLON + HEADINGS_EL, XMLUtils.EMPTY_ATTRIBUTES);
        Iterator widgetDefinitionIt = definition.getWidgetDefinitions().iterator();
        while (widgetDefinitionIt.hasNext()) {
            WidgetDefinition widgetDefinition = (WidgetDefinition)widgetDefinitionIt.next();
            contentHandler.startElement(FormsConstants.INSTANCE_NS, HEADING_EL, FormsConstants.INSTANCE_PREFIX_COLON + HEADING_EL, XMLUtils.EMPTY_ATTRIBUTES);
            widgetDefinition.generateLabel(contentHandler);
            contentHandler.endElement(FormsConstants.INSTANCE_NS, HEADING_EL, FormsConstants.INSTANCE_PREFIX_COLON + HEADING_EL);
        }
        contentHandler.endElement(FormsConstants.INSTANCE_NS, HEADINGS_EL, FormsConstants.INSTANCE_PREFIX_COLON + HEADINGS_EL);
    }

    public void generateItemSaxFragment(ContentHandler contentHandler, Locale locale) throws SAXException {
        // the actual rows in the repeater
        Iterator rowIt = rows.iterator();
        while (rowIt.hasNext()) {
            RepeaterRow row = (RepeaterRow)rowIt.next();
            row.generateSaxFragment(contentHandler, locale);
        }
    }

    /**
     * Generates the label of a certain widget in this repeater.
     */
    public void generateWidgetLabel(String widgetId, ContentHandler contentHandler) throws SAXException {
        WidgetDefinition widgetDefinition = definition.getWidgetDefinition(widgetId);
        if (widgetDefinition == null) {
            throw new SAXException("Repeater '" + getRequestParameterName() + "' at " + getLocation()
                                   + " contains no widget with id '" + widgetId + "'.");
        }

        widgetDefinition.generateLabel(contentHandler);
    }

    /**
     * Generates a repeater-size element with a size attribute indicating the size of this repeater.
     */
    public void generateSize(ContentHandler contentHandler) throws SAXException {
        AttributesImpl attrs = getXMLElementAttributes();
        contentHandler.startElement(FormsConstants.INSTANCE_NS, REPEATER_SIZE_EL, FormsConstants.INSTANCE_PREFIX_COLON + REPEATER_SIZE_EL, attrs);
        contentHandler.endElement(FormsConstants.INSTANCE_NS, REPEATER_SIZE_EL, FormsConstants.INSTANCE_PREFIX_COLON + REPEATER_SIZE_EL);
    }

    /**
     * Set a validation error on this field. This allows repeaters be externally marked as invalid by
     * application logic.
     *
     * @return the validation error
     */
    public ValidationError getValidationError() {
        return this.validationError;
    }

    /**
     * set a validation error
     */
    public void setValidationError(ValidationError error) {
        this.validationError = error;
    }

    public class RepeaterRow extends AbstractContainerWidget {

        private static final String ROW_EL = "repeater-row";

        public RepeaterRow(RepeaterDefinition definition) {
            super(definition);
            setParent(Repeater.this);
            definition.createWidgets(this);
        }

        public WidgetDefinition getDefinition() {
            return Repeater.this.getDefinition();
        }

        private int cachedPosition = -100;
        private String cachedId = "--undefined--";

        public String getId() {
            int pos = rows.indexOf(this);
            if (pos == -1) {
                throw new IllegalStateException("Row has currently no position");
            }

            if (pos != this.cachedPosition) {
                this.cachedPosition = pos;
                // id of a RepeaterRow is the position of the row in the list of rows.
                this.cachedId = String.valueOf(pos);
                widgetNameChanged();
            }
            return this.cachedId;
        }

        public String getRequestParameterName() {
            // Get the id to check potential position change
            getId();

            return super.getRequestParameterName();
        }

        public Form getForm() {
            return Repeater.this.getForm();
        }

        public void initialize() {
            // Initialize children but don't call super.initialize() that would call the repeater's
            // on-create handlers for each row.
            Iterator i = getChildren();
            while (i.hasNext()) {
                ((Widget) i.next()).initialize();
            }
        }

        public boolean validate() {
            // Validate only child widtgets, as the definition's validators are those of the parent repeater
            return widgets.validate();
        }

        /**
         * @return "repeater-row"
         */
        public String getXMLElementName() {
            return ROW_EL;
        }

        public void generateLabel(ContentHandler contentHandler) throws SAXException {
            // this widget has its label generated in the context of the repeater
        }

        public void generateDisplayData(ContentHandler contentHandler)
        throws SAXException {
            // this widget has its display-data generated in the context of the repeater
        }

        public void broadcastEvent(WidgetEvent event) {
            throw new UnsupportedOperationException("Widget " + this + " doesn't handle events.");
        }
    }

}
