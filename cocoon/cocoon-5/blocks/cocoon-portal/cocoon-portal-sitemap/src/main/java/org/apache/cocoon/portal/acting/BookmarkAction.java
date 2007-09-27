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
package org.apache.cocoon.portal.acting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.SAXConfigurationHandler;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.portal.acting.helpers.CopletMapping;
import org.apache.cocoon.portal.acting.helpers.FullScreenMapping;
import org.apache.cocoon.portal.acting.helpers.LayoutMapping;
import org.apache.cocoon.portal.acting.helpers.Mapping;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceValidity;
import org.xml.sax.SAXException;

/**
 * This action helps you in creating bookmarks
 * 
 * The definition file is:
 * <bookmarks>
 * <events>
 *   <event type="jxpath" id="ID">
 *     <targetid>tagetId</targetid>
 *     <targettype>layout|coplet</targettype>
 *     <path/>
 *   </event>
 *   <event type="fullscreen" id="ID">
 *     <targetid>copletId</targetid>
 *     <layoutid>layoutId</layoutid>
 *   </event>
 * </events>
 * </bookmarks>
 *
 * @version $Id$
 */
public class BookmarkAction
    extends AbstractPortalAction
    implements Parameterizable {

    protected Map eventMap = new HashMap();

    protected String historyParameterName;

    protected String configurationFile;
    
    protected SourceValidity oldValidity;

    protected boolean reloadCheck = true;

    /**
     * @see org.apache.avalon.framework.parameters.Parameterizable#parameterize(org.apache.avalon.framework.parameters.Parameters)
     */
    public void parameterize(Parameters parameters) throws ParameterException {
        this.historyParameterName = parameters.getParameter("history-parameter-name", "history");
        this.reloadCheck = parameters.getParameterAsBoolean("check-reload", this.reloadCheck);
        this.configurationFile = parameters.getParameter("src", null);
        if ( this.configurationFile == null ) return;

        // The "lazy-load" parameter allows to defer loading of the config from "src" at
        // the first call to act. This is for now undocumented until
        // That was needed in the case of a dynamic source ("cocoon://blah") produced by the sitemap where
        // the action is defined. Loading immediately in that case leads to an infinite loop where the sitemap
        // is constantly reloaded.
        if (!parameters.getParameterAsBoolean("lazy-load", false)) {
            loadConfig();
        }
    }

    private void loadConfig() throws ParameterException {
        Configuration config;
        org.apache.excalibur.source.SourceResolver resolver = null;
        Source source = null;  
        try {
        	try {
        		resolver = (org.apache.excalibur.source.SourceResolver) this.manager.lookup(org.apache.excalibur.source.SourceResolver.ROLE);
        		source = resolver.resolveURI(configurationFile);
        	} catch (IOException ioe) {
				throw new ParameterException("Unable to read configuration from " + configurationFile, ioe);
        	} catch (ServiceException se) {
    			throw new ParameterException("Unable to lookup source resolver.", se);
        	}
        	SourceValidity newValidity = source.getValidity();
        	if ( this.oldValidity == null
                 || this.oldValidity.isValid() == SourceValidity.INVALID
                 || this.oldValidity.isValid(newValidity) == SourceValidity.INVALID)	{
                this.oldValidity = newValidity;
        		try {
        			SAXConfigurationHandler handler = new SAXConfigurationHandler();
        			SourceUtil.toSAX(source, handler);
        			config = handler.getConfiguration();
        		} catch (ProcessingException se) {
        			throw new ParameterException("Unable to read configuration from " + configurationFile, se);
        		} catch (SAXException se) {
        			throw new ParameterException("Unable to read configuration from " + configurationFile, se);
        		} catch (IOException ioe) {
        			throw new ParameterException("Unable to read configuration from " + configurationFile, ioe);
        		}
        		Configuration[] events = config.getChild("events").getChildren("event");
        		
        		if ( events != null ) {
        			for(int i=0; i<events.length;i++) {
        				try {
        					final String type = events[i].getAttribute("type");
        					final String id = events[i].getAttribute("id");
        					if ( "jxpath".equals(type) ) {
        						if ( this.eventMap.containsKey(id)) {
        							throw new ParameterException("The id for the event " + id + " is not unique.");
        						}
        						final String targetType = events[i].getChild("targettype").getValue();
        						final String targetId = events[i].getChild("targetid").getValue();
        						final String path = events[i].getChild("path").getValue();
        						if ( "layout".equals(targetType) ) {
        							LayoutMapping mapping = new LayoutMapping();
        							mapping.layoutId = targetId;
        							mapping.path = path;
        							this.eventMap.put(id, mapping);
        						} else if ( "coplet".equals(targetType) ) {
        							CopletMapping mapping = new CopletMapping();
        							mapping.copletId = targetId;
        							mapping.path = path;  
        							this.eventMap.put(id, mapping);
        						} else {
        							throw new ParameterException("Unknown target type " + targetType);
        						}
        					} else if ( "fullscreen".equals(type) ) {
        						if ( this.eventMap.containsKey(id)) {
        							throw new ParameterException("The id for the event " + id + " is not unique.");
        						}
        						final String targetId = events[i].getChild("targetid").getValue();
        						final String layoutId = events[i].getChild("layoutid").getValue();
        						FullScreenMapping mapping = new FullScreenMapping();
        						mapping.copletId = targetId;
        						mapping.layoutId = layoutId;
        						this.eventMap.put(id, mapping);                        
        					} else {
        						throw new ParameterException("Unknown event type for event " + id + ": " + type);                        
        					}
        				} catch (ConfigurationException ce) {
        					throw new ParameterException("Configuration exception" ,ce);
        				}
        			}
        		}
        	}
        } finally {
        	if (resolver != null) {
        		resolver.release(source);
        	}
        	if (resolver != null) {
        		this.manager.release(resolver);
        	}
        }
        if ( !this.reloadCheck ) {
            this.configurationFile = null;
        }
    }

    /**
     * @see org.apache.cocoon.acting.Action#act(org.apache.cocoon.environment.Redirector, org.apache.cocoon.environment.SourceResolver, java.util.Map, java.lang.String, org.apache.avalon.framework.parameters.Parameters)
     */
    public Map act(Redirector redirector,
                   SourceResolver resolver,
                   Map objectModel,
                   String source,
                   Parameters par)
    throws Exception {
        if (this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("Bookmark action called with resolver="+resolver+
                                   ", objectModel="+objectModel+
                                   ", source="+source+
                                   ", par="+par);
        }

        if (this.configurationFile != null) {
            this.loadConfig();
        }

        Map result;
        this.portalService.getPortalManager().process();

        final Request request = ObjectModelHelper.getRequest(objectModel);
        final HttpSession session = request.getSession(false);
        final List events = new ArrayList();

        // is the history invoked?
        final String historyValue = request.getParameter(this.historyParameterName);
        if ( historyValue != null && session != null) {
            // get the history
            final List history = (List)session.getAttribute("portal-history");
            if ( history != null ) {
                final int index = Integer.parseInt(historyValue);
                final List state = (List)history.get(index);
                if ( state != null ) {
                    final Iterator iter = state.iterator();
                    while ( iter.hasNext() ) {
                        Mapping m = (Mapping)iter.next();
                        events.add(m.getEvent(this.portalService, null));
                    }
                    while (history.size() > index ) {
                        history.remove(history.size()-1);
                    }
                }
            }
        }
        Enumeration enumeration = request.getParameterNames();
        while (enumeration.hasMoreElements()) {
            String name = (String)enumeration.nextElement();
            String value = request.getParameter(name);
            
            Mapping m = (Mapping) this.eventMap.get(name);
            if ( m != null ) {
                events.add(m.getEvent(this.portalService, value));
            }                
        }
        String uri = this.portalService.getLinkService().getLinkURI(events);
        result = new HashMap();
        result.put("uri", uri.substring(uri.indexOf('?')+1));

        return result;
    }
}
