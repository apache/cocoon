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
package org.apache.cocoon.forms.event.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.forms.event.ProcessingPhase;
import org.apache.cocoon.forms.event.ProcessingPhaseEvent;
import org.apache.cocoon.forms.event.ProcessingPhaseListener;
import org.apache.cocoon.forms.formmodel.Field;
import org.apache.cocoon.forms.formmodel.Widget;

/**
 * This processing phase listener can be used to dynamically change the
 * required fields in a form.
 * A field can be required depending on the value of another field or
 * depending on the pressed submit button.
 *
 * @version $Id$
 */
public class RequiredControl
    implements ProcessingPhaseListener, Configurable {

    protected final List descriptions = new ArrayList();

    /**
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration config) throws ConfigurationException {
        final Configuration[] children = config.getChildren("required");
        for(int i=0; i<children.length; i++) {
            final Configuration current = children[i];
            RequiredDescription desc;
            final String refId = current.getAttribute("widget-id");
            if ( current.getAttribute("submit-id", null) != null ) {
                desc = new RequiredDescription(refId, current.getAttribute("submit-id"));
            } else {
                final String path = current.getAttribute("widget-path");
                final String value = current.getAttribute("widget-value");
                desc = new RequiredDescription(refId, path, value);
            }
            this.descriptions.add(desc);
        }
    }

    /**
     * @see org.apache.cocoon.forms.event.ProcessingPhaseListener#phaseEnded(org.apache.cocoon.forms.event.ProcessingPhaseEvent)
     */
    public void phaseEnded(ProcessingPhaseEvent event) {
        if ( event.getPhase().getValue() == ProcessingPhase.READ_FROM_REQUEST_VALUE 
             || event.getPhase().getValue() == ProcessingPhase.LOAD_MODEL_VALUE) {
            final Iterator i = this.descriptions.iterator();
            while ( i.hasNext() ) {
                final RequiredDescription desc = (RequiredDescription)i.next();
                desc.process(event.getSourceWidget());
            }
        }
    }

    protected static final class RequiredDescription {

        final static int DEPENDS_REQUIRED_MODE = 1;
        final static int SUBMIT_REQUIRED_MODE = 2;

        protected final int mode;
        protected final String referenceId;
        protected String widgetName;
        protected String widgetValue;

        public RequiredDescription(String referenceId, String submitId) {
            this.mode = SUBMIT_REQUIRED_MODE;
            this.referenceId = referenceId;
            this.widgetName = submitId;
        }

        public RequiredDescription(String referenceId, String widget, String value) {
            this.mode = DEPENDS_REQUIRED_MODE;
            this.referenceId = referenceId;
            this.widgetName = widget;
            this.widgetValue = value;
        }

        /**
         * @see org.apache.cocoon.forms.validation.WidgetValidator#validate(org.apache.cocoon.forms.formmodel.Widget)
         */
        public void process(Widget form) {
            final Widget widget = form.lookupWidget(this.referenceId);
            if ( widget == null ) {
                throw new IllegalArgumentException("Widget '" + this.referenceId + "' not found in form.");
            }
            if (! (widget instanceof Field)) {
                // Invalid widget type
                throw new IllegalArgumentException("Widget '" + widget.getRequestParameterName() + "' is not a Field");
            }

            boolean required = false;
            if ( mode == DEPENDS_REQUIRED_MODE ) {
                final Widget w = form.lookupWidget(this.widgetName);
                if ( w != null ) {
                    if ( w.getValue() != null && w.getValue().equals(this.widgetValue)) {
                        required = true;
                    }
                }
            } else if ( mode == SUBMIT_REQUIRED_MODE ) {
                if ( widget.getForm().getSubmitWidget() != null) {
                    if ( this.widgetName.equals(widget.getForm().getSubmitWidget().getId())) {
                        required = true;
                    }
                }
            }
            ((Field)widget).setRequired(required);
        }
    }
}
