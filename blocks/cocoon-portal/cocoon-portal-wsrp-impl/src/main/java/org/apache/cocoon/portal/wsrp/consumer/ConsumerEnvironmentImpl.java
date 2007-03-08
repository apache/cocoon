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

import java.util.Properties;

import oasis.names.tc.wsrp.v1.types.StateChange;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.om.CopletInstance;
import org.apache.cocoon.portal.wsrp.adapter.WSRPAdapter;
import org.apache.wsrp4j.consumer.PortletDriverRegistry;
import org.apache.wsrp4j.consumer.PortletRegistry;
import org.apache.wsrp4j.consumer.ProducerRegistry;
import org.apache.wsrp4j.consumer.SessionHandler;
import org.apache.wsrp4j.consumer.URLGenerator;
import org.apache.wsrp4j.consumer.URLRewriter;
import org.apache.wsrp4j.consumer.URLTemplateComposer;
import org.apache.wsrp4j.consumer.User;
import org.apache.wsrp4j.consumer.UserRegistry;
import org.apache.wsrp4j.consumer.driver.GenericConsumerEnvironment;
import org.apache.wsrp4j.consumer.util.ConsumerConstants;
import org.apache.wsrp4j.util.Constants;
import org.apache.wsrp4j.util.Modes;
import org.apache.wsrp4j.util.WindowStates;

/**
 * Implements the consumer environment interface. <br/>
 *
 * @version $Id$
 **/
public class ConsumerEnvironmentImpl
    extends GenericConsumerEnvironment
    implements Disposable {

    /** The generator used to rewrite the urls. */
    protected URLGenerator urlGenerator;

    /** The wsrp adapter. */
    protected WSRPAdapter adapter;

    /**
     * Initialize the consumer
     *
     * @param service
     * @param adapter
     * @throws Exception
     */
    public void init(PortalService service,
                     WSRPAdapter adapter)
    throws Exception {
        this.adapter = adapter;
        final Properties params = adapter.getAdapterConfiguration();
        this.setConsumerAgent("not set");

        // To define the locales per end-user
        // the configuration must be set within UserContextProviderImpl
        // the following lines define the global locales
        String[] supportedLocales = new String[1];
        supportedLocales[0] = Constants.LOCALE_EN_US;
        this.setSupportedLocales(supportedLocales);

        // define the modes the consumer supports
        String[] supportedModes = new String[3];
        supportedModes[0] = Modes._view;
        supportedModes[1] = Modes._help;
        supportedModes[2] = Modes._edit;
        this.setSupportedModes(supportedModes);

        // define the window states the consumer supports
        String[] supportedWindowStates = new String[3];
        supportedWindowStates[0] = WindowStates._normal;
        supportedWindowStates[1] = WindowStates._maximized;
        supportedWindowStates[2] = WindowStates._minimized;
        this.setSupportedWindowStates(supportedWindowStates);

        // define portlet state change behaviour
        this.setPortletStateChange(StateChange.readWrite);

        // define the mime types the consumer supports
        this.setMimeTypes(new String[] { Constants.MIME_TYPE_HTML });

        // define the character sets the consumer supports
        this.setCharacterEncodingSet(new String[] { Constants.UTF_8 });

        // set the authentication method the consumer uses
        this.setUserAuthentication(ConsumerConstants.NONE);

        // THE ORDER IN WHICH THE FOLLOWING OBJECTS ARE INSTANCIATED IS IMPORTANT
        this.setUserRegistry((UserRegistry)adapter.createObject(params.getProperty("user-registry-class", UserRegistryImpl.class.getName())));
        this.setSessionHandler((SessionHandler)adapter.createObject(params.getProperty("session-handler-class", SessionHandlerImpl.class.getName())));
        this.setProducerRegistry((ProducerRegistry)adapter.createObject(params.getProperty("producer-registry-class", ProducerRegistryImpl.class.getName())));
        this.setPortletRegistry((PortletRegistry)adapter.createObject(params.getProperty("portlet-registry-class", PortletRegistryImpl.class.getName())));

        this.setTemplateComposer((URLTemplateComposer)adapter.createObject(params.getProperty("url-template-composer-class", URLTemplateComposerImpl.class.getName())));
        this.setURLRewriter((URLRewriter)adapter.createObject(params.getProperty("url-rewriter-class", URLRewriterImpl.class.getName())));

        this.setPortletDriverRegistry((PortletDriverRegistry)adapter.createObject(params.getProperty("portlet-driver-registry-class", PortletDriverRegistryImpl.class.getName())));

        this.urlGenerator = (URLGenerator)adapter.createObject(params.getProperty("url-generator-class", URLGeneratorImpl.class.getName()));
        this.getTemplateComposer().setURLGenerator(this.urlGenerator);
        this.getURLRewriter().setURLGenerator(this.urlGenerator);
    }

    /**
     * @return URLGenerator the used url generator.
     */
    public URLGenerator getURLGenerator() {
        return this.urlGenerator;
    }

    /**
     * @see org.apache.wsrp4j.consumer.driver.GenericConsumerEnvironment#getSupportedLocales()
     */
    public String[] getSupportedLocales() {
        CopletInstance coplet = this.adapter.getCurrentCopletInstanceData();
        User user = (User)coplet.getTemporaryAttribute(WSRPAdapter.ATTRIBUTE_NAME_USER);

        return ((UserContextExtension)user.getUserContext()).getSupportedLocales();
    }

    /**
     * @see org.apache.wsrp4j.consumer.ConsumerCapabilities#getUserAuthentication()
     */
    public String getUserAuthentication() {
        CopletInstance coplet = this.adapter.getCurrentCopletInstanceData();
        User user = (User)coplet.getTemporaryAttribute(WSRPAdapter.ATTRIBUTE_NAME_USER);

        return ((UserContextExtension)user.getUserContext()).getUserAuthentication();
    }

    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        // dispose in reverse order
        ContainerUtil.dispose(this.urlGenerator);
        ContainerUtil.dispose(this.getPortletDriverRegistry());
        ContainerUtil.dispose(this.getURLRewriter());
        ContainerUtil.dispose(this.getTemplateComposer());
        ContainerUtil.dispose(this.getPortletRegistry());
        ContainerUtil.dispose(this.getProducerRegistry());
        ContainerUtil.dispose(this.getSessionHandler());
        ContainerUtil.dispose(this.getUserRegistry());
    }
}
