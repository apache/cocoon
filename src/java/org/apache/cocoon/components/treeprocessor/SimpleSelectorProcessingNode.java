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
package org.apache.cocoon.components.treeprocessor;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentSelector;
import org.apache.avalon.framework.thread.ThreadSafe;

/**
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: SimpleSelectorProcessingNode.java,v 1.3 2004/03/05 13:02:51 bdelacretaz Exp $
 */

public abstract class SimpleSelectorProcessingNode extends SimpleParentProcessingNode {

    /** The node component name (e.g. action name, selector name, etc) */
    protected String componentName;

    /** Selector where to get components from */
    protected ComponentSelector selector;

    public SimpleSelectorProcessingNode(String componentName) {
        this.componentName = componentName;
    }

    public void setSelector(ComponentSelector selector) throws ComponentException {
        this.selector = selector;
    }

    /**
     * Tests if the component designated by this node using the selector and component name
     * is <code>ThreadSafe</code>, and return it if true.
     * <p>
     * Note : this method must be called <i>after</i> <code>setSelector()</code>.
     */
    protected Component getThreadSafeComponent() throws ComponentException {
        return getThreadSafeComponent(this.componentName);
    }

    /**
     * Tests if the component designated by this node using the selector and component name
     * is <code>ThreadSafe</code>, and return it if true.
     * <p>
     * Note : this method must be called <i>after</i> <code>setSelector()</code>.
     */
    protected Component getThreadSafeComponent(String name) throws ComponentException {
        Component component = this.selector.select(name);
        if (component instanceof ThreadSafe) {
            return component;
        } else {
            this.selector.release(component);
            return null;
        }
    }
}
