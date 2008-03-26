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
package org.apache.cocoon.template.environment;

import org.xml.sax.Locator;

/**
 * Facade to the Locator to be set on the consumer prior to sending other
 * events, location member changeable
 * @version SVN $Id$
 */
public class LocatorFacade implements Locator {
    private Locator locator;

    public LocatorFacade(Locator initialLocator) {
        this.locator = initialLocator;
    }

    public void setDocumentLocator(Locator newLocator) {
        this.locator = newLocator;
    }

    public int getColumnNumber() {
        return this.locator.getColumnNumber();
    }

    public int getLineNumber() {
        return this.locator.getLineNumber();
    }

    public String getPublicId() {
        return this.locator.getPublicId();
    }

    public String getSystemId() {
        return this.locator.getSystemId();
    }
}