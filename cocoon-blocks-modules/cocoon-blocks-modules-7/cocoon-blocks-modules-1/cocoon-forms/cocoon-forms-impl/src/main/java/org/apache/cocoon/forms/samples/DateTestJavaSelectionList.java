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
package org.apache.cocoon.forms.samples;

import java.util.Calendar;
import java.util.Date;

import org.apache.cocoon.forms.datatype.AbstractJavaSelectionList;

public class DateTestJavaSelectionList extends AbstractJavaSelectionList {

    /* (non-Javadoc)
     * @see org.apache.cocoon.forms.datatype.AbstractJavaSelectionList#build()
     */
    protected boolean build() throws Exception {
        Calendar c = Calendar.getInstance();
        c.set(2003, 0, 1);
        this.addItem(c.getTime(), (String)null);
        c.set(2004, 0, 1);
        this.addItem(c.getTime(), (String)null);
        this.addItem(new Date(), (String)null);
        return false;
    }
}
