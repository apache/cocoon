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
package org.apache.cocoon.acting;

import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.cocoon.Constants;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.util.NetUtils;

/**
 * This action sets a new LINK_OBJECT entry in the objectModel entry.
 *
 * @author <a href="mailto:huber@apache.org">Bernhard Huber</a>
 * @since 16 December 2002
 * @version CVS $Id: LinkTranslatorMapAction.java,v 1.2 2003/09/05 07:04:35 cziegeler Exp $
 */
public class LinkTranslatorMapAction extends ComposerAction implements ThreadSafe {

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
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception {
        Map linkObjectMap = (Map) objectModel.get(Constants.LINK_OBJECT);

        String[] parameterNames = parameters.getNames();
        for (int i = 0; i < parameterNames.length; i++) {
            String parameterName = parameterNames[i];
            final String LINK_MAP_PREFIX = "linkMap:";
            if (parameterName.startsWith(LINK_MAP_PREFIX)) {
                String linkKey = parameterName.substring(LINK_MAP_PREFIX.length());
                String linkValue = parameters.getParameter(parameterName, null);

                if (linkValue != null) {
                    if (linkObjectMap == null) {
                        linkObjectMap = new HashMap();
                        objectModel.put(Constants.LINK_OBJECT, linkObjectMap);
                    }
                    getLogger().debug("Add mapping from " +
                            String.valueOf(linkKey) + " to " + String.valueOf(linkValue));
                    linkObjectMap.put(linkKey, linkValue);
                }
            }
        }

        /*
         *  expect base = "/.."
         */
        String base = parameters.getParameter("url-base", "");
        // get the extension with starting dot
        Request request = ObjectModelHelper.getRequest(objectModel);
        String requestURI = request.getRequestURI();

        String extension = NetUtils.getExtension(requestURI);
        String path = NetUtils.getPath(requestURI);
        getLogger().debug("LinkMapTranslator 1 " + "path " + String.valueOf(path) );
        path = path + base;
        getLogger().debug("LinkMapTranslator 2 " + "path " + String.valueOf(path) );
        path = NetUtils.normalize(path);

        getLogger().debug("LinkMapTranslator 3 " +
                "path " + String.valueOf(path) + ", " +
                "base " + String.valueOf(base) + ", " +
                "ext " + String.valueOf(extension));

        if (extension != null) {
            LinkPatternHashMap lphm = new LinkPatternHashMap(path, extension);
            lphm.setParent(linkObjectMap);
            lphm.enableLogging(getLogger());

            objectModel.put(Constants.LINK_OBJECT, lphm);
        }

        return null;
    }


    /**
     * A special links map
     */
    public static class LinkPatternHashMap extends HashMap implements LogEnabled {
        Logger logger;
        String pageExtension;
        String path;

        Map parent;


        /**
         * Constructor for the LinkPatternHashMap object
         *
         * @param  pageExtension  Description of the Parameter
         * @param  path           Description of the Parameter
         */
        public LinkPatternHashMap(String path, String pageExtension) {
            this.pageExtension = pageExtension;
            this.path = path;
            this.parent = null;
        }


        /**
         * Sets the parent attribute of the LinkPatternHashMap object
         *
         * @param  parent  The new parent value
         */
        public void setParent(Map parent) {
            this.parent = parent;
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

                    if (logger != null && logger.isDebugEnabled()) {
                        logger.debug("href " + String.valueOf(keyString) + " mapped to " + String.valueOf(newHref));
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
                logger.debug("newHref " + String.valueOf(newHref) + ", " + " path " + String.valueOf(path));
                if (newHref.startsWith(this.path)) {
                    final int pathLength = path.length();

                    newHref = newHref.substring(pathLength);
                    if (newHref.startsWith("/") && newHref.length() > 1) {
                        newHref = newHref.substring(1);
                    }
                }
                logger.debug("newHref stripped " + String.valueOf(newHref));
            }

            // now nearly finished
            Object result = null;

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
            logger.debug("For key " + String.valueOf(key) +
                    " result is " + String.valueOf(result));

            return result;
        }


        /**
         * Description of the Method
         *
         * @param  logger  Description of the Parameter
         */
        public void enableLogging(Logger logger) {
            this.logger = logger;
        }
    }
}
