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
package org.apache.cocoon.generation;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentSelector;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
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
    
    private ComponentSelector m_generatorSelector;
    private Generator m_delegate;
    private Serializable m_key;
    private Event m_event;
    
    private Map m_types = new HashMap();
    
    
    // ---------------------------------------------------- lifecycle methods
    
    public void service(ServiceManager manager)  throws ServiceException {
        super.service(manager);
        m_generatorSelector = (ComponentSelector) 
            manager.lookup(Generator.ROLE + "Selector");
    }

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
        } catch (ComponentException e) {
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

    public void setConsumer(XMLConsumer consumer) {
        m_delegate.setConsumer(consumer);
    }

    public void generate() throws IOException, SAXException, ProcessingException {
        m_delegate.generate();
    }

    public void recycle() {
        if ( m_delegate != null ) {
            m_generatorSelector.release(m_delegate);
        }
        m_delegate = null;
        m_key = null;
        m_event = null;
        super.recycle();
    }

    
    // ---------------------------------------------------- caching strategy
    
    public Serializable getKey() {
        return m_key;
    }

    public SourceValidity getValidity() {
        return new EventValidity(m_event);
    }

}
