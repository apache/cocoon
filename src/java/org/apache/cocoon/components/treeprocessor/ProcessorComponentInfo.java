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
package org.apache.cocoon.components.treeprocessor;

import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.service.ServiceManager;

/**
 * Holds informations defined in &lt;map:components&gt; such as default hint, labels and mime-types
 * that are needed when building a processor and to manage inheritance when building child processors.
 * <p>
 * In previous versions of the sitemap engine, these informations where store in specialized
 * extensions of ComponentSelector (<code>SitemapComponentSelector</code> and
 * <code>OutputComponentSelector</code>), which led to a strong dependency on the chosen component
 * container implementation.
 * 
 * @version CVS $Id: ProcessorComponentInfo.java,v 1.1 2004/07/15 12:49:50 sylvain Exp $
 */
public class ProcessorComponentInfo {
    
    /** Component info for the parent processor */
    ProcessorComponentInfo parent;
    
    /** The service manager for this processor */
    private ServiceManager manager;
    
    /** Lock that prevents further modification */
    private boolean locked = false;
    
//    /**
//     * Does this processor have mount instructions? If yes, we must keep this data to
//     * build child processors, else we can discard it.
//     */
//    private boolean hasMount;
    
    /**
     * Component-related data (see methods below for key names). We use a single Map
     * to reduce memory usage, as each kind of data has a limited number of entries.
     */
    private Map data;
    
    public ProcessorComponentInfo(ProcessorComponentInfo parent) {
        this.parent = parent;
    }
    
    public void setServiceManager(ServiceManager manager) {
        if (locked) throw new IllegalStateException("ProcessorComponentInfo is locked");
        this.manager = manager;
    }
    
    public ServiceManager getServiceManager() {
        return this.manager;
    }
    
//    public void foundMountInstruction() {
//        this.hasMount = true;
//    }
//    
//    public boolean hasMountInstruction() {
//        return this.hasMount;
//    }
    
    /** Store some data, creating the storage map if needed */
    private void setData(String key, Object value) {
        if (locked) throw new IllegalStateException("ProcessorComponentInfo is locked");
        if (this.data == null) this.data = new HashMap();
        this.data.put(key, value);
    }
    
    /** Get some data, asking the parent if needed */
    private Object getData(String key) {
        // Need to check containsKey as the default hint may be unspecified (i.e. no "default" attribute)
        if (this.data != null && this.data.containsKey(key)) {
            return this.data.get(key);
        } else if (this.parent != null) {
            // Ask parent
            return this.parent.getData(key);
        } else {
            return null;
        }
    }
    
    /**
     * Lock this component info object at the end of processor building to prevent
     * any further changes.
     */
    public void lock() {
        this.locked = true;
    }
    
    public void setDefaultType(String role, String hint) {
        setData("defaultType/" + role, hint);
    }
    
    public String getDefaultType(String role) {
        return (String)getData("defaultType/" + role);
    }
    
    public void setPipelineHint(String role, String type, String hint) {
        setData("pipelineHint/" + role + "/" + type, hint);
    }
    
    public String getPipelineHint(String role, String type) {
        return (String)getData("pipelineHint/" + role + "/" + type);
    }
    
    public void setMimeType(String role, String type, String mimeType) {
        setData("mimeType/" + role + "/" + type, mimeType);
    }
    
    public String getMimeType(String role, String type) {
        return (String)getData("mimeType/" + role + "/" + type);
    }
    
    public void setLabels(String role, String type, String[] labels) {
        setData("labels/" + role + "/" + type, labels);
    }
    
    public String[] getLabels(String role, String type) {
        return (String[])getData("labels/" + role + "/" + type);
    }
    
    public boolean hasLabel(String role, String type, String label) {
        String[] labels = this.getLabels(role, type);
        if (labels != null) {
            for (int i = 0; i < labels.length; i++) {
                if (labels[i].equals(label))
                    return true;
            }
        }
        return false;
    }
}