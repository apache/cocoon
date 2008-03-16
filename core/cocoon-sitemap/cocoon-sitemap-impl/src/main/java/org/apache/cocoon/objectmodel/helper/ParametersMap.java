/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
/**
 * 
 */
package org.apache.cocoon.objectmodel.helper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.el.objectmodel.ObjectModel;

/**
 * <p>Wrapper class for {@link Parameters} class that exposes parameters through {@link Map} interface.</p>
 * <p>Use this wrapper if you want to put {@link Parameters} in {@link ObjectModel}.</p>
 */
public final class ParametersMap extends Parameters implements Map {

    protected final Parameters wrappedParameters;
    protected Map map;

    public ParametersMap(Parameters wrapped) {
        wrappedParameters = wrapped;
    }

    public boolean equals(Object arg0) {
        return wrappedParameters.equals(arg0);
    }

    public String[] getNames() {
        return wrappedParameters.getNames();
    }

    public String getParameter(String arg0, String arg1) {
        return wrappedParameters.getParameter(arg0, arg1);
    }

    public String getParameter(String arg0) throws ParameterException {
        return wrappedParameters.getParameter(arg0);
    }

    public boolean getParameterAsBoolean(String arg0, boolean arg1) {
        return wrappedParameters.getParameterAsBoolean(arg0, arg1);
    }

    public boolean getParameterAsBoolean(String arg0) throws ParameterException {
        return wrappedParameters.getParameterAsBoolean(arg0);
    }

    public float getParameterAsFloat(String arg0, float arg1) {
        return wrappedParameters.getParameterAsFloat(arg0, arg1);
    }

    public float getParameterAsFloat(String arg0) throws ParameterException {
        return wrappedParameters.getParameterAsFloat(arg0);
    }

    public int getParameterAsInteger(String arg0, int arg1) {
        return wrappedParameters.getParameterAsInteger(arg0, arg1);
    }

    public int getParameterAsInteger(String arg0) throws ParameterException {
        return wrappedParameters.getParameterAsInteger(arg0);
    }

    public long getParameterAsLong(String arg0, long arg1) {
        return wrappedParameters.getParameterAsLong(arg0, arg1);
    }

    public long getParameterAsLong(String arg0) throws ParameterException {
        return wrappedParameters.getParameterAsLong(arg0);
    }

    public Iterator getParameterNames() {
        return wrappedParameters.getParameterNames();
    }

    public int hashCode() {
        return wrappedParameters.hashCode();
    }

    public boolean isParameter(String arg0) {
        return wrappedParameters.isParameter(arg0);
    }

    public void makeReadOnly() {
        wrappedParameters.makeReadOnly();
    }

    public Parameters merge(Parameters arg0) {
        return wrappedParameters.merge(arg0);
    }

    public void removeParameter(String arg0) {
        wrappedParameters.removeParameter(arg0);
    }

    public String setParameter(String arg0, String arg1) throws IllegalStateException {
        return wrappedParameters.setParameter(arg0, arg1);
    }

    public void clear() {
        this.checkWriteable();
    }

    protected Map getMap() {
        if ( this.map == null ) {
            this.map = new HashMap();
            String[] names = this.getNames();
            for(int i=0; i<names.length;i++) {
                map.put(names[i], this.getParameter(names[i], null));
            }
        }
        return this.map;
    }

    public boolean containsKey(Object arg0) {
        if ( arg0 == null ) {
            return false;
        }
        return this.getParameter(arg0.toString(), null) != null;
    }

    public boolean containsValue(Object arg0) {
        return this.getMap().containsValue(arg0);
    }

    public Set entrySet() {
        return this.getMap().entrySet();
    }

    public Object get(Object arg0) {
        if ( arg0 == null ) {
            return null;
        }
        return this.getParameter(arg0.toString(), null);
    }

    public boolean isEmpty() {
        return this.getNames().length == 0;
    }

    public Set keySet() {
        return this.getMap().keySet();
    }

    public Object put(Object arg0, Object arg1) {
        this.checkWriteable();
        return null;
    }

    public void putAll(Map arg0) {
        this.checkWriteable();
    }

    public Object remove(Object arg0) {
        this.checkWriteable();
        return null;
    }

    public int size() {
        return this.getNames().length;
    }

    public Collection values() {
        return this.getMap().values();
    }
}