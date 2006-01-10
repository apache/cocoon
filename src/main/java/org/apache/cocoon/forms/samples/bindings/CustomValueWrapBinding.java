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
package org.apache.cocoon.forms.samples.bindings;

import org.apache.cocoon.forms.binding.AbstractCustomBinding;
import org.apache.cocoon.forms.binding.Binding;
import org.apache.cocoon.forms.binding.BindingException;
import org.apache.cocoon.forms.formmodel.Widget;
import org.apache.cocoon.forms.util.DomHelper;
import org.apache.commons.jxpath.JXPathContext;
import org.w3c.dom.Element;

/**
 * CustomValueWrapBinding
 */
public class CustomValueWrapBinding extends AbstractCustomBinding {

    private static final char DEFAULT_DELIMITER = '*';
    private final String prefix;
    private final String suffix;
    
    public CustomValueWrapBinding() {
        this(DEFAULT_DELIMITER);
    }

    public CustomValueWrapBinding(char delimiter) {
        this(delimiter, delimiter);
    }
    
    public CustomValueWrapBinding(char prefix, char suffix) {
        this.prefix = ""+ prefix + prefix;
        this.suffix = "" + suffix + suffix;
    }

    /**
     * This unwraps the value from the model by removing the 2 prefix and suffix-chars 
     * before setting it onto the model
     * 
     * Method signature and semantics complies to {@link AbstractCustomBinding#doLoad(Widget, JXPathContext)}
     */
    public void doLoad(Widget frmModel, JXPathContext jxpc) throws BindingException {
        String appValue = (String)jxpc.getValue(".");
        String formValue= null;
        if (appValue.startsWith(this.prefix) 
                && appValue.endsWith(suffix) 
                && appValue.length() >= this.prefix.length() + this.suffix.length()) {
            formValue = appValue.substring(this.prefix.length(), 
                                           appValue.length() - this.suffix.length());
        }        
        frmModel.setValue(formValue);
    }

    /**
     * This wraps the value from the form between 2 prefix and suffix-chars 
     * before saving to the model 
     * 
     * Method signature and semantics complies to {@link AbstractCustomBinding#doSave(Widget, JXPathContext)}
     */
    public void doSave(Widget frmModel, JXPathContext jxpc) throws BindingException {
        Object formValue = frmModel.getValue();       
        jxpc.setValue(".", "" + this.prefix + formValue + this.suffix);        
    }
    
    
    /** 
     * Builds the actual binding class based on its XML config.
     * 
     * @param config the {@link Element} holding the config for the binding to create.
     * @return the configured binding
     * @throws BindingException when the creation fails
     */
    public static Binding createBinding(Element config) throws BindingException{
        
        try {
            String pfx = DomHelper.getAttribute(config, "prefixchar", null);
            String sfx = DomHelper.getAttribute(config, "suffixchar", null);
            
            final char prefixChar = (pfx!=null) ? pfx.charAt(0) : DEFAULT_DELIMITER;
            final char suffixChar = (sfx!=null) ? sfx.charAt(0) : DEFAULT_DELIMITER;
            
            return new CustomValueWrapBinding(prefixChar, suffixChar);
        } catch (Exception e) {
            throw new BindingException("Could not create instance of CustomValueWrapBinding." ,e);
        }
    }
}
