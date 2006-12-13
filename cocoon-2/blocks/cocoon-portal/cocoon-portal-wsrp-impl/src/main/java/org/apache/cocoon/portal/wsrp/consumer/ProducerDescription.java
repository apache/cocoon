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

import oasis.names.tc.wsrp.v1.types.RegistrationData;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.portal.wsrp.adapter.WSRPAdapter;
import org.apache.wsrp4j.consumer.ConsumerEnvironment;

/**
 * Describes and creates producers.<br/>
 * If the registration interface url is set (not null) this implies
 * that the producer requires a registration. If the producer does
 * not require a registration, set the registration interface url
 * to null<br/>
 *
 * @version $Id$
 */
public class ProducerDescription {

    /** Unique string of the producer */
    protected String id;

    /** The wsrp makup-interface-url (required) */
    protected String markupInterfaceUrl;

    /** The wsrp service-description-interface-url (required) */
    protected String serviceDescriptionInterfaceUrl;

    /** The wsrp registration-interface-url (optional) */
    protected String registrationInterfaceUrl;

    /** The wsrp portlet-management-interface-url (optional) */
    protected String portletManagementInterfaceUrl;
    
    /** name of the producer */
    protected String name;
    
    /** description of the producer */
    protected String description;

    /** The registration data. */
    protected RegistrationData registrationData;

    /**
     * Default constructor
     */
    public ProducerDescription() {
        this(null);
    }

    /**
     * Constructor<br/>
     * 
     * @param id of the producer
     */
    public ProducerDescription(String id) {
        this(id, null, null);
    }

    /**
     * Constructor<br/>
     * 
     * @param id of the producer
     * @param markupUrl markup-interface-url
     * @param sdUrl service-description-interface-url
     */
    public ProducerDescription(String id, String markupUrl, String sdUrl) {
        this(id, markupUrl, sdUrl, null, null);
    }

    /**
     * Constructor<br/>
     * 
     * @param id of the producer
     * @param markupUrl markup-interface-url
     * @param sdUrl service-description-interface-url
     * @param regUrl registration-interface-url
     * @param pmUrl portlet-management-interface-url
     */
    public ProducerDescription(String id, 
                               String markupUrl, 
                               String sdUrl,
                               String regUrl,
                               String pmUrl) {
        this.id = id;
        this.markupInterfaceUrl = markupUrl;
        this.serviceDescriptionInterfaceUrl = sdUrl;
        this.registrationInterfaceUrl = regUrl;
        this.portletManagementInterfaceUrl = pmUrl;
    }

    /**
     * @return producer-id
     */
    public String getId() {
        return id;
    }

    /**
     * set the producer-id<br/>
     * 
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return markup-interface-url
     */
    public String getMarkupInterfaceUrl() {
        return markupInterfaceUrl;
    }

    /**
     * set the markup-interface-url<br/>
     * 
     * @param markupInterfaceUrl
     */
    public void setMarkupInterfaceUrl(String markupInterfaceUrl) {
        this.markupInterfaceUrl = markupInterfaceUrl;
    }

    /**
     * @return portlet-management-interface-url
     */
    public String getPortletManagementInterfaceUrl() {
        return portletManagementInterfaceUrl;
    }

    /**
     * Set the portlet-management-interface-url<br/>
     * 
     * @param portletManagementInterfaceUrl
     */
    public void setPortletManagementInterfaceUrl(String portletManagementInterfaceUrl) {
        this.portletManagementInterfaceUrl = portletManagementInterfaceUrl;
    }

    /**
     * @return registration-interface-url
     */
    public String getRegistrationInterfaceUrl() {
        return registrationInterfaceUrl;
    }

    /**
     * Set the registration-interface-url<br/>
     * 
     * @param registrationInterfaceUrl
     */
    public void setRegistrationInterfaceUrl(String registrationInterfaceUrl) {
        this.registrationInterfaceUrl = registrationInterfaceUrl;
    }

    /**
     * @return service-description-interface-url
     */
    public String getServiceDescriptionInterfaceUrl() {
        return serviceDescriptionInterfaceUrl;
    }

    /**
     * Set the service-description-interface-url<br/>
     * 
     * @param serviceDescriptionInterfaceUrl
     */
    public void setServiceDescriptionInterfaceUrl(String serviceDescriptionInterfaceUrl) {
        this.serviceDescriptionInterfaceUrl = serviceDescriptionInterfaceUrl;
    }

    /**
     * Create a producer description from a configuration<br/>
     * 
     * @param config
     * @return the producer-description
     * @throws ConfigurationException
     */
    public static ProducerDescription fromConfiguration(Configuration config, ConsumerEnvironment env)
    throws ConfigurationException {
        final String producerId = config.getAttribute("id");
        final ProducerDescription desc = new ProducerDescription(producerId);

        desc.setMarkupInterfaceUrl(config.getChild("markup-interface-url").getValue());
        desc.setServiceDescriptionInterfaceUrl(config.getChild("service-description-interface-url").getValue());
        desc.setRegistrationInterfaceUrl(config.getChild("registration-interface-url").getValue(null));
        desc.setPortletManagementInterfaceUrl(config.getChild("portlet-management-interface-url").getValue(null));
        boolean registrationRequired;
        if ( desc.getRegistrationInterfaceUrl() != null ) {
            registrationRequired = config.getChild("registration-interface-url").getAttributeAsBoolean("registration-required", true);
        } else {
            registrationRequired = false;
        }
        if ( registrationRequired == false ) {
            desc.setRegistrationInterfaceUrl(null);
        } else {
            // get the registration data
            desc.setRegistrationData(createRegistrationData(config.getChild("registration-data"), env));
        }

        // optional information
        desc.setName(config.getChild("name").getValue(null));
        desc.setDescription(config.getChild("description").getValue(null));

        return desc;
    }

    public static RegistrationData createRegistrationData(Configuration config,
                                                          ConsumerEnvironment env) {
        RegistrationData registrationData = new RegistrationData(
                env.getConsumerAgent(),
                env.getSupportedModes(),
                WSRPAdapter.CONSUMER_URL,  //consumer-name
                null,         //consumerUserScopes
                env.getSupportedWindowStates(),
                null,         //customUserProfileData
                null,         //extensions
                false,        //methodGetSupported 
                null          //registrationProperties
                );
        return registrationData;        
    }

    /**
     * @return the producer-description as string
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the producer-description<br/>
     * 
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return name of the producer
     */
    public String getName() {
        return name;
    }

    /**
     * Set the producer-name<br/>
     * 
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    public RegistrationData getRegistrationData() {
        return registrationData;
    }

    public void setRegistrationData(RegistrationData registrationData) {
        this.registrationData = registrationData;
    }
}
