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

import org.apache.cocoon.objectmodel.ObjectModel;

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
    public Literal(String val) {
        this.value = val.toCharArray();
    }

    public String getValue() {
        return new String(this.value);
    }

    public char[] getCharArray() {
        return value;
    }

    public void setLenient(Boolean lenient) {
    }

    public Object getValue(ObjectModel objectModel) throws Exception {
        return null;
    }

    public int getIntValue(ObjectModel objectModel) throws Exception {
        return 0;
    }

    public Number getNumberValue(ObjectModel objectModel) throws Exception {
        return null;
    }

    public String getStringValue(ObjectModel objectModel) throws Exception {
        return null;
    }

    public Boolean getBooleanValue(ObjectModel objectModel) throws Exception {
        return null;
    }

    public Iterator getIterator(ObjectModel objectModel) throws Exception {
        return null;
    }

    public Object getNode(ObjectModel objectModel) throws Exception {
        return null;
    }

    public Object getCompiledExpression() {
        return null;
    }

    private final char[] value;
}
