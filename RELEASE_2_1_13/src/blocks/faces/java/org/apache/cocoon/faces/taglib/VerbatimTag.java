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
package org.apache.cocoon.faces.taglib;

import org.xml.sax.SAXException;

import javax.faces.component.UIComponent;
import javax.faces.component.UIOutput;

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
