/*
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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
package org.apache.cocoon.portal.event.impl;

/**
 * This events changes the value of an instance
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: JXPathEvent.java,v 1.3 2004/03/05 13:02:12 bdelacretaz Exp $
 */
public class JXPathEvent
    extends AbstractActionEvent {

    protected String path;
    
    protected Object value;
    
    /**
     * @return Returns the path.
     */
    public String getPath() {
        return path;
    }

    /**
     * @return Returns the value.
     */
    public Object getValue() {
        return value;
    }

    public JXPathEvent(Object target, String path, Object value) {
        super( target );
        this.path = path;
        this.value = value;
    }

}
