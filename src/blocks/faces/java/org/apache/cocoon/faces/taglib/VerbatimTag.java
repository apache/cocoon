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
package org.apache.cocoon.faces.taglib;

import org.apache.cocoon.taglib.TagSupport;

import org.apache.cocoon.faces.FacesUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import javax.faces.component.UIComponent;
import javax.faces.component.UIOutput;
import javax.faces.component.UIParameter;
import javax.faces.FacesException;

/**
 * @version CVS $Id$
 */
public class VerbatimTag extends UIComponentBodyTag {

    protected String getComponentType() {
        return "javax.faces.Output";
    }

    protected String getRendererType() {
        return "javax.faces.Text";
    }

    protected void setProperties(UIComponent component) {
        super.setProperties(component);
        // TODO: VerbatimTag: Implement escape attribute support
        component.setTransient(true);
    }

    public void recycle() {
        super.recycle();
    }

    public int doAfterBody() throws SAXException {
        if (content != null) {
            String value = content.getContent().toString();
            ((UIOutput)getComponentInstance()).setValue(value);
        }

        return getDoAfterBody();
    }
}
