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
package org.apache.cocoon.acting;

import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.commons.logging.Log;

import org.apache.cocoon.Constants;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.util.NetUtils;

/**
 * This action sets a new LINK_OBJECT entry in the objectModel entry.
 *
 * @since 16 December 2002
 * @version $Id$
 */
public class LinkTranslatorMapAction extends AbstractAction
                                     implements ThreadSafe {

    protected static final String LINK_MAP_PREFIX = "linkMap:";

    /**
     * Execute the LinkTranslatorMapAction.
     *
     * @param  redirector     Cocoon's redirector
     * @param  resolver       Cocoon's source resolver, used for testing if a source is resolvable
     * @param  source         the source, e.g.: index.html
     * @param  parameters     of this action
     * @param  objectModel    Description of the Parameter
     * @exception  Exception  Description of the Exception
     */
    public Map act(Redirector     redirector,
                   SourceResolver resolver,
                   Map            objectModel,
                   String         source,
                   Parameters     parameters)
    throws Exception {
        Map linkObjectMap = (Map) objectModel.get(Constants.LINK_OBJECT);

        final String[] parameterNames = parameters.getNames();
        for (int i = 0; i < parameterNames.length; i++) {
            String parameterName = parameterNames[i];
            if (parameterName.startsWith(LINK_MAP_PREFIX)) {
                final String linkKey = parameterName.substring(LINK_MAP_PREFIX.length());
                final String linkValue = parameters.getParameter(parameterName, null);

                if (linkValue != null) {
                    if (linkObjectMap == null) {
                        linkObjectMap = new HashMap();
                        objectModel.put(Constants.LINK_OBJECT, linkObjectMap);
                    }
                    getLogger().debug("Add mapping from " + linkKey + " to " + linkValue);
                    linkObjectMap.put(linkKey, linkValue);
                }
            }
        }

        //
        //  expect base = "/.."
        //
        final String base = parameters.getParameter("url-base", "");
        // get the extension with starting dot
        final Request request = ObjectModelHelper.getRequest(objectModel);
        final String requestURI = request.getRequestURI();

        String extension = NetUtils.getExtension(requestURI);
        String path = NetUtils.getPath(requestURI);
        getLogger().debug("LinkMapTranslator 1 " + "path " + path);
        path = path + base;
        getLogger().debug("LinkMapTranslator 2 " + "path " + path);
        path = NetUtils.normalize(path);

        getLogger().debug("LinkMapTranslator 3 " +
                "path " + path + ", " +
                "base " + base + ", " +
                "ext " + extension);

        if (extension != null) {
            LinkPatternHashMap lphm = new LinkPatternHashMap(path, extension, linkObjectMap, getLogger());

            objectModel.put(Constants.LINK_OBJECT, lphm);
        }

        return null;
    }


    /**
     * A special links map
     */
    public static class LinkPatternHashMap extends HashMap {

        protected final Log logger;
        protected final String pageExtension;
        protected final String path;
        protected final Map parent;


        /**
         * Constructor for the LinkPatternHashMap object
         *
         * @param  pageExtension  Description of the Parameter
         * @param  path           Description of the Parameter
         */
        public LinkPatternHashMap(String path,
                                  String pageExtension,
                                  Map    parentMap,
                                  Log    logger) {
            this.pageExtension = pageExtension;
            this.path = path;
            this.parent = parentMap;
            this.logger = logger;
        }

        /**
         * assume that LinkTranslator wants to check iff a href link
         * is a URI mapped to some URI.
         *
         * @param  key  Description of the Parameter
         * @return      Description of the Return Value
         */
        public Object get(Object key) {
            String newHref = null;
            if (key instanceof String) {
                String keyString = (String) key;
                final String PAGE_SCHEMA = "page:";
                final int PAGE_SCHEMA_LENGTH = PAGE_SCHEMA.length();

                // does key is a page:* URI ?
                if (keyString.startsWith(PAGE_SCHEMA)) {
                    // strip page, append pageExtension
                    // that's the new href!
                    String strippedPageSchema = keyString.substring(PAGE_SCHEMA_LENGTH);
                    // to-do: handle parameters!!, like page:index?a=b&c=d
                    newHref = strippedPageSchema + pageExtension;

                    if (logger.isDebugEnabled()) {
                        logger.debug("href " + keyString + " mapped to " + newHref);
                    }
                }

                if (newHref == null) {
                    // try simple get
                    newHref = (String) super.get(key);
                    if (newHref == null && parent != null) {
                        // if parent is defined try parent get
                        newHref = (String) parent.get(key);
                    }
                }

                if (newHref == null) {
                    newHref = keyString;
                }

                // strip path
                if (logger.isDebugEnabled()) {
                    logger.debug("newHref " + newHref + ", " + " path " + path);
                }
                if (newHref.startsWith(this.path)) {
                    final int pathLength = path.length();

                    newHref = newHref.substring(pathLength);
                    if (newHref.startsWith("/") && newHref.length() > 1) {
                        newHref = newHref.substring(1);
                    }
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("newHref stripped " + newHref);
                }
            }

            // now nearly finished
            Object result;

            if (newHref != null) {
                // if href is non null, take it as result
                result = newHref;
            } else {
                // try simple get
                result = super.get(key);
                if (result == null && parent != null) {
                    // if parent is defined try parent get
                    result = parent.get(key);
                }
            }
            if (logger.isDebugEnabled()) {
                logger.debug("For key " + key + " result is " + result);
            }

            return result;
        }
    }
}
