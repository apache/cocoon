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
package org.apache.cocoon.samples.parentcm;

import org.apache.avalon.framework.component.Component;

import java.util.Date;

/**
 * Interface for a simple time-keeping component.
 * @author ?
 * @version CVS $Id: Time.java,v 1.2 2004/03/10 09:54:05 cziegeler Exp $
 */
public interface Time extends Component {

    String ROLE = Time.class.getName();

    /**
     * Gets the current time.
     */
    Date getTime ();
}

