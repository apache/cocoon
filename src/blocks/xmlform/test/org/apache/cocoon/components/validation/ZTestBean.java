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
package org.apache.cocoon.components.validation;

import java.util.ArrayList;

/**
 * Just a test bean.
 *
 * @version CVS $Id: ZTestBean.java,v 1.2 2004/03/05 13:02:38 bdelacretaz Exp $
 */
public class ZTestBean {
    private String name = "dog";
    private String scope = "galaxy";
    private int count = 0;
    private ArrayList preferences = new ArrayList();
    private ZNestedBean personal = new ZNestedBean();

    public ZTestBean() {
        preferences.add("likeVodka");
        preferences.add("likeSkiing");
    }

    public ZTestBean(String newName, String newScope) {
        this();
        name = newName;
        scope = newScope;
    }

    public String getName() {
        return name;
    }

    public void setName(String newName) {
        name = newName;
    }

    public void setScope(String newScope) {
        scope = newScope;
    }

    public String getScope() {
        return scope;
    }

    public ArrayList getPreferences() {
        return preferences;
    }

    public ZNestedBean getPersonalInfo() {
        return personal;
    }

    public void setPersonalInfo(ZNestedBean newPersonal) {
        personal = newPersonal;
    }

    public int getCount() {
        return count;
    }

    public void incrementCount() {
        count++;
    }
}
