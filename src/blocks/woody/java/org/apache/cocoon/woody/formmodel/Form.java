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
package org.apache.cocoon.woody.formmodel;

import java.util.Iterator;
import java.util.Locale;
import java.util.StringTokenizer;

import org.apache.cocoon.woody.Constants;
import org.apache.cocoon.woody.FormContext;
import org.apache.cocoon.woody.event.FormHandler;
import org.apache.cocoon.woody.event.ProcessingPhase;
import org.apache.cocoon.woody.event.ProcessingPhaseEvent;
import org.apache.cocoon.woody.event.ProcessingPhaseListener;
import org.apache.cocoon.woody.event.WidgetEvent;
import org.apache.cocoon.woody.event.WidgetEventMulticaster;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.commons.collections.CursorableLinkedList;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * A widget that serves as a container for other widgets, the top-level widget in
 * a form description file.
 * 
 * @author Bruno Dumon
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @version CVS $Id: Form.java,v 1.13 2003/12/29 17:52:12 stefano Exp $
 */
public class Form extends AbstractContainerWidget {
    
    private Boolean endProcessing;
    private Locale locale = Locale.getDefault();
    private CursorableLinkedList events;
    private FormDefinition definition;
    private FormHandler formHandler;
    private Widget submitWidget;
    private ProcessingPhase phase = ProcessingPhase.LOAD_MODEL;
    private boolean isValid = false;
    private ProcessingPhaseListener listener;

    public Form(FormDefinition definition) {
        super(definition);
        setLocation(definition.getLocation());
    }

    /**
     * Events produced by child widgets should not be fired immediately, but queued in order to ensure
     * an overall consistency of the widget tree before being handled.
     * 
     * @param event the event to queue
     */
    public void addWidgetEvent(WidgetEvent event) {
        
        if (this.events == null) {
            this.events = new CursorableLinkedList();
        }
        
        // FIXME: limit the number of events to detect recursive event loops ?
        this.events.add(event);
    }
    
