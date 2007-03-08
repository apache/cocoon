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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.om.CopletInstance;
import org.apache.cocoon.portal.wsrp.adapter.WSRPAdapter;
import org.apache.wsrp4j.consumer.URLGenerator;
import org.apache.wsrp4j.util.Constants;

/**
 * Implements the URLGenerator interface providing methods
 * to query the consumer's urls.
 *
 * @version $Id$
 */
public class URLGeneratorImpl
    implements URLGenerator, RequiresPortalService, RequiresWSRPAdapter {

    /** The portal service. */
    protected PortalService service;

    /** The WSRP Adapter. */
    protected WSRPAdapter adapter;

    /**
     * @see org.apache.cocoon.portal.wsrp.consumer.RequiresWSRPAdapter#setWSRPAdapter(org.apache.cocoon.portal.wsrp.adapter.WSRPAdapter)
     */
    public void setWSRPAdapter(WSRPAdapter adapter) {
        this.adapter = adapter;
    }

    /**
     * @see org.apache.cocoon.portal.wsrp.consumer.RequiresPortalService#setPortalService(org.apache.cocoon.portal.PortalService)
     */
    public void setPortalService(PortalService service) {
        this.service = service;
    }

    /**
     * @see org.apache.wsrp4j.consumer.URLGenerator#getBlockingActionURL(java.util.Map)
     */
    public String getBlockingActionURL(Map params) {
        return this.generateUrl(params);
    }

    /**
     * @see org.apache.wsrp4j.consumer.URLGenerator#getRenderURL(java.util.Map)
     */
    public String getRenderURL(Map params) {
        return this.generateUrl(params);
    }

    /**
     * @see org.apache.wsrp4j.consumer.URLGenerator#getResourceURL(java.util.Map)
     */
    public String getResourceURL(Map params) {
        // this is a little bit tricky
        // we create a usual portal link first with
        // all the infos
        String portalLink = this.generateUrl(params);

        // now we replace the portal pipeline with
        // the resource pipeline
        int linkEndPos = portalLink.indexOf('?');
        int pipelineStartPos = portalLink.lastIndexOf('/', linkEndPos);

        StringBuffer buffer = new StringBuffer();
        buffer.append(portalLink.substring(0, pipelineStartPos+1));
        buffer.append("wsrprsc");
        buffer.append(portalLink.substring(linkEndPos));
        return buffer.toString();
    }

    /**
     * @see org.apache.wsrp4j.consumer.URLGenerator#getNamespacedToken(java.lang.String)
     */
    public String getNamespacedToken(String token) {
        final CopletInstance coplet = this.adapter.getCurrentCopletInstanceData();
        return coplet.getId();
    }

    /**
     * Generate the url.<br/>
     * We simply create a new wsrp event and use the portal link service.<br/>
     *
     * @param params Url-parameters
     * @return portal-url including all required attributes
     */
    protected String generateUrl(Map params) {
        if ( params == null ) {
            params = new HashMap();
        }
        Boolean secureLink = null;
        if ( "true".equalsIgnoreCase((String)params.get(Constants.SECURE_URL)) ) {
            secureLink = Boolean.TRUE;
        }
        final CopletInstance coplet = this.adapter.getCurrentCopletInstanceData();
        params.put(WSRPAdapter.REQUEST_PARAMETER_NAME, coplet.getId());
        final StringBuffer buffer = new StringBuffer(this.service.getLinkService().getRefreshLinkURI(secureLink));
        boolean hasParams = buffer.indexOf("?") > 0;
        Iterator i = params.entrySet().iterator();
        while ( i.hasNext() ) {
            final Map.Entry entry = (Map.Entry)i.next();
            if ( hasParams ) {
                buffer.append('&');
            } else {
                hasParams = true;
                buffer.append('?');
            }
            buffer.append(entry.getKey()).append('=').append(entry.getValue());
        }
        // append consumer parameters
        Map consumerParameters = (Map)coplet.getTemporaryAttribute(WSRPAdapter.ATTRIBUTE_NAME_CONSUMER_MAP);
        if ( consumerParameters != null ) {
            i = consumerParameters.entrySet().iterator();
            while (i.hasNext()) {
                final Map.Entry entry = (Map.Entry)i.next();
                if ( hasParams ) {
                    buffer.append('&');
                } else {
                    hasParams = true;
                    buffer.append('?');
                }
                buffer.append(entry.getKey()).append('=').append(entry.getValue());
            }
        }
        return buffer.toString();
    }
}
