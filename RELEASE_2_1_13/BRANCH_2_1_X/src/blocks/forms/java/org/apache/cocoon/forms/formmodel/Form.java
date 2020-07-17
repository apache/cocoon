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

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.apache.cocoon.forms.FormContext;
import org.apache.cocoon.forms.event.FormHandler;
import org.apache.cocoon.forms.event.ProcessingPhase;
import org.apache.cocoon.forms.event.ProcessingPhaseEvent;
import org.apache.cocoon.forms.event.ProcessingPhaseListener;
import org.apache.cocoon.forms.event.WidgetEvent;
import org.apache.cocoon.forms.event.WidgetEventMulticaster;
import org.apache.cocoon.forms.validation.ValidationError;
import org.apache.cocoon.forms.validation.ValidationErrorAware;
import org.apache.commons.collections.list.CursorableLinkedList;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;

/**
 * A widget that serves as a container for other widgets, the top-level widget in
 * a form description file.
 *
 * @version $Id$
 */
public class Form extends AbstractContainerWidget
                  implements ValidationErrorAware {

    /** Form parameter containing the submit widget's id */
    public static final String SUBMIT_ID_PARAMETER = "forms_submit_id";

    private static final String FORM_EL = "form";

    private final FormDefinition definition;

    /**
     * If non-null, indicates that form processing should terminate at the end of the current phase.
     * If true, interaction with the form is finished. It doesn't imply that the form is valid though.
     * If false, interaction isn't finished and the form should be redisplayed (processing was triggered
     * by e.g. and action or a field with event listeners).
     */
    private Boolean endProcessing;
    private Locale locale = Locale.getDefault();
    private FormHandler formHandler;
    private Widget submitWidget;
    private boolean isValid;
    private ProcessingPhaseListener listener;

    //In the "readFromRequest" phase, events are buffered to ensure that all widgets had the chance
    //to read their value before events get fired.
    private boolean bufferEvents;
    private CursorableLinkedList events;

    /** Widgets that need to be updated in the client when in AJAX mode */
    private Set updatedWidgets;

    /** Widgets that have at least one descendant that has to be updated */
    private Set childUpdatedWidgets;

    /** Optional id which overrides the value from the form definition */
    private String id;


    public Form(FormDefinition definition) {
        super(definition);
        this.definition = definition;
        this.listener = definition.getProcessingPhaseListener();
    }

    /**
     * Initialize the form by recursively initializing all its children. Any events occuring within the
     * initialization phase are buffered and fired after initialization is complete, so that any action
     * from a widget on another one occurs after that other widget has been given the opportunity to
     * initialize itself.
     */
    public void initialize() {
        try {
            // Start buffering events
            this.bufferEvents = true;
            super.initialize();
            // Fire events, still buffering them: this ensures they will be handled in the same
            // order as they were added.
            fireEvents();
        } finally {
            // Stop buffering events
            this.bufferEvents = false;
        }
    }

    public WidgetDefinition getDefinition() {
        return this.definition;
    }

    /**
     * Events produced by child widgets should not be fired immediately, but queued in order to ensure
     * an overall consistency of the widget tree before being handled.
     *
     * @param event the event to queue
     */
    public void addWidgetEvent(WidgetEvent event) {

        if (this.bufferEvents) {
            if (this.events == null) {
                this.events = new CursorableLinkedList();
            }

            // FIXME: limit the number of events to detect recursive event loops ?
            this.events.add(event);
        } else {
            // Send it right now
            event.getSourceWidget().broadcastEvent(event);
        }
    }

    /**
     * Mark a widget as being updated. When it Ajax mode, only updated widgets will be redisplayed
     *
     * @param widget the updated widget
     * @return <code>true</code> if this widget was added to the list (i.e. wasn't alredy marked for update)
     */
    public boolean addWidgetUpdate(Widget widget) {
        if (this.updatedWidgets != null) {
            if (this.updatedWidgets.add(widget.getRequestParameterName())) {
                // Wasn't already there: register parents
                Widget parent = widget.getParent();
                while (parent != this && parent != null) {
                    if (this.childUpdatedWidgets.add(parent.getRequestParameterName())) {
                        parent = parent.getParent();
                    } else {
                        // Parent already there, and therefore its own parents.
                        break;
                    }
                }
                return true;
            }
        }
        return false;
    }

    public Set getUpdatedWidgetIds() {
        return this.updatedWidgets;
    }

    public Set getChildUpdatedWidgetIds() {
        return this.childUpdatedWidgets;
    }

    /**
     * Fire the events that have been queued.
     * Note that event handling can fire new events.
     */
    private void fireEvents() {
        if (this.events != null) {
            try {
                CursorableLinkedList.Cursor cursor = this.events.cursor();
                while (cursor.hasNext()) {
                    WidgetEvent event = (WidgetEvent) cursor.next();
                    event.getSourceWidget().broadcastEvent(event);
                    if (formHandler != null) {
                        formHandler.handleEvent(event);
                    }
                }
                cursor.close();
            } finally {
                this.events.clear();
            }
        }
    }

    /**
     * Inform the form that the values will be loaded.
     */
    public void informStartLoadingModel() {
        // nothing to do here
        // TODO - we could remove this method?
    }

    /**
     * Inform the form that the values are loaded.
     */
    public void informEndLoadingModel() {
        // Notify the end of the load phase
        if (this.listener != null) {
            this.listener.phaseEnded(new ProcessingPhaseEvent(this, ProcessingPhase.LOAD_MODEL));
        }
    }

    /**
     * Inform the form that the values will be saved.
     */
    public void informStartSavingModel() {
        // nothing to do here
        // TODO - we could remove this method?
    }

    /**
     * Inform the form that the values are saved.
     */
    public void informEndSavingModel() {
        // Notify the end of the save phase
        if (this.listener != null) {
            this.listener.phaseEnded(new ProcessingPhaseEvent(this, ProcessingPhase.SAVE_MODEL));
        }
    }

    /**
     * Get the locale to be used to process this form.
     *
     * @return the form's locale.
     */
    public Locale getLocale() {
        return this.locale;
    }

    /**
     * Get the widget that triggered the current processing. Note that it can be any widget, and
     * not necessarily an action or a submit.
     *
     * @return the widget that submitted this form.
     */
    public Widget getSubmitWidget() {
        return this.submitWidget;
    }

    /**
     * Set the widget that triggered the current form processing.
     *
     * @param widget the widget
     */
    public void setSubmitWidget(Widget widget) {
        if (this.submitWidget == widget) {
            return;
        }

        if (this.submitWidget != null) {
            throw new IllegalStateException("Submit widget already set to " + this.submitWidget +
                                            ". Cannot set also " + widget);
        }

        // Check that the submit widget is active
        if (widget.getCombinedState() != WidgetState.ACTIVE) {
            throw new IllegalStateException("Widget " + widget + " that submitted the form is not active.");
        }

        // If the submit widget is not an action (e.g. a field with an event listener),
        // we end form processing after the current phase and redisplay the form.
        // Actions (including submits) will call endProcessing() themselves and it's their
        // responsibility to indicate how form processing should continue.
        if (!(widget instanceof Action)) {
            endProcessing(true);
        }
        this.submitWidget = widget;
    }

    public boolean hasFormHandler() {
       return (this.formHandler != null);
    }

    public void setFormHandler(FormHandler formHandler) {
        this.formHandler = formHandler;
    }

    public void addProcessingPhaseListener(ProcessingPhaseListener listener) {
        this.listener = WidgetEventMulticaster.add(this.listener, listener);
    }

    public void removeProcessingPhaseListener(ProcessingPhaseListener listener) {
        this.listener = WidgetEventMulticaster.remove(this.listener, listener);
    }

    /**
     * Processes a form submit. If the form is finished, i.e. the form should not be redisplayed to the user,
     * then this method returns true, otherwise it returns false. To know if the form was sucessfully
     * validated, use the {@link #isValid()} method.
     * <p>
     * Form processing consists in multiple steps:
     * <ul>
     *  <li>all widgets read their value from the request (i.e.
     *      {@link #readFromRequest(FormContext)} is called recursively on
     *       the whole widget tree)
     *  <li>if there is an action event, call the FormHandler
     *  <li>perform validation.
     * </ul>
     * This processing can be interrupted by the widgets (or their event listeners) by calling
     * {@link #endProcessing(boolean)}.
     * <p>
     * Note that this method is synchronized as a Form is not thread-safe. This should not be a
     * bottleneck as such concurrent requests can only happen for a single user.
     */
    public synchronized boolean process(FormContext formContext) {
        // Is this an AJAX request?
        if (formContext.getRequest().getParameter("cocoon-ajax") != null) {
            this.updatedWidgets = new HashSet();
            this.childUpdatedWidgets = new HashSet();
        }

        // Fire the binding phase events
        fireEvents();

        // setup processing
        this.submitWidget = null;
        this.locale = formContext.getLocale();
        this.endProcessing = null;
        this.isValid = false;

        // Notify the end of the current phase
        if (this.listener != null) {
            this.listener.phaseEnded(new ProcessingPhaseEvent(this, ProcessingPhase.PROCESSING_INITIALIZE));
        }

        try {
            // Start buffering events
            this.bufferEvents = true;
            this.submitWidget = null;

            doReadFromRequest(formContext);

            // Find the submit widget, if not an action
            // This has to occur after reading from the request, to handle stateless forms
            // where the submit widget is recreated when the request is read (e.g. a row-action).

            // Note that we don't check this if the submit widget was already set, as it can cause problems
            // if the user triggers submit with an input (which sets 'forms_submit_id'), then clicks back
            // and submits using a regular submit button.
            if (getSubmitWidget() == null) {
                String submitId = formContext.getRequest().getParameter(SUBMIT_ID_PARAMETER);
                if (!StringUtils.isEmpty(submitId)) {
                    // if the form has an ID, it is used as part of the submitId too and must be removed
                    if(!StringUtils.isEmpty(this.getId())) {
                        submitId = submitId.substring(submitId.indexOf('.')+1);
                    }
                    Widget submit = this.lookupWidget(submitId.replace('.', '/'));
                    if (submit == null) {
                        throw new IllegalArgumentException("Invalid submit id (no such widget): " + submitId);
                    }
                    setSubmitWidget(submit);
                }
            }

            // Fire events, still buffering them: this ensures they will be handled in the same
            // order as they were added.
            fireEvents();

        } finally {
            // No need for buffering in the following phases
            this.bufferEvents = false;
        }

        // Notify the end of the current phase
        if (this.listener != null) {
            this.listener.phaseEnded(new ProcessingPhaseEvent(this, ProcessingPhase.READ_FROM_REQUEST));
        }

        if (this.endProcessing != null) {
            return this.endProcessing.booleanValue();
        }

        return validate();
    }

    /**
     * End the current form processing after the current phase.
     *
     * @param redisplayForm indicates if the form should be redisplayed to the user.
     */
    public void endProcessing(boolean redisplayForm) {
        // Set the indicator that terminates the form processing.
        // If redisplayForm is true, interaction is not finished and process() must
        // return false, hence the negation below.
        this.endProcessing = BooleanUtils.toBooleanObject(!redisplayForm);
    }

    /**
     * Was form validation successful ?
     *
     * @return <code>true</code> if the form was successfully validated.
     */
    public boolean isValid() {
        return this.isValid;
    }

    public void readFromRequest(FormContext formContext) {
        throw new UnsupportedOperationException("Please use Form.process()");
    }

    private void doReadFromRequest(FormContext formContext) {
        // let all individual widgets read their value from the request object
        super.readFromRequest(formContext);
    }

    /**
     * Set a validation error on this field. This allows the form to be externally marked as invalid by
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

    /**
     * Performs validation phase of form processing.
     */
    public boolean validate() {
        // Validate the form
        this.isValid = super.validate();

        // FIXME: Is this check needed, before invoking the listener?
        if (this.endProcessing != null) {
            this.wasValid = this.endProcessing.booleanValue();
            return this.wasValid;
        }

        // Notify the end of the current phase
        if (this.listener != null) {
            this.listener.phaseEnded(new ProcessingPhaseEvent(this, ProcessingPhase.VALIDATE));
        }

        if (this.endProcessing != null) {
            // De-validate the form if one of the listeners asked to end the processing
            // This allows for additional application-level validation.
            this.isValid = false;
            this.wasValid = this.endProcessing.booleanValue();
            return this.wasValid;
        }

        this.wasValid = this.isValid && this.validationError == null;
        return this.wasValid;
    }

    public String getXMLElementName() {
        return FORM_EL;
    }

    /**
     * @see org.apache.cocoon.forms.formmodel.AbstractWidget#getId()
     */
    public String getId() {
        if (this.id != null) {
            return this.id;
        }
        return super.getId();
    }

    /**
     * Set the optional id.
     * @param value A new id.
     */
    public void setId(String value) {
        this.id = value;
    }
}
