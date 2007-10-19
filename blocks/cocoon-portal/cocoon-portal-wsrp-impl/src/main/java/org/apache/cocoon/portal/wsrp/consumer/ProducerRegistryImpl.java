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
package org.apache.cocoon.portal.wsrp.consumer;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wsrp4j.consumer.ConsumerEnvironment;
import org.apache.wsrp4j.consumer.Producer;
import org.apache.wsrp4j.consumer.driver.GenericProducerRegistryImpl;
import org.apache.wsrp4j.consumer.driver.ProducerImpl;
import org.apache.wsrp4j.exception.WSRPException;

import org.apache.cocoon.portal.wsrp.adapter.WSRPAdapter;

/**
 * A producer registry storing all producers in a {@link java.util.Hashtable}
 * in memory.<br/>
 * On startup/login the registry is full by the wsrp adapter.
 *
 * @version $Id$
 */
public class ProducerRegistryImpl extends GenericProducerRegistryImpl
                                  implements Configurable, RequiresConsumerEnvironment, RequiresWSRPAdapter {

    /** The logger. */
    protected final Log logger = LogFactory.getLog(getClass());

    /** The environment. */
    protected ConsumerEnvironment environment;

    /** All producer descriptions. */
    protected Map descriptions = new Hashtable();

    /** Initialized? */
    protected boolean initialized;

    /** The wsrp adapter. */
    protected WSRPAdapter adapter;

    /**
     * @see org.apache.cocoon.portal.wsrp.consumer.RequiresConsumerEnvironment#setConsumerEnvironment(org.apache.wsrp4j.consumer.ConsumerEnvironment)
     */
    public void setConsumerEnvironment(ConsumerEnvironment env) {
        this.environment = env;
    }

    /**
     * @see org.apache.cocoon.portal.wsrp.consumer.RequiresWSRPAdapter#setWSRPAdapter(org.apache.cocoon.portal.wsrp.adapter.WSRPAdapter)
     */
    public void setWSRPAdapter(WSRPAdapter adapter) {
        this.adapter = adapter;
    }

    /**
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration c) throws ConfigurationException {
        if ( c != null ) {
            Configuration config = c.getChild("producers", true);
            // create a list of descriptions
            Configuration[] children = config.getChildren("producer");
            for(int i=0; i<children.length; i++) {
                final Configuration current = children[i];
                final ProducerDescription desc = ProducerDescription.fromConfiguration(current, this.environment);
                this.descriptions.put(desc.getId(), desc);
            }
        }
    }

    /**
     * Check if we have read our configuration already.
     * If not, read the config and invoke the configure method.
     */
    protected void checkInitialized() {
        if ( !this.initialized ) {
            synchronized (this) {
                if (! this.initialized ) {
                    this.initialized = true;
                    try {
                        this.configure(this.adapter.getWsrpConfiguration());
                    } catch (ConfigurationException ce) {
                        this.logger.error("Unable to read wsrp configuration.", ce);
                    }
                }
            }
        }
    }

    /**
     * Add a new producer<br/>
     * 
     * @param desc The producer description.
     * @return Returns true if the producer could be added.
     */
    public boolean addProducer(ProducerDescription desc) {
        this.checkInitialized();
        try {
            final Producer producer = new ProducerImpl(desc.getId(),
                                        desc.getMarkupInterfaceUrl(),
                                        desc.getServiceDescriptionInterfaceUrl(),
                                        desc.getRegistrationInterfaceUrl(),
                                        desc.getPortletManagementInterfaceUrl(),
                                        desc.getRegistrationData());
            producer.setName(desc.getName());
            producer.setDescription(desc.getDescription());
            this.addProducer(producer);
            return true;
        } catch (WSRPException we) {
            this.logger.error("Unable to add wsrp producer: " + desc.getId()
                            + " - Continuing without configured producer.", we);
            return false;
        }
    }

    /**
     * @see org.apache.wsrp4j.consumer.ProducerRegistry#addProducer(org.apache.wsrp4j.consumer.Producer)
     */
    public void addProducer(Producer producer) {
        this.checkInitialized();
        // remove the description
        this.descriptions.remove(producer.getID());
        super.addProducer(producer);
    }

    /**
     * @see org.apache.wsrp4j.consumer.ProducerRegistry#existsProducer(java.lang.String)
     */
    public boolean existsProducer(String id) {
        checkInitialized();
        if (this.descriptions.containsKey(id)) {
            return true;
        }

        return super.existsProducer(id);
    }

    /**
     * @see org.apache.wsrp4j.consumer.ProducerRegistry#getAllProducers()
     */
    public Iterator getAllProducers() {
        this.checkInitialized();
        // create all producers from pending descriptions
        if ( this.descriptions.size() > 0 ) {
            final Iterator i = this.descriptions.values().iterator();
            while ( i.hasNext() ) {
                final ProducerDescription desc = (ProducerDescription)i.next();
                this.addProducer(desc);
            }
            this.descriptions.clear();
        }
        return super.getAllProducers();
    }

    /**
     * @see org.apache.wsrp4j.consumer.ProducerRegistry#getProducer(java.lang.String)
     */
    public Producer getProducer(String id) {
        this.checkInitialized();
        // create pending description
        ProducerDescription desc = (ProducerDescription)this.descriptions.remove(id);
        if ( desc != null ) {
            this.addProducer(desc);
        }
        return super.getProducer(id);
    }

    /**
     * @see org.apache.wsrp4j.consumer.ProducerRegistry#removeAllProducers()
     */
    public void removeAllProducers() {
        // we only remove all producers if we are initialized
        if ( this.initialized ) {
            Iterator iter = this.descriptions.values().iterator();
            while (iter.hasNext()) {
                final Producer producer = (Producer) iter.next();
                try {
                    producer.deregister();    
                } catch (WSRPException e) {
                    this.logger.error("deregister() producer: " + producer.getName());
                }
            }
        }
        this.descriptions.clear();
        super.removeAllProducers();
    }

    /**
     * @see org.apache.wsrp4j.consumer.ProducerRegistry#removeProducer(java.lang.String)
     */
    public Producer removeProducer(String id) {
        this.checkInitialized();
        // unfortunately we have to return the producer, so
        // we have to create a pending producer first just
        // to be able to remove it later on
        if ( this.descriptions.containsKey(id) ) {
            this.getProducer(id);
        }
        return super.removeProducer(id);
    }
}
