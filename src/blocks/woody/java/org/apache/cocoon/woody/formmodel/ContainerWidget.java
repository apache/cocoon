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
package org.apache.cocoon.woody.formmodel;
import java.util.Iterator;

/**
 * Interface to be implemented by Widgets which contain other widgets.
 *
 * @author Timothy Larson
 * @version $Id: ContainerWidget.java,v 1.9 2004/03/09 13:53:55 reinhard Exp $
 */
public interface ContainerWidget extends Widget {
    
    /**
     * Adds a child widget.
     */
    public void addWidget(Widget widget);

    /**
     * Checks if there is a child widget with the given id.
     */
    public boolean hasWidget(String id);

    /**
     * Gets the child widget with the given id.
     * Returns null if there is no child with the given id.
     */
    public Widget getWidget(String id);

    /**
     * Returns an iterator over the widgets this object contains
     */
    public Iterator getChildren();

}
