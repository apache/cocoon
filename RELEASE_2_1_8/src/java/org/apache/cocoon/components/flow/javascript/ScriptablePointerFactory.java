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
package org.apache.cocoon.components.flow.javascript;

import org.apache.commons.jxpath.ri.model.NodePointerFactory;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.QName;
import java.util.Locale;
import org.mozilla.javascript.Scriptable;

/**
 *
 * @version CVS $Id: ScriptablePointerFactory.java,v 1.3 2004/03/05 13:02:46 bdelacretaz Exp $
 */
public class ScriptablePointerFactory implements NodePointerFactory {

    public int getOrder() {
        return 1;
    }

    public NodePointer createNodePointer(QName name, Object object,
                                         Locale locale) {
        if (object instanceof Scriptable) {
            return new ScriptablePointer(name, (Scriptable)object, locale);
        }
        return null;
    }

    public NodePointer createNodePointer(NodePointer parent,
                                         QName name, Object object) {
        if (object instanceof Scriptable) {
            return new ScriptablePointer(parent, name, 
                                         (Scriptable)object);
        }
        return null;
    }
}