    /**
     * Fire the widget events that have been queued. Note that event handling can fire new
     * events.
     */
    private void fireWidgetEvents() {
        if (this.events != null) {
            CursorableLinkedList.Cursor cursor = this.events.cursor();
            while(cursor.hasNext()) {
                WidgetEvent event = (WidgetEvent)cursor.next();
                event.getSourceWidget().broadcastEvent(event);
                if (formHandler != null)
                    formHandler.handleEvent(event);
            }
            cursor.close();
        
            this.events.clear();
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
        if (this.submitWidget != null && this.submitWidget != widget) {
            throw new IllegalStateException("SubmitWidget can only be set once.");
        }
        if (!(widget instanceof Action)) {
            endProcessing(true);
        }
        this.submitWidget = widget;
    }

    public void setFormHandler(FormHandler formHandler) {
        this.formHandler = formHandler;
    }

// TODO: going through the form for load and save ensures state consistency. To we add this or
// keep the binding strictly separate ?
//    public void load(Object data, Binding binding) {
//        if (this.phase != ProcessingPhase.LOAD_MODEL) {
//            throw new IllegalStateException("Cannot load form in phase " + this.phase);
//        }
//        binding.loadFormFromModel(this, data);
//    }
//
//    public void save(Object data, Binding binding) throws BindingException {
//        if (this.phase != ProcessingPhase.VALIDATE) {
//            throw new IllegalStateException("Cannot save model in phase " + this.phase);
//        }
//        
//        if (!isValid()) {
//            throw new IllegalStateException("Cannot save an invalid form.");
//        }
//        this.phase = ProcessingPhase.SAVE_MODEL;
//        binding.saveFormToModel(this, data);
//    }

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
     *  <li>all widgets read their value from the request (i.e. {@link #readFromRequest} is called recursively on
     *       the whole widget tree)
     *  <li>if there is an action event, call the FormHandler
     *  <li>perform validation.
     * </ul>
     * This processing can be interrupted by the widgets (or their event listeners) by calling
     * {@link #endProcessing(boolean)}.
     */
    public boolean process(FormContext formContext) {
        
        // Fire the binding phase events
        fireWidgetEvents();
        
        // setup processing
        this.submitWidget = null;
        this.locale = formContext.getLocale();
        this.endProcessing = null;
        this.isValid = false;
        
        // Notify the end of the current phase
        if (this.listener != null) {
            this.listener.phaseEnded(new ProcessingPhaseEvent(this, this.phase));
        }
        
        this.phase = ProcessingPhase.READ_FROM_REQUEST;
        // Find the submit widget, if not an action
        this.submitWidget = null;
        String submitId = formContext.getRequest().getParameter("woody_submit_id");
        if (submitId != null && submitId.length() > 0) {
            StringTokenizer stok = new StringTokenizer(submitId, ".");
            Widget submit = this;
            while (stok.hasMoreTokens()) {
                submit = submit.getWidget(stok.nextToken());
                if (submit == null) {
                    throw new IllegalArgumentException("Invalid submit id (no such widget): " + submitId);
                }
            }
            
            setSubmitWidget(submit);
        }
        
        doReadFromRequest(formContext);
        fireWidgetEvents();
        
        // Notify the end of the current phase
        if (this.listener != null) {
            this.listener.phaseEnded(new ProcessingPhaseEvent(this, this.phase));
        }

        if (this.endProcessing != null) {
            return this.endProcessing.booleanValue();
        }

        // Validate the form
        this.phase = ProcessingPhase.VALIDATE;
        this.isValid = doValidate(formContext);
        
        if (this.endProcessing != null) {
            return this.endProcessing.booleanValue();
        }
        
        // Notify the end of the current phase
        if (this.listener != null) {
            this.listener.phaseEnded(new ProcessingPhaseEvent(this, this.phase));
        }
        
        if (this.endProcessing != null) {
            // De-validate the form if one of the listeners asked to end the processing
            // This allows for additional application-level validation.
            this.isValid = false;
            return this.endProcessing.booleanValue();
        }
        
        return this.isValid;
    }
    
    /**
     * End the current form processing after the current phase.
     * 
     * @param redisplayForm indicates if the form should be redisplayed to the user.
     */
    public void endProcessing(boolean redisplayForm) {
        this.endProcessing = new Boolean(!redisplayForm);
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

    public boolean validate(FormContext formContext) {
        throw new UnsupportedOperationException("Please use Form.process()");
    }

    public boolean doValidate(FormContext formContext) {
        return super.validate(formContext); 
    }

    private static final String FORM_EL = "form";
    private static final String CHILDREN_EL = "children";

    public void generateSaxFragment(ContentHandler contentHandler, Locale locale) throws SAXException {
        AttributesImpl formAttrs = new AttributesImpl();
        formAttrs.addCDATAAttribute("id", definition.getId());
        contentHandler.startElement(Constants.WI_NS, FORM_EL, Constants.WI_PREFIX_COLON + FORM_EL, Constants.EMPTY_ATTRS);
        definition.generateLabel(contentHandler);

        contentHandler.startElement(Constants.WI_NS, CHILDREN_EL, Constants.WI_PREFIX_COLON + CHILDREN_EL, Constants.EMPTY_ATTRS);
        Iterator widgetIt = widgets.iterator();
        while (widgetIt.hasNext()) {
            Widget widget = (Widget)widgetIt.next();
            widget.generateSaxFragment(contentHandler, locale);
        }
        contentHandler.endElement(Constants.WI_NS, CHILDREN_EL, Constants.WI_PREFIX_COLON + CHILDREN_EL);

        contentHandler.endElement(Constants.WI_NS, FORM_EL, Constants.WI_PREFIX_COLON + FORM_EL);
    }

    public void generateLabel(ContentHandler contentHandler) throws SAXException {
        definition.generateLabel(contentHandler);
    }
}
