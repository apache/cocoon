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
import java.util.Iterator;

/**
 * Interface to be implemented by Widgets which contain other widgets. So all
 * widgets together form a widget tree, with its root being the {@link Form}
 * widget, the {@link ContainerWidget}s being the branches/forks, and the
 * {@link Widget}s with values being the leaves.
 *
 * @version $Id: ContainerWidget.java,v 1.5 2004/05/07 16:43:42 mpo Exp $
 */
public interface ContainerWidget extends Widget {
    
    /**
     * Adds a child widget.
     */
    public void addChild(Widget widget);
    //TODO: check to remove since we have no removeChild
    

    /**
     * Checks if there is a child widget with the given id.
     */
    public boolean hasChild(String id);

    /**
     * Gets the child widget with the given id.
     * @return null if there is no child with the given id.
     */
    public Widget getChild(String id);

    /**
     * @return an iterator over the widgets this object contains
     */
    public Iterator getChildren();

}
