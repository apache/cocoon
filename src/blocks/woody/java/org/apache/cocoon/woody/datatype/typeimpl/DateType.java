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
package org.apache.cocoon.woody.datatype.typeimpl;

import java.util.Date;

/**
 * A {@link org.apache.cocoon.woody.datatype.Datatype Datatype} implementation for
 * java.util.Date's (so includes a time-component).
 * @version $Id: DateType.java,v 1.6 2004/03/09 13:53:53 reinhard Exp $
 */
public class DateType extends AbstractDatatype {

    public Class getTypeClass() {
        return Date.class;
    }

    public String getDescriptiveName() {
        return "date";
    }
}
