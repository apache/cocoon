/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.components.modules.input;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.component.ComponentSelector;
import org.apache.avalon.framework.thread.ThreadSafe;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * This modules allows to "chain" several other modules. If a module
 * returns "null" as attribute value, the next module in the chain is
 * queried until either a value can be obtained or the end of the
 * chain is reached.
 *
 * <p>A typical example would be to "chain" request parameters,
 * session attributes, and constants in this order. This way, an
 * application could have a default skin that could be overridden by a
 * user in her/his profile stored in the session. In addition, the
 * user could request a different skin through passing a request
 * parameter.</p>
 *
 * <p>Usage:</p>
 *
 * <p> Any number of &lt;input-module/&gt; blocks may appear in the
 * component configuration. The @name attribute is used as the name of
 * the requested input module. The complete &lt;input-module/&gt;
 * block is passed at run-time to the module and thus can contain any
 * configuration data for that particular module.</p>
 *
 * <p>Configuration:</p>
 *
 * <p>It can be controlled whether it returns a flat or a deep view,
 * i.e. whether only values from the first module are returned if
 * non-null or they are merged with values from other modules
 * <code>&lt;all-values&gt;true&lt;/all-values&gt;</code>. The same is
 * possible for the attribute names
 * (<code>&lt;all-names/&gt;</code>). In addition, empty strings could
 * be treated the same as null values
 * (<code>&lt;empty-as-null/&gt;</code>).</p>
 *
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @version CVS $Id: ChainMetaModule.java,v 1.3 2004/02/15 21:30:00 haul Exp $
 */
public class ChainMetaModule extends AbstractMetaModule implements ThreadSafe {

    private ModuleHolder[] inputs = null;

    private boolean emptyAsNull = false;
    private boolean allNames = false;
    private boolean allValues = false;

    public void configure(Configuration config) throws ConfigurationException {

        Configuration[] confs = config.getChildren("input-module");
        if (confs.length > 0) {
            this.inputs = new ModuleHolder[confs.length];
            int j = 0;
            for (int i=0; i<confs.length; i++) {
                ModuleHolder module = new ModuleHolder();
                module.name = confs[i].getAttribute("name",null);
                if (module.name == null) {
                    if (getLogger().isErrorEnabled())
                        getLogger().error("No name attribute for module configuration. Skipping.");
                    continue;
                }
                module.config = confs[i];
                this.inputs[j]=module;
                j++;
            }
        }
        this.emptyAsNull = config.getChild("empty-as-null").getValueAsBoolean(this.emptyAsNull);
        this.allNames = config.getChild("all-names").getValueAsBoolean(this.allNames);
        this.allValues = config.getChild("all-values").getValueAsBoolean(this.allValues);
    }


    public synchronized void lazy_initialize() {

        try {
            // obtain input modules
            if (!this.initialized) {
                this.inputSelector=(ComponentSelector) this.manager.lookup(INPUT_MODULE_SELECTOR); 
                if (this.inputSelector != null && this.inputSelector instanceof ThreadSafe) {
                    
                    for (int i=0; i<this.inputs.length; i++) {
                        if (this.inputs[i].name != null) 
                            this.inputs[i].input = obtainModule(this.inputs[i].name);
                    }
                    
                } else if (!(this.inputSelector instanceof ThreadSafe) ) {
                    this.manager.release(this.inputSelector);
                    this.inputSelector = null;
                }
                
                this.initialized = true;
            }
        } catch (Exception e) {
            if (getLogger().isWarnEnabled()) 
                getLogger().warn("A problem occurred setting up input modules :'" + e.getMessage());
        }
    }

    
    public void dispose() {
        
        if (this.inputSelector != null) {
            
            for (int i=0; i<this.inputs.length; i++) {
                if (this.inputs[i].input != null)
                    this.inputSelector.release(this.inputs[i].input);
            }
            
            this.manager.release(this.inputSelector);
        }
    }


