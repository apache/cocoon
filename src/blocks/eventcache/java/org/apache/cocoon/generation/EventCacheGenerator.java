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
package org.apache.cocoon.generation;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.caching.validity.Event;
import org.apache.cocoon.caching.validity.EventFactory;
import org.apache.cocoon.caching.validity.EventValidity;
import org.apache.cocoon.caching.validity.NameValueEventFactory;
import org.apache.cocoon.caching.validity.NamedEventFactory;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.excalibur.source.SourceUtil;
import org.apache.excalibur.source.SourceValidity;
import org.xml.sax.SAXException;

/**
 * Generator wrapper that forwards generation to
 * to its delegate but overides its caching strategy
 * by replacing it with an event-cache aware implementation.
 * 
 * <p>
 *  Sitemap configuration is as follows:<br>
 *   &lt;map:generator name="event-cache" src="org.apache.cocoon.generation.EventCacheGenerator" &gt;<br>
 *   &nbsp;&nbsp;&lt;factory name="my-event" value="com.my.EventFactoryImpl" /&gt;<br>
 *   &nbsp;&nbsp;&lt;factory name="my-other-event" value="com.myother.EventFactoryImpl" /&gt;<br>
 *   &lt;/map:generator&gt;<br>
 * </p>
 * 
 * Two event types are preconfigured: NamedEvent (name="named") and NameValueEvent
 * (name="name-value").
 * 
 * <p>
 *  Pipeline usage is as follows:<br>
 *  &lt;map:generate type="event-cache" src="delegate-src"&gt;<br>
 *    &nbsp;&nbsp;&lt;map:parameter name="delegate" value="delegate-type" /&gt;<br>
 *    &nbsp;&nbsp;&lt;map:parameter name="event-type" value="my-event" /&gt;<br>
 *    &nbsp;&nbsp;&lt;!-- my event parameters --&gt;<br>
 *    &nbsp;&nbsp;&lt;map:parameter name="event-name" value="some name" /&gt;<br>
 *    &nbsp;&nbsp;&lt;map:parameter name="event-value" value="some value" /&gt;<br>
 *  &lt;/map:generate&gt;
 * </p>
 * 
 * The two preconfigured event types take the following parameters:<br>
 * - event-type 'named': parameter 'event-name'<br>
 * - event-type 'name-value': parameter 'event-name' and parameter 'event-value'<br>
 * 
 * <p>
 *  The src attribute and all parameters are passed as is to delegate generator.
 * </p>
 * 
 * TODO: share common code with EventCacheTransformer
 * @author Unico Hommes
 */
public class EventCacheGenerator extends ServiceableGenerator 
implements Configurable, CacheableProcessingComponent {


    // ---------------------------------------------------- constants
    
    public static final String NAME_VALUE_EVENT_TYPE = "name-value";
    public static final String EVENT_TYPE_DEFAULT = "named";
    public static final String NAMED_EVENT_TYPE = EVENT_TYPE_DEFAULT;
        
    public static final String FACTORY_CONF = "factory";    
    public static final String FACTORY_NAME_CONF = "name";
    public static final String FACTORY_NAME_TYPE = "src";
    
    public static final String DELEGATE_PARAM = "delegate";
    public static final String EVENT_TYPE_PARAM = "event-type";

    
    // ---------------------------------------------------- member variables
    
    private ServiceSelector m_generatorSelector;
    private Generator m_delegate;
    private Serializable m_key;
    private Event m_event;
    
    private Map m_types = new HashMap();
    
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager)  throws ServiceException {
        super.service(manager);
        m_generatorSelector = (ServiceSelector) 
            manager.lookup(Generator.ROLE + "Selector");
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration configuration) throws ConfigurationException {
        Configuration[] factories = configuration.getChildren(FACTORY_CONF);
        for (int i = 0; i < factories.length; i++) {
            Configuration child = factories[i];
            String name = child.getAttribute(FACTORY_NAME_CONF);
            String src = child.getAttribute(FACTORY_NAME_TYPE);
            try {
                EventFactory factory = (EventFactory) Class.forName(src).newInstance();
                m_types.put(name,factory);
            } catch (Exception e) {
                final String message =
                    "Unable to create EventFactory of type " + src;
                throw new ConfigurationException(message, e);
            }
        }
        if (!m_types.containsKey(NAMED_EVENT_TYPE)) {
            m_types.put(NAMED_EVENT_TYPE,new NamedEventFactory());
        }
        if (!m_types.containsKey(NAME_VALUE_EVENT_TYPE)) {
            m_types.put(NAME_VALUE_EVENT_TYPE,new NameValueEventFactory());
        }
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.sitemap.SitemapModelComponent#setup(org.apache.cocoon.environment.SourceResolver, java.util.Map, java.lang.String, org.apache.avalon.framework.parameters.Parameters)
     */
    public void setup(SourceResolver resolver, Map objectModel, String src,
                      Parameters par)
    throws ProcessingException, SAXException, IOException {

        // delegate
        String delegate = par.getParameter(DELEGATE_PARAM, null);
        if (delegate == null) {
            String message =
                "Required parameter 'delegate' is missing";
            throw new ProcessingException(message);
        }
        try {
            m_delegate = (Generator) m_generatorSelector.select(delegate);
        } catch (ServiceException e) {
            final String message =
                "Transformer '" + delegate + "' could not be found.";
            throw new ProcessingException(message); 
        }        
        m_delegate.setup(resolver, objectModel, src, par);
        
        // event
        String eventType = par.getParameter(EVENT_TYPE_PARAM, EVENT_TYPE_DEFAULT);
        EventFactory factory = (EventFactory) m_types.get(eventType);
        if (factory == null) {
            throw new ProcessingException("No such type of event: " + eventType);
        }
        try {
            m_event = factory.createEvent(par);
        } catch (ParameterException e) {
            final String message = "Failure creating Event";
            throw new ProcessingException(message,e);
        }
        
        // key - TODO: use delegates key?
        m_key = SourceUtil.appendParameters(src,par);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.xml.XMLProducer#setConsumer(org.apache.cocoon.xml.XMLConsumer)
     */
    public void setConsumer(XMLConsumer consumer) {
        m_delegate.setConsumer(consumer);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.generation.Generator#generate()
     */
    public void generate() throws IOException, SAXException, ProcessingException {
        m_delegate.generate();
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.excalibur.pool.Recyclable#recycle()
     */
    public void recycle() {
        if ( m_delegate != null ) {
            m_generatorSelector.release(m_delegate);
        }
        m_delegate = null;
        m_key = null;
        m_event = null;
        super.recycle();
    }

    
    /* (non-Javadoc)
     * @see org.apache.cocoon.caching.CacheableProcessingComponent#getKey()
     */
    public Serializable getKey() {
        return m_key;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.caching.CacheableProcessingComponent#getValidity()
     */
    public SourceValidity getValidity() {
        return new EventValidity(m_event);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( this.manager != null ) {
            this.manager.release(m_generatorSelector);
            m_generatorSelector = null;
        }
        super.dispose();
    }
}
