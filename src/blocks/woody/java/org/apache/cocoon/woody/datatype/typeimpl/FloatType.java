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

/**
 * A {@link org.apache.cocoon.woody.datatype.Datatype Datatype} implementation
 * for float numbers (backed by the java.lang.Float class).
 * @version $Id: FloatType.java,v 1.3 2004/03/05 13:02:29 bdelacretaz Exp $
 */
public class FloatType extends AbstractDatatype {
    public Class getTypeClass() {
        return Float.class;
    }

    public String getDescriptiveName() {
        return "float";
    }
}
