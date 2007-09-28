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
package org.apache.cocoon.forms.datatype;

/**
 * A selection list that takes its values from the custom java class itself.
 * @version $Id$
 */
public interface JavaSelectionList extends SelectionList {

	void setDatatype(Datatype datatype);

	boolean isNullable();

	void setNullable(boolean nullable);

	String getAttribute(String name);

	void setAttribute(String name, String value);

	void removeAttribute(String name);
}
