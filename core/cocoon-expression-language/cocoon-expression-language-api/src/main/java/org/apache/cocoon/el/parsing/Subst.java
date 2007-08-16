/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.el.parsing;

import java.util.Iterator;

import org.apache.cocoon.objectmodel.ObjectModel;

/**
 * @version $Id$
 */
public interface Subst {

    public Object getCompiledExpression();

    public Object getNode(ObjectModel objectModel) throws Exception;

    public Iterator getIterator(ObjectModel objectModel) throws Exception;

    public Boolean getBooleanValue(ObjectModel objectModel) throws Exception;

    public String getStringValue(ObjectModel objectModel) throws Exception;

    public Number getNumberValue(ObjectModel objectModel) throws Exception;

    public int getIntValue(ObjectModel objectModel) throws Exception;

    public Object getValue(ObjectModel objectModel) throws Exception;

    public void setLenient(Boolean lenient);

    public String getRaw();

    public static final Iterator EMPTY_ITER = new Iterator() {
            public boolean hasNext() {
                return false;
            }
    
            public Object next() {
                return null;
            }
    
            public void remove() {
                // EMPTY
            }
        };
    public static final Iterator NULL_ITER = new Iterator() {
            public boolean hasNext() {
                return true;
            }
    
            public Object next() {
                return null;
            }
    
            public void remove() {
                // EMPTY
            }
        };
    // VOID
}