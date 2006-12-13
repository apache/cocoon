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

import org.apache.cocoon.portal.om.CopletInstance;
import org.apache.cocoon.portal.wsrp.adapter.WSRPAdapter;
import org.apache.wsrp4j.consumer.URLGenerator;
import org.apache.wsrp4j.consumer.URLTemplateComposer;
import org.apache.wsrp4j.util.Constants;

/**
 * Implements the {@link org.apache.wsrp4j.consumer.URLTemplateComposer} interface
 * providing methods to generate URL templates.<br/>
 * The generated templates will be transmitted to producers (or respectively portlets)
 * that are willing to properly write URLs for a consumer. (With templates the consumer
 * indicates how it needs URLs formatted in order to process them properly.)
 *
 * @version $Id$
 */
public class URLTemplateComposerImpl
    implements URLTemplateComposer, RequiresWSRPAdapter {

    /** The url generator. */
    protected URLGenerator urlGenerator;

    /** The wsrp adapter. */
    protected WSRPAdapter adapter;

    /**
     * @see org.apache.cocoon.portal.wsrp.consumer.RequiresWSRPAdapter#setWSRPAdapter(org.apache.cocoon.portal.wsrp.adapter.WSRPAdapter)
     */
    public void setWSRPAdapter(WSRPAdapter adapter) {
        this.adapter = adapter;
    }

    /**
     * @see org.apache.wsrp4j.consumer.URLTemplateComposer#setURLGenerator(org.apache.wsrp4j.consumer.URLGenerator)
     */
    public void setURLGenerator(URLGenerator urlGenerator) {
        this.urlGenerator = urlGenerator;
    }

    /**
     * @see org.apache.wsrp4j.consumer.URLTemplateComposer#createBlockingActionTemplate(boolean, boolean, boolean, boolean)
     */
    public String createBlockingActionTemplate(boolean includePortletHandle,
                                               boolean includeUserContextKey,
                                               boolean includePortletInstanceKey,
                                               boolean includeSessionID) {

        return createTemplate(
                    urlGenerator.getBlockingActionURL(null),
                    true,
                    true,
                    true,
                    true,
                    true,
                    false,
                    false,
                    false,
                    includePortletHandle,
                    includeUserContextKey,
                    includePortletInstanceKey,
                    includeSessionID);
    }

    /**
     * @see org.apache.wsrp4j.consumer.URLTemplateComposer#createSecureBlockingActionTemplate(boolean, boolean, boolean, boolean)
     */
    public String createSecureBlockingActionTemplate(boolean includePortletHandle,
                                                     boolean includeUserContextKey,
                                                     boolean includePortletInstanceKey,
                                                     boolean includeSessionID) {
        return createTemplate(
                    urlGenerator.getBlockingActionURL(null),
                    true,
                    true,
                    true,
                    true,
                    true,
                    true,
                    false,
                    false,
                    includePortletHandle,
                    includeUserContextKey,
                    includePortletInstanceKey,
                    includeSessionID);
    }

    /**
     * @see org.apache.wsrp4j.consumer.URLTemplateComposer#createRenderTemplate(boolean, boolean, boolean, boolean)
     */
    public String createRenderTemplate(boolean includePortletHandle,
                                       boolean includeUserContextKey,
                                       boolean includePortletInstanceKey,
                                       boolean includeSessionID) {
        return createTemplate(
                    urlGenerator.getRenderURL(null),
                    true,
                    true,
                    true,
                    true,
                    true,
                    false,
                    false,
                    false,
                    includePortletHandle,
                    includeUserContextKey,
                    includePortletInstanceKey,
                    includeSessionID);
    }

    /**
     * @see org.apache.wsrp4j.consumer.URLTemplateComposer#createSecureRenderTemplate(boolean, boolean, boolean, boolean)
     */
    public String createSecureRenderTemplate(boolean includePortletHandle,
                                             boolean includeUserContextKey,
                                             boolean includePortletInstanceKey,
                                             boolean includeSessionID) {
        return createTemplate(
                    urlGenerator.getRenderURL(null),
                    true,
                    true,
                    true,
                    true,
                    true,
                    true,
                    false,
                    false,
                    includePortletHandle,
                    includeUserContextKey,
                    includePortletInstanceKey,
                    includeSessionID);
    }

    /**
     * @see org.apache.wsrp4j.consumer.URLTemplateComposer#createResourceTemplate(boolean, boolean, boolean, boolean)
     */
    public String createResourceTemplate(boolean includePortletHandle,
                                         boolean includeUserContextKey,
                                         boolean includePortletInstanceKey,
                                         boolean includeSessionID) {
        return createTemplate(
                    urlGenerator.getResourceURL(null),
                    true,
                    false,
                    false,
                    false,
                    false,
                    false,
                    true,
                    true,
                    includePortletHandle,
                    includeUserContextKey,
                    includePortletInstanceKey,
                    includeSessionID);
    }

    /**
     * @see org.apache.wsrp4j.consumer.URLTemplateComposer#createSecureResourceTemplate(boolean, boolean, boolean, boolean)
     */
    public String createSecureResourceTemplate(boolean includePortletHandle,
                                               boolean includeUserContextKey,
                                               boolean includePortletInstanceKey,
                                               boolean includeSessionID) {
        return  createTemplate(
                    urlGenerator.getResourceURL(null),
                    true,
                    false,
                    false,
                    false,
                    false,
                    true,
                    true,
                    true,
                    includePortletHandle,
                    includeUserContextKey,
                    includePortletInstanceKey,
                    includeSessionID);
    }

    /**
     * @see org.apache.wsrp4j.consumer.URLTemplateComposer#createDefaultTemplate(boolean, boolean, boolean, boolean)
     */
    public String createDefaultTemplate(boolean includePortletHandle,
                                        boolean includeUserContextKey,
                                        boolean includePortletInstanceKey,
                                        boolean includeSessionID) {
        return createTemplate(
                    urlGenerator.getRenderURL(null),
                    true,
                    true,
                    true,
                    true,
                    true,
                    false,
                    true,
                    true,
                    includePortletHandle,
                    includeUserContextKey,
                    includePortletInstanceKey,
                    includeSessionID);
    }

    /**
     * @see org.apache.wsrp4j.consumer.URLTemplateComposer#createSecureDefaultTemplate(boolean, boolean, boolean, boolean)
     */
    public String createSecureDefaultTemplate(boolean includePortletHandle,
                                              boolean includeUserContextKey,
                                              boolean includePortletInstanceKey,
                                              boolean includeSessionID) {
        return createTemplate(
                    urlGenerator.getRenderURL(null),
                    true,
                    true,
                    true,
                    true,
                    true,
                    true,
                    true,
                    true,
                    includePortletHandle,
                    includeUserContextKey,
                    includePortletInstanceKey,
                    includeSessionID);
    }

    /**
     * @see org.apache.wsrp4j.consumer.URLTemplateComposer#getNamespacePrefix()
     */
    public String getNamespacePrefix() {
        final CopletInstance coplet = this.adapter.getCurrentCopletInstanceData();
        return coplet.getId();
    }

    /**
     * creates the url for the producer<br/>
     * 
     * @param url
     * @param needsURLType
     * @param needsPortletMode
     * @param needsNavState
     * @param needsInteractionState
     * @param needsWinState
     * @param needsSecURL
     * @param needsURL
     * @param needsRewriteResource
     * @param needsPortletHandle
     * @param needsUserContextKey
     * @param needsPortletInstanceKey
     * @param needsSessionID
     * @return url for the producer
     */
    protected String createTemplate(String url,
                                    boolean needsURLType,
                                    boolean needsPortletMode,
                                    boolean needsNavState,
                                    boolean needsInteractionState,
                                    boolean needsWinState,
                                    boolean needsSecURL,
                                    boolean needsURL,
                                    boolean needsRewriteResource,
                                    boolean needsPortletHandle,
                                    boolean needsUserContextKey,
                                    boolean needsPortletInstanceKey,
                                    boolean needsSessionID) {

        StringBuffer template = new StringBuffer();
        StringBuffer remainder = null;

        boolean isFirstParam = true;
        int index;

        // check if url already contains parameters
        if ((index = url.indexOf(Constants.PARAMS_START)) != -1) {
            template.append(url.substring(0, index));
            remainder = new StringBuffer(url.substring(index + 1));
        } else {
            template.append(url.toString());
        }

        if (needsURLType) {
            if (isFirstParam) {
                template.append(Constants.PARAMS_START);
                isFirstParam = false;
            }
            template.append(insertPair(Constants.URL_TYPE));
        }

        if (needsPortletMode) {
            if (isFirstParam) {
                template.append(Constants.PARAMS_START);
                isFirstParam = false;
            } else {
                template.append(Constants.NEXT_PARAM);
            }
            template.append(insertPair(Constants.PORTLET_MODE));
        }

        if (needsNavState) {
            if (isFirstParam) {
                template.append(Constants.PARAMS_START);
                isFirstParam = false;
            } else {
                template.append(Constants.NEXT_PARAM);
            }
            template.append(insertPair(Constants.NAVIGATIONAL_STATE));
        }

        if (needsInteractionState) {
            if (isFirstParam) {
                template.append(Constants.PARAMS_START);
                isFirstParam = false;
            } else {
                template.append(Constants.NEXT_PARAM);
            }
            template.append(insertPair(Constants.INTERACTION_STATE));
        }

        if (needsWinState) {
            if (isFirstParam) {
                template.append(Constants.PARAMS_START);
                isFirstParam = false;
            } else {
                template.append(Constants.NEXT_PARAM);
            }
            template.append(insertPair(Constants.WINDOW_STATE));
        }

        if (needsSecURL) {
            if (isFirstParam) {
                template.append(Constants.PARAMS_START);
                isFirstParam = false;
            } else {
                template.append(Constants.NEXT_PARAM);
            }
            template.append(insertPair(Constants.SECURE_URL));
        }

        if (needsURL) {
            if (isFirstParam) {
                template.append(Constants.PARAMS_START);
                isFirstParam = false;
            } else {
                template.append(Constants.NEXT_PARAM);
            }
            template.append(insertPair(Constants.URL));
        }

        if (needsRewriteResource) {
            if (isFirstParam) {
                template.append(Constants.PARAMS_START);
                isFirstParam = false;
            } else {
                template.append(Constants.NEXT_PARAM);
            }
            template.append(insertPair(Constants.REWRITE_RESOURCE));
        }

        if (needsPortletHandle) {
            if (isFirstParam) { 
                template.append(Constants.PARAMS_START);
                isFirstParam = false;
            } else {
                template.append(Constants.NEXT_PARAM);
            }
            template.append(insertPair(Constants.PORTLET_HANDLE));
        }

        if (needsUserContextKey) {
            if (isFirstParam) {
                template.append(Constants.PARAMS_START);
                isFirstParam = false;
            } else {
                template.append(Constants.NEXT_PARAM);
            }
            template.append(insertPair(Constants.USER_CONTEXT_KEY));
        }

        if (needsPortletInstanceKey) {
            if (isFirstParam) {
                template.append(Constants.PARAMS_START);
                isFirstParam = false;
            } else {
                template.append(Constants.NEXT_PARAM);
            }
            template.append(insertPair(Constants.PORTLET_INSTANCE_KEY));
        }

        if (needsSessionID) {
            if (isFirstParam) {
                template.append(Constants.PARAMS_START);
                isFirstParam = false;
            } else {
                template.append(Constants.NEXT_PARAM);
            }
            template.append(insertPair(Constants.SESSION_ID));
        }

        // append remainder (static parameters)
        if (remainder != null) {
            template.append(Constants.NEXT_PARAM);
            template.append(remainder);
        }

        return template.toString();
    }

    /**
     * creates a pair of an attribute<br/>
     * 
     * @param token
     * @return String with the following format: token={token}
     */
    protected String insertPair(String token) {
        StringBuffer result = new StringBuffer(token);
        result.append(Constants.EQUALS);
        result.append(Constants.REPLACE_START);
        result.append(token);
        result.append(Constants.REPLACE_END);

        return result.toString();
    }
}