    public Object[] getAttributeValues( String attr, Configuration modeConf, Map objectModel ) 
        throws ConfigurationException {

        if (!this.initialized) {
            this.lazy_initialize();
        }

        // obtain correct configuration objects
        // default vs dynamic
        Configuration[] inputConfigs = null;
        boolean allValues = this.allValues;
        boolean emptyAsNull = this.emptyAsNull;
        if (modeConf!=null && modeConf.getChildren().length > 0) {
            inputConfigs = modeConf.getChildren("input-module");
            emptyAsNull = modeConf.getChild("empty-as-null").getValueAsBoolean(emptyAsNull);
            allValues = modeConf.getChild("all-values").getValueAsBoolean(allValues);
            if (inputConfigs.length == 0) inputConfigs = null;
        }

        Object[] value = null;
        boolean debug = getLogger().isDebugEnabled();
        List values = null;
        if (allValues) values = new ArrayList();

        if (inputConfigs == null) {
            // static configuration branch
            int i = 0;
            while (i < this.inputs.length && (value == null || allValues)) {
                if (this.inputs[i].name != null) {
                    value = getValues(attr, objectModel, this.inputs[i].input, this.inputs[i].name, this.inputs[i].config);
                    if (emptyAsNull && value != null && value.length == 0) value = null;
                    if (emptyAsNull && value != null && value.length == 1 && 
                        value[0] instanceof String && value[0].equals("")) value = null;
                    if (debug) getLogger().debug("read from "+this.inputs[i].name+" attribute "+attr+" as "+value);
                    if (allValues && value != null) values.addAll(Arrays.asList(value));
                }
                i++;
            }
        } else {
            // run-time configuration branch
            int i = 0;
            while (i < inputConfigs.length && (value == null || allValues)) {
                String name = inputConfigs[i].getAttribute("name",null);
                if (name != null) {
                    value = getValues(attr, objectModel, null, name, inputConfigs[i]);
                    if (emptyAsNull && value != null && value.length == 0) value = null;
                    if (emptyAsNull && value != null && value.length == 1 && 
                        value[0] instanceof String && value[0].equals("")) value = null;
                    if (debug) getLogger().debug("read from "+name+" attribute "+attr+" as "+value);
                    if (allValues && value != null) values.addAll(Arrays.asList(value));
                }
                i++;
            }
        }
        if (debug) getLogger().debug("result chaining for "+attr+" is "+(allValues? values.toArray() : value));
        return (allValues? values.toArray() : value);
    }

    private void addIterator(Collection col, Iterator iter) {
        while (iter != null && iter.hasNext())
            col.add(iter.next());
    }


    public Iterator getAttributeNames( Configuration modeConf, Map objectModel ) 
        throws ConfigurationException {

        if (!this.initialized) {
            this.lazy_initialize();
        }

        // obtain correct configuration objects
        // default vs dynamic
        Configuration[] inputConfigs = null;
        boolean emptyAsNull = this.emptyAsNull;
        boolean allNames = this.allNames;
        if (modeConf!=null && modeConf.getChildren().length > 0) {
            inputConfigs = modeConf.getChildren("input-module");
            emptyAsNull = modeConf.getChild("empty-as-null").getValueAsBoolean(emptyAsNull);
            allNames = modeConf.getChild("all-names").getValueAsBoolean(allNames);
            if (inputConfigs.length == 0) inputConfigs = null;
        }

        Iterator value = null;
        Collection values = null;
        if (allNames) values = new ArrayList();
        boolean debug = getLogger().isDebugEnabled();

        if (inputConfigs == null) {
            // static configuration branch
            int i = 0;
            while (i < this.inputs.length && (value == null || allNames)) {
                if (this.inputs[i].name != null) {
                    value = getNames(objectModel, this.inputs[i].input, this.inputs[i].name, this.inputs[i].config);
                    if (debug) getLogger().debug("read from "+this.inputs[i].name+" AttributeNames as "+value);
                    if (allNames && value != null) addIterator(values, value);
                }
                i++;
            }
        } else {
            // run-time configuration branch
            int i = 0;
            while (i < inputConfigs.length && value == null) {
                String name = inputConfigs[i].getAttribute("name",null);
                if (name != null) {
                    value = getNames(objectModel, null, name, inputConfigs[i]);
                    if (debug) getLogger().debug("read from "+name+" AttributeNames as "+value);
                    if (allNames && value != null) addIterator(values, value);
                }
                i++;
            }
        }
        if (debug) getLogger().debug("result chaining names is "+(allNames? values.iterator() : value));
        return (allNames? values.iterator() : value);
     }


    public Object getAttribute( String attr, Configuration modeConf, Map objectModel ) 
        throws ConfigurationException {

        Object[] values = this.getAttributeValues(attr,modeConf,objectModel);
        if (getLogger().isDebugEnabled()) getLogger().debug("result chaining single for "+attr+" is "+(values != null? values[0] : "null"));
        return (values != null? values[0] : null);
    }


}
