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
package org.apache.cocoon.faces;

import org.apache.cocoon.faces.taglib.UIComponentTag;
import org.apache.cocoon.taglib.Tag;

import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import java.util.Iterator;
import java.util.Map;

/**
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id$
 */
public class FacesUtils {

    /**
     * Key of the {@link FacesContext} in the object model.
     */
    private static final String FACES_CONTEXT_OBJECT = "javax.faces.webapp.FACES_CONTEXT";

    /**
     * Find current FacesContext, and store it in the objectModel
     */
    public static FacesContext getFacesContext(Tag tag, Map objectModel) {
        FacesContext context = (FacesContext) objectModel.get(FACES_CONTEXT_OBJECT);
        if (context == null) {
            context = FacesContext.getCurrentInstance();
            if (context == null) {
                throw new FacesException("Tag <" + tag.getClass().getName() + "> " +
                                         "could not find current FacesContext");
            }
            objectModel.put(FACES_CONTEXT_OBJECT, context);
        }

        return context;
    }

    /**
     * Find child component by ID
     */
    public static UIComponent getChild(UIComponent component, String id) {
        for (Iterator kids = component.getChildren().iterator(); kids.hasNext();) {
            UIComponent kid = (UIComponent) kids.next();
            if (id.equals(kid.getId())) {
                return kid;
            }
        }

        return null;
    }

    /**
     * Remove child component by ID
     */
    public static UIComponent removeChild(UIComponent component, String id) {
        UIComponent kid = getChild(component, id);
        if (kid != null) {
            component.getChildren().remove(kid);
        }

        return kid;
    }

    /**
     * Is this an expression?
     */
    public static boolean isExpression(String value) {
        if (value == null) {
            return false;
        }

        int i = value.indexOf("#{");
        return i != -1 && i < value.indexOf('}');
    }

    /**
     * Evaluate expression
     */
    public static Object evaluate(FacesContext context, String value) {
        if (isExpression(value)) {
            return context.getApplication().createValueBinding(value).getValue(context);
        }

        return value;
    }

    /**
     *
     */
    public static UIComponentTag findParentUIComponentTag(Tag tag) {
        Tag parent = tag;
        do {
            parent = parent.getParent();
        } while (parent != null && !(parent instanceof UIComponentTag));

        return (UIComponentTag) parent;
    }
}
