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
package org.apache.garbage.tree;

import java.util.Iterator;

/**
 * 
 * 
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>, February 2003
 * @version CVS $Id: Events.java,v 1.2 2004/03/05 10:07:24 bdelacretaz Exp $
 */
public interface Events {

    /**
     * Add a new event to this <code>Events</code>.
     *
     * @param event The <code>Event</code> instance to add.
     */
    public void append(Event event);

    /**
     * Return an <code>Iterator</code> over the events contained in this
     * <code>Events</code> instance.
     */
    public Iterator iterator();

}
