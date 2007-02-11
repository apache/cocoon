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
package org.apache.cocoon.forms.formmodel;

/**
 * The definition of an upload widget.
 * 
 * @author <a href="mailto:uv@upaya.co.uk">Upayavira</a>
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @version CVS $Id$
 */
public class UploadDefinition extends AbstractWidgetDefinition {
    private boolean required;
    private String mimeTypes;
    
    public UploadDefinition(boolean required, String mimeTypes) {
        this.required = required;
        this.mimeTypes = mimeTypes;
    }

    public Widget createInstance() {
        Upload upload = new Upload(this);
        return upload;
    }

    public boolean isRequired() {
        return required;
    }
    
    public String getMimeTypes() {
        return this.mimeTypes;
    }
}
