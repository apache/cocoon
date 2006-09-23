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

import java.util.Locale;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Interface to be implemented by selection lists.
 * @version $Id$
 */
public interface SelectionList {
    
    public static final String SELECTION_LIST_EL = "selection-list";
    public static final String ITEM_EL = "item";
    public static final String LABEL_EL = "label";

    Datatype getDatatype();

    void generateSaxFragment(ContentHandler contentHandler, Locale locale) throws SAXException;
}
