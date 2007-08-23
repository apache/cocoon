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
package org.apache.cocoon.template.expression;

import java.util.Iterator;

import org.apache.cocoon.el.objectmodel.ObjectModel;
import org.apache.cocoon.el.parsing.Subst;

/*
  From efficiency reasons it might be better to split this class in
  two, one that represent attribute content that are strings and one
  for Character content that are handled as char arrays. Here the
  content is stored as a char array as there in most cases will be
  much more content in elements than in attributes, so it is better to
  avoid copying there. 
*/
/**
 * @version $Id$
 */
public class Literal implements Subst {
    
    private final char[] charValue;
    private final Object value;
    
    public Literal(String val) {
        this.value = val;
        this.charValue = val.toCharArray();
    }
    
    public Literal(Integer val) {
        this.value = val;
        this.charValue = val.toString().toCharArray();
    }
    
    public Literal(Boolean val) {
        this.value = val;
        this.charValue = val.toString().toCharArray();
    }

    public char[] getCharArray() {
        return charValue;
    }

    public void setLenient(Boolean lenient) {
        //ignore
    }

    public Object getValue(ObjectModel objectModel) throws Exception {
        return value;
    }

    public int getIntValue(ObjectModel objectModel) throws Exception {
        return value instanceof Integer ? ((Integer)value).intValue() : Integer.parseInt(getStringValue(objectModel));
    }

    public Number getNumberValue(ObjectModel objectModel) throws Exception {
        throw new UnsupportedOperationException();
    }

    public String getStringValue(ObjectModel objectModel) throws Exception {
        return value.toString();
    }

    public Boolean getBooleanValue(ObjectModel objectModel) throws Exception {
        throw new UnsupportedOperationException();
    }

    public Iterator getIterator(ObjectModel objectModel) throws Exception {
        throw new UnsupportedOperationException();
    }

    public Object getNode(ObjectModel objectModel) throws Exception {
        return value;
    }

    public Object getCompiledExpression() {
        throw new UnsupportedOperationException();
    }

    public String getRaw() {
        return value.toString();
    }

}
