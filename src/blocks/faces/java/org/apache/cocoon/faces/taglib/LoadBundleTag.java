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
package org.apache.cocoon.faces.taglib;

import org.apache.cocoon.taglib.TagSupport;
import org.apache.cocoon.util.ClassUtils;

import org.apache.cocoon.faces.FacesUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import javax.faces.FacesException;
import javax.faces.context.FacesContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * @version CVS $Id$
 */
public class LoadBundleTag extends TagSupport {

    private String basename;
    private String var;

    public void setBasename(String basename) {
        this.basename = basename;
    }

    public void setVar(String var) {
        this.var = var;
    }

    public int doStartTag(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        final FacesContext context = FacesUtils.getFacesContext(this, objectModel);

        final String basename = (String) FacesUtils.evaluate(context, this.basename);

        final ResourceBundle bundle = ResourceBundle.getBundle(basename,
                                                               context.getViewRoot().getLocale(),
                                                               ClassUtils.getClassLoader());

        if (bundle == null) {
            throw new FacesException("Tag <" + getClass().getName() + "> " +
                                     "could not find ResourceBundle for <" + basename + ">");
        }

        context.getExternalContext().getRequestMap().put(this.var, new BundleMap(bundle));

        return SKIP_BODY;
    }

    public void recycle() {
        super.recycle();
        this.basename = null;
        this.var = null;
    }
}

class BundleMap implements Map {
    private ResourceBundle bundle;

    public BundleMap (ResourceBundle bundle) {
        this.bundle = bundle;
    }

    public void clear() {
        throw new UnsupportedOperationException("BundleMap does not support clear()");
    }

    public boolean containsKey(Object key) {
        if (key == null) {
            return false;
        }

        try {
            this.bundle.getObject(key.toString());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean containsValue(Object value) {
        if (value == null) {
            return false;
        }

        for (Enumeration i = this.bundle.getKeys(); i.hasMoreElements();) {
            Object obj = bundle.getObject((String) i.nextElement());
            if (value == obj || value.equals(obj)) {
                return true;
            }
        }

        return false;
    }

    public Set entrySet() {
        final HashMap entries = new HashMap();
        for (Enumeration i = this.bundle.getKeys(); i.hasMoreElements();) {
            String key = (String) i.nextElement();
            entries.put(key, this.bundle.getObject(key));
        }

        return entries.entrySet();
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Map)) {
            return false;
        }

        return entrySet().equals(((Map) obj).entrySet());
    }

    public Object get(Object key) {
        if (key == null) {
            return null;
        }

        return bundle.getObject(key.toString());
    }

    public int hashCode() {
        return this.bundle.hashCode();
    }

    public boolean isEmpty() {
        return !this.bundle.getKeys().hasMoreElements();
    }

    public Set keySet() {
        final Set keys = new HashSet();
        for (Enumeration i = this.bundle.getKeys(); i.hasMoreElements();) {
            keys.add(i.nextElement());
        }
        return keys;
    }

    public Object put(Object k, Object v) {
        throw new UnsupportedOperationException("BundleMap does not support put()");
    }

    public void putAll(Map t) {
        throw new UnsupportedOperationException("BundleMap does not support putAll()");
    }

    public Object remove(Object k) {
        throw new UnsupportedOperationException("BundleMap does not support remove()");
    }

    public int size() {
        int result = 0;
        for (Enumeration i = this.bundle.getKeys(); i.hasMoreElements();) {
            i.nextElement();
            result ++;
        }

        return result;
    }

    public Collection values() {
        ArrayList values = new ArrayList();
        for (Enumeration i = this.bundle.getKeys(); i.hasMoreElements();) {
            values.add(this.bundle.getObject((String) i.nextElement()));
        }
        return values;
    }
}
